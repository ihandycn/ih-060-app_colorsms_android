package com.android.messaging.notificationcleaner;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuglePackageManager {

    private PackageManager mPackageManager;

    private final Object mLock = new Object();
    private List<ApplicationInfo> installedApplications = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Drawable> installedAppIcons = new HashMap<>();
    private Map<String, String> installedAppLabels = new HashMap<>();
    private List<String> installedPackageNames = Collections.synchronizedList(new ArrayList<>());
    private List<String> suggestedPackageNames = new ArrayList<>();
    private List<AppInfo> installedAppInfos = Collections.synchronizedList(new ArrayList<>());


    public static BuglePackageManager getInstance() {
        return BuglePackageManagerHolder.sInstance;
    }

    private static class BuglePackageManagerHolder {
        private static final BuglePackageManager sInstance = new BuglePackageManager();
    }

    private BuglePackageManager() {
        mPackageManager = HSApplication.getContext().getPackageManager();
    }

    public List<ApplicationInfo> getInstalledApplications() {
        return Collections.unmodifiableList(new ArrayList<>(installedApplications));
    }

    public Drawable getApplicationIcon(String packageName) {
        if (NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_1.equals(packageName)) {
            return HSApplication.getContext().getResources().getDrawable(R.drawable.notification_cleaner_fake_notification_icon_1);
        }
        if (NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_2.equals(packageName)) {
            return HSApplication.getContext().getResources().getDrawable(R.drawable.notification_cleaner_fake_notification_icon_2);
        }

        Drawable icon;
        synchronized (mLock) {
            icon = installedAppIcons.get(packageName);
        }
        if (null == icon) {
            try {
                icon = mPackageManager.getApplicationIcon(packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (icon != null) {
                synchronized (mLock) {
                    installedAppIcons.put(packageName, icon);
                }
            }
        }
        return icon;
    }

    public Drawable getApplicationIcon(ApplicationInfo application) {
        Drawable icon;
        synchronized (mLock) {
            icon = installedAppIcons.get(application.packageName);
        }
        if (icon == null) {
            icon = getAppIconFromSys(application);
            if (icon != null) {
                synchronized (mLock) {
                    installedAppIcons.put(application.packageName, icon);
                }
            }
        }
        return icon;
    }

    public String getApplicationLabel(ApplicationInfo application) {
        if (null == application) {
            return null;
        }
        synchronized (mLock) {
            String label = installedAppLabels.get(application.packageName);
            if (TextUtils.isEmpty(label)) {
                installedAppLabels.put(application.packageName,
                        label = mPackageManager.getApplicationLabel(application).toString());
            }
            return label;
        }
    }

    public String getApplicationLabel(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return "";
        }

        synchronized (mLock) {
            String label = installedAppLabels.get(packageName);
            if (TextUtils.isEmpty(label)) {
                try {
                    ApplicationInfo application = getApplicationInfo(packageName);
                    label = mPackageManager.getApplicationLabel(application).toString();
                    if (!TextUtils.isEmpty(label)) {
                        installedAppLabels.put(application.packageName, label);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return label;
        }
    }

    public void updateInstalledApplications() {
        Threads.postOnThreadPoolExecutor(() -> updateInstalledApplicationsOnWorkerThread());
    }

    public void updateInstalledApplicationsOnWorkerThread() {
        // get all application infos from system
        List<ApplicationInfo> applicationInfos = null;
        try {
            applicationInfos = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == applicationInfos) {
            return;
        }

        // cache information
        synchronized (mLock) {
            installedApplications.clear();
            installedAppInfos.clear();
            installedPackageNames.clear();

            for (ApplicationInfo app : applicationInfos) {
                if (app == null || TextUtils.isEmpty(app.packageName) || !filter(app)) {
                    continue;
                }

                // cache application info
                installedApplications.add(app);

                // cache package name
                if (!TextUtils.isEmpty(app.packageName)) {
                    installedPackageNames.add(app.packageName);
                }

                // cache app info
                AppInfo appInfo = new AppInfo();
                appInfo.packageName = app.packageName;
                appInfo.appIcon = getAppIconFromSys(app);
                installedAppInfos.add(appInfo);

                // cache app icons and labels
                Drawable icon = installedAppIcons.get(app.packageName);
                if (icon == null) {
                    installedAppIcons.put(app.packageName, getAppIconFromSys(app));
                }
                String label = installedAppLabels.get(app.packageName);
                if (TextUtils.isEmpty(label)) {
                    try {
                        installedAppLabels.put(app.packageName, mPackageManager.getApplicationLabel(app).toString());
                    } catch (Exception e) {
                    }
                }
            }
        }

        Threads.postOnMainThread(() -> {
            getSuggestLockPackageNames();
           // HSGlobalNotificationCenter.sendNotification(EVENT_PACKAGE_INFO_REFRESH_COMPLETE);
        });
    }

    public List<String> getSuggestLockPackageNames() {
        if (suggestedPackageNames.size() > 0) {
            return suggestedPackageNames;
        }

        StringBuilder builder = new StringBuilder();
        // init from plist
        List<Map<String, Object>> whiteList = (List<Map<String, Object>>) HSConfig.getList("Application", "AppLock", "AppLockWhiteList");
        for (Map<String, Object> appClass : whiteList) {
            List<String> apps = (List<String>) appClass.get("Apps");
            synchronized (mLock) {
                for (String packageName : apps) {
                    if (installedPackageNames.contains(packageName)) {
                        suggestedPackageNames.add(packageName);
                        builder.append(packageName).append(",");
                    }
                }
            }
            builder.append(";");
        }
        return suggestedPackageNames;
    }

    public List<String> getSuggestedLockPackageNamesAll() {
        List<String> packageNames = new ArrayList<>();
        List<Map<String, Object>> whiteList = (List<Map<String, Object>>) HSConfig.getList("Application", "AppLock", "AppLockWhiteList");
        for (Map<String, Object> appClass : whiteList) {
            List<String> apps = (List<String>) appClass.get("Apps");
            synchronized (mLock) {
                for (String packageName : apps) {
                    packageNames.add(packageName);
                }
            }
        }
        return packageNames;
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        ApplicationInfo applicationInfo = null;
        synchronized (mLock) {
            for (ApplicationInfo app : installedApplications) {
                if (app.packageName.equals(packageName)) {
                    applicationInfo = app;
                }
            }
        }
        if (null == applicationInfo) {
            try {
                applicationInfo = mPackageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return applicationInfo;
    }

    private Drawable getAppIconFromSys(ApplicationInfo app) {
        Drawable icon = null;
        try {
            icon = mPackageManager.getApplicationIcon(app.packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (null == icon) {
                icon = app.loadIcon(mPackageManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return icon;
    }

    private boolean filter(ApplicationInfo applicationInfo) {
        String packageName = applicationInfo.packageName;
        try {
            // Rule out packages not listed in user's launcher
            if (mPackageManager.getLaunchIntentForPackage(packageName) == null) {
                return false;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        // exclude self
        if (HSApplication.getContext().getPackageName().equals(applicationInfo.packageName)) {
            return false;
        }
        return true;
    }
}
