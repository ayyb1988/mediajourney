package com.mediajourney.videocache;

import com.mediajourney.utils.CacheLog;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mediajourney.videocache.Preconditions.checkNotNull;

/**
 * Proxy for {@link Source} with caching support ({@link Cache}).
 * <p/>
 * Can be used only for sources with persistent data (that doesn't change with time).
 * Method {@link #read(byte[], long, int)} will be blocked while fetching data from source.
 * Useful for streaming something with caching e.g. streaming video/audio etc.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class ProxyCache {

    private static final String TAG = "CacheLog-ProxyCache";
    private static final int MAX_READ_SOURCE_ATTEMPTS = 1;

    private final Source source;
    private final Cache cache;
    private final Object wc = new Object();
    private final Object stopLock = new Object();
    private final AtomicInteger readSourceErrorsCount;
    private volatile Thread sourceReaderThread;
    private volatile boolean stopped;
    private volatile int percentsAvailable = -1;

    public ProxyCache(Source source, Cache cache) {
        this.source = checkNotNull(source);
        this.cache = checkNotNull(cache);
        this.readSourceErrorsCount = new AtomicInteger();
    }

    /**
     * 这个是边缓存边播放的关键，先往文件中写入数据，直到写完（整个文件写完或者8192个写完）或者中断。
     * buffer：一次读取的buffer
     * offset：当前的已有缓存的偏移
     * lenght: 一次读取buffer的大小
     */
    public int read(byte[] buffer, long offset, int length) throws ProxyCacheException {
        ProxyCacheUtils.assertBuffer(buffer, offset, length);

        //如果没有缓存完，并且缓存的大小小于需要缓存的大小(一次8192个字节)，并且sourceReaderThread线程没有停止
        while (!cache.isCompleted() && cache.available() < (offset + length) && !stopped) {
            //异步的读取数据, 这里为什么要这样设计呐？？(本来已经在子线程了，为什么还要在开启线程进行读取网络数据呐？sourceReaderThread)
            readSourceAsync();
            //等待，最大时长1s秒钟,每过1s中检查是否有错误发生
            waitForSourceData();
            checkReadSourceErrorsCount();
        }
        //从缓存中读取最大的8192个字节数据给到播放器
        int read = cache.read(buffer, offset, length);
        if (cache.isCompleted() && percentsAvailable != 100) {
            percentsAvailable = 100;
            onCachePercentsAvailableChanged(100);
        }
        return read;
    }

    private void checkReadSourceErrorsCount() throws ProxyCacheException {
        int errorsCount = readSourceErrorsCount.get();
        if (errorsCount >= MAX_READ_SOURCE_ATTEMPTS) {
            readSourceErrorsCount.set(0);
            //如果读取中有一场发生，则抛出异常
            throw new ProxyCacheException("Error reading source " + errorsCount + " times");
        }
    }

    public void shutdown() {
        synchronized (stopLock) {
            if (CacheLog.DEBUG) {
                CacheLog.e(TAG, "Shutdown proxy for " + source);
            }
            try {
                stopped = true;
                if (sourceReaderThread != null) {
                    sourceReaderThread.interrupt();
                }
                cache.close();
            } catch (ProxyCacheException e) {
                onError(e);
            }
        }
    }

    private synchronized void readSourceAsync() throws ProxyCacheException {
        boolean readingInProgress = sourceReaderThread != null && sourceReaderThread.getState() != Thread.State.TERMINATED;
        //如果已经还没有停止，并且 还没有缓存完 并且 没有在读取中 则开启新的数据读取线程 线程
        if (!stopped && !cache.isCompleted() && !readingInProgress) {
            //在这个SourceReaderRunnable中进行
            sourceReaderThread = new Thread(new SourceReaderRunnable(), "Source reader for " + source);
            sourceReaderThread.start();
        }
    }

    private void waitForSourceData() throws ProxyCacheException {
        synchronized (wc) {
            try {
                wc.wait(1000);
            } catch (InterruptedException e) {
                throw new ProxyCacheException("Waiting source data is interrupted!", e);
            }
        }
    }

    private void notifyNewCacheDataAvailable(long cacheAvailable, long sourceAvailable) {
        onCacheAvailable(cacheAvailable, sourceAvailable);

        synchronized (wc) {
            wc.notifyAll();
        }
    }

    protected void onCacheAvailable(long cacheAvailable, long sourceLength) {
        boolean zeroLengthSource = sourceLength == 0;
        int percents = zeroLengthSource ? 100 : (int) ((float) cacheAvailable / sourceLength * 100);
        boolean percentsChanged = percents != percentsAvailable;
        boolean sourceLengthKnown = sourceLength >= 0;
        if (sourceLengthKnown && percentsChanged) {
            onCachePercentsAvailableChanged(percents);
        }
        percentsAvailable = percents;
    }

    protected void onCachePercentsAvailableChanged(int percentsAvailable) {
    }

    private void readSource() {
        long sourceAvailable = -1;
        long offset = 0;
        try {
            //已经缓存的大小
            offset = cache.available();
            //开启 HttpUrlConnetion，获取一个inputStream
            source.open(offset);
            //文件的大小
            sourceAvailable = source.length();
            byte[] buffer = new byte[ProxyCacheUtils.DEFAULT_BUFFER_SIZE];
            int readBytes;
            //HttpUrlSource.read,不断的读取数据从inputstream
            while ((readBytes = source.read(buffer)) != -1) {
                synchronized (stopLock) {
                    if (isStopped()) {
                        return;
                    }
                    //往缓存文件中写入数据,一次写入8192字节
                    cache.append(buffer, readBytes);
                }
                offset += readBytes;
                notifyNewCacheDataAvailable(offset, sourceAvailable);
            }
            tryComplete();
            onSourceRead();
        } catch (Throwable e) {
            //如果读取过程中发生了错误，则进行原子加操作，每过1s秒会检查该标记位
            readSourceErrorsCount.incrementAndGet();
            onError(e);
        } finally {
            closeSource();
            notifyNewCacheDataAvailable(offset, sourceAvailable);
        }
    }

    private void onSourceRead() {
        // guaranteed notify listeners after source read and cache completed
        percentsAvailable = 100;
        onCachePercentsAvailableChanged(percentsAvailable);
    }

    private void tryComplete() throws ProxyCacheException {
        synchronized (stopLock) {
            if (!isStopped() && cache.available() == source.length()) {
                cache.complete();
            }
        }
    }

    private boolean isStopped() {
        return Thread.currentThread().isInterrupted() || stopped;
    }

    private void closeSource() {
        try {
            source.close();
        } catch (ProxyCacheException e) {
            onError(new ProxyCacheException("Error closing source " + source, e));
        }
    }

    protected final void onError(final Throwable e) {
        boolean interruption = e instanceof InterruptedProxyCacheException;
        if (CacheLog.DEBUG) {
            if (interruption) {
                CacheLog.e(TAG, "ProxyCache is interrupted");
            } else {
                CacheLog.e(TAG, "ProxyCache error" + e.getMessage());
            }
        }
    }

    private class SourceReaderRunnable implements Runnable {

        @Override
        public void run() {
            readSource();
        }
    }
}
