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

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.PhoneUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Toasts;

public class SetAsDefaultGuideActivity extends BaseActivity {
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;

    public static final int USER_PRESENT = 1;
    public static final int DEFAULT_CHANGED = 2;

    @IntDef({USER_PRESENT, DEFAULT_CHANGED})
    @interface DialogType {
    }

    private int mType;

    public static void startActivity(Context context, @DialogType int type) {
        Intent intent = new Intent(context, SetAsDefaultGuideActivity.class);
        intent.putExtra("from", type);
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_as_default_guide);

        mType = getIntent().getIntExtra("from", USER_PRESENT);

        if (mType == USER_PRESENT) {
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
            topImage.setImageResource(R.drawable.set_as_default_top_image_user_present);
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
                BugleAnalytics.logEvent("SMS_DefaultAlert_BtnClick", true, "type", "Unlock");
            } else {
                BugleAnalytics.logEvent("SMS_DefaultAlert_BtnClick", true, "type", "Cleared");
            }
            final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(SetAsDefaultGuideActivity.this);
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
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
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
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
}
