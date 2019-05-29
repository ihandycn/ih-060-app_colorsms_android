package com.android.messaging.backup;

public class BackupManager {
    interface MessageBackupListener {
        void onBackupStart();

        void onSyncAndLoadSuccess(int messageCount);

        void onBackupUpdate(int backedUpCount);

        void onBackupSuccess();

        void onBackupFailed();

        void onUploadSuccess();

        void onUploadFailed();
    }

    interface MessageRestoreListener {
        void onDownloadStart();

        void onDownloadSuccess();

        void onDownloadFailed();

        void onRestoreStart(int messageCount);

        void onRestoreUpdate(int restoredCount);

        void onRestoreSuccess();

        void onRestoreFailed();
    }

    interface MessageDeleteListener {
        void onDeleteStart();

        void onScanFinished(int deleteCount);

        void onDeleteUpdate(int deletedCount);

        void onDeleteFinished();
    }

    private static BackupManager sInstance = new BackupManager();

    public static BackupManager getInstance() {
        return sInstance;
    }

    public void backupMessages(BackupInfo info, MessageBackupListener messageBackupListener) {

    }

    public void restoreMessages(BackupInfo backupInfo, MessageRestoreListener messageRestoreListener) {

    }

    public void deleteLocalMessages(int remainDays, MessageDeleteListener messageDeleteListener) {

    }

    public BackupInfo getLocalBackupInfo() {
        return new BackupInfo(BackupInfo.LOCAL, "ddd", "fasdfasdfa");
    }

    public BackupInfo getCloudBackupInfo() {
        return new BackupInfo(BackupInfo.CLOUD, "aaa", "fasdfasfasdfafa");
    }

    public BackupInfo getLocalRestoreInfo() {
        return new BackupInfo(BackupInfo.LOCAL, "ddd", "fasdfasdfa");
    }

    public BackupInfo getCloudRestoreInfo() {
        return new BackupInfo(BackupInfo.CLOUD, "aaa", "fasdfasfasdfafa");
    }

    private void syncMessages() {

    }
}
