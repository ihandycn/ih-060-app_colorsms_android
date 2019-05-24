package com.android.messaging.privatebox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.messaging.privatebox.ui.SelfVerifyActivity;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.HomeKeyWatcher;

public class AppPrivateLockManager {
    private boolean mIsAppLocked;
    private HomeKeyWatcher mHomeWatcher;
    private LockScreenReceiver mLockScreenReceiver;
    private static AppPrivateLockManager sInstance = new AppPrivateLockManager();

    public static AppPrivateLockManager getInstance() {
        return sInstance;
    }

    private AppPrivateLockManager() {

    }

    public void startAppLockWatch() {
        mHomeWatcher = new HomeKeyWatcher(HSApplication.getContext());
        mHomeWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                mIsAppLocked = true;
            }

            @Override
            public void onRecentsPressed() {
                mIsAppLocked = true;
            }
        });
        mHomeWatcher.startWatch();

        mLockScreenReceiver = new LockScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        HSApplication.getContext().registerReceiver(mLockScreenReceiver, filter);
    }

    public void checkLockStateAndSelfVerify() {
        if (mIsAppLocked) {
            SelfVerifyActivity.startVerifyActivityForAppLocked();
        }
    }

    public void stopAppLockWatch() {
        mHomeWatcher.stopWatch();
        HSApplication.getContext().unregisterReceiver(mLockScreenReceiver);
    }

    public void unlockAppLock() {
        mIsAppLocked = false;
    }

    public void lockAppLock() {
        mIsAppLocked = true;
    }

    class LockScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
               // SelfVerifyActivity.startVerifyActivityForAppLocked();
                mIsAppLocked = true;
            }
        }
    }
}
