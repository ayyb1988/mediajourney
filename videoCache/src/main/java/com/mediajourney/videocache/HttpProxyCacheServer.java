package com.mediajourney.videocache;

import android.content.Context;
import android.net.Uri;

import com.mediajourney.utils.CacheLog;
import com.mediajourney.videocache.file.DiskUsage;
import com.mediajourney.videocache.file.FileNameGenerator;
import com.mediajourney.videocache.file.Md5FileNameGenerator;
import com.mediajourney.videocache.file.TotalCountLruDiskUsage;
import com.mediajourney.videocache.file.TotalSizeLruDiskUsage;
import com.mediajourney.videocache.headers.EmptyHeadersInjector;
import com.mediajourney.videocache.headers.HeaderInjector;
import com.mediajourney.videocache.sourcestorage.SourceInfoStorage;
import com.mediajourney.videocache.sourcestorage.SourceInfoStorageFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mediajourney.videocache.Preconditions.checkAllNotNull;
import static com.mediajourney.videocache.Preconditions.checkNotNull;

/**
 * Simple lightweight proxy server with file caching support that handles HTTP requests.
 * Typical usage:
 * <pre><code>
 * public onCreate(Bundle state) {
 *      super.onCreate(state);
 *
 *      HttpProxyCacheServer proxy = getProxy();
 *      String proxyUrl = proxy.getProxyUrl(VIDEO_URL);
 *      videoView.setVideoPath(proxyUrl);
 * }
 *
 * private HttpProxyCacheServer getProxy() {
 * // should return single instance of HttpProxyCacheServer shared for whole app.
 * }
 * </code></pre>
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class HttpProxyCacheServer {

    private static final String TAG = "CacheLog-HttpProxyCacheServer";


    private final Object clientsLock = new Object();
    private final ExecutorService socketProcessor = Executors.newFixedThreadPool(8);
    private final Map<String, HttpProxyCacheServerClients> clientsMap = new ConcurrentHashMap<>();
    private final ServerSocket serverSocket;
    private final int port;
    private final Thread waitConnectionThread;
    private final Config config;
    private final Pinger pinger;

    public HttpProxyCacheServer(Context context) {
        this(new Builder(context).buildConfig());
    }

    private static final String PROXY_HOST = "127.0.0.1";
    private HttpProxyCacheServer(Config config) {
        this.config = checkNotNull(config);
        try {
            //根据host生成本地代理服务器的地址
            InetAddress inetAddress = InetAddress.getByName(PROXY_HOST);
            //创建ServerSocket，最大可于8个client进行连接
            this.serverSocket = new ServerSocket(0, 8, inetAddress);
            //有系统自动分配一个端口
            this.port = serverSocket.getLocalPort();
            IgnoreHostProxySelector.install(PROXY_HOST, port);
            //等待waitConnectionThread线程启动
            CountDownLatch startSignal = new CountDownLatch(1);
            //开启一个线程接收socket连接
            this.waitConnectionThread = new Thread(new WaitRequestsRunnable(startSignal));
            this.waitConnectionThread.start();
            //阻塞当前线程，直到startSignal.countDown();
            startSignal.await(); // freeze thread, wait for server starts
            this.pinger = new Pinger(PROXY_HOST, port);
        } catch (IOException | InterruptedException e) {
            socketProcessor.shutdown();
            throw new IllegalStateException("Error starting local proxy server", e);
        }
    }

    /**
     * Returns url that wrap original url and should be used for client (MediaPlayer, ExoPlayer, etc).
     * <p>
     * If file for this url is fully cached (it means method {@link #isCached(String)} returns {@code true})
     * then file:// uri to cached file will be returned.
     * <p>
     * Calling this method has same effect as calling {@link #getProxyUrl(String, boolean)} with 2nd parameter set to {@code true}.
     *
     * @param url a url to file that should be cached.
     * @return a wrapped by proxy url if file is not fully cached or url pointed to cache file otherwise.
     */
    public String getProxyUrl(String url) {
        return getProxyUrl(url, true);
    }

    /**
     * Returns url that wrap original url and should be used for client (MediaPlayer, ExoPlayer, etc).
     * <p>
     * If parameter {@code allowCachedFileUri} is {@code true} and file for this url is fully cached
     * (it means method {@link #isCached(String)} returns {@code true}) then file:// uri to cached file will be returned.
     *
     * @param url                a url to file that should be cached.
     * @param allowCachedFileUri {@code true} if allow to return file:// uri if url is fully cached
     * @return a wrapped by proxy url if file is not fully cached or url pointed to cache file otherwise (if {@code allowCachedFileUri} is {@code true}).
     */
    /**
     * 整个策略如下：
     * > 如果本地已经缓存了，就直接那本地地址的Uri，并且touch一下文件，把时间更新后最新，因为后面LruCache是根据文件被访问的时间进行排序的，
     * > 如果文件没有被缓存，那么就会先走一下 isAlive() 方法， 这里会ping一下目标url，确保url是一个有效的，如果用户是通过代理访问的话，就会ping不通，
     * 这样就还是原生url，正常情况都会进入这个 appendToProxyUrl 方法里面。
     */
    public String getProxyUrl(String url, boolean allowCachedFileUri) {
        //如果本地已经缓存了，就直接那本地地址的Uri，
        // 并且touch一下文件，把时间更新后最新，因为后面LruCache是根据文件被访问的时间进行排序的，
        if (allowCachedFileUri && isCached(url)) {
            File cacheFile = getCacheFile(url);
            touchFileSafely(cacheFile);
            return Uri.fromFile(cacheFile).toString();
        }
        //如果文件没有被缓存，那么就会先走一下 isAlive() 方法， 这里会ping一下目标url，确保url是一个有效的，
        // 如果用户是通过代理访问的话(比如已经使用了其他的代理，比如P2P或者免流等)，就会ping不通，这样就还是原生url，
        // 正常情况都会进入这个 appendToProxyUrl 方法里面。转化请求的url为代理地址：http://172.0.0.1:port/url
        return isAlive() ? appendToProxyUrl(url) : url;
    }

    public void registerCacheListener(CacheListener cacheListener, String url) {
        checkAllNotNull(cacheListener, url);
        synchronized (clientsLock) {
            try {
                getClients(url).registerCacheListener(cacheListener);
            } catch (ProxyCacheException e) {
                if (CacheLog.DEBUG) {
                    CacheLog.w(TAG, "Error registering cache listener" + e.getMessage());
                }
            }
        }
    }

    public void unregisterCacheListener(CacheListener cacheListener, String url) {
        checkAllNotNull(cacheListener, url);
        synchronized (clientsLock) {
            try {
                getClients(url).unregisterCacheListener(cacheListener);
            } catch (ProxyCacheException e) {
                if (CacheLog.DEBUG) {
                    CacheLog.w(TAG, "Error registering cache listener" + e.getMessage());
                }
            }
        }
    }

    public void unregisterCacheListener(CacheListener cacheListener) {
        checkNotNull(cacheListener);
        synchronized (clientsLock) {
            for (HttpProxyCacheServerClients clients : clientsMap.values()) {
                clients.unregisterCacheListener(cacheListener);
            }
        }
    }

    /**
     * Checks is cache contains fully cached file for particular url.
     *
     * @param url an url cache file will be checked for.
     * @return {@code true} if cache contains fully cached file for passed in parameters url.
     */
    public boolean isCached(String url) {
        checkNotNull(url, "Url can't be null!");
        return getCacheFile(url).exists();
    }

    public void shutdown() {
        if (CacheLog.DEBUG) {
            CacheLog.i(TAG, "Shutdown proxy server");
        }

        shutdownClients();

        config.sourceInfoStorage.release();

        waitConnectionThread.interrupt();
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            onError(new ProxyCacheException("Error shutting down proxy server", e));
        }
    }

    private boolean isAlive() {
        return pinger.ping(3, 70);   // 70+140+280=max~500ms
    }

    private String appendToProxyUrl(String url) {
        return String.format(Locale.US, "http://%s:%d/%s", PROXY_HOST, port, ProxyCacheUtils.encode(url));
    }

    private File getCacheFile(String url) {
        File cacheDir = config.cacheRoot;
        String fileName = config.fileNameGenerator.generate(url);
        return new File(cacheDir, fileName);
    }

    private void touchFileSafely(File cacheFile) {
        try {
            config.diskUsage.touch(cacheFile);
        } catch (IOException e) {
            if (CacheLog.DEBUG) {
                CacheLog.e(TAG, "Error touching file " + cacheFile + e.getMessage());
            }
        }
    }

    private void shutdownClients() {
        synchronized (clientsLock) {
            for (HttpProxyCacheServerClients clients : clientsMap.values()) {
                clients.shutdown();
            }
            clientsMap.clear();
        }
    }

    private void waitForRequest() {
        try {
            //如果线程没有interrupt，不断的轮询，用于检测是否有新的socket连接
            while (!Thread.currentThread().isInterrupted()) {
                //阻塞的方法 用于socket连接
                //socketServer通过监听本地host:port，如果有对应的请求触发就进行一个socket连接
                Socket socket = serverSocket.accept();
                //线程池，同时最大可以有8个socket连接
                socketProcessor.submit(new SocketProcessorRunnable(socket));
            }
        } catch (IOException e) {
            onError(new ProxyCacheException("Error during waiting connection", e));
        }
    }

    private void processSocket(Socket socket) {
        try {
            //通过输入流（即请求转换过的url等信息）生成GetRequest对象
            GetRequest request = GetRequest.read(socket.getInputStream());
            String url = ProxyCacheUtils.decode(request.uri);
            //url是"ping" 返回200，可以ping通
            if (pinger.isPingRequest(url)) {
                pinger.responseToPing(socket);
            } else {
                //获取 HttpProxyCacheServerClients ，并进行request处理
                HttpProxyCacheServerClients clients = getClients(url);
                clients.processRequest(request, socket);
            }
        } catch (SocketException e) {
            // There is no way to determine that client closed connection http://stackoverflow.com/a/10241044/999458
            // So just to prevent log flooding don't log stacktrace
        } catch (ProxyCacheException | IOException e) {
            onError(new ProxyCacheException("Error processing request", e));
        } finally {
            //socket处理完毕之后，在finally中，关闭socket连接释放资源
            releaseSocket(socket);
        }
    }

    private HttpProxyCacheServerClients getClients(String url) throws ProxyCacheException {
        synchronized (clientsLock) {
            //clientsMap的key是url，value是对应的HttpProxyCacheServerClients
            //有些url会带有一些时效性的信息，这里key，可以改为一些能够唯一表示的id来进行表示
            //一个url请求 对应一个HttpProxyCacheServerClients
            HttpProxyCacheServerClients clients = clientsMap.get(url);
            if (clients == null) {
                clients = new HttpProxyCacheServerClients(url, config);
                clientsMap.put(url, clients);
            }
            return clients;
        }
    }

    private int getClientsCount() {
        synchronized (clientsLock) {
            int count = 0;
            for (HttpProxyCacheServerClients clients : clientsMap.values()) {
                count += clients.getClientsCount();
            }
            return count;
        }
    }

    private void releaseSocket(Socket socket) {
        closeSocketInput(socket);
        closeSocketOutput(socket);
        closeSocket(socket);
    }

    private void closeSocketInput(Socket socket) {
        try {
            if (!socket.isInputShutdown()) {
                socket.shutdownInput();
            }
        } catch (SocketException e) {
            // There is no way to determine that client closed connection http://stackoverflow.com/a/10241044/999458
            // So just to prevent log flooding don't log stacktrace
            if (CacheLog.DEBUG) {
                CacheLog.d(TAG, "Releasing input stream… Socket is closed by client.");
            }
        } catch (IOException e) {
            onError(new ProxyCacheException("Error closing socket input stream", e));
        }
    }

    private void closeSocketOutput(Socket socket) {
        try {
            if (!socket.isOutputShutdown()) {
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            if (CacheLog.DEBUG) {
                CacheLog.w(TAG, "Failed to close socket on proxy side: {}. It seems client have already closed connection." + e.getMessage());
            }
        }
    }

    private void closeSocket(Socket socket) {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            onError(new ProxyCacheException("Error closing socket", e));
        }
    }

    private void onError(Throwable e) {
        if (CacheLog.DEBUG) {
            CacheLog.e(TAG, "HttpProxyCacheServer error" + e.getMessage());
        }
    }

    private final class WaitRequestsRunnable implements Runnable {

        private final CountDownLatch startSignal;

        public WaitRequestsRunnable(CountDownLatch startSignal) {
            this.startSignal = startSignal;
        }

        @Override
        public void run() {
            startSignal.countDown();
            //开启一个线程，在线程中轮训
            waitForRequest();
        }
    }

    private final class SocketProcessorRunnable implements Runnable {

        private final Socket socket;

        public SocketProcessorRunnable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            processSocket(socket);
        }
    }

    /**
     * Builder for {@link HttpProxyCacheServer}.
     */
    public static final class Builder {

        private static final long DEFAULT_MAX_SIZE = 512 * 1024 * 1024;

        private File cacheRoot;
        private FileNameGenerator fileNameGenerator;
        private DiskUsage diskUsage;
        private SourceInfoStorage sourceInfoStorage;
        private HeaderInjector headerInjector;

        public Builder(Context context) {
            this.sourceInfoStorage = SourceInfoStorageFactory.newSourceInfoStorage(context);
            this.cacheRoot = StorageUtils.getIndividualCacheDirectory(context);
            this.diskUsage = new TotalSizeLruDiskUsage(DEFAULT_MAX_SIZE);
            this.fileNameGenerator = new Md5FileNameGenerator();
            this.headerInjector = new EmptyHeadersInjector();
        }

        /**
         * Overrides default cache folder to be used for caching files.
         * <p>
         * By default AndroidVideoCache uses
         * '/Android/data/[app_package_name]/cache/media-cache/' if card is mounted and app has appropriate permission
         * or 'media-cache' subdirectory in default application's cache directory otherwise.
         * </p>
         * <b>Note</b> directory must be used <b>only</b> for AndroidVideoCache files.
         *
         * @param file a cache directory, can't be null.
         * @return a builder.
         */
        public Builder cacheDirectory(File file) {
            this.cacheRoot = checkNotNull(file);
            return this;
        }

        /**
         * Overrides default cache file name generator {@link Md5FileNameGenerator} .
         *
         * @param fileNameGenerator a new file name generator.
         * @return a builder.
         */
        public Builder fileNameGenerator(FileNameGenerator fileNameGenerator) {
            this.fileNameGenerator = checkNotNull(fileNameGenerator);
            return this;
        }

        /**
         * Sets max cache size in bytes.
         * <p>
         * All files that exceeds limit will be deleted using LRU strategy.
         * Default value is 512 Mb.
         * </p>
         * Note this method overrides result of calling {@link #maxCacheFilesCount(int)}
         *
         * @param maxSize max cache size in bytes.
         * @return a builder.
         */
        public Builder maxCacheSize(long maxSize) {
            this.diskUsage = new TotalSizeLruDiskUsage(maxSize);
            return this;
        }

        /**
         * Sets max cache files count.
         * All files that exceeds limit will be deleted using LRU strategy.
         * Note this method overrides result of calling {@link #maxCacheSize(long)}
         *
         * @param count max cache files count.
         * @return a builder.
         */
        public Builder maxCacheFilesCount(int count) {
            this.diskUsage = new TotalCountLruDiskUsage(count);
            return this;
        }

        /**
         * Set custom DiskUsage logic for handling when to keep or clean cache.
         *
         * @param diskUsage a disk usage strategy, cant be {@code null}.
         * @return a builder.
         */
        public Builder diskUsage(DiskUsage diskUsage) {
            this.diskUsage = checkNotNull(diskUsage);
            return this;
        }

        /**
         * Add headers along the request to the server
         *
         * @param headerInjector to inject header base on url
         * @return a builder
         */
        public Builder headerInjector(HeaderInjector headerInjector) {
            this.headerInjector = checkNotNull(headerInjector);
            return this;
        }

        /**
         * Builds new instance of {@link HttpProxyCacheServer}.
         *
         * @return proxy cache. Only single instance should be used across whole app.
         */
        public HttpProxyCacheServer build() {
            Config config = buildConfig();
            return new HttpProxyCacheServer(config);
        }

        private Config buildConfig() {
            //cacheRoot: 设置缓存路径
            //fileNameGenerator: 设置文件名，一般用url的md5或者唯一表示的业务id/hash
            //diskUsage: 缓存的lru策略，有个touch方法，用于更新文件的修改时间（这个的实现也很有意思）。
            //           支持设置缓存总大小以及缓存总个数的阀值。也可以自行扩展比如设置缓存的有效期
            //sourceInfoStorage : 缓存信息的存储，根据唯一表示存储/查询对应的缓存路径等信息

            return new Config(cacheRoot, fileNameGenerator, diskUsage, sourceInfoStorage, headerInjector);
        }

    }
}
