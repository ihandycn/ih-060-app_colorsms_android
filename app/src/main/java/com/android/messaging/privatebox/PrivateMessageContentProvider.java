package com.android.messaging.privatebox;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;

public class PrivateMessageContentProvider extends ContentProvider {
    public static final String CONTENT_AUTHORITY = "com.color.sms.messages";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int SMS = 1;
    private static final int MMS = 2;
    private static final int MMS_INBOX = 3;
    private static final int MMS_SENT = 4;
    private static final int MMS_DRAFT = 5;
    private static final int MMS_OUTBOX = 6;
    private static final int SMS_MESSAGE = 7;
    private static final int MMS_MESSAGE = 8;

    public static final String SMS_PATH = "sms";
    public static final String MMS_PATH = "mms";
    public static final String SMS_MESSAGE_PATH = "sms/#";
    public static final String MMS_MESSAGE_PATH = "mms/#";
    public static final String MMS_INBOX_PATH = "mms/inbox";
    public static final String MMS_SENT_PATH = "mms/sent";
    public static final String MMS_DRAFTS_PATH = "mms/drafts";
    public static final String MMS_OUTBOX_PATH = "mms/outbox";

    public PrivateMessageContentProvider() {

    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(CONTENT_AUTHORITY, SMS_PATH, SMS);
        matcher.addURI(CONTENT_AUTHORITY, MMS_PATH, MMS);
        matcher.addURI(CONTENT_AUTHORITY, SMS_MESSAGE_PATH, SMS_MESSAGE);
        matcher.addURI(CONTENT_AUTHORITY, MMS_MESSAGE_PATH, MMS_MESSAGE);
        matcher.addURI(CONTENT_AUTHORITY, MMS_INBOX_PATH, MMS_INBOX);
        matcher.addURI(CONTENT_AUTHORITY, MMS_SENT_PATH, MMS_SENT);
        matcher.addURI(CONTENT_AUTHORITY, MMS_DRAFTS_PATH, MMS_DRAFT);
        matcher.addURI(CONTENT_AUTHORITY, MMS_OUTBOX_PATH, MMS_OUTBOX);
        return matcher;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        DatabaseWrapper db = DataModel.get().getDatabase();
        long id = ContentUris.parseId(uri);
        int count = 0;
        switch (buildUriMatcher().match(uri)) {
            case SMS:
                count = db.delete(PrivateSmsEntry.SMS_MESSAGE_TABLE, selection, selectionArgs);
                break;
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_OUTBOX:
            case MMS_DRAFT:
            case MMS:
                count = db.delete(PrivateMmsEntry.MMS_MESSAGE_TABLE, selection, selectionArgs);
                break;
            case SMS_MESSAGE:
                count = db.delete(PrivateSmsEntry.SMS_MESSAGE_TABLE,
                        PrivateSmsEntry._ID + " =? ", new String[]{String.valueOf(id)});
                break;
            case MMS_MESSAGE:
                count = db.delete(PrivateMmsEntry.MMS_MESSAGE_TABLE,
                        PrivateMmsEntry._ID + " =? ", new String[]{String.valueOf(id)});
                break;

        }
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data

        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        DatabaseWrapper db = DataModel.get().getDatabase();
        Uri resultUri = null;
        long id;
        switch (buildUriMatcher().match(uri)) {
            case SMS:
                id = db.insert(PrivateSmsEntry.SMS_MESSAGE_TABLE, null, values);
                resultUri = PrivateSmsEntry.buildUri(id);
                break;
            case MMS:
                id = db.insert(PrivateMmsEntry.MMS_MESSAGE_TABLE, null, values);
                resultUri = PrivateMmsEntry.buildUri(id);
                break;
            case MMS_INBOX:
                if (values != null) {
                    values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_INBOX);
                }
                id = db.insert(PrivateMmsEntry.MMS_MESSAGE_TABLE, null, values);
                resultUri = PrivateMmsEntry.buildUri(id);
                break;
            case MMS_SENT:
                if (values != null) {
                    values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT);
                }
                id = db.insert(PrivateMmsEntry.MMS_MESSAGE_TABLE, null, values);
                resultUri = PrivateMmsEntry.buildUri(id);
                break;
            case MMS_OUTBOX:
                if (values != null) {
                    values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_OUTBOX);
                }
                id = db.insert(PrivateMmsEntry.MMS_MESSAGE_TABLE, null, values);
                resultUri = PrivateMmsEntry.buildUri(id);
                break;
            case MMS_DRAFT:
                if (values != null) {
                    values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_DRAFTS);
                }
                id = db.insert(PrivateMmsEntry.MMS_MESSAGE_TABLE, null, values);
                resultUri = PrivateMmsEntry.buildUri(id);
                break;
        }

        return resultUri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor resultCursor = null;
        long id = ContentUris.parseId(uri);
        switch (buildUriMatcher().match(uri)) {
            case SMS:
                if (projection == null) {
                    projection = PrivateSmsEntry.sProjection;
                }
                if (TextUtils.isEmpty(selection)) {
                    resultCursor = db.query(PrivateSmsEntry.SMS_MESSAGE_TABLE, projection,
                            PrivateSmsEntry._ID + " =? ", new String[]{String.valueOf(id)},
                            sortOrder, null, null, null);
                } else {
                    resultCursor = db.query(PrivateSmsEntry.SMS_MESSAGE_TABLE, projection,
                            selection, selectionArgs, sortOrder, null, null, null);
                }
                break;
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_OUTBOX:
            case MMS_DRAFT:
            case MMS:
                if (projection == null) {
                    projection = PrivateMmsEntry.sProjection;
                }
                if (TextUtils.isEmpty(selection)) {
                    resultCursor = db.query(PrivateMmsEntry.MMS_MESSAGE_TABLE, projection,
                            PrivateMmsEntry._ID + " =? ", new String[]{String.valueOf(id)},
                            sortOrder, null, null, null);
                } else {
                    resultCursor = db.query(PrivateMmsEntry.MMS_MESSAGE_TABLE, projection,
                            selection, selectionArgs, sortOrder, null, null, null);
                }
                break;
            case SMS_MESSAGE:
                if (projection == null) {
                    projection = PrivateSmsEntry.sProjection;
                }
                resultCursor = db.query(PrivateSmsEntry.SMS_MESSAGE_TABLE, projection,
                        PrivateSmsEntry._ID + " =? ", new String[]{String.valueOf(id)},
                        sortOrder, null, null, null);
                break;
            case MMS_MESSAGE:
                if (projection == null) {
                    projection = PrivateMmsEntry.sProjection;
                }
                resultCursor = db.query(PrivateMmsEntry.MMS_MESSAGE_TABLE, projection,
                        PrivateMmsEntry._ID + " =? ", new String[]{String.valueOf(id)},
                        sortOrder, null, null, null);
                break;

        }
        return resultCursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        DatabaseWrapper db = DataModel.get().getDatabase();
        long id = ContentUris.parseId(uri);
        int count = 0;
        switch (buildUriMatcher().match(uri)) {
            case SMS:
                if (TextUtils.isEmpty(selection)) {
                    count = db.update(PrivateSmsEntry.SMS_MESSAGE_TABLE, values, selection, selectionArgs);
                } else {
                    count = db.update(PrivateSmsEntry.SMS_MESSAGE_TABLE, values,
                            PrivateSmsEntry._ID + " =? ", new String[]{String.valueOf(id)});
                }
                break;
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_OUTBOX:
            case MMS_DRAFT:
            case MMS:
                count = db.update(PrivateMmsEntry.MMS_MESSAGE_TABLE, values, selection, selectionArgs);
                break;
            case SMS_MESSAGE:
                count = db.update(PrivateSmsEntry.SMS_MESSAGE_TABLE, values,
                        PrivateSmsEntry._ID + " =? ", new String[]{String.valueOf(id)});
                break;
            case MMS_MESSAGE:
                count = db.update(PrivateMmsEntry.MMS_MESSAGE_TABLE, values,
                        PrivateMmsEntry._ID + " =? ", new String[]{String.valueOf(id)});
                break;
        }
        return count;
    }
}
