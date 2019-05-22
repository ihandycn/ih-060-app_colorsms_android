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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.SparseArray;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DataModelImpl;
import com.android.messaging.datamodel.MemoryCacheManager;
import com.android.messaging.datamodel.ParticipantRefresh.ContactContentObserver;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.media.BugleMediaCacheManager;
import com.android.messaging.datamodel.media.MediaCacheManager;
import com.android.messaging.datamodel.media.MediaResourceManager;
import com.android.messaging.sms.BugleCarrierConfigValuesLoader;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleApplicationPrefs;
import com.android.messaging.util.BugleCustomizePrefs;
import com.android.messaging.util.BugleGservices;
import com.android.messaging.util.BugleGservicesImpl;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.BugleSubscriptionPrefs;
import com.android.messaging.util.BugleWidgetPrefs;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.MediaUtil;
import com.android.messaging.util.MediaUtilImpl;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.superapps.util.Compats;
import com.superapps.util.Threads;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

class FactoryImpl extends Factory {
    private BugleApplication mApplication;
    private DataModel mDataModel;
    private BugleGservices mBugleGservices;
    private BugleApplicationPrefs mBugleApplicationPrefs;
    private BugleWidgetPrefs mBugleWidgetPrefs;
    private BuglePrefs mBugleCustomizePrefs;
    private Context mApplicationContext;
    private UIIntents mUIIntents;
    private MemoryCacheManager mMemoryCacheManager;
    private MediaResourceManager mMediaResourceManager;
    private MediaCacheManager mMediaCacheManager;
    private ContactContentObserver mContactContentObserver;
    private PhoneUtils mPhoneUtils;
    private MediaUtil mMediaUtil;
    private SparseArray<BugleSubscriptionPrefs> mSubscriptionPrefs;
    private BugleCarrierConfigValuesLoader mCarrierConfigValuesLoader;
    private boolean mIsForeground;

    // Cached instance for Pre-L_MR1
    private static final Object PHONEUTILS_INSTANCE_LOCK = new Object();
    private static PhoneUtils sPhoneUtilsInstancePreLMR1 = null;
    // Cached subId->instance for L_MR1 and beyond
    private static final ConcurrentHashMap<Integer, PhoneUtils> sPhoneUtilsInstanceCacheLMR1 =
            new ConcurrentHashMap<>();

    private FactoryImpl() {
    }

    public static Factory registerDB(final Context applicationContext) {
        // This only gets called once (from BugleApplication.onCreate), but its not called in tests.
        Assert.isTrue(!sRegistered);
        Assert.isNull(Factory.get());

        final FactoryImpl factory = new FactoryImpl();
        Factory.setInstance(factory);
        sRegistered = true;

        factory.mDataModel = new DataModelImpl(applicationContext);
        return factory;
    }

