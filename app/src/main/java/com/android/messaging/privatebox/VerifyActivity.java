package com.android.messaging.privatebox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.view.GestureLockView;
import com.android.messaging.privatebox.view.PINIndicatorView;
import com.android.messaging.privatebox.view.PINKeyboardView;
import com.android.messaging.util.ViewUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

public abstract class VerifyActivity extends BaseActivity
        implements INotificationObserver {

    private static final String TAG = "LockBaseActivity";

    public static final String EVENT_UNLOCK_APP_SUCCESS = "EVENT_UNLOCK_APP_SUCCESS";

    private GestureLockView gestureLockView;
    private PINKeyboardView pinUnlockView;
    private PINIndicatorView pinIndicatorView;
    protected ImageView panelAppIcon;

    protected View background;

    protected boolean fromLockActivity = false;  //true: activity上push一个lock activity，后台回前台，锁屏回来等；false：从main activity 入口进入，先push lock activity，然后push installed app activity

    public static final String INTENT_KEY_FROM_LOCK_ACTIVITY = "INTENT_KEY_FROM_LOCK_ACTIVITY";

    protected boolean mIsDontLockApp = false;
    protected boolean mIsToLockMode = false;

    private ViewGroup mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());

        mainContainer = findViewById(R.id.lock_container);

        background = findViewById(R.id.background);

        gestureLockView = findViewById(R.id.gesture_unlock_view);
        gestureLockView.setPasswordOrAppUnLock(false);

        pinUnlockView = findViewById(R.id.pin_unlock_view);
        pinIndicatorView = findViewById(R.id.pin_indicator_view);

        pinUnlockView.setOnKeyboardClickListener(i -> {
//            if (fingerprintTopTipContainer.getVisibility() == View.VISIBLE) {
//                if (!isSelfLock) {
//                    fingerprintTopTipContainer.setVisibility(View.INVISIBLE);
//                }
//                pinIndicatorView.setVisibility(View.VISIBLE);
//            }
            if (i >= 0) {
                pinIndicatorView.inc(i);
            } else {
                pinIndicatorView.dec();
            }
        });
        pinIndicatorView.setOnPINFinishedListener(decodedPIN -> {
            if (decodedPIN.equals(PrivateBoxSettings.getUnlockPIN())) {
                onUnlockSucceed();
            } else {
                performFingerprintShakeAnimation(pinIndicatorView, () -> {
                    if (pinIndicatorView != null) {
                        pinIndicatorView.clear();
                    }
                });
            }
        });
        gestureLockView.setOnGestureFinishListener(new GestureLockView.OnGestureFinishListener() {
            @Override
            public void onGestureLayoutFinished(int topMargin) {
                if (topMargin > 0) {
                    int maxMargin = Dimensions.getPhoneHeight(VerifyActivity.this) / 9;
                    if (maxMargin > topMargin) {
                        int panelAppIconBottomMargin = maxMargin - topMargin;
                        ViewUtils.setMargins(panelAppIcon, 0, 0, 0, panelAppIconBottomMargin);
                    }
                }
            }

            @Override
            public void onSetPasswordFinished(String password) {
                if (TextUtils.isEmpty(password)) {
                    return;
                } else {
                    if (password.equals(PrivateBoxSettings.getUnlockGesture())) {
                        onUnlockSucceed();
                    }
                }
            }

            @Override
            public void onPasswordVerifyFinished(boolean result, String password) {
                if (password.equals(PrivateBoxSettings.getUnlockGesture())) {
                    onUnlockSucceed();
                }
            }
        });

        resetUnlockUI();


        HSGlobalNotificationCenter.addObserver(EVENT_UNLOCK_APP_SUCCESS, this);
    }

    protected abstract int getLayoutResId();

    protected abstract Drawable getProtectedAppIcon();

    protected abstract String getProtectedAppName();

    protected abstract Drawable getBackgroundDrawable();

    protected abstract Drawable getPanelAppIcon();

    protected abstract int getFingerprintTipColor();

    protected void onUnlockSucceed() {
        setResult(RESULT_OK);

        overridePendingTransition(0, R.anim.fade_out_long);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (null != intent) {
            setIntent(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        resetUnlockUI();
    }

    @Override
    protected void onStop() {
        HSLog.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Navigations.startActivitySafely(this, startMain);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void performFingerprintShakeAnimation(View fingerprintView, Runnable endRunnable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(fingerprintView, View.TRANSLATION_X, 0, 25, 5, -15, -5, 5, 0, -5, 0, 5, 0);
        animator.setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (endRunnable != null) {
                    endRunnable.run();
                }
            }
        });
        animator.start();
    }

    private void performDismissAnimation(View view, Runnable endRunnable) {
        view.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                view.setAlpha(1f);

                if (endRunnable != null) {
                    endRunnable.run();
                }
            }
        }).start();
    }

    private void performEnterAnimation(View view) {
        view.setTranslationY(Dimensions.pxFromDp(148));
        view.setAlpha(0f);
        view.animate().translationY(0).alpha(1f).setDuration(300).start();
    }

    private void performAlphaEnterAnimation(View view) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(320).start();
    }

    private void resetUnlockUI() {
        showPatternOrPinView();
        pinIndicatorView.clear();
    }

    private void showPatternOrPinView() {
        switch (PrivateBoxSettings.getLockStyle()) {
            case PATTERN:
                gestureLockView.setVisibility(View.VISIBLE);
                pinUnlockView.setVisibility(View.INVISIBLE);
                pinIndicatorView.setVisibility(View.INVISIBLE);
                break;
            case PIN:
                gestureLockView.setVisibility(View.INVISIBLE);
                pinUnlockView.setVisibility(View.VISIBLE);
                pinIndicatorView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    @Override public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_UNLOCK_APP_SUCCESS:
                finish();
                break;
            default:
                break;
        }
    }
}
