package com.android.messaging.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;

import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;

public class ShortcutUtils {

    public static final String SAMSUNG_PACKAGE_NAME = "com.samsung.android.messaging";

    public static void addShortCut(Context context) {
        Drawable smsIcon;
        try {
            smsIcon = context.getPackageManager().getApplicationIcon(SAMSUNG_PACKAGE_NAME);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (smsIcon != null && ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Intent shortcutInfoIntent = new Intent(context, ConversationListActivity.class);
//            shortcutInfoIntent.putExtra(ConversationListActivity.EXTRA_FROM_DESKTOP_ICON, true);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW);

            IconCompat iconCompat = IconCompat.createWithBitmap(
                    DisplayUtils.drawable2Bitmap(smsIcon));
            ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(context, context.getResources().getString(R.string.app_name))
                    .setIcon(iconCompat)
                    .setShortLabel(context.getResources().getString(R.string.app_name))
                    .setIntent(shortcutInfoIntent)
                    .build();

            PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0
                    , new Intent(context, ShortcutReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            ShortcutManagerCompat.requestPinShortcut(context, info, shortcutCallbackIntent.getIntentSender());
        }
    }

}
