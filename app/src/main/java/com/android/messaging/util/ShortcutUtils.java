package com.android.messaging.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;

import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Bitmaps;

import java.util.ArrayList;
import java.util.List;

public class ShortcutUtils {

    public static Drawable sIcon;
    public static Drawable sIconCombined;
    public static List<String> sPackages = new ArrayList<>();

    static {
        sPackages.add("com.samsung.android.messaging");
        sPackages.add("com.android.mms");
        sPackages.add("com.google.android.apps.messaging");
        sPackages.add("com.motorola.messaging");
        sPackages.add("com.sonyericsson.conversations");
        sPackages.add("com.htc.sense.mms");
        sPackages.add("com.asus.message");
        sPackages.add("com.lge.message");
    }

    public static void addShortCut(Context context) {
        if (getSystemSMSIcon() != null) {
            Intent shortcutInfoIntent = new Intent(context, ConversationListActivity.class);
            shortcutInfoIntent.putExtra(ConversationListActivity.EXTRA_FROM_DESKTOP_ICON, true);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW);

            IconCompat iconCompat = IconCompat.createWithBitmap(
                    DisplayUtils.drawable2Bitmap(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? sIcon : sIconCombined));
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

    public static Drawable getSystemSMSIcon() {
        if (sIcon != null) {
            return sIcon;
        }

        int i = 0;
        while (sIcon == null && i < sPackages.size()) {
            try {
                sIcon = HSApplication.getContext().getPackageManager().getApplicationIcon(sPackages.get(i));
                Bitmap icon = Bitmaps.drawable2Bitmap(sIcon);
                Bitmap badge = BitmapFactory.decodeResource(HSApplication.getContext().getResources(),
                        R.drawable.create_shortcut_badge);
                Bitmap combined = Bitmap.createBitmap(icon.getWidth() + 8, icon.getHeight() + 8, Bitmap.Config.ARGB_8888);
                Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
                Canvas canvas = new Canvas(combined);
                canvas.drawBitmap(icon, 4, 4, bitmapPaint);
                canvas.drawBitmap(badge, combined.getWidth() - badge.getWidth(), combined.getHeight() - badge.getHeight(), bitmapPaint);
                sIconCombined = new BitmapDrawable(HSApplication.getContext().getResources(), combined);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            i++;
        }
        return sIcon;
    }
}
