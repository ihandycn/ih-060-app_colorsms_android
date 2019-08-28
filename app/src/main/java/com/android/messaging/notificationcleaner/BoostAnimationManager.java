package com.android.messaging.notificationcleaner;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.Bitmaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoostAnimationManager {
    public static final int COUNT_ICON = 7;

    private List<String> drawablePackageList = new ArrayList<>();
    private List<String> boostDrawablePackageList = new ArrayList<>();

    public BoostAnimationManager() {
    }

    public Bitmap[] getBoostAppIconBitmaps(Context context) {
        Drawable[] drawables = getBoostAppIconDrawables(context);
        Bitmap[] bitmaps = new Bitmap[COUNT_ICON];
        for (int i = 0; i < drawables.length; i++) {
            bitmaps[i] = drawableToBitmap(drawables[i]);
        }
        return bitmaps;
    }

    public @NonNull Drawable[] getBoostAppIconDrawables(Context context) {
        drawablePackageList.clear();
        boostDrawablePackageList.clear();
        Drawable[] drawables = new Drawable[COUNT_ICON];
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return getRandomAppIcon(drawables, 0);
        }

        int i = 0;
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : runningAppProcesses) {
            if (null != appProcessInfo) {
                String processName = appProcessInfo.processName;
                if (!TextUtils.isEmpty(processName)) {
                    String packageName = processName.split(":")[0].trim();
                    if (packageName.equals(HSApplication.getContext().getPackageName())) {
                        continue;
                    }
                    String securityPackageName = context.getPackageName();
                    boolean isSystemApp = isSystemApp(context, packageName);
                    boolean isLaunchAbleApp = isLaunchAbleApp(context, packageName);
                    boolean isSelf = false;
                    boolean isDuplicate = drawablePackageList.contains(packageName);
                    if (!TextUtils.isEmpty(securityPackageName)) {
                        isSelf = securityPackageName.equals(packageName);
                    }

                    if (!TextUtils.isEmpty(packageName) && !isSystemApp && isLaunchAbleApp && !isSelf && !isDuplicate) {
                        if (i >= drawables.length) {
                            break;
                        }
                        Drawable currentDrawable = BuglePackageManager.getInstance().getApplicationIcon(packageName);
                        drawables[i] = currentDrawable;
                        drawablePackageList.add(packageName);
                        boostDrawablePackageList.add(packageName);
                        i++;
                    }
                }
            }
        }

        if (i < drawables.length) {
            return getRandomAppIcon(drawables, i);
        }
        return drawables;
    }

    public static boolean isLaunchAbleApp(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        boolean result = null != context.getPackageManager().resolveActivity(intent, 0);
        return result;
    }

    private @NonNull
    Drawable[] getRandomAppIcon(Drawable[] drawables, int currentIndex) {
        if (null == drawables) {
            return new Drawable[0];
        }

        List<ApplicationInfo> list = BuglePackageManager.getInstance().getInstalledApplications();
        List<String> applicationInfoList = new ArrayList<>();
        for (ApplicationInfo info: list) {
            applicationInfoList.add(info.packageName);
        }
        if (null == applicationInfoList || applicationInfoList.size() == 0) {
            return new Drawable[0];
        }

        List<String> apps = new ArrayList<>();
        if (drawablePackageList.size() > 0) {
            for (String applicationInfo : applicationInfoList) {
                if (null != applicationInfo && !drawablePackageList.contains(applicationInfo)) {
                    apps.add(applicationInfo);
                }
            }
        } else {
            apps.addAll(applicationInfoList);
        }

        int size = apps.size();
        if (apps.size() == 0 || size < drawables.length) {
            return new Drawable[0];
        }

        if (currentIndex >= drawables.length) {
            return new Drawable[0];
        }

        int[] randomIndex = getUniqueRandomInts(0, size, drawables.length - currentIndex);
        for (int i = currentIndex; i < drawables.length; i++) {
            if (null != randomIndex && (i - currentIndex) < randomIndex.length) {
                int index = randomIndex[i - currentIndex];
                String packageName = apps.get(index);
                drawables[i] = BuglePackageManager.getInstance().getApplicationIcon(apps.get(index));
                boostDrawablePackageList.add(packageName);
            }
        }
        return drawables;
    }

    private static boolean isSystemApp(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            return null != applicationInfo && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static @NonNull Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return Bitmaps.createFallbackBitmap();
        }
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmaps.createFallbackBitmap(); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int[] getUniqueRandomInts(int start, int end, int n) {
        if (n > end - start || end <= start) {
            return null;
        }
        List<Integer> numberList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            numberList.add(i);
        }
        Collections.shuffle(numberList);
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = numberList.get(i);
        }
        return result;
    }
}
