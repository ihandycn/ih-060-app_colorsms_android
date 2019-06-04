package com.android.messaging.ui.invitefriends;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.text.format.DateUtils;

import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Preferences;

import static com.android.messaging.ui.invitefriends.InviteFriendsRewardDialogActivity.INTENT_KEY_OCCASION;
import static com.android.messaging.ui.invitefriends.InviteFriendsRewardDialogActivity.INTENT_KEY_TIME;

public class InviteFriendsConditions {
    public static final String SHOW_INVITE_FRIENDS_DIALOG_AFTER_CHANGE_THEME_10_SECS = "SHOW_INVITE_FRIENDS_DIALOG_AFTER_CHANGE_THEME_10_SECS";

    private static final String INVITE_FRIENDS_DIALOG_SHOW_COUNT = "INVITE_FRIENDS_DIALOG_SHOW_COUNT";
    private static final String PREF_KEY_LAST_INVITE_FRIENDS_DIALOG_SHOW_TIME = "PREF_KEY_LAST_INVITE_FRIENDS_DIALOG_SHOW_TIME";

    private static long sMainPageCreateTime = 0L;

    public static final int BACK_TO_MAIN_PAGE = 0;
    public static final int CHANGE_THEME = 1;
    public static final int SEND_SMS = 2;


    @IntDef({BACK_TO_MAIN_PAGE, CHANGE_THEME, SEND_SMS})
    @interface InviteFriendsTiming {
    }

    public static boolean showInviteFriendsDialogIfProper(Activity activity, @InviteFriendsTiming int timing) {
        // version code control

        Preferences preferences = Preferences.get(BuglePrefs.SHARED_PREFERENCES_NAME);
        if (System.currentTimeMillis() - preferences.getLong(PREF_KEY_LAST_INVITE_FRIENDS_DIALOG_SHOW_TIME, -1L)
                <= 12 * DateUtils.HOUR_IN_MILLIS) {
            return false;
        }
        String type = "";
        boolean isTimingValid = true;
        switch (timing) {
            case BACK_TO_MAIN_PAGE:
                isTimingValid = System.currentTimeMillis() - sMainPageCreateTime > 3 * DateUtils.MINUTE_IN_MILLIS;
                type = "BackFromDetailPage";
                break;
            case CHANGE_THEME:
                type = "ChangeTheme";
                break;
            case SEND_SMS:
                type = "SendSMS";
                break;
        }

        if (isTimingValid) {
            int showedCount = preferences.getInt(INVITE_FRIENDS_DIALOG_SHOW_COUNT, 0);
            if (showedCount < 2) {
                showedCount++;
                Intent intent = new Intent(activity, InviteFriendsRewardDialogActivity.class);
                intent.putExtra(INTENT_KEY_TIME, String.valueOf(showedCount));
                intent.putExtra(INTENT_KEY_OCCASION, type);
                activity.startActivity(intent);
                BugleAnalytics.logEvent("Invite_GuideAlert_Show", true, "time", String.valueOf(showedCount),
                        "occasion", type);
                preferences.putInt(INVITE_FRIENDS_DIALOG_SHOW_COUNT, showedCount);
            }
        }

        return false;

    }

    public static long getMainPageCreateTime() {
        return sMainPageCreateTime;
    }

    public static void setMainPageCreateTime(long mainPageCreateTime) {
        sMainPageCreateTime = mainPageCreateTime;
    }
}
