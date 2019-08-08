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
import com.android.messaging.util.PendingIntentConstants;
import com.android.messaging.util.SetDefaultPushAutopilotUtils;
import com.superapps.util.Notifications;

public class SetDefaultNotification {

    private Context mContext;
    public static final String TYPE = "set_default";

    public SetDefaultNotification(Context context) {
        mContext = context;
    }

    private RemoteViews createRemoteView() {
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.notification_set_default_push);
        Resources resources = mContext.getResources();
        view.setInt(R.id.container, "setBackgroundResource", R.drawable.notification_set_default_bg);

        SpannableString sp = new SpannableString(resources.getString(R.string.set_default_push_title));
//        TypefaceSpan typefaceSpan = new TypefaceSpan(Typeface.createFromAsset(mContext.getAssets(), "fonts/Custom-Bold.ttf"));
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setTextViewText(R.id.title, sp);

        view.setTextViewText(R.id.description, resources.getString(R.string.set_default_push_description));
        return view;
    }

    public boolean getEnablePush() {
        return SetDefaultPushAutopilotUtils.getEnablePush();
    }

    public void sendNotification() {
        SetDefaultPushAutopilotUtils.logPushSetDefaultShow();

        NotificationCompat.Builder builder;
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = BugleNotificationChannelUtil.getSetDefaultNotificationChannel();
            builder = new NotificationCompat.Builder(mContext, notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(mContext);
        }

        Intent i = new Intent(mContext, WelcomeSetAsDefaultActivity.class);
        i.putExtra(WelcomeSetAsDefaultActivity.EXTRA_FROM_PUSH_START, true);
        i.putExtra(ActiveNotification.EXTRA_CUR_TYPE, SetDefaultNotification.TYPE);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);

        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(mContext.getResources().getString(R.string.set_default_push_title));
        builder.setContentText(mContext.getResources().getString(R.string.set_default_push_description));
        builder.setOnlyAlertOnce(true);
        builder.setContent(createRemoteView());
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);
        Notification notification = builder.build();

        Notifications.notifySafely(PendingIntentConstants.SMS_NOTIFICATION_ID_SET_DEFAULT, notification, notificationChannel);
    }
}
