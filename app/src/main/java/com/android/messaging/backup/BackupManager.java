package com.android.messaging.backup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.Telephony;
import android.text.format.DateUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.ui.appsettings.GeneralSettingSyncManager;
import com.android.messaging.util.CommonUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Threads;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BackupManager {
    public static final boolean RECOVERY_MODE = true;

    public interface MessageBackupListener {
        void onBackupStart();

        void onSyncAndLoadSuccess(int messageCount);

        void onBackupUpdate(int backedUpCount);

        void onBackupFailed();

        void onUploadStart();

        void onUploadSuccess();

        void onUploadFailed();

        void onBackupSuccess();
    }

    public interface MessageRestoreListener {
        void onDownloadStart();

        void onDownloadSuccess();

        void onDownloadFailed();

        void onRestoreStart(int messageCount);

        void onRestoreUpdate(int restoredCount);

        void onRestoreSuccess();

        void onRestoreFailed();
    }

    public interface MessageDeleteListener {
        void onDeleteStart();

        void onDeleteFailed();

        void onDeleteSuccess();
    }

    public interface CloudFileListLoadListener {
        void onLoadSuccess(List<BackupInfo> cloudList);

        void onLoadCancel();
    }

    interface CloudFileDownloadListener {
        void onDownloadSuccess(File file);

        void onDownloadFailed();
    }

    interface CloudFileUploadListener {
        void onUploadSuccess();

        void onUploadFailed();
    }

    interface MessageRestoreToDBListener {
        void onRestoreStart();

        void onRestoreUpdate(int restoredCount);

        void onRestoreSuccess();

        void onRestoreFailed();
    }

    private static BackupManager sInstance = new BackupManager();

    public static BackupManager getInstance() {
        return sInstance;
    }

    public void restoreMessages(BackupInfo backupInfo, MessageRestoreListener messageRestoreListener) {
        Threads.postOnThreadPoolExecutor(() -> {
            WeakReference<MessageRestoreListener> weakListener = new WeakReference<>(messageRestoreListener);
            CloudFileDownloadListener listener = new CloudFileDownloadListener() {
                @Override
                public void onDownloadSuccess(File file) {
                    if (weakListener.get() != null || backupInfo.getLocationType() == BackupInfo.CLOUD) {
                        weakListener.get().onDownloadSuccess();
                    }
                    //2.resolve file messages
                    List<BackupSmsMessage> backupMessages = BackupPersistManager.get().resolveMessages(file);
                    if (backupMessages.size() <= 0) {
                        weakListener.get().onRestoreFailed();
                        return;
                    }

                    //3.sync messages
                    Threads.postOnThreadPoolExecutor(() -> {
                        MessageRestoreToDBListener listener1 = new MessageRestoreToDBListener() {
                            @Override
                            public void onRestoreStart() {
                                Threads.postOnMainThread(() -> {
                                    if (weakListener.get() != null) {
                                        weakListener.get().onRestoreStart(backupMessages.size());
                                    }
                                });
                            }

                            @Override
                            public void onRestoreUpdate(int restoredCount) {
                                Threads.postOnMainThread(() -> {
                                    if (weakListener.get() != null) {
                                        weakListener.get().onRestoreUpdate(restoredCount);
                                    }
                                });
                            }

                            @Override
                            public void onRestoreSuccess() {
                                if (weakListener.get() != null) {
                                    weakListener.get().onRestoreSuccess();
                                }
                            }

                            @Override
                            public void onRestoreFailed() {
                                if (weakListener.get() != null) {
                                    weakListener.get().onRestoreFailed();
                                }
                            }
                        };
                        RestoreManager.get().restore(backupMessages, listener1);
                    });
                }

                @Override
                public void onDownloadFailed() {
                    if (weakListener.get() != null) {
                        weakListener.get().onDownloadFailed();
                    }
                }
            };
            //1.load backup file, local or cloud
            File backupFile;
            if (backupInfo.getLocationType() == BackupInfo.LOCAL) {
                backupFile = new File(CommonUtils.getDirectory(BackupPersistManager.BASE_PATH),
                        backupInfo.getKey());
                if (!backupFile.exists()) {
                    listener.onDownloadFailed();
                } else {
                    //go to next step
                    listener.onDownloadSuccess(backupFile);
                }
            } else {
                MessageRestoreListener listener1 = weakListener.get();
                if (listener1 != null) {
                    listener1.onDownloadStart();
                }
                downloadCloudBackup(backupInfo, listener);
            }
        });
    }

    public void backupMessages(@BackupInfo.BackupLocationType int backupType, MessageBackupListener listener) {
        Threads.postOnThreadPoolExecutor(() -> {
            WeakReference<MessageBackupListener> listenerWeakReference = new WeakReference<>(listener);
            if (listenerWeakReference.get() != null) {
                listenerWeakReference.get().onBackupStart();
            }

            DatabaseWrapper db = DataModel.get().getDatabase();
            try {
                db.execSQL(BackupDatabaseHelper.MessageColumn.CREATE_BACKUP_TABLE_SQL);
            } catch (Exception ignored) {

            }
            //1.sync
            long time = BackupSyncManager.get().sync();
            if (time == BackupSyncManager.SYNC_FAILED) {
                if (listenerWeakReference.get() != null) {
                    listenerWeakReference.get().onBackupFailed();
                }
                return;
            }

            //2.persist
            File file = BackupPersistManager.get().persistMessages(listenerWeakReference.get());

            if (file == null) {
                if (listenerWeakReference.get() != null) {
                    listenerWeakReference.get().onBackupFailed();
                }
                return;
            }

            if (backupType == BackupInfo.LOCAL) {
                if (listenerWeakReference.get() != null) {
                    listenerWeakReference.get().onBackupSuccess();
                }
                return;
            } else {
                if (listenerWeakReference.get() != null) {
                    listenerWeakReference.get().onUploadStart();
                }
            }

            //3.upload
            CloudFileUploadListener uploadListener = new CloudFileUploadListener() {
                @Override
                public void onUploadSuccess() {
                    if (listenerWeakReference.get() != null) {
                        listenerWeakReference.get().onUploadSuccess();
                        listenerWeakReference.get().onBackupSuccess();
                    }
                    if (backupType == BackupInfo.CLOUD) {
                        file.delete();
                    }
                }

                @Override
                public void onUploadFailed() {
                    if (listenerWeakReference.get() != null) {
                        listenerWeakReference.get().onUploadFailed();
                    }
                }
            };
            addCloudBackup(file, uploadListener);
        });
    }

    public void getCloudBackupFilesName(CloudFileListLoadListener listener) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (listener != null) {
                listener.onLoadCancel();
            }
        }
        DatabaseReference mDatabase = GeneralSettingSyncManager.getAuthUserRef();
        mDatabase.child("backup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<BackupInfo> list = new ArrayList<>();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    String fileName = s.getKey();
                    String filePath = (String) s.getValue();
                    BackupInfo info = new BackupInfo(BackupInfo.CLOUD, fileName, filePath);
                    list.add(info);
                }
                if (listener != null) {
                    listener.onLoadSuccess(list);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (listener != null) {
                    listener.onLoadCancel();
                }
            }
        });
    }

    public List<BackupInfo> getLocalBackupFilesInfo() {
        File file = CommonUtils.getDirectory(BackupPersistManager.BASE_PATH);
        if (!file.exists()) {
            return null;
        }
        File[] list = file.listFiles();
        if (list.length > 0) {
            List<BackupInfo> backupInfoList = new ArrayList<>();
            for (File file1 : list) {
                BackupInfo info = new BackupInfo(BackupInfo.LOCAL, file1.getName(), file1.getAbsolutePath());
                backupInfoList.add(info);
            }
            return backupInfoList;
        }
        return null;
    }

    private void removeCloudBackup(BackupInfo info) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference reference = storageReference.child("backup").child(uid).child(info.getKey());

        reference.delete()
                .addOnSuccessListener(a -> {
                    DatabaseReference mDatabase = GeneralSettingSyncManager.getAuthUserRef();
                    mDatabase.child("backup").child(info.getKey()).setValue(null)
                            .addOnSuccessListener(aVoid -> {
//                                if (listener != null) {
//                                    listener.onUploadSuccess();
//                                }
                            })
                            .addOnFailureListener(e -> {
//                                if (listener != null) {
//                                    listener.onUploadFailed();
//                                }
                            });
                })
                .addOnFailureListener(e -> {

                });
    }

    public void addCloudBackup(File file, CloudFileUploadListener listener) {
        //upload file
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference reference = storageReference.child("backup").child(uid).child(file.getName());
        Uri fileUri = Uri.fromFile(file);
        reference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Uri remoteUri = taskSnapshot.getUploadSessionUri();
                    if (remoteUri != null) {
                        //record file name in db
                        DatabaseReference mDatabase = GeneralSettingSyncManager.getAuthUserRef();
                        if (BackupManager.RECOVERY_MODE) {
                            //clear all records
                            mDatabase.child("backup").setValue(null)
                                    .addOnSuccessListener(a -> {
                                        //add new record
                                        mDatabase.child("backup").child(file.getName()).setValue(remoteUri.toString())
                                                .addOnSuccessListener(aVoid -> {
                                                    if (listener != null) {
                                                        listener.onUploadSuccess();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    if (listener != null) {
                                                        listener.onUploadFailed();
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        if (listener != null) {
                                            listener.onUploadFailed();
                                        }
                                    });
                        } else {
                            //add new record
                            mDatabase.child("backup").child(file.getName()).setValue(remoteUri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        if (listener != null) {
                                            listener.onUploadSuccess();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (listener != null) {
                                            listener.onUploadFailed();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onUploadFailed();
                    }
                });
    }

    public void downloadCloudBackup(BackupInfo info, CloudFileDownloadListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference reference = storageReference.child("backup").child(uid).child(info.getKey());
        File file;
        try {
            file = File.createTempFile(info.getKey(), null);
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onDownloadFailed();
            }
            return;
        }

        reference.getFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    //run on main thread
                    if (listener != null && file != null) {
                        listener.onDownloadSuccess(file);
                    }
                    file.deleteOnExit();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onDownloadFailed();
                    }
                    if (file != null) {
                        file.delete();
                    }
                });
    }

    public void deleteLocalMessages(int remainDays, MessageDeleteListener messageDeleteListener) {
        Threads.postOnThreadPoolExecutor(() -> {
            if (messageDeleteListener != null) {
                messageDeleteListener.onDeleteStart();
            }
            long time = System.currentTimeMillis() - remainDays * DateUtils.DAY_IN_MILLIS;
            try {
                DatabaseWrapper db = DataModel.get().getDatabase();
                ContentValues values = new ContentValues();
                values.put(BackupDatabaseHelper.MessageColumn.HIDDEN, 1);
                db.update(BackupDatabaseHelper.BACKUP_MESSAGE_TABLE, values,
                        BackupDatabaseHelper.MessageColumn.DATE + "<" + time, null);

                ContentResolver resolver = HSApplication.getContext().getContentResolver();
                resolver.delete(Telephony.Sms.CONTENT_URI, Telephony.Sms.DATE + "<" + time, null);
                if (messageDeleteListener != null) {
                    messageDeleteListener.onDeleteSuccess();
                }
            } catch (Exception e) {
                if (messageDeleteListener != null) {
                    messageDeleteListener.onDeleteFailed();
                }
            }
        });
    }
}
