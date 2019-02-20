/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v4.os.TraceCompat;
import android.support.v7.mms.CarrierConfigValuesLoader;
import android.support.v7.mms.MmsManager;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;

import com.android.ex.photo.util.PhotoViewAnalytics;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.debug.BlockCanaryConfig;
import com.android.messaging.debug.UploadLeakService;
import com.android.messaging.receiver.SmsReceiver;
import com.android.messaging.sms.ApnDatabase;
import com.android.messaging.sms.BugleApnSettingsLoader;
import com.android.messaging.sms.BugleUserAgentInfoLoader;
import com.android.messaging.sms.MmsConfig;
import com.android.messaging.smsshow.MessagingMsgCenterFactoryImpl;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.SetAsDefaultGuideActivity;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.upgrader.Upgrader;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleGservices;
import com.android.messaging.util.BugleGservicesKeys;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.BuglePrefsKeys;
import com.android.messaging.util.BugleTimeTicker;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.DefaultSmsAppChangeObserver;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.Trace;
import com.crashlytics.android.Crashlytics;
import com.github.moduth.blockcanary.BlockCanary;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.permanent.HSPermanentUtils;
import com.ihs.device.permanent.PermanentService;
import com.messagecenter.customize.MessageCenterManager;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.debug.SharedPreferencesOptimizer;
import com.superapps.taskrunner.ParallelBackgroundTask;
import com.superapps.taskrunner.SyncMainThreadTask;
import com.superapps.taskrunner.Task;
import com.superapps.taskrunner.TaskRunner;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;
import static com.android.messaging.debug.DebugConfig.ENABLE_BLOCK_CANARY;
import static com.android.messaging.debug.DebugConfig.ENABLE_LEAK_CANARY;

/**
 * The application object
 */
public class BugleApplication extends HSApplication implements UncaughtExceptionHandler {
    private static final String TAG = LogUtil.BUGLE_TAG;

    private UncaughtExceptionHandler sSystemUncaughtExceptionHandler;
    private static boolean sRunningTests = false;
    private static final int KEEP_ALIVE_NOTIFICATION_ID = 20000;
    private static final int KEEP_ALIVE_NOTIFICATION_ID_OREO = 20001;

    @VisibleForTesting
    protected static void setTestsRunning() {
        sRunningTests = true;
    }

    /**
     * @return true if we're running unit tests.
     */
    public static boolean isRunningTests() {
        return sRunningTests;
    }

    @Override protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        Trace.beginSection("app.onCreate");
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
        initLeakCanaryAsync();
        SharedPreferencesOptimizer.install(true);
        String packageName = getPackageName();
        String processName = getProcessName();
        boolean isOnMainProcess = TextUtils.equals(processName, packageName);
        if (isOnMainProcess) {
            onMainProcessApplicationCreate();
        }

        initKeepAlive();

        sSystemUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        Trace.endSection();

