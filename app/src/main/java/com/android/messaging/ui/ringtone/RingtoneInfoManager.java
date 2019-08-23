package com.android.messaging.ui.ringtone;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.UriUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class RingtoneInfoManager {

    private static String PREF_FILE_NAME = "pref_file_ringtone";

    private static String PREF_KEY_APP_TYPE = "pref_key_app_type";
    private static String PREF_KEY_APP_NAME = "pref_key_app_name";
    private static String PREF_KEY_APP_URI = "pref_key_app_uri";

    public static String SILENT_URI = "";

    public static RingtoneInfo getCurSound() {
        final Preferences prefs = Preferences.get(PREF_FILE_NAME);
        String ringtoneUri = prefs.getString(PREF_KEY_APP_URI, null);
        String ringtoneName = "";
        int ringtoneType = RingtoneInfo.TYPE_SYSTEM;

        if (ringtoneUri == null) {
            ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
            return getSystemRingtoneInfo(ringtoneUri);
        } else {
            ringtoneName = prefs.getString(PREF_KEY_APP_NAME, "");
            ringtoneType = prefs.getInt(PREF_KEY_APP_TYPE, ringtoneType);
            RingtoneInfo info = new RingtoneInfo();
            info.uri = ringtoneUri;
            info.name = ringtoneName;
            info.type = ringtoneType;
            return info;
        }

    }

    public static void setCurSound(RingtoneInfo info) {
        info.uri = info.uri == null ? SILENT_URI : info.uri;
        Context context = HSApplication.getContext();

        Preferences prefs = Preferences.get(PREF_FILE_NAME);

        String prefKey = context.getString(R.string.notification_sound_pref_key);
        String currentRingtone = prefs.getString(prefKey, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        if (currentRingtone != null && !currentRingtone.equals(info.uri)) {
            BugleAnalytics.logEvent("Customize_Notification_Sound_Change", true, "from", "settings");
            BugleFirebaseAnalytics.logEvent("Customize_Notification_Sound_Change", "from", "settings");
        }
        prefs.putInt(PREF_KEY_APP_TYPE, info.type);
        prefs.putString(PREF_KEY_APP_NAME, info.name);
        prefs.putString(PREF_KEY_APP_URI, info.uri);
    }

    public static RingtoneInfo getSystemRingtoneInfo(String uri) {
        if (uri.equals(SILENT_URI)) {
            RingtoneInfo info = new RingtoneInfo();
            info.name = HSApplication.getContext().getString(R.string.silent_ringtone);
            info.type = RingtoneInfo.TYPE_SYSTEM;
            info.uri = uri;
            return info;
        }
        final Ringtone tone = RingtoneManager.getRingtone(HSApplication.getContext(), Uri.parse(uri));
        String name;
        if (tone != null) {
            name = tone.getTitle(HSApplication.getContext());
        } else {
            name = "";
        }
        RingtoneInfo info = new RingtoneInfo();
        info.name = name;
        info.type = RingtoneInfo.TYPE_SYSTEM;
        info.uri = uri;
        return info;
    }

    public static RingtoneInfo getRingtoneDefault() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return getSystemRingtoneInfo(uri.toString());
    }

    public static RingtoneInfo getConversationRingtoneInfo(String uriStr) {
        if (uriStr.equals(RingtoneInfoManager.SILENT_URI)) {
            RingtoneInfo info = new RingtoneInfo();
            info.uri = RingtoneInfoManager.SILENT_URI;
            info.name = HSApplication.getContext().getString(R.string.silent_ringtone);
            info.type = RingtoneInfo.TYPE_SYSTEM;
            return info;
        }

        String[] items = uriStr.split("\\|"); // file name is put in database with uri
        if (items.length > 1 && items[1].length() > 0) {
            RingtoneInfo info = new RingtoneInfo();
            info.uri = items[1];
            info.name = items[0];
            info.type = RingtoneInfo.TYPE_FILE;
            return info;
        }

        Uri uri = Uri.parse(uriStr);
        int type = RingtoneInfo.TYPE_FILE;
        String name = "";

        if (UriUtil.isBugleAppResource(uri)) {
            type = RingtoneInfo.TYPE_APP;
            for (RingtoneInfo item : getAppRingtoneInfoFromConfig()) {
                if (uriStr.equals(item.uri)) {
                    name = item.name;
                    break;
                }
            }
        } else if (UriUtil.isMediaStoreUri(uri)) {
            return getSystemRingtoneInfo(uriStr);
        }
        RingtoneInfo info = new RingtoneInfo();
        info.uri = uriStr;
        info.name = name;
        info.type = type;
        return info;
    }

    public static List<RingtoneInfo> getAppRingtoneInfoFromConfig() {
        // TODO: 2019-08-20 fix it
        List<String> list = (List<String>) HSConfig.getList("Application", "Ringtone", "RingtoneList");
        String packageName = HSApplication.getContext().getPackageName();
        Context context = HSApplication.getContext();
        List<RingtoneInfo> ansList = new ArrayList<>();
        for (String item : list) {
            RingtoneInfo info = new RingtoneInfo();
            info.name = item;
            info.uri = Uri.parse("android.resource://" + packageName + "/" +
                    context.getResources().getIdentifier("ringtone_" + item, "raw", packageName)).toString();
            info.type = RingtoneInfo.TYPE_APP;

            ansList.add(info);
        }
        return ansList;
    }


    /**
     * Return a ringtone Uri for the string representation passed in. Use the app
     * and system defaults as fallbacks
     *
     * @param ringtoneString is the ringtone to resolve
     * @return the Uri of the ringtone or the fallback ringtone
     */
    public static Uri getNotificationRingtoneUri(String ringtoneString) {

        if (ringtoneString == null) {
            // No override specified, fall back to system-wide setting.
            ringtoneString = RingtoneInfoManager.getCurSound().uri;
            if (!TextUtils.isEmpty(ringtoneString)) {
                // We have set a value, even if it is the default Uri at some point
                return Uri.parse(ringtoneString);
            } else if (ringtoneString == null) {
                // We have no setting specified (== null), so we default to the system default
                return Settings.System.DEFAULT_NOTIFICATION_URI;
            } else {
                // An empty string (== "") here is the result of selecting "None" as the ringtone
                return null;
            }
        }

        String[] items = ringtoneString.split("\\|");   // ringtone name is put into database with uri
        if (items.length > 1 && items[1].length() > 0) {
            ringtoneString = items[1];
        }

        if (!TextUtils.isEmpty(ringtoneString)) {
            // We have set a value, even if it is the default Uri at some point
            return Uri.parse(ringtoneString);
        } else if (ringtoneString == null) {
            // We have no setting specified (== null), so we default to the system default
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        } else {
            // An empty string (== "") here is the result of selecting "None" as the ringtone
            return null;
        }
    }

    public static void upgrade(){
        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
        String prefKey = HSApplication.getContext().getString(R.string.notification_sound_pref_key);
        String ringtoneString = prefs.getString(prefKey, null);
        if (ringtoneString != null) {
            RingtoneInfo info = getSystemRingtoneInfo(ringtoneString);
            setCurSound(info);
        }
    }
}
