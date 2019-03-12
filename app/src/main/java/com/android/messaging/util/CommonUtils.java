package com.android.messaging.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.messaging.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Compats;
import com.superapps.util.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    public static final String INTENT_KEY_SNAP_TO_PAGE = "snap.to.page";
    public static final String INTENT_KEY_SHOW_ALL_APPS = "show.all.apps";
    public static final String INTENT_KEY_SHOW_SEARCH_LAYOUT = "show.search.layout";
    public static final String INTENT_KEY_SHOW_SET_DEFAULT_SOURCE = "show.set.default.source";
    public static final String INTENT_KEY_WALLPAPER_SELECTED = "INTENT_KEY_WALLPAPER_SELECTED";
    public static final String INTENT_KEY_RESET_LAUNCHER_VIEW = "reset.launcher.view";

    public static final String INTENT_KEY_APPLY_THEME_OR_WALLPAPER = "apply_theme_or_wallpaper";
    public static final String INTENT_KEY_WALLPAPER_SCROLLABLE = "intent_key_wallpaper_scrollable";
    public static final int WALLPAPER_NONE_SCROLLABLE = -1;
    public static final int WALLPAPER_SCROLLABLE_TRUE = 0;
    public static final int WALLPAPER_SCROLLABLE_FALSE = 1;
    public static final int INTENT_VALUE_APPLY_THEME = 1;
    public static final int INTENT_VALUE_APPLY_WALLPAPER = 2;
    public static final String INTENT_KEY_APPLIED_THEME_PACKAGE = "applied_theme_package";


    public static final int DEFAULT_DEVICE_SCREEN_HEIGHT = 1920;
    public static final int DEFAULT_DEVICE_SCREEN_WIDTH = 1080;
    private static int DEVICE_SCREEN_WIDTH = -1;
    private static int DEVICE_SCREEN_HEIGHT = -1;

    public static final boolean UNDER_JB_MAR2 = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static final boolean ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_JB_MR2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static final boolean ATLEAST_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static final boolean ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static final boolean ATLEAST_LOLLIPOP_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final boolean ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean ATLEAST_N = Build.VERSION.SDK_INT >= 24;

    public static final boolean ATLEAST_O = Build.VERSION.SDK_INT >= 26;

    // Cache variables
    private static Boolean sMainProcess;

    public static boolean isMainProcess(Context context) {
        if (sMainProcess == null) {
            sMainProcess = TextUtils.equals(context.getPackageName(), HSApplication.getProcessName());
        }
        return sMainProcess;
    }

    private static List<ActivityManager.RunningAppProcessInfo> getRunningProcesses(ActivityManager am) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            runningAppProcesses = new ArrayList<>(0);
        }
        return runningAppProcesses;
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isFloatWindowAllowedBelowNavigationBar() {
        // We don't know the exact conditions. This is what we've found now.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && Compats.IS_SONY_DEVICE) {
            return false;
        }
        return true;
    }

    /**
     * Sets up transparent navigation and status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentSystemBarsForLmp(Activity activityContext) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                    .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * Sets up transparent status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentStatusBarsForLmp(Activity activityContext) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * Retrieve, creating if needed, a new directory of given name in which we
     * can place our own custom data files.
     */
    public static @Nullable File getDirectory(String dirPath) {
        File file = HSApplication.getContext().getFilesDir();
        String[] path = dirPath.split(File.separator);
        for (String dir : path) {
            file = new File(file, dir);
            if (!file.exists() && !file.mkdir()) {
                HSLog.w(TAG, "Error making directory");
                return null;
            }
        }
        return file;
    }

    public static Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) return (Activity) context;
        if (context instanceof ContextWrapper)
            return getActivity(((ContextWrapper) context).getBaseContext());
        return null;
    }

    private static boolean startActivitySafely(Context context, Intent intent) {
        try {
            addNewTaskFlagIfNeeded(context, intent);
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException | NullPointerException ignored) {
            return false;
        }
        return true;
    }

    private static void addNewTaskFlagIfNeeded(Context context, Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    public static boolean unregisterReceiver(Context context, BroadcastReceiver receiver) {
        if (receiver == null) {
            return true;
        }
        try {
            context.unregisterReceiver(receiver);
            return true;
        } catch (Exception e) {
            HSLog.e(TAG, "Error unregistering broadcast receiver: " + receiver + " at ");
            e.printStackTrace();
            return false;
        }
    }

    public static Uri resourceToUri(Context context, int resID) {
        Resources res = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                res.getResourcePackageName(resID) + '/' +
                res.getResourceTypeName(resID) + '/' +
                res.getResourceEntryName(resID));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void immersiveStatusAndNavigationBar(Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * @return true: the screen is bright;
     * false: the screen is dark.
     */
    public static boolean isInteractive() {
        PowerManager pm = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isScreenOn();
    }

    public static float getTextWidth(String text, TextPaint textPaint) {
        return textPaint.measureText(text);
    }

    /**
     * 用于跳转到 google play 下载 主题、locker、蜘蛛纸牌等被推广 app 的界面
     *
     * @param feature              字符串不包含特殊字符，例如 GuideView
     * @param viewType             字符串不包含特殊字符串，例如 ButtonOK，ButtonDownload
     * @param targetAppPackageName 要跳转的 locker 的包名
     */
    public static void directToMarket(String feature, String viewType, String targetAppPackageName) {
        Map<String, String> paras = new HashMap<>();
        StringBuilder parametersStr = new StringBuilder();
        parametersStr.append("packageName=" + BuildConfig.APPLICATION_ID);
        paras.put("TargetPackageName", targetAppPackageName);
        if (!TextUtils.isEmpty(feature)) {
            parametersStr.append("&feature=" + feature);
            paras.put("feature", feature);
        }
        if (!TextUtils.isEmpty(viewType)) {
            parametersStr.append("&viewType=" + viewType);
            paras.put("viewType", viewType);
        }
        parametersStr.append("&versionName=" + BuildConfig.VERSION_NAME);
        parametersStr.append("&internal=" + BuildConfig.APPLICATION_ID);

//        LauncherAnalytics.logEvent("DirectToGooglePlay", paras);

        String appPackageName = targetAppPackageName;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            intent.setData(Uri.parse("market://details?id=" + appPackageName + "&referrer=" + Uri.encode(parametersStr.toString())));
            HSApplication.getContext().startActivity(intent);

            HSLog.d(TAG + ">>>market  " + intent.getDataString());
        } catch (Exception e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName + "&referrer=" + Uri.encode(parametersStr.toString())));
            HSApplication.getContext().startActivity(intent);

            HSLog.d(TAG + ">>>web  " + intent.getDataString());
        }
    }

    /**
     * Get epoch time when user installed us. You should always call this method rather than reading
     * LauncherConstants.PREF_KEY_INSTALLED_TIME yourself to get better estimation for upgraded users.
     */
    private static long sInstallTime;
    private static final String PREF_KEY_INSTALLED_TIME = "pref_key_installed_time";

    public static long getAppInstallTimeMillis() {
        if (sInstallTime <= 0) {
            Preferences prefs = Preferences.getDefault();
            if ((sInstallTime = prefs.getLong(PREF_KEY_INSTALLED_TIME, 0)) == 0) {
                sInstallTime = System.currentTimeMillis();
                prefs.putLong(PREF_KEY_INSTALLED_TIME, sInstallTime);
            }
        }
        return sInstallTime;
    }

    public static boolean isNewUser() {
        return HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }
}
