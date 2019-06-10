package com.android.messaging.backup;

import android.database.Cursor;
import android.text.TextUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.OsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BackupPersistManager {

    static final String BASE_PATH = "backups";

    private static final char VERSION_1_START_CODE = 17;
    private static final char VERSION_1_SPLIT_CODE = 18;

    private static BackupPersistManager sInstance;

    public static BackupPersistManager get() {
        if (sInstance == null) {
            sInstance = new BackupPersistManager();
        }
        return sInstance;
    }

    private BackupPersistManager() {

    }

    public List<BackupSmsMessage> resolveMessages(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            int size = (int) file.length();
            byte[] data = new byte[size];
            is.read(data);
            is.close();
            String text = new String(data, "UTF-8");
            return parseText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public File persistMessages(BackupManager.MessageBackupListener listener) {
        WeakReference<BackupManager.MessageBackupListener> listenerWeakReference = new WeakReference<>(listener);
        //1.load from db
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = db.query(BackupDatabaseHelper.BACKUP_MESSAGE_TABLE,
                BackupDatabaseHelper.MessageColumn.getProjection(),
                null, null, null, null, null);

        if (listenerWeakReference.get() != null) {
            listenerWeakReference.get().onSyncAndLoadSuccess(cursor.getCount());
        }

        if (cursor == null) {
            return null;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        //2.cursor -> SmsMessage
        List<BackupSmsMessage> messages = new ArrayList<>();
        if (cursor != null) {
            int i = 1;
            while (cursor.moveToNext()) {
                BackupSmsMessage message = new BackupSmsMessage();
                message.load(cursor);
                messages.add(message);
                if (listenerWeakReference.get() != null) {
                    listenerWeakReference.get().onBackupUpdate(i);
                }
                i++;
            }
            cursor.close();
        }

        //3.SmsMessage -> string
        String[] messageStr = new String[messages.size()];

        for (int i = 0; i < messageStr.length; i++) {
            messageStr[i] = getMessageString(messages.get(i));
        }

        //4.write string
        File file = new File(CommonUtils.getDirectory(BASE_PATH), String.valueOf(System.currentTimeMillis()));

        try {
            FileWriter writer = new FileWriter(file, true);
            for (String s : messageStr) {
                writer.write(s);
            }
            writer.flush();
            writer.close();

            //remove useless files
            if (BackupManager.RECOVERY_MODE) {
                for (File f : CommonUtils.getDirectory(BASE_PATH).listFiles()) {
                    if (!f.getName().equals(file.getName())) {
                        f.delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public List<BackupSmsMessage> parseText(String text) {
        List<BackupSmsMessage> messageList = new ArrayList<>();
        String[] infoList = text.split(String.valueOf(VERSION_1_START_CODE));
        for (String k : infoList) {
            if (TextUtils.isEmpty(k)) {
                continue;
            }
            String[] info = k.split(String.valueOf(VERSION_1_SPLIT_CODE));

            if (info.length >= 17) {
                BackupSmsMessage message = new BackupSmsMessage();
                load(info, message);
                messageList.add(message);
            }
        }
        return messageList;
    }

    public void load(String[] info, BackupSmsMessage message) {
        int i = 0;
        message.mMessageId = Long.valueOf(info[i++]);
        message.mThreadId = Long.valueOf(info[i++]);
        message.mAddress = info[i++];
        message.mPerson = Long.valueOf(info[i++]);
        message.mDate = Long.valueOf(info[i++]);
        message.mDateSend = Long.valueOf(info[i++]);
        message.mProtocol = Integer.parseInt(info[i++]);
        message.mRead = Integer.parseInt(info[i++]);
        message.mSeen = Integer.parseInt(info[i++]);
        message.mStatus = Integer.parseInt(info[i++]);
        message.mType = Integer.parseInt(info[i++]);
        message.mReplyPathPresent = Integer.parseInt(info[i++]);
        message.mSubject = info[i++];
        message.mBody = info[i++];
        message.mServiceCenter = info[i++];
        message.mLocked = Integer.parseInt(info[i++]);
        message.mErrorCode = Integer.parseInt(info[i++]);
        if (OsUtil.isAtLeastL_MR1() && info.length == i + 1) {
            message.mSubId = Integer.parseInt(info[i]);
        }
    }

    public String getMessageString(BackupSmsMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message.mMessageId).append(VERSION_1_SPLIT_CODE)
                .append(message.mThreadId).append(VERSION_1_SPLIT_CODE)
                .append(message.mAddress).append(VERSION_1_SPLIT_CODE)
                .append(message.mPerson).append(VERSION_1_SPLIT_CODE)
                .append(message.mDate).append(VERSION_1_SPLIT_CODE)
                .append(message.mDateSend).append(VERSION_1_SPLIT_CODE)
                .append(message.mProtocol).append(VERSION_1_SPLIT_CODE)
                .append(message.mRead).append(VERSION_1_SPLIT_CODE)
                .append(message.mSeen).append(VERSION_1_SPLIT_CODE)
                .append(message.mStatus).append(VERSION_1_SPLIT_CODE)
                .append(message.mType).append(VERSION_1_SPLIT_CODE)
                .append(message.mReplyPathPresent).append(VERSION_1_SPLIT_CODE)
                .append(message.mSubject).append(VERSION_1_SPLIT_CODE)
                .append(message.mBody).append(VERSION_1_SPLIT_CODE)
                .append(message.mServiceCenter).append(VERSION_1_SPLIT_CODE)
                .append(message.mLocked).append(VERSION_1_SPLIT_CODE)
                .append(message.mErrorCode).append(VERSION_1_SPLIT_CODE);
        if (OsUtil.isAtLeastL_MR1()) {
            sb.append(message.mSubId).append(VERSION_1_SPLIT_CODE);
        }
        sb.append(VERSION_1_START_CODE);
        return sb.toString();
    }
}
