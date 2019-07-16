package com.android.messaging.ui.emoji.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EmojiStyleDownloadManager {

    private final String TAG = EmojiStyleDownloadManager.class.getSimpleName();
    private final List<EmojiStyleDownloadTask> mConnections = new ArrayList<>();
    private static final File dir = new File(HSApplication.getContext().getFilesDir(), "emoji");

    private static EmojiStyleDownloadManager INSTANCE = new EmojiStyleDownloadManager();

    private EmojiStyleDownloadManager() {
    }

    public static EmojiStyleDownloadManager getInstance(){
        return INSTANCE;
    }

    // callback must not a instance of inner class, because WeakReference.get() may return null;
    public void downloadEmojiStyle(String name, final DownloadCallback callback) {
        EmojiStyleDownloadTask task = new EmojiStyleDownloadTask(getUrl(name), name);
        task.setCallBack(callback);
        Threads.postOnThreadPoolExecutor(task);
        synchronized (mConnections) {
            mConnections.add(task);
        }
    }

    public static File getBaseDir(){
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

    private String getUrl(String name) {
        Map<String, String> map = getEmojiStyleConfig();
        return map.get(name);
    }

    private Map<String, String> getEmojiStyleConfig() {
        return (Map<String, String>) HSConfig.getMap("Application", "EmojiStyle");
    }

    public class EmojiStyleDownloadTask implements Runnable {
        public static final long INTERVAL_UPDATE_PROGRESS = 100;

        private final String mUrl;
        private String mName;
        private long mTotalSize;
        private WeakReference<DownloadCallback> callBack;

        private final File file;
        private boolean mCanceled;
        private long mDownloadSize = 0;

        private Handler mMainHandler = new Handler(Looper.getMainLooper());

        public EmojiStyleDownloadTask(String url, String name) {
            mName = name;
            mUrl = url;
            file = new File(dir, url.substring(url.lastIndexOf("/") + 1));
        }

        @Override
        public void run() {
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
                e.printStackTrace();
                onDownloadFailed(e.toString());
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

        public void setCallBack(DownloadCallback callback) {
            callBack = new WeakReference<>(callback);
        }

        public long getFileSize(){
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
                    if (callBack.get() != null) {
                        callBack.get().onCancel();
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
                    if (callBack.get() != null) {
                        callBack.get().onSuccess(EmojiStyleDownloadTask.this);
                    }
                }
            });
        }

        private void onDownloadFailed(String msg) {
            synchronized (mConnections) {
                mConnections.remove(this);
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callBack.get() != null) {
                        callBack.get().onFail(EmojiStyleDownloadTask.this, msg);
                    }
                }
            });
        }

        private void onDownloadProgress(long downloadSize, long totalSize) {
            if (callBack != null) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack.get() != null) {
                            callBack.get().onUpdate(downloadSize, totalSize);
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
                if(file.exists()){
                    file.delete();
                }
                onDownloadFailed("decompress failed");
            }
        }

    }

    public void unZipFolder(String zipFileString, String outPathString) throws IOException {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPathString + File.separator + szName);
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
        inZip.close();
    }


    public interface DownloadCallback {
        void onFail(EmojiStyleDownloadTask task, String msg);

        void onSuccess(EmojiStyleDownloadTask task);

        void onUpdate(long downloadSize, long totalSize);

        void onCancel();
    }
}
