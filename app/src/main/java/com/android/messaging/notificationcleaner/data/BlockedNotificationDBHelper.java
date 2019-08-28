package com.android.messaging.notificationcleaner.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlockedNotificationDBHelper extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOTIFICATION_PACKAGE_NAME = "package_name";
    public static final String COLUMN_NOTIFICATION_TITLE = "mTitle";
    public static final String COLUMN_NOTIFICATION_TEXT = "text";
    public static final String COLUMN_NOTIFICATION_POST_TIME = "post_time";

    private static final int DB_VERSION = 1;
    private static final String DB_FILE_NAME = "Notifications.db";
    private static final String TABLE_BLOCK_NOTIFICATIONS = "BlockNotifications";

    public BlockedNotificationDBHelper(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_BLOCK_NOTIFICATIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTIFICATION_PACKAGE_NAME + " TEXT, " +
                COLUMN_NOTIFICATION_TITLE + " TEXT, " +
                COLUMN_NOTIFICATION_TEXT + " TEXT, " +
                COLUMN_NOTIFICATION_POST_TIME + " LONG)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insert(ContentValues values) {
        return getWritableDatabase().insert(TABLE_BLOCK_NOTIFICATIONS, null, values);
    }

    public int delete(String whereClause, final String[] whereArgs) {
        return getWritableDatabase().delete(TABLE_BLOCK_NOTIFICATIONS, whereClause, whereArgs);
    }

    public Cursor query(String[] projection, String selection, String[] selectionArgs, String groupBy, String sortOrder, String limit) {
        return getReadableDatabase().query(TABLE_BLOCK_NOTIFICATIONS, projection, selection, selectionArgs, groupBy, null, sortOrder, limit);
    }
}
