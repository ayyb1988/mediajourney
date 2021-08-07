package com.mediajourney.videocache;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mediajourney.videocache.file.FileCache;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mediajourney.videocache.Preconditions.checkNotNull;

/**
 * Client for {@link HttpProxyCacheServer}
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
final class HttpProxyCacheServerClients {

    private final AtomicInteger clientsCount = new AtomicInteger(0);
    private final String url;
    private volatile HttpProxyCache proxyCache;
    private final List<CacheListener> listeners = new CopyOnWriteArrayList<>();
    private final CacheListener uiCacheListener;
    private final Config config;

    public HttpProxyCacheServerClients(String url, Config config) {
        this.url = checkNotNull(url);
        this.config = checkNotNull(config);
        this.uiCacheListener = new UiListenerHandler(url, listeners);
    }

    public void processRequest(GetRequest request, Socket socket) throws ProxyCacheException, IOException {
        //proxyCache的初始化，如果没有则重新newHttpProxyCache，否则复用即可
        startProcessRequest();
        try {
            //原子操作用于记录当前有多少个socketClient
            clientsCount.incrementAndGet();
            //缓存代理开始处理
            proxyCache.processRequest(request, socket);
        } finally {
            //结束
            finishProcessRequest();
        }
    }

    private synchronized void startProcessRequest() throws ProxyCacheException {
        proxyCache = proxyCache == null ? newHttpProxyCache() : proxyCache;
    }

    private synchronized void finishProcessRequest() {
        if (clientsCount.decrementAndGet() <= 0) {
            //sourceReaderThread中断
            //FileChannel关闭
            //touch下文件
            proxyCache.shutdown();
            proxyCache = null;
        }
    }

    public void registerCacheListener(CacheListener cacheListener) {
        listeners.add(cacheListener);
    }

    public void unregisterCacheListener(CacheListener cacheListener) {
        listeners.remove(cacheListener);
    }

    public void shutdown() {
        listeners.clear();
        if (proxyCache != null) {
            proxyCache.registerCacheListener(null);
            proxyCache.shutdown();
            proxyCache = null;
        }
        clientsCount.set(0);
    }

    public int getClientsCount() {
        return clientsCount.get();
    }

    private HttpProxyCache newHttpProxyCache() throws ProxyCacheException {
        //HttpUrlSource 持有url，开启HttpUrlConnetcion来获取inputStream
        HttpUrlSource source = new HttpUrlSource(url, config.sourceInfoStorage, config.headerInjector);
        //缓存总以.download存在，缓存完后更名
        FileCache cache = new FileCache(config.generateCacheFile(url), config.diskUsage);
        HttpProxyCache httpProxyCache = new HttpProxyCache(source, cache);
        httpProxyCache.registerCacheListener(uiCacheListener);
        return httpProxyCache;
    }

    private static final class UiListenerHandler extends Handler implements CacheListener {

        private final String url;
        private final List<CacheListener> listeners;

        public UiListenerHandler(String url, List<CacheListener> listeners) {
            super(Looper.getMainLooper());
            this.url = url;
            this.listeners = listeners;
        }

        @Override
        public void onCacheAvailable(File file, String url, int percentsAvailable) {
            Message message = obtainMessage();
            message.arg1 = percentsAvailable;
            message.obj = file;
            sendMessage(message);
        }

        @Override
        public void handleMessage(Message msg) {
            for (CacheListener cacheListener : listeners) {
                cacheListener.onCacheAvailable((File) msg.obj, url, msg.arg1);
            }
        }
    }
}
