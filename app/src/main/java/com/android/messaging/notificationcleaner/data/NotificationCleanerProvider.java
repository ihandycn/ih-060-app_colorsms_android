package com.android.messaging.notificationcleaner.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.messaging.notificationcleaner.DateUtil;
import com.android.messaging.notificationcleaner.NotificationCleanerConstants;
import com.android.messaging.notificationcleaner.NotificationCleanerUtil;
import com.android.messaging.notificationcleaner.BuglePackageManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationCleanerProvider extends ContentProvider {

    private static final String AUTHORITY_NAME = ".notification_organizer";

    private static final String SWITCHER_PATH = "switcher";
    private static final String BLOCK_APP_PATH = "block_apps";
    private static final String BLOCK_NOTIFICATION_PATH = "block_notifications";
    private static final String BLOCK_NOTIFICATION_APP_PATH = "block_notifications_app";

    private static final int CODE_BLOCK_NOTIFICATION = 3;
    private static final int CODE_BLOCK_NOTIFICATION_APP = 4;

    private static final String PREF_FILE_NAME = "optimizer_notification_organizer";
    private static final String PREF_KEY_UNBLOCK_APP_LIST = "PREF_KEY_UNBLOCK_APP_LIST";
    private static final String PREF_KEY_SWITCHER = "PREF_KEY_SWITCHER";
    private static final String PREF_KEY_TIME_MILLIS = "PREF_KEY_TIME_MILLIS";
    private static final String PREF_KEY_FIRST_GUIDE_FLAG = "PREF_KEY_FIRST_GUIDE_FLAG";
    private static final String PREF_KEY_SWITCH_OFF_BEFORE_FLAG = "PREF_KEY_SWITCH_OFF_BEFORE_FLAG";

    public static final String EXTRA_APP_PACKAGE_NAME = "EXTRA_APP_PACKAGE_NAME";
    public static final String EXTRA_APP_PACKAGE_NAME_LIST = "EXTRA_APP_PACKAGE_NAME_LIST";
    public static final String EXTRA_IS_APP_BLOCKED = "EXTRA_IS_APP_BLOCKED";
    public static final String EXTRA_SWITCH_STATE = "EXTRA_SWITCH_STATE";
    public static final String EXTRA_KEY_TIME_MILLIS = "EXTRA_KEY_TIME_MILLIS";
    public static final String EXTRA_IS_FIRST_GUIDE_FLAG = "EXTRA_IS_FIRST_GUIDE_FLAG";
    public static final String EXTRA_HAS_BEEN_SWITCHED_OFF = "EXTRA_HAS_BEEN_SWITCHED_OFF";

    public static final String METHOD_SET_SWITCH = "METHOD_SET_SWITCH";
    public static final String METHOD_GET_SWITCH = "METHOD_GET_SWITCH";

    public static final String METHOD_ADD_APP_TO_UNBLOCK_LIST = "METHOD_ADD_APP_TO_UNBLOCK_LIST";
    public static final String METHOD_REMOVE_APP_FROM_UNBLOCK_LIST = "METHOD_REMOVE_APP_FROM_UNBLOCK_LIST";
    public static final String METHOD_GET_UNBLOCKED_APP_LIST = "METHOD_GET_UNBLOCKED_APP_LIST";
    public static final String METHOD_IS_APP_BLOCKED = "METHOD_IS_APP_BLOCKED";

    public static final String METHOD_GET_LAST_MAKING_TOAST_TIME = "METHOD_GET_LAST_MAKING_TOAST_TIME";
    public static final String METHOD_SET_LAST_MAKING_TOAST_TIME = "METHOD_SET_LAST_MAKING_TOAST_TIME";

    public static final String METHOD_GET_FIRST_GUIDE_FLAG = "METHOD_GET_FIRST_GUIDE_FLAG";
    public static final String METHOD_SET_FIRST_GUIDE_FLAG = "METHOD_SET_FIRST_GUIDE_FLAG";

    public static final String METHOD_HAS_BEEN_SWITCHED_OFF = "METHOD_HAS_BEEN_SWITCHED_OFF";

    private static final int PROVIDER_CALL_RETRY_TIMES = 2;

    private static final String SEPARATOR_APP_PACKAGE_NAMES_TEXT = ";";
    private static final int MAX_NOTIFICATION_SHOW_BLOCKED_APP_COUNT = 7;
    private static final int MAX_QUERY_BLOCKED_NOTIFICATION_COUNT = 500;

    private UriMatcher uriMatcher;
    private BlockedNotificationDBHelper blockNotificationDBHelper;

    public static Uri createOrganizerContentUri(Context context) {
        return Uri.parse("content://" + context.getPackageName() + AUTHORITY_NAME + "/");
    }

    public static Uri createBlockNotificationContentUri(Context context) {
        return Uri.parse("content://" + context.getPackageName() + AUTHORITY_NAME + "/" + BLOCK_NOTIFICATION_PATH);
    }

    public static Uri createBlockNotificationGroupByPackageNameContentUri(Context context) {
        return Uri.parse("content://" + context.getPackageName() + AUTHORITY_NAME + "/" + BLOCK_NOTIFICATION_APP_PATH);
    }

    public static Uri createBlockAppContentUri(Context context) {
        return Uri.parse("content://" + context.getPackageName() + AUTHORITY_NAME + "/" + BLOCK_APP_PATH);
    }

    public static Uri createSwitcherContentUri(Context context) {
        return Uri.parse("content://" + context.getPackageName() + AUTHORITY_NAME + "/" + SWITCHER_PATH);
    }

    public static boolean isNotificationOrganizerSwitchOn() {
        Bundle retBundle = callProvider(createSwitcherContentUri(HSApplication.getContext()), METHOD_GET_SWITCH, null);
        HSLog.d(NotificationCleanerConstants.TAG, "isNotificationOrganizerSwitchOn ======= retBundle = " + retBundle);
        return (null == retBundle) || retBundle.getBoolean(EXTRA_SWITCH_STATE, true);
    }

    public static void switchNotificationOrganizer(boolean isOpen) {
        if (isOpen) {
            NotificationCleanerUtil.setNotificationCleanerEverOpened();
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_SWITCH_STATE, isOpen);

        callProvider(NotificationCleanerProvider.createSwitcherContentUri(HSApplication.getContext()),
                NotificationCleanerProvider.METHOD_SET_SWITCH, bundle);
    }

    public static boolean isFirstGuide() {
        Bundle retBundle = callProvider(createOrganizerContentUri(HSApplication.getContext()), METHOD_GET_FIRST_GUIDE_FLAG);

        return retBundle != null && retBundle.getBoolean(EXTRA_IS_FIRST_GUIDE_FLAG);
    }

    public static void setFirstGuideFlag(boolean isFirstGuide) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_IS_FIRST_GUIDE_FLAG, isFirstGuide);

        callProvider(createOrganizerContentUri(HSApplication.getContext()), METHOD_SET_FIRST_GUIDE_FLAG, bundle);
    }

    public static boolean hasBeenSwitchedOff() {
        Bundle retBundle = callProvider(createOrganizerContentUri(HSApplication.getContext()), METHOD_HAS_BEEN_SWITCHED_OFF);

        return retBundle != null && retBundle.getBoolean(EXTRA_HAS_BEEN_SWITCHED_OFF);
    }

    public static List<String> fetchRecentBlockedAppPackageNameList(boolean isContainUnblockApp) {
        Cursor cursor = HSApplication.getContext().getContentResolver().query(
                createBlockNotificationGroupByPackageNameContentUri(HSApplication.getContext()),
                new String[]{BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME},
                getSelectionAppBlockedAndTimeValid(isContainUnblockApp),
                new String[]{String.valueOf(DateUtil.getStartTimeStampOfDaysAgo(NotificationCleanerConstants.DAYS_NOTIFICATIONS_KEEP))},
                BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME + " DESC");

        if (null == cursor) {
            return new ArrayList<>();
        }

        List<String> apps = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                apps.add(cursor.getString(cursor.getColumnIndex(
                        BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME)));
            }

        } finally {
            cursor.close();
        }

        return apps;
    }

    public static boolean isAppBlocked(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        Bundle extra = new Bundle();
        extra.putString(EXTRA_APP_PACKAGE_NAME, packageName);

        Bundle retBundle = callProvider(createBlockAppContentUri(HSApplication.getContext()), METHOD_IS_APP_BLOCKED, extra);

        return retBundle != null && retBundle.getBoolean(EXTRA_IS_APP_BLOCKED);
    }

    public static int fetchBlockedAndTimeValidNotificationCount(boolean isContainUnblockApp) {
        Cursor cursor = HSApplication.getContext().getContentResolver().query(
                NotificationCleanerProvider.createBlockNotificationContentUri(HSApplication.getContext()),
                new String[]{"Count(*) count"}, getSelectionAppBlockedAndTimeValid(isContainUnblockApp),
                new String[]{String.valueOf(DateUtil.getStartTimeStampOfDaysAgo(NotificationCleanerConstants.DAYS_NOTIFICATIONS_KEEP))},
                null);

        if (cursor == null) {
            return 0;
        }

        int validBlockNotificationCount = 0;
        try {
            if (cursor.moveToFirst()) {
                validBlockNotificationCount = cursor.getInt(cursor.getColumnIndex("count"));
            }
        } finally {
            cursor.close();
        }

        return validBlockNotificationCount;
    }

    public static List<BlockedNotificationInfo> fetchBlockedAndTimeValidNotificationDataList(boolean isContainUnblockApp) {
        Cursor cursor = HSApplication.getContext().getContentResolver().query(
                NotificationCleanerProvider.createBlockNotificationContentUri(HSApplication.getContext()),
                null,
                getSelectionAppBlockedAndTimeValid(isContainUnblockApp),
                new String[]{String.valueOf(DateUtil.getStartTimeStampOfDaysAgo(NotificationCleanerConstants.DAYS_NOTIFICATIONS_KEEP))},
                BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME + " DESC");

        if (null == cursor) {
            return new ArrayList<>();
        }

        List<BlockedNotificationInfo> notifications = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                BlockedNotificationInfo appNotificationInfo = new BlockedNotificationInfo();

                appNotificationInfo.packageName = cursor.getString(cursor.getColumnIndex(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME));
                appNotificationInfo.postTime = cursor.getLong(cursor.getColumnIndex(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME));
                appNotificationInfo.text = cursor.getString(cursor.getColumnIndex(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_TEXT));
                appNotificationInfo.title = cursor.getString(cursor.getColumnIndex(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_TITLE));
                appNotificationInfo.idInDB = cursor.getInt(cursor.getColumnIndex(BlockedNotificationDBHelper.COLUMN_ID));

                notifications.add(appNotificationInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            cursor.close();
        }
        return notifications;
    }

    private static String getSelectionAppBlockedAndTimeValid(boolean isContainUnblockApp) {
        if (isContainUnblockApp) {
            return BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME + ">? ";
        }

        Bundle retBundle = callProvider(createBlockAppContentUri(HSApplication.getContext()), METHOD_GET_UNBLOCKED_APP_LIST);

        if (retBundle == null) {
            return BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME + ">? ";
        }

        List<String> unblockedAppList = retBundle.getStringArrayList(EXTRA_APP_PACKAGE_NAME_LIST);

        if (unblockedAppList == null) {
            return BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME + ">? ";
        }

        String where = BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME + " NOT IN (";
        if (!unblockedAppList.isEmpty()) {
            where += "\'" + unblockedAppList.get(0) + "\'";
        }

        for (int i = 1; i < unblockedAppList.size(); ++i) {
            where += ",\'" + unblockedAppList.get(i) + "\'";
        }
        where += ") AND (" + BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME + ">?)";
        return where;
    }

    private static Bundle callProvider(Uri uri, String method) {
        return callProvider(uri, method, null);
    }

    private static Bundle callProvider(Uri uri, String method, Bundle extras) {
        int retry = 0;
        while (retry <= PROVIDER_CALL_RETRY_TIMES) {
            try {
                return HSApplication.getContext().getContentResolver().call(uri, method, null, extras);
            } catch (Exception e) {
                retry++;
                Thread.yield();
            }
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(HSApplication.getContext().getPackageName() + AUTHORITY_NAME, BLOCK_NOTIFICATION_PATH, CODE_BLOCK_NOTIFICATION);
        uriMatcher.addURI(HSApplication.getContext().getPackageName() + AUTHORITY_NAME, BLOCK_NOTIFICATION_APP_PATH, CODE_BLOCK_NOTIFICATION_APP);
        blockNotificationDBHelper = new BlockedNotificationDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case CODE_BLOCK_NOTIFICATION:
                return blockNotificationDBHelper.query(
                        projection, selection, selectionArgs, null, sortOrder, String.valueOf(MAX_QUERY_BLOCKED_NOTIFICATION_COUNT));
            case CODE_BLOCK_NOTIFICATION_APP:
                return blockNotificationDBHelper.query(projection, selection, selectionArgs,
                        BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME,
                        sortOrder, String.valueOf(MAX_NOTIFICATION_SHOW_BLOCKED_APP_COUNT));
            default:
                if (HSLog.isDebugging()) { // TODO using professional way to judge debugging
                    throw new IllegalArgumentException("Error Uri: " + uri);
                }
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case CODE_BLOCK_NOTIFICATION:
                final long id = blockNotificationDBHelper.insert(values);
                if (HSApplication.isDebugging && id < 0) {
                    throw new SQLiteException("Unable to insert " + values + " for " + uri);
                }

                Uri returnUri = ContentUris.withAppendedId(uri, id);
                HSApplication.getContext().getContentResolver().notifyChange(returnUri, null);
                HSApplication.getContext().getContentResolver().notifyChange(createBlockAppContentUri(getContext()), null);

                return returnUri;

            default:
                return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case CODE_BLOCK_NOTIFICATION: {
                int count = 0;

                try {
                    count = blockNotificationDBHelper.delete(selection, selectionArgs);
                    HSApplication.getContext().getContentResolver().notifyChange(uri, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return count;
            }

            default:
                return -1;
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, String arg, Bundle extras) {
        Bundle bundle = null;
        switch (method) {
            case METHOD_SET_SWITCH:
                final boolean isOpen = extras.getBoolean(EXTRA_SWITCH_STATE);
                HSPreferenceHelper preferenceHelper = HSPreferenceHelper.create(getContext(), PREF_FILE_NAME);
                preferenceHelper.putBoolean(PREF_KEY_SWITCHER, isOpen);
                HSApplication.getContext().getContentResolver().notifyChange(createSwitcherContentUri(getContext()), null);
                if (!isOpen) {
                    preferenceHelper.putBoolean(PREF_KEY_SWITCH_OFF_BEFORE_FLAG, true);
                }
                break;
            case METHOD_GET_SWITCH:
                bundle = new Bundle();
                bundle.putBoolean(EXTRA_SWITCH_STATE, HSPreferenceHelper.create(getContext(), PREF_FILE_NAME)
                        .getBoolean(PREF_KEY_SWITCHER, false));
                break;
            case METHOD_ADD_APP_TO_UNBLOCK_LIST: {
                List<String> appList = getUnblockAppList();
                if (appList.contains(extras.getString(EXTRA_APP_PACKAGE_NAME))) {
                    break;
                }
                appList.add(extras.getString(EXTRA_APP_PACKAGE_NAME));
                HSPreferenceHelper.create(getContext(), PREF_FILE_NAME).putString(PREF_KEY_UNBLOCK_APP_LIST,
                        TextUtils.join(SEPARATOR_APP_PACKAGE_NAMES_TEXT, appList));
                HSApplication.getContext().getContentResolver().notifyChange(createBlockAppContentUri(getContext()), null);
                break;
            }
            case METHOD_REMOVE_APP_FROM_UNBLOCK_LIST: {
                List<String> appList = getUnblockAppList();
                if (!appList.remove(extras.getString(EXTRA_APP_PACKAGE_NAME))) {
                    break;
                }
                HSPreferenceHelper.create(getContext(), PREF_FILE_NAME).putString(PREF_KEY_UNBLOCK_APP_LIST,
                        TextUtils.join(SEPARATOR_APP_PACKAGE_NAMES_TEXT, appList));
                HSApplication.getContext().getContentResolver().notifyChange(createBlockAppContentUri(getContext()), null);
                break;
            }
            case METHOD_GET_UNBLOCKED_APP_LIST:
                bundle = new Bundle();
                bundle.putStringArrayList(EXTRA_APP_PACKAGE_NAME_LIST, getUnblockAppList());
                break;
            case METHOD_IS_APP_BLOCKED:
                bundle = new Bundle();
                bundle.putBoolean(EXTRA_IS_APP_BLOCKED, !getUnblockAppList().contains(extras.getString(EXTRA_APP_PACKAGE_NAME)));
                break;
            case METHOD_GET_LAST_MAKING_TOAST_TIME:
                bundle = new Bundle();
                bundle.putLong(EXTRA_KEY_TIME_MILLIS,
                        HSPreferenceHelper.create(getContext(), PREF_FILE_NAME).getLong(PREF_KEY_TIME_MILLIS, 0));
                break;
            case METHOD_SET_LAST_MAKING_TOAST_TIME:
                HSPreferenceHelper.create(getContext(), PREF_FILE_NAME)
                        .putLong(PREF_KEY_TIME_MILLIS, extras.getLong(EXTRA_KEY_TIME_MILLIS));
                break;
            case METHOD_GET_FIRST_GUIDE_FLAG:
                bundle = new Bundle();
                bundle.putBoolean(EXTRA_IS_FIRST_GUIDE_FLAG,
                        HSPreferenceHelper.create(getContext(), PREF_FILE_NAME).getBoolean(PREF_KEY_FIRST_GUIDE_FLAG, true));
                break;
            case METHOD_SET_FIRST_GUIDE_FLAG:
                HSPreferenceHelper.create(getContext(), PREF_FILE_NAME)
                        .putBoolean(PREF_KEY_FIRST_GUIDE_FLAG, extras.getBoolean(EXTRA_IS_FIRST_GUIDE_FLAG));
                break;
            case METHOD_HAS_BEEN_SWITCHED_OFF:
                bundle = new Bundle();
                bundle.putBoolean(EXTRA_HAS_BEEN_SWITCHED_OFF,
                        HSPreferenceHelper.create(getContext(), PREF_FILE_NAME).getBoolean(PREF_KEY_SWITCH_OFF_BEFORE_FLAG, false));
                break;
            default:
                break;
        }

        return bundle;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getUnblockAppList() {
        String appsText = HSPreferenceHelper.create(getContext(), PREF_FILE_NAME)
                .getString(PREF_KEY_UNBLOCK_APP_LIST, null);
        ArrayList<String> unblockAppList = new ArrayList<>();
        if (TextUtils.isEmpty(appsText)) {
            List<String> unblockAppsInConfig = (List<String>) HSConfig.getList("Application", "NotificationCleaner", "UnblockAppsList");
            List<ApplicationInfo> applicationInfoList = BuglePackageManager.getInstance().getInstalledApplications();
            for (ApplicationInfo applicationInfo : applicationInfoList) {
                if (null != applicationInfo && unblockAppsInConfig.contains(applicationInfo.packageName)) {
                    unblockAppList.add(applicationInfo.packageName);
                }
            }
            // Add self to unblock app list
            if (!unblockAppList.contains(HSApplication.getContext().getPackageName())) {
                unblockAppList.add(HSApplication.getContext().getPackageName());
            }
            HSPreferenceHelper.create(getContext(), PREF_FILE_NAME).putString(PREF_KEY_UNBLOCK_APP_LIST,
                    TextUtils.join(SEPARATOR_APP_PACKAGE_NAMES_TEXT, unblockAppList));
            return unblockAppList;
        }
        return new ArrayList<>(Arrays.asList(appsText.split(SEPARATOR_APP_PACKAGE_NAMES_TEXT)));
    }

}
