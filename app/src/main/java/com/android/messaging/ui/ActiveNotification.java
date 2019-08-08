package com.android.messaging.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;

import com.android.messaging.R;
import com.android.messaging.datamodel.media.BugleNotificationChannelUtil;
import com.android.messaging.ui.welcome.WelcomeSetAsDefaultActivity;
import com.android.messaging.util.ActivePushAutopilotUtils;
import com.android.messaging.util.PendingIntentConstants;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Notifications;
import com.superapps.util.Preferences;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class ActiveNotification {

    private final int PUSH_TYPE_COUNT = 3;
    private final String PREF_SET_DEFAULT_CUR_TYPE = "pref_set_default_cur_type";
    public static final String EXTRA_CUR_TYPE = "cur_type";

    public static final String TYPE_THEME = "theme";
    public static final String TYPE_EMOJIS = "emojis";
    public static final String TYPE_SMS = "send_message";

    private String mCurType;
    private Context mContext;

    private NotificationCompat.Builder mBuilder;

    public ActiveNotification(Context context) {
        this.mContext = context;
    }

    private String getPushType() {
        return ActivePushAutopilotUtils.getPushDescription();
    }

    public boolean getEnablePush() {
        return ActivePushAutopilotUtils.getEnablePush();
    }

    private RemoteViews getViewTypeTheme() {
        RemoteViews view = new RemoteViews(HSApplication.getContext().getPackageName(), R.layout.notification_set_default_push);
        Resources resources = mContext.getResources();
        view.setInt(R.id.container, "setBackgroundResource", R.drawable.notification_active_push_theme);

        SpannableString sp = new SpannableString(resources.getString(R.string.active_push_title_theme));
//        TypefaceSpan typefaceSpan = new TypefaceSpan(Typeface.createFromAsset(mContext.getAssets(), "fonts/Custom-Bold.ttf"));
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setTextViewText(R.id.title, sp);

        view.setTextViewText(R.id.description, resources.getString(R.string.active_push_description_theme));

        mBuilder.setContentTitle(resources.getString(R.string.active_push_title_theme));
        mBuilder.setContentText(resources.getString(R.string.active_push_description_theme));
        mCurType = TYPE_THEME;
        return view;
    }

    private RemoteViews getViewTypeEmojis() {
        RemoteViews view = new RemoteViews(HSApplication.getContext().getPackageName(), R.layout.notification_set_default_push_text_shadow);
        Resources resources = mContext.getResources();
        view.setInt(R.id.container, "setBackgroundResource", R.drawable.notification_active_push_emojis);

        SpannableString sp = new SpannableString(resources.getString(R.string.active_push_title_emojis));
//        TypefaceSpan typefaceSpan = new TypefaceSpan(Typeface.createFromAsset(mContext.getAssets(), "fonts/Custom-Bold.ttf"));
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setTextViewText(R.id.title, sp);

        view.setTextViewTextSize(R.id.title, COMPLEX_UNIT_SP, 20f);
        view.setTextViewText(R.id.description, resources.getString(R.string.active_push_description_emojis));

        mBuilder.setContentTitle(resources.getString(R.string.active_push_title_theme));
        mBuilder.setContentText(resources.getString(R.string.active_push_description_theme));
        mCurType = TYPE_EMOJIS;
        return view;
    }

    private RemoteViews getViewTypeSendMessage() {
        RemoteViews view = new RemoteViews(HSApplication.getContext().getPackageName(), R.layout.notification_set_default_push);
        Resources resources = mContext.getResources();
        view.setInt(R.id.container, "setBackgroundResource", R.drawable.notification_active_push_sms);

        SpannableString sp = new SpannableString(resources.getString(R.string.active_push_title_send_message));
//        TypefaceSpan typefaceSpan = new TypefaceSpan(Typeface.createFromAsset(mContext.getAssets(), "fonts/Custom-Bold.ttf"));
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setTextViewText(R.id.title, sp);

        view.setTextViewText(R.id.description, resources.getString(R.string.active_push_description_send_message));
        view.setTextColor(R.id.description, 0xe6ffffff);

        mBuilder.setContentTitle(resources.getString(R.string.active_push_title_theme));
        mBuilder.setContentText(resources.getString(R.string.active_push_description_theme));
        mCurType = TYPE_SMS;
        return view;
    }

    private RemoteViews createRemoteView() {
        RemoteViews view;

        String pushType = getPushType();
        switch (pushType) {
            case TYPE_THEME:
                Preferences.getDefault().putInt(PREF_SET_DEFAULT_CUR_TYPE, 0);
                view = getViewTypeTheme();
                break;
            case TYPE_EMOJIS:
                Preferences.getDefault().putInt(PREF_SET_DEFAULT_CUR_TYPE, 1);
                view = getViewTypeEmojis();
                break;
            case TYPE_SMS:
                Preferences.getDefault().putInt(PREF_SET_DEFAULT_CUR_TYPE, 2);
                view = getViewTypeSendMessage();
                break;
            default:
                int lastType = Preferences.getDefault().getInt(PREF_SET_DEFAULT_CUR_TYPE, 0);
                int curType = (lastType + 1) % PUSH_TYPE_COUNT;
                switch (curType) {
                    case 0:
                        view = getViewTypeTheme();
                        break;
                    case 1:
                        view = getViewTypeEmojis();
                        break;
                    case 2:
                        view = getViewTypeSendMessage();
                        break;
                    default:
                        view = getViewTypeTheme();
                        break;
                }
                Preferences.getDefault().putInt(PREF_SET_DEFAULT_CUR_TYPE, curType);
                break;
        }
        return view;
    }

    public void sendNotification() {
        ActivePushAutopilotUtils.logPushShow();

        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = BugleNotificationChannelUtil.getSetDefaultNotificationChannel();
            mBuilder = new NotificationCompat.Builder(mContext, notificationChannel.getId());
        } else {
            mBuilder = new NotificationCompat.Builder(mContext);
        }

        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContent(createRemoteView());
        mBuilder.setOnlyAlertOnce(true);

        Intent i = new Intent(mContext, WelcomeSetAsDefaultActivity.class);
        i.putExtra(WelcomeSetAsDefaultActivity.EXTRA_FROM_PUSH_START, true);
        if (mCurType != null) {
            i.putExtra(WelcomeSetAsDefaultActivity.EXTRA_FROM_PUSH_TYPE, mCurType);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);

        Notification notification = mBuilder.build();
        Notifications.notifySafely(PendingIntentConstants.SMS_NOTIFICATION_ID_ACTIVE_PUSH, notification, notificationChannel);
    }
}
