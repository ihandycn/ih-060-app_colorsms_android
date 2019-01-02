package com.android.messaging.download;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Downloader {

    private static final String TAG = Downloader.class.getSimpleName();

    private static final String DOWNLOAD_FILE_DIR = "message-download-file";
    private static final String THREAD_TAG = "message-download-thread-";

    private static final int NUMBER_OF_ALIVE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private final ThreadPoolExecutor mExecutor;
    private final ThreadFactory mDefaultThreadFactory = Executors.defaultThreadFactory();
    private final LinkedBlockingDeque<Runnable> mTaskDeque = new LinkedBlockingDeque<>();

    private File mCacheDir;

    private final List<DownloadingRecord> mDownloadingItems = new ArrayList<>();

    private static class ClassHolder {
        private final static Downloader INSTANCE = new Downloader();
    }

    public static Downloader getInstance() {
        return ClassHolder.INSTANCE;
    }

    private Downloader() {
        int poolSize = Math.min(NUMBER_OF_ALIVE_CORES * 2, 3);
        HSLog.d(TAG, "Pool size = " + poolSize);
        mExecutor = new ThreadPoolExecutor(
                poolSize, // Initial pool size
                poolSize, // Max pool size, not used as we are providing an unbounded queue to the executor
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mTaskDeque,
                new ThreadFactory() {
                    private AtomicInteger threadCount = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = mDefaultThreadFactory.newThread(r);
                        thread.setName(THREAD_TAG + threadCount.getAndIncrement());
                        thread.setPriority(Thread.MIN_PRIORITY);
                        return thread;
                    }
                }
        );
        mExecutor.allowCoreThreadTimeOut(true);
        mCacheDir = FileUtils.getCacheDirectory(DOWNLOAD_FILE_DIR, true);
    }

    public boolean isDownloading(String url) {
        synchronized (mDownloadingItems) {
            for (DownloadingRecord downloadingItem : mDownloadingItems) {
                if (downloadingItem.url.equals(url)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isDownloaded(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        File file = getDownloadFile(url);
        return file.exists() && file.length() > 0;
    }

    public synchronized void download(final String url, DownloadListener listener) {
        if (TextUtils.isEmpty(url) && listener != null) {
            listener.onStart(url);
            listener.onFail(url, "url is null!!!");
            return;
        }
        Runnable taskRunnable = () -> {
            HSHttpConnection connection;

            synchronized (mDownloadingItems) {
                for (DownloadingRecord downloadingItem : mDownloadingItems) {
                    if (downloadingItem.url.equals(url)) {
                        HSLog.d(TAG, "url:  " + url + " already downloading");
                        if (listener != null) {
                            Threads.postOnMainThread(() -> {
                                listener.onFail(url, "this url: " + url + "  already downloading!!!");
                            });
                        }
                        return;
                    }
                }
                connection = new HSHttpConnection(url, HttpRequest.Method.GET);
                mDownloadingItems.add(new DownloadingRecord(url, connection));
            }

            if (mCacheDir.exists()) {
                final File output = getDownloadFile(url);
                HSLog.d(TAG, "Temp cache file path: " + output.getAbsolutePath());
                connection.setDownloadFile(output);
                connection.setDataReceivedListener((hsHttpConnection, bytes, l, l1) -> {
                    HSLog.d(TAG, "url: " + url + ", bytes.length = " + bytes.length + " l = " + l + " l1 = " + l1);
                    if (listener != null && l1 > 0) {
                        Threads.postOnMainThread(() -> {
                            listener.onProgress(url, l * 1.0f / l1);
                        });
                    }
                });
                connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                    @Override
                    public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                        if (!isDownloadTaskCanceled()) {
                            removeTask(url);
                            boolean connectionSucceeded = hsHttpConnection.isSucceeded();
                            if (connectionSucceeded) {
                                if (listener != null) {
                                    Threads.postOnMainThread(() -> listener.onSuccess(url, output));
                                }
                            } else {
                                if (listener != null) {
                                    Threads.postOnMainThread(() -> listener.onFail(url, "Connection Failed!!!"));
                                }
                            }
                        } else {
                            if (listener != null) {
                                Threads.postOnMainThread(() -> listener.onCancel(url));
                            }
                            //noinspection ResultOfMethodCallIgnored
                            output.delete();
                        }
                    }

                    @Override
                    public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                        if (!isDownloadTaskCanceled()) {
                            removeTask(url);
                            if (listener != null) {
                                Threads.postOnMainThread(() -> listener.onFail(url, "Connection Failed: " + hsError.getMessage()));
                            }
                        }
                    }

                    private boolean isDownloadTaskCanceled() {
                        synchronized (mDownloadingItems) {
                            for (int i = 0; i < mDownloadingItems.size(); i++) {
                                DownloadingRecord record = mDownloadingItems.get(i);
                                if (record.url.equals(url)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
                if (listener != null) {
                    Threads.postOnMainThread(() -> listener.onStart(url));
                }
                connection.startSync();
            }
        };
        mExecutor.execute(taskRunnable);
    }

    /**
     * @param url null for all
     */
    public void cancelTask(String url) {
        synchronized (mDownloadingItems) {
            for (Iterator<DownloadingRecord> iterator = mDownloadingItems.iterator();
                 iterator.hasNext(); ) {
                DownloadingRecord downloadingItem = iterator.next();
                if (TextUtils.isEmpty(url)) {
                    downloadingItem.connection.cancel();
                    iterator.remove();
                } else if (downloadingItem.url.equals(url)) {
                    downloadingItem.connection.cancel();
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public void cancelAllTask() {
        mTaskDeque.clear();
        cancelTask(null);
    }

    public File getDownloadFile(String url) {
        return new File(mCacheDir, FileUtils.md5(url));
    }

    private void removeTask(@NonNull String url) {
        synchronized (mDownloadingItems) {
            for (Iterator<DownloadingRecord> iterator = mDownloadingItems.iterator();
                 iterator.hasNext(); ) {
                DownloadingRecord downloadingItem = iterator.next();
                if (downloadingItem.url.equals(url)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private static class DownloadingRecord {
        String url;
        HSHttpConnection connection;

        DownloadingRecord(String url, HSHttpConnection connection) {
            this.url = url;
            this.connection = connection;
        }
    }
}
