package com.android.messaging.ui.emoji.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EmojiStyleDownloadManager {

    private final String TAG = EmojiStyleDownloadManager.class.getSimpleName();
    private final List<EmojiStyleDownloadTask> mConnections = new ArrayList<>();
    private static final File dir = new File(HSApplication.getContext().getFilesDir(), "emoji");

    private static EmojiStyleDownloadManager INSTANCE = new EmojiStyleDownloadManager();

    private EmojiStyleDownloadManager() {
    }

    public static EmojiStyleDownloadManager getInstance() {
        return INSTANCE;
    }

    public void rebindDownloadTask(String name, DownloadCallback callback) {
        for (EmojiStyleDownloadTask task : mConnections) {
            if (task.mName.equals(name)) {
                task.setCallback(callback);
                break;
            }
        }
    }

    public void downloadEmojiStyle(String url, String name, final DownloadCallback callback) {
        HSLog.i("emoji_download", url);
        synchronized (mConnections) {
            for (EmojiStyleDownloadTask task : mConnections) {
                // if has the same task in task list, use it and do not create new.
                if (task.getName().equals(name)) {
                    task.setCallback(callback);
                    return;
                }
            }
        }
        EmojiStyleDownloadTask task = new EmojiStyleDownloadTask(url, name);
        task.setCallback(callback);
        Threads.postOnThreadPoolExecutor(task);
        synchronized (mConnections) {
            mConnections.add(task);
        }
    }

    public static File getBaseDir() {
        return dir;
    }

    public boolean isDownloaded(String name) {
        File file = new File(dir, name);
        return file.exists();
    }

    public boolean isDownloading(String name) {
        synchronized (mConnections) {
            for (EmojiStyleDownloadTask task : mConnections) {
                if (task.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void cancelDownload(@NonNull final String name) {
        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                EmojiStyleDownloadTask target = null;
                synchronized (mConnections) {
                    for (EmojiStyleDownloadTask task : mConnections) {
                        if (task.getName().equals(name)) {
                            task.cancel();
                            target = task;
                            break;
                        }
                    }
                    if (target != null) {
                        mConnections.remove(target);
                    }
                }
            }
        });
    }

    public void cancelAllDownload() {
        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                synchronized (mConnections) {
                    for (EmojiStyleDownloadTask task : mConnections) {
                        task.cancel();
                    }
                    mConnections.clear();
                }
            }
        });
    }

    private void retryTask(EmojiStyleDownloadTask task) {
        synchronized (mConnections) {
            for (EmojiStyleDownloadTask item : mConnections) {
                if (item == task) {
                    Threads.postOnThreadPoolExecutor(task);
                    break;
                }
            }
        }
    }

    public class EmojiStyleDownloadTask implements Runnable {
        public static final long INTERVAL_UPDATE_PROGRESS = 100;

        private final String mUrl;
        private String mName;
        private long mTotalSize;
        private DownloadCallback callback;

        private final File file;
        private boolean mCanceled;
        private long mDownloadSize = 0;
        private int mRepeatCount = 0;
        private final int MAX_REPEAT = 5;

        private Handler mMainHandler = new Handler(Looper.getMainLooper());

        public EmojiStyleDownloadTask(String url, String name) {
            mName = name;
            mUrl = url;
            file = new File(dir, mName + ".zip");
        }

        @Override
        public void run() {
            mRepeatCount++;
            if (isCanceled()) {
                onDownloadCancel();
            }
            if (TextUtils.isEmpty(mUrl) || TextUtils.isEmpty(mName)) {
                String msg = "url is empty " + mUrl.isEmpty() + " mName is empty ";
                Log.e(TAG, "run: " + msg);
                onDownloadCancel();
                return;
            }

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    onDownloadFailed("mkdir fail");
                }
            }
            if (file.exists()) {
                onDownloadFinished(file);
                return;
            }

            final String tmpFileName = mName + "_temp";
            final File tmpFile = new File(dir, tmpFileName);
            RandomAccessFile fileAcc = null;
            HttpURLConnection conn = null;

            long startLocation = tmpFile.length();
            mDownloadSize = startLocation;
            try {
                fileAcc = new RandomAccessFile(tmpFile, "rwd");

                URL u = new URL(mUrl);
                conn = (HttpURLConnection) u.openConnection();

                mTotalSize = getFileSize();
                if (mTotalSize != 0) {
                    conn.setRequestProperty("Range", "bytes=" + startLocation + "-" + mTotalSize);
                }
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Accept", "video/mp4, image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                conn.setReadTimeout(10000);
                conn.connect();

                int code = conn.getResponseCode();
                if (code >= 300) {
                    onDownloadFailed("download err : Http response =  " + code + ", msg : " + conn.getResponseMessage());
                    return;
                }
                if (mTotalSize == 0) {
                    mTotalSize = conn.getContentLength();
                    saveFileSize(mTotalSize);
                }

                InputStream is = conn.getInputStream();
                fileAcc.seek(startLocation);
                byte[] buffer = new byte[2048];
                int len;
                long timestamp = SystemClock.elapsedRealtime();
                while ((len = is.read(buffer)) != -1) {
                    if (isCanceled()) {
                        HSLog.d(TAG, "emoji style download canceled. Save size = " + mDownloadSize);
                        break;
                    }
                    fileAcc.write(buffer, 0, len);
                    mDownloadSize += len;
                    long now = SystemClock.elapsedRealtime();
                    if (mDownloadSize > mTotalSize * 0.9 || now - timestamp > INTERVAL_UPDATE_PROGRESS) {
                        timestamp = now;
                        onDownloadProgress(mDownloadSize, mTotalSize);
                    }
                }
                if (isCanceled()) {
                    onDownloadCancel();
                } else {
                    onDownloadFinished(tmpFile);
                }
            } catch (Exception e) {
                HSLog.e(TAG, "emoji style download error");
                e.printStackTrace();
                onDownloadFailed("internet: " + e.toString());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (fileAcc != null) {
                    try {
                        fileAcc.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }

        public String getName() {
            return mName;
        }

        public boolean isCanceled() {
            return mCanceled;
        }

        public void cancel() {
            mCanceled = true;
        }

        public void setCallback(DownloadCallback callback) {
            this.callback = callback;
        }

        public long getFileSize() {
            return EmojiManager.getEmojiStyleFileSize(mName);
        }

        public void saveFileSize(long totalSize) {
            EmojiManager.setEmojiStyleFileSize(mName, totalSize);
        }

        private void onDownloadCancel() {
            synchronized (mConnections) {
                mConnections.remove(this);
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onCancel();
                    }
                }
            });
        }

        private void onDownloadSuccess() {
            synchronized (mConnections) {
                mConnections.remove(this);
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    EmojiManager.setEmojiStyleDownloaded(mName);
                    if (callback != null) {
                        callback.onSuccess(EmojiStyleDownloadTask.this);
                    }
                }
            });
        }

        private void onDownloadFailed(String msg) {
            if (mRepeatCount < MAX_REPEAT && msg.contains("internet")) {
                HSLog.d(TAG, "download try again");
                retryTask(this);
                return;
            }
            synchronized (mConnections) {
                mConnections.remove(this);
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onFail(EmojiStyleDownloadTask.this, msg);
                    }
                }
            });
        }

        private void onDownloadProgress(long downloadSize, long totalSize) {
            HSLog.i("emoji_download", downloadSize + "  " + totalSize);
            if (callback != null) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onUpdate(downloadSize, totalSize);
                        }
                    }
                });
            }
        }

        private void onDownloadFinished(File temp) {
            synchronized (mConnections) {
                mConnections.remove(this);
            }
            boolean moveSuccess = temp.renameTo(file);
            if (!moveSuccess) {
                onDownloadFailed("download has finished but rename failed");
                return;
            }
            temp.delete();
            try {
                unZipFolder(file.getAbsolutePath(), dir.getAbsolutePath());
                file.delete();
                onDownloadSuccess();
            } catch (IOException e) {
                e.printStackTrace();
                File file = new File(dir, mName);
                if (file.exists()) {
                    file.delete();
                }
                onDownloadFailed("decompress failed");
            }
        }

        private void unZipFolder(String zipFileString, String outputPathDir) throws IOException {
            String tempPath = outputPathDir + File.separator + "temp";
            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
            ZipEntry zipEntry;
            String szName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(tempPath + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(tempPath + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }

            File dir = new File(tempPath);
            File file = null;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    if (file == null) {
                        file = f;
                        continue;
                    }
                    if (f.length() > file.length()) {
                        file = f;
                    }
                }
            }
            file.renameTo(new File(outputPathDir, mName));
            inZip.close();
        }

    }

    public interface DownloadCallback {
        void onFail(EmojiStyleDownloadTask task, String msg);

        void onSuccess(EmojiStyleDownloadTask task);

        void onUpdate(long downloadSize, long totalSize);

        void onCancel();
    }
}
