package com.android.messaging.privatebox.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateBoxSettings;
import com.android.messaging.privatebox.ui.view.GestureLockView;
import com.android.messaging.privatebox.ui.view.PINIndicatorView;
import com.android.messaging.privatebox.ui.view.PINKeyboardView;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.ViewUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

public abstract class VerifyActivity extends BaseActivity implements INotificationObserver {

    private static final String TAG = "VerifyActivity";

    public static final String EVENT_UNLOCK_APP_SUCCESS = "EVENT_UNLOCK_APP_SUCCESS";

    private GestureLockView gestureLockView;
    private PINKeyboardView pinUnlockView;
    private PINIndicatorView pinIndicatorView;
    protected ImageView panelAppIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());

        findViewById(R.id.background).setBackgroundColor(PrimaryColors.getPrimaryColor());

        gestureLockView = findViewById(R.id.gesture_unlock_view);
        gestureLockView.setPasswordOrAppUnLock(false);

        pinUnlockView = findViewById(R.id.pin_unlock_view);
        pinIndicatorView = findViewById(R.id.pin_indicator_view);

        pinUnlockView.setOnKeyboardClickListener(i -> {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
