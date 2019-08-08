package com.android.messaging.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.SetDefaultPushAutopilotUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

public class SetAsDefaultGuideActivity extends AppCompatActivity {
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;

    public static final int USER_PRESENT = 1;
    public static final int DEFAULT_CHANGED = 2;

    public static final String KEY_FOR_USER_PRESENT_DAYS_COUNT = "user_present_days_count";

    private int userPresentTimes = 0;
    private boolean mShouldPush = true;

    @IntDef({USER_PRESENT, DEFAULT_CHANGED})
    @interface DialogType {
    }

    private int mType;

    public static void startActivity(Context context, @DialogType int type) {
        Intent intent = new Intent(context, SetAsDefaultGuideActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("from", type);
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_as_default_guide);

        SetDefaultPushAutopilotUtils.logAlertSetDefaultShow();

        mType = getIntent().getIntExtra("from", USER_PRESENT);

        if (mType == USER_PRESENT) {
            userPresentTimes = Preferences.getDefault().getInt(KEY_FOR_USER_PRESENT_DAYS_COUNT, 1);
            BugleAnalytics.logEvent("SMS_DefaultAlert_Show", true, "type", "Unlock");
        } else {
            BugleAnalytics.logEvent("SMS_DefaultAlert_Show", true, "type", "Cleared");
        }

        CommonUtils.immersiveStatusAndNavigationBar(getWindow());

        initView();
    }

    private void initView() {
        TextView title = findViewById(R.id.set_as_default_title);
        TextView subtitle = findViewById(R.id.set_as_default_content);
        TextView okBtn = findViewById(R.id.set_as_default_ok_btn);
        ImageView topImage = findViewById(R.id.set_as_default_top_image);

        if (mType == USER_PRESENT) {
            title.setText(R.string.set_as_default_dialog_title_user_present);
            subtitle.setText(R.string.set_as_default_dialog_description_user_present);
            if (userPresentTimes >= 3) {
                topImage.setImageResource(R.drawable.set_as_default_top_image_user_present);
            } else {
                topImage.setImageResource(R.drawable.theme_upgrade_banner);
            }
            okBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.dialog_positive_button_color),
                    Dimensions.pxFromDp(3.3f), true));
            okBtn.setText(R.string.set_as_default_dialog_button_ok_user_present);
        } else {
            title.setText(R.string.set_as_default_dialog_title_default_change);
            subtitle.setText(R.string.set_as_default_dialog_description_default_change);
            topImage.setImageResource(R.drawable.set_as_default_top_image_cleard);
            okBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.dialog_positive_button_color),
                    Dimensions.pxFromDp(3.3f), true));
            okBtn.setText(R.string.set_as_default_dialog_button_ok_default_change);
        }

        findViewById(R.id.set_as_default_cancel_btn).setOnClickListener((v) -> finish());

        okBtn.setOnClickListener((v) -> {
            if (mType == USER_PRESENT) {
                SetDefaultPushAutopilotUtils.logAlertSetDefaultClick();
                BugleAnalytics.logEvent("SMS_DefaultAlert_BtnClick", true, "type", "Unlock");
            } else {
                BugleAnalytics.logEvent("SMS_DefaultAlert_BtnClick", true, "type", "Cleared");
            }
            final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(SetAsDefaultGuideActivity.this);
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);

            mShouldPush = false;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mType = intent.getIntExtra("from", USER_PRESENT);

        if (mType == USER_PRESENT) {
            BugleAnalytics.logEvent("SMS_DefaultAlert_Show", true, "type", "Unlock");
        } else {
            BugleAnalytics.logEvent("SMS_DefaultAlert_Show", true, "type", "Cleared");
        }

        CommonUtils.immersiveStatusAndNavigationBar(getWindow());

        initView();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (DefaultSMSUtils.isDefaultSmsApp(true)) {
                if (mType == USER_PRESENT) {
                    BugleAnalytics.logEvent("SMS_DefaultAlert_SetDefault_Success", true, "type", "Unlock");
                } else {
                    BugleAnalytics.logEvent("SMS_DefaultAlert_SetDefault_Success", true, "type", "Cleared");
                }
                finish();
                if (mType == DEFAULT_CHANGED) {
                    Intent intent = new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setComponent(new ComponentName(this, ConversationListActivity.class))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    Navigations.startActivitySafely(this, intent);
                    overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);

                    SetDefaultPushAutopilotUtils.logAlertSetDefaultSuccess();
                }
            } else {
                Toasts.showToast(R.string.welcome_set_default_failed_toast, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.app_lock_fade_out_long);
    }

    @Override
    protected void onDestroy() {
        if(mType == USER_PRESENT && mShouldPush) {
            SetDefaultNotification notification = new SetDefaultNotification(this);
            if (notification.getEnablePush()) {
                notification.sendNotification();
            }
        }
        super.onDestroy();
    }
}
