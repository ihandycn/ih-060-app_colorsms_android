package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateBoxSettings;
import com.android.messaging.privatebox.ui.view.GestureLockView;
import com.android.messaging.privatebox.ui.view.PINIndicatorView;
import com.android.messaging.privatebox.ui.view.PINKeyboardView;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;

public class PrivateBoxSetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private enum PasswordSetMode {
        NORMAL_FIRST_SET,
        NORMAL_SECOND_SET
    }

    private static final String TAG = "PasswordSetActivity";
    public static final String INTENT_EXTRA_RESET_PASSWORD = "INTENT_EXTRA_RESET_PASSWORD";
    public static final String INTENT_EXTRA_FORGET_PASSWORD = "INTENT_EXTRA_FORGET_PASSWORD";
    public static final String INTENT_KER_PASSWORD_STATUS = "key_password_status";
    public static final String RESET_PASSWORD = "reset_password";
    public static final String SET_PASSWORD = "set_password";
    private String password = PrivateBoxSettings.PASSWORD_PLACEHOLDER;

    private PasswordSetMode passwordSetMode;

    private GestureLockView gestureLockView;
    private PINKeyboardView pinUnlockView;
    private PINIndicatorView pinIndicatorView;

    private TextView promptLine;
    private TextView promptSubLine;
    private LottieAnimationView patternGuideView;
    private ImageView pinGuideView;
    private TextView operationTv;

    private Animation shakeAnimation;
    private boolean isResetPassword = false;
    private boolean isForgetPassword = false;
    private PrivateBoxSettings.PasswordStyle currentPasswordStyle;
    private Handler handler = new Handler();

    private class GestureListener implements GestureLockView.OnGestureFinishListener {
        @Override
        public void onGestureLayoutFinished(int topMargin) {
        }

        @Override
        public void onSetPasswordFinished(String password) {
            HSLog.d("onSetPasswordFinished pwd == " + password);
            if (null != operationTv) {
                operationTv.setText(R.string.reset_password);
                operationTv.setTag(getString(R.string.reset_password));
            }

            if (TextUtils.isEmpty(password)) {
                performShakeAnimation(getString(R.string.set_gesture_sub_prompt), true);
                if (isResetPassword) {
                } else {
                    BugleAnalytics.logEvent("AppLock_Setpassword_First_Input", "State", "fail");
                }
            } else {
                passwordSetMode = PasswordSetMode.NORMAL_SECOND_SET;
                PrivateBoxSetPasswordActivity.this.password = password;

                currentPasswordStyle = PrivateBoxSettings.PasswordStyle.PATTERN;
                updateCurrentProtectionUI(currentPasswordStyle, false);
                if (isResetPassword) {
                } else {
                    BugleAnalytics.logEvent("AppLock_Setpassword_First_Input", "State", "success");
                    BugleAnalytics.logEvent("AppLock_Setpassword_Second_Shown", "Type", "Pattern");
                }
            }
        }

        @Override
        public void onPasswordVerifyFinished(boolean result, String password) {
            if (result) {
                PrivateBoxSettings.setUnlockGesture(password);
                currentPasswordStyle = PrivateBoxSettings.PasswordStyle.PATTERN;
                PrivateBoxSettings.setLockStyle(currentPasswordStyle);
                Intent intent = new Intent();
                if (isResetPassword) {
                    intent.putExtra(INTENT_KER_PASSWORD_STATUS, RESET_PASSWORD);
                } else {
                    BugleAnalytics.logEvent("AppLock_Setpassword_Second_Input", "State", "success");
                    BugleAnalytics.logEvent("AppLock_Setpassword_Success", "Type", "pattern");
                    intent.putExtra(INTENT_KER_PASSWORD_STATUS, SET_PASSWORD);
                }

                onPasswordSetSucceed();
                setResult(RESULT_OK, intent);
                finish();
                HSLog.i("onGestureFinish(), NORMAL_SECOND_SET, finish");
            } else {
                performShakeAnimation(getString(R.string.gesture_not_confirmed_sub_prompt), false);
                promptSubLine.setText("");
                if (isResetPassword) {
                } else {
                    BugleAnalytics.logEvent("AppLock_Setpassword_Second_Input", "State", "fail");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_password);

        if (null != getIntent()) {
            isResetPassword = getIntent().getBooleanExtra(INTENT_EXTRA_RESET_PASSWORD, false);
            isForgetPassword = getIntent().getBooleanExtra(INTENT_EXTRA_FORGET_PASSWORD, false);
        }
        currentPasswordStyle = PrivateBoxSettings.getLockStyle();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(isResetPassword ?
                getString(R.string.reset_password) : getString(R.string.menu_privacy_box));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.top_container).setBackgroundColor(PrimaryColors.getPrimaryColor());

        operationTv = findViewById(R.id.operation_tv);
        operationTv.setTextColor(PrimaryColors.getPrimaryColor());
        operationTv.setOnClickListener(this);

        patternGuideView = findViewById(R.id.set_lock_pattern_guide);
        patternGuideView.loop(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                patternGuideView.playAnimation();
                handler.postDelayed(this, 1500);
            }
        }, 500);

        pinGuideView = findViewById(R.id.set_lock_pin_guide);

        promptLine = findViewById(R.id.lock_text_switcher);
        promptSubLine = findViewById(R.id.lock_text_sub_switcher);

        gestureLockView = findViewById(R.id.gesture_unlock_view);
        gestureLockView.setPasswordOrAppUnLock(true);
        gestureLockView.hidePath(false);
        gestureLockView.setOnGestureFinishListener(new GestureListener());

        pinUnlockView = findViewById(R.id.pin_unlock_view);
        pinUnlockView.setOnKeyboardClickListener(new PINKeyboardView.OnKeyboardClickListener() {
            @Override
            public void onKeyboardClick(int i) {
                if (i >= 0) {
                    pinIndicatorView.inc(i);
                } else {
                    pinIndicatorView.dec();
                }
            }
        });

        pinIndicatorView = findViewById(R.id.pin_indicator_view);
        pinIndicatorView.setOnPINFinishedListener(new PINIndicatorView.OnPINFinishedListener() {
            @Override
            public void onPINFinished(String decodedPIN) {
                if (null != operationTv) {
                    operationTv.setText(R.string.reset_password);
                    operationTv.setTag(getString(R.string.reset_password));
                }

                switch (passwordSetMode) {
                    case NORMAL_FIRST_SET:
                        passwordSetMode = PasswordSetMode.NORMAL_SECOND_SET;

                        password = decodedPIN;

                        pinIndicatorView.clear();
                        currentPasswordStyle = PrivateBoxSettings.PasswordStyle.PIN;
                        updateCurrentProtectionUI(currentPasswordStyle, false);
                        if (isResetPassword) {
                        } else {
                            BugleAnalytics.logEvent("AppLock_Setpassword_First_Input", "State", "success");
                            BugleAnalytics.logEvent("AppLock_Setpassword_Second_Shown", "Type", "Pin");
                        }
                        break;
                    case NORMAL_SECOND_SET:
                        if (decodedPIN.equals(password)) {
                            PrivateBoxSettings.setUnlockPIN(password);
                            currentPasswordStyle = PrivateBoxSettings.PasswordStyle.PIN;
                            PrivateBoxSettings.setLockStyle(currentPasswordStyle);
                            Intent intent = new Intent();
                            if (isResetPassword) {
                                intent.putExtra(INTENT_KER_PASSWORD_STATUS, RESET_PASSWORD);
                            } else {
                                BugleAnalytics.logEvent("AppLock_Setpassword_Second_Input", "State", "success");
                                BugleAnalytics.logEvent("AppLock_Setpassword_Success", "Type", "number");
                                intent.putExtra(INTENT_KER_PASSWORD_STATUS, SET_PASSWORD);
                            }

                            onPasswordSetSucceed();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            performShakeAnimation(getString(R.string.password_not_confirmed_sub_prompt), false);
                            if (isResetPassword) {
                            } else {
                                BugleAnalytics.logEvent("AppLock_Setpassword_Second_Input", "State", "fail");
                            }
                        }
                        break;
                }
            }
        });

        passwordSetMode = PasswordSetMode.NORMAL_FIRST_SET;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateCurrentProtectionUI(currentPasswordStyle, true);

        BugleAnalytics.logEvent("AppLock_Setpassword_Shown");
    }

    @Override
    protected void onStart() {
        super.onStart();

        HSLog.d(TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        HSLog.d(TAG, "onStop()");
        if (isForgetPassword) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacksAndMessages(null);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, getIntent());
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED, getIntent());
        }

        return super.onKeyDown(keyCode, event);
    }

    private void performShakeAnimation(final String msg, boolean isPromptSubLine) {
        if (shakeAnimation == null) {
            shakeAnimation = AnimationUtils.loadAnimation(PrivateBoxSetPasswordActivity.this, R.anim.left_right_shake);
            shakeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (pinIndicatorView != null) {
                        pinIndicatorView.clear();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        if (isPromptSubLine) {
            promptSubLine.startAnimation(shakeAnimation);
        } else {
            if (promptLine.getText().equals(msg)) {
                promptLine.startAnimation(shakeAnimation);
            } else {
                promptLine.setText(msg);
                promptLine.startAnimation(shakeAnimation);
            }
        }
    }

    private void updateCurrentProtectionUI(PrivateBoxSettings.PasswordStyle passwordStyle, boolean isUpdateOperationText) {
        switch (passwordStyle) {

            case PATTERN:
                patternGuideView.setVisibility(View.VISIBLE);
                pinGuideView.setVisibility(View.INVISIBLE);
                gestureLockView.setVisibility(View.VISIBLE);
                pinUnlockView.setVisibility(View.INVISIBLE);
                pinIndicatorView.setVisibility(View.INVISIBLE);
                if (isUpdateOperationText) {
                    operationTv.setText(R.string.lock_activity_change_to_pin);
                }

                switch (passwordSetMode) {

                    case NORMAL_FIRST_SET:
                        if (PrivateBoxSettings.isAnyPasswordSet()) {
                            promptLine.setText(getString(R.string.draw_unlock_pattern));
                        } else {
                            promptLine.setText(getString(R.string.please_set_password));
                        }
                        promptSubLine.setText(getString(R.string.set_gesture_sub_prompt));
                        break;

                    case NORMAL_SECOND_SET:
                        promptLine.setText(getString(R.string.confirm_prompt_gesture));
                        promptSubLine.setText("");
                        pinGuideView.setVisibility(View.INVISIBLE);
                        break;
                }
                break;

            case PIN:
                pinUnlockView.setVisibility(View.VISIBLE);
                pinIndicatorView.setVisibility(View.VISIBLE);
                gestureLockView.setVisibility(View.INVISIBLE);
                patternGuideView.setVisibility(View.INVISIBLE);
                pinGuideView.setVisibility(View.VISIBLE);
                if (isUpdateOperationText) {
                    operationTv.setText(R.string.lock_activity_change_to_pattern);
                }

                switch (passwordSetMode) {
                    case NORMAL_FIRST_SET:
                        if (PrivateBoxSettings.isAnyPasswordSet()) {
                            promptLine.setText(getString(R.string.set_password_prompt));
                        } else {
                            promptLine.setText(getString(R.string.set_password_prompt));
                        }
                        promptSubLine.setText(getString(R.string.set_password_sub_prompt));
                        break;

                    case NORMAL_SECOND_SET:
                        promptLine.setText(getString(R.string.confirm_prompt_password));
                        promptSubLine.setText("");
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.operation_tv:
                if (null != operationTv && null != operationTv.getTag()) {
                    String operationTag = operationTv.getTag().toString();
                    if (!TextUtils.isEmpty(operationTag) && operationTag.equals(getString(R.string.reset_password))) {
                        pinIndicatorView.clear();
                        password = PrivateBoxSettings.PASSWORD_PLACEHOLDER;
                        passwordSetMode = PasswordSetMode.NORMAL_FIRST_SET;
                        gestureLockView.setVerifyPassword(null);

                        updateCurrentProtectionUI(currentPasswordStyle, true);
                        operationTv.setTag("");
                        break;
                    }
                }

                BugleAnalytics.logEvent("AppLock_Setpassword_ChangeType_Clicked", "Type", currentPasswordStyle.toString());
                switch (currentPasswordStyle) {
                    case PATTERN:
                        currentPasswordStyle = PrivateBoxSettings.PasswordStyle.PIN;
                        break;
                    case PIN:
                        currentPasswordStyle = PrivateBoxSettings.PasswordStyle.PATTERN;
                        break;
                }

                pinIndicatorView.clear();
                password = PrivateBoxSettings.PASSWORD_PLACEHOLDER;
                passwordSetMode = PasswordSetMode.NORMAL_FIRST_SET;

                updateCurrentProtectionUI(currentPasswordStyle, true);
                break;
            default:
                break;
        }
    }

    private void onPasswordSetSucceed() {
        if (!isResetPassword && !isForgetPassword) {
            Intent intent = new Intent(PrivateBoxSetPasswordActivity.this, PrivateConversationListActivity.class);
            if (getIntent().hasExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST)) {
                intent.putExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST,
                        getIntent().getStringArrayExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST)
                );
            }
            Navigations.startActivitySafely(PrivateBoxSetPasswordActivity.this, intent);
        }
    }
}