        CommonUtils.getAppInstallTimeMillis();
    }

    private void onMainProcessApplicationCreate() {
        TraceCompat.beginSection("Application#onMainProcessApplicationCreate");
        try {
            List<Task> initWorks = new ArrayList<>();

            initWorks.add(new ParallelBackgroundTask("Upgrade", () -> Upgrader.getUpgrader(this).upgrade()));

            initWorks.add(new SyncMainThreadTask("InitFactoryImpl", this::initFactoryImpl));

            initWorks.add(new SyncMainThreadTask("InitPhotoViewAnalytics", this::initPhotoViewAnalytics));

            initWorks.add(new SyncMainThreadTask("InitEmojiConfig", () -> EmojiConfig.getInstance().doInit();

            initWorks.add(new SyncMainThreadTask("InitMessageCenter", this::initMessageCenterLib));

            initWorks.add(new SyncMainThreadTask("InitTimeTicker", () -> new BugleTimeTicker().start()));

            initWorks.add(new SyncMainThreadTask("InitObserverDefaultSmsChanged", this::initObserveDefaultSmsAppChanged));

            initWorks.add(new SyncMainThreadTask("InitObserveScreenStatusChanged", this::initObserveUserPresentChanged));

            TaskRunner.run(initWorks);
        } finally {
            TraceCompat.endSection();
        }
    }

    private void initFactoryImpl() {
        // Note onCreate is called in both test and real application environments
        if (!sRunningTests) {
            // Only create the factory if not running tests
            FactoryImpl.register(getApplicationContext(), this);
        } else {
            LogUtil.e(TAG, "BugleApplication.onCreate: FactoryImpl.register skipped for test run");
        }
    }

    private void initKeepAlive() {
        // Init keep alive arguments
        HSPermanentUtils.initKeepAlive(true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                null,
                new PermanentService.PermanentServiceListener() {
                    @Override
                    public Notification getForegroundNotification() {
                        return null;
                    }

                    @Override
                    public int getNotificationID() {
                        return KEEP_ALIVE_NOTIFICATION_ID;
                    }

                    @Override
                    public int getNotificationIDForOreo() {
                        return KEEP_ALIVE_NOTIFICATION_ID_OREO;
                    }

                    @Override
                    public void onServiceCreate() {

                    }
                });
        // Start permanent services
        Threads.postOnMainThreadDelayed(HSPermanentUtils::startKeepAlive, 10 * 1000);
    }

    private void initObserveDefaultSmsAppChanged() {
        Uri uri = Settings.Secure.getUriFor("sms_default_application");

        Context context = getApplicationContext();
        context.getContentResolver().registerContentObserver(uri, false, new DefaultSmsAppChangeObserver(null));
    }

    private void initObserveUserPresentChanged() {
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);

        final String KEY_FOR_LAST_USER_PRESENT_TIME = "last_user_present_time";
        final String KEY_FOR_TODAY_USER_PRESENT_COUNT = "today_user_present_count";
        BroadcastCenter.register(getApplicationContext(), (context, intent) -> {
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                return;
            }
            long lastUserPresent = Preferences.getDefault().getLong(KEY_FOR_LAST_USER_PRESENT_TIME, 0);
            long now = System.currentTimeMillis();
            if (Calendars.isSameDay(lastUserPresent, now)) {
                Preferences.getDefault().incrementAndGetInt(KEY_FOR_TODAY_USER_PRESENT_COUNT);
            } else {
                Preferences.getDefault().putInt(KEY_FOR_TODAY_USER_PRESENT_COUNT, 1);
                Preferences.getDefault().putLong(KEY_FOR_LAST_USER_PRESENT_TIME, now);
            }
            if (Preferences.getDefault().getInt(KEY_FOR_TODAY_USER_PRESENT_COUNT, 0) == 3) {
                Preferences.getDefault().doLimitedTimes(new Runnable() {
                    @Override
                    public void run() {
                        SetAsDefaultGuideActivity.startActivity(getApplicationContext(), SetAsDefaultGuideActivity.USER_PRESENT);
                    }
                }, "show_set_as_default_dialog_when_user_present", 3);
            }
        }, screenFilter);
    }

    private void initPhotoViewAnalytics() {
        PhotoViewAnalytics.initAnalytics(BugleAnalytics::logEvent);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Update conversation drawables when changing writing systems
        // (Right-To-Left / Left-To-Right)
        ConversationDrawables.get().updateDrawables();
    }

    // Called by the "real" factory from FactoryImpl.register() (i.e. not run in tests)
    public void initializeSync(final Factory factory) {
        Trace.beginSection("app.initializeSync");
        final Context context = factory.getApplicationContext();
        final BugleGservices bugleGservices = factory.getBugleGservices();
        final BuglePrefs buglePrefs = factory.getApplicationPrefs();
        final DataModel dataModel = factory.getDataModel();
        final CarrierConfigValuesLoader carrierConfigValuesLoader =
                factory.getCarrierConfigValuesLoader();

        maybeStartProfiling();

        // execute init works only after sms default set
        BugleApplication.updateAppConfig(context, true);

        // Initialize MMS lib
        initMmsLib(context, bugleGservices, carrierConfigValuesLoader);
        // Initialize APN database
        ApnDatabase.initializeAppContext(context);
        // Fixup messages in flight if we crashed and send any pending
        dataModel.onApplicationCreated();
        // Register carrier config change receiver
        if (OsUtil.isAtLeastM()) {
            registerCarrierConfigChangeReceiver(context);
        }

        Trace.endSection();
    }

    private static void registerCarrierConfigChangeReceiver(final Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.i(TAG, "Carrier config changed. Reloading MMS config.");
                MmsConfig.loadAsync();
            }
        }, new IntentFilter(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED));
    }

    private static void initMmsLib(final Context context, final BugleGservices bugleGservices,
                                   final CarrierConfigValuesLoader carrierConfigValuesLoader) {
        MmsManager.setApnSettingsLoader(new BugleApnSettingsLoader(context));
        MmsManager.setCarrierConfigValuesLoader(carrierConfigValuesLoader);
        MmsManager.setUserAgentInfoLoader(new BugleUserAgentInfoLoader(context));
        MmsManager.setUseWakeLock(true);
        // If Gservices is configured not to use mms api, force MmsManager to always use
        // legacy mms sending logic
        MmsManager.setForceLegacyMms(!bugleGservices.getBoolean(
                BugleGservicesKeys.USE_MMS_API_IF_PRESENT,
                BugleGservicesKeys.USE_MMS_API_IF_PRESENT_DEFAULT));
        bugleGservices.registerForChanges(new Runnable() {
            @Override
            public void run() {
                MmsManager.setForceLegacyMms(!bugleGservices.getBoolean(
                        BugleGservicesKeys.USE_MMS_API_IF_PRESENT,
                        BugleGservicesKeys.USE_MMS_API_IF_PRESENT_DEFAULT));
            }
        });
    }

    public static void updateAppConfig(final Context context, boolean isDefaultSms) {
        // Make sure we set the correct state for the SMS/MMS receivers
        SmsReceiver.updateSmsReceiveHandler(context, isDefaultSms);
    }

    // Called from thread started in FactoryImpl.register() (i.e. not run in tests)
    public void initializeAsync(final Factory factory) {
        // Handle shared prefs upgrade & Load MMS Configuration

        Trace.beginSection("app.initializeAsync");
        maybeHandleSharedPrefsUpgrade(factory);
        MmsConfig.load();
        Trace.endSection();
    }

    public void initMessageCenterLib() {
        MessageCenterManager.init(new MessagingMsgCenterFactoryImpl());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (LogUtil.isLoggable(TAG, LogUtil.DEBUG)) {
            LogUtil.d(TAG, "BugleApplication.onLowMemory");
        }
        Factory.get().reclaimMemory();
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        final boolean background = getMainLooper().getThread() != thread;
        if (background) {
            LogUtil.e(TAG, "Uncaught exception in background thread " + thread, ex);

            final Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    sSystemUncaughtExceptionHandler.uncaughtException(thread, ex);
                }
            });
        } else {
            sSystemUncaughtExceptionHandler.uncaughtException(thread, ex);
        }
    }

    private void initLeakCanaryAsync() {
        HSLog.d(TAG, "initializeAsync start " + "leak canary enabled ? " + ENABLE_LEAK_CANARY);
        Threads.postOnThreadPoolExecutor(() -> {
            if (ENABLE_LEAK_CANARY) {
                ExcludedRefs excludedRefs = AndroidExcludedRefs
                        .createAppDefaults()
                        .instanceField("android.view.ViewConfiguration", "mContext").reason("In AOSP the ViewConfiguration class does not have a context. Here we have ViewConfiguration.sConfigurations (static field) holding on to a ViewConfiguration instance that has a context that is the activity. Observed here: https://github.com/square/leakcanary/issues/1#issuecomment-100324683")
                        .build();
                LeakCanary.refWatcher(BugleApplication.this)
                        .watchDelay(20, TimeUnit.SECONDS)
                        .listenerServiceClass(UploadLeakService.class)
                        .excludedRefs(excludedRefs)
                        .buildAndInstall();
            }

            if (ENABLE_BLOCK_CANARY) {
                BlockCanary.install(BugleApplication.this, new BlockCanaryConfig()).start();
            }
        });
    }

    private void maybeStartProfiling() {
        // App startup profiling support. To use it:
        //  adb shell setprop log.tag.BugleProfile DEBUG
        //  #   Start the app, wait for a 30s, download trace file:
        //  adb pull /data/data/com.android.messaging/cache/startup.trace /tmp
        //  # Open trace file (using adt/tools/traceview)
        if (android.util.Log.isLoggable(LogUtil.PROFILE_TAG, android.util.Log.DEBUG)) {
            // Start method tracing with a big enough buffer and let it run for 30s.
            // Note we use a logging tag as we don't want to wait for gservices to start up.
            final File file = DebugUtils.getDebugFile("startup.trace", true);
            if (file != null) {
                android.os.Debug.startMethodTracing(file.getAbsolutePath(), 160 * 1024 * 1024);
                new Handler(Looper.getMainLooper()).postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                android.os.Debug.stopMethodTracing();
                                // Allow world to see trace file
                                DebugUtils.ensureReadable(file);
                                LogUtil.d(LogUtil.PROFILE_TAG, "Tracing complete - "
                                        + file.getAbsolutePath());
                            }
                        }, 30000);
            }
        }
    }

    private void maybeHandleSharedPrefsUpgrade(final Factory factory) {
        final int existingVersion = factory.getApplicationPrefs().getInt(
                BuglePrefsKeys.SHARED_PREFERENCES_VERSION,
                BuglePrefsKeys.SHARED_PREFERENCES_VERSION_DEFAULT);
        final int targetVersion = Integer.parseInt(getString(R.string.pref_version));
        if (targetVersion > existingVersion) {
            LogUtil.i(LogUtil.BUGLE_TAG, "Upgrading shared prefs from " + existingVersion +
                    " to " + targetVersion);
            try {
                // Perform upgrade on application-wide prefs.
                factory.getApplicationPrefs().onUpgrade(existingVersion, targetVersion);
                // Perform upgrade on each subscription's prefs.
                PhoneUtils.forEachActiveSubscription(new PhoneUtils.SubscriptionRunnable() {
                    @Override
                    public void runForSubscription(final int subId) {
                        factory.getSubscriptionPrefs(subId)
                                .onUpgrade(existingVersion, targetVersion);
                    }
                });
                factory.getApplicationPrefs().putInt(BuglePrefsKeys.SHARED_PREFERENCES_VERSION,
                        targetVersion);
            } catch (final Exception ex) {
                // Upgrade failed. Don't crash the app because we can always fall back to the
                // default settings.
                LogUtil.e(LogUtil.BUGLE_TAG, "Failed to upgrade shared prefs", ex);
            }
        } else if (targetVersion < existingVersion) {
            // We don't care about downgrade since real user shouldn't encounter this, so log it
            // and ignore any prefs migration.
            LogUtil.e(LogUtil.BUGLE_TAG, "Shared prefs downgrade requested and ignored. " +
                    "oldVersion = " + existingVersion + ", newVersion = " + targetVersion);
        }
    }
}