    public static Factory register(final Context applicationContext,
                                   final BugleApplication application) {
        FactoryImpl factory = (FactoryImpl) Factory.get();
        // At this point Factory is published. Services can now get initialized and depend on
        // Factory.get().
        factory.mApplication = application;
        factory.mApplicationContext = applicationContext;
        factory.mMemoryCacheManager = new MemoryCacheManager();
        factory.mMediaCacheManager = new BugleMediaCacheManager();
        factory.mMediaResourceManager = new MediaResourceManager();
        factory.mBugleGservices = new BugleGservicesImpl(applicationContext);
        factory.mBugleApplicationPrefs = new BugleApplicationPrefs(applicationContext);
        factory.mBugleWidgetPrefs = new BugleWidgetPrefs(applicationContext);
        factory.mBugleCustomizePrefs = new BugleCustomizePrefs(applicationContext);
        factory.mUIIntents = new UIIntentsImpl();
        factory.mContactContentObserver = new ContactContentObserver();
        factory.mMediaUtil = new MediaUtilImpl();
        factory.mSubscriptionPrefs = new SparseArray<BugleSubscriptionPrefs>();
        factory.mCarrierConfigValuesLoader = new BugleCarrierConfigValuesLoader(applicationContext);
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (!(activity instanceof MessageBoxActivity)) {
                    factory.mIsForeground = true;
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (!(activity instanceof MessageBoxActivity)) {
                    factory.mIsForeground = false;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Compats.IS_SAMSUNG_DEVICE) {
                        Object systemService = applicationContext.getSystemService(Class.forName("com.samsung.android.content.clipboard.SemClipboardManager"));
                        Field mContext = systemService.getClass().getDeclaredField("mContext");
                        mContext.setAccessible(true);
                        mContext.set(systemService, null);
                    }
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) { //ignored

                }
            }
        });

        Assert.initializeGservices(factory.mBugleGservices);
        LogUtil.initializeGservices(factory.mBugleGservices);

        if (PhoneUtils.getDefault().isDefaultSmsApp() && OsUtil.hasRequiredPermissions()) {
            factory.onDefaultSmsSetAndPermissionsGranted();
        }
        PhoneUtils.getDefault().registerDefaultSmsPackageChange(
                () -> {
                    if (OsUtil.hasRequiredPermissions()) {
                        factory.onDefaultSmsSetAndPermissionsGranted();
                    }
                },
                () -> BugleApplication.updateAppConfig(factory.getApplicationContext(), false));

        return factory;
    }

    @Override
    public void onDefaultSmsSetAndPermissionsGranted() {
        if (sInitialized) {
            return;
        }
        sInitialized = true;

        Threads.postOnMainThread(() -> mApplication.initializeSync(this));

        Threads.postOnThreadPoolExecutor(() -> mApplication.initializeAsync(FactoryImpl.this));
    }

    @Override
    public Context getApplicationContext() {
        return mApplicationContext;
    }

    @Override
    public DataModel getDataModel() {
        return mDataModel;
    }

    @Override
    public BugleGservices getBugleGservices() {
        return mBugleGservices;
    }

    @Override
    public BuglePrefs getApplicationPrefs() {
        return mBugleApplicationPrefs;
    }

    @Override
    public BuglePrefs getWidgetPrefs() {
        return mBugleWidgetPrefs;
    }

    @Override
    public BuglePrefs getCustomizePrefs() {
        return mBugleCustomizePrefs;
    }

    @Override
    public BuglePrefs getSubscriptionPrefs(int subId) {
        subId = PhoneUtils.getDefault().getEffectiveSubId(subId);
        BugleSubscriptionPrefs pref = mSubscriptionPrefs.get(subId);
        if (pref == null) {
            synchronized (this) {
                if ((pref = mSubscriptionPrefs.get(subId)) == null) {
                    pref = new BugleSubscriptionPrefs(getApplicationContext(), subId);
                    mSubscriptionPrefs.put(subId, pref);
                }
            }
        }
        return pref;
    }

    @Override
    public UIIntents getUIIntents() {
        return mUIIntents;
    }

    @Override
    public MemoryCacheManager getMemoryCacheManager() {
        return mMemoryCacheManager;
    }

    @Override
    public MediaResourceManager getMediaResourceManager() {
        return mMediaResourceManager;
    }

    @Override
    public MediaCacheManager getMediaCacheManager() {
        return mMediaCacheManager;
    }

    @Override
    public ContactContentObserver getContactContentObserver() {
        return mContactContentObserver;
    }

    @Override
    public PhoneUtils getPhoneUtils(int subId) {
        if (OsUtil.isAtLeastL_MR1()) {
            if (subId == ParticipantData.DEFAULT_SELF_SUB_ID) {
                subId = SmsManager.getDefaultSmsSubscriptionId();
            }
            if (subId < 0) {
                LogUtil.w(LogUtil.BUGLE_TAG, "PhoneUtils.getForLMR1(): invalid subId = " + subId);
                subId = ParticipantData.DEFAULT_SELF_SUB_ID;
            }
            PhoneUtils instance = sPhoneUtilsInstanceCacheLMR1.get(subId);
            if (instance == null) {
                instance = new PhoneUtils.PhoneUtilsLMR1(subId);
                sPhoneUtilsInstanceCacheLMR1.putIfAbsent(subId, instance);
            }
            return instance;
        } else {
            Assert.isTrue(subId == ParticipantData.DEFAULT_SELF_SUB_ID);
            if (sPhoneUtilsInstancePreLMR1 == null) {
                synchronized (PHONEUTILS_INSTANCE_LOCK) {
                    if (sPhoneUtilsInstancePreLMR1 == null) {
                        sPhoneUtilsInstancePreLMR1 = new PhoneUtils.PhoneUtilsPreLMR1();
                    }
                }
            }
            return sPhoneUtilsInstancePreLMR1;
        }
    }

    @Override
    public boolean getIsForeground() {
        return mIsForeground;
    }

    @Override
    public void reclaimMemory() {
        mMemoryCacheManager.reclaimMemory();
    }

    @Override
    public void onActivityResume() {
    }

    @Override
    public MediaUtil getMediaUtil() {
        return mMediaUtil;
    }

    @Override
    public BugleCarrierConfigValuesLoader getCarrierConfigValuesLoader() {
        return mCarrierConfigValuesLoader;
    }
}
