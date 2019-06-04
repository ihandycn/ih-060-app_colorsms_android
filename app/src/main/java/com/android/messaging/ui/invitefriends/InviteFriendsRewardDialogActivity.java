package com.android.messaging.ui.invitefriends;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class InviteFriendsRewardDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_friends_reward_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        ImageView closeButton = findViewById(R.id.close_btn);
        closeButton.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffe9eff1,
                Dimensions.pxFromDp(16f), true));
        closeButton.setOnClickListener(v -> finish());

        LinearLayout mainContent = findViewById(R.id.main_content);
        mainContent.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                Dimensions.pxFromDp(36), false));

        changeDescription();

        TextView inviteButton = findViewById(R.id.invite_button);
        inviteButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(20f), true));
        inviteButton.setOnClickListener(v -> {
            Intent Intent = new Intent(InviteFriendsRewardDialogActivity.this, InviteFriendsActivity.class);
            startActivity(Intent);
            finish();
            InviteFriendsTest.logGuideAlertClick();
        });

        InviteFriendsTest.logGuideAlertShow();
    }

    private void changeDescription() {
        TextView titleTv = findViewById(R.id.title);
        TextView descriptionTv = findViewById(R.id.description);
        String type = InviteFriendsTest.getAlertType();

        String title;
        String description;

        if ("default".equals(type)) {
            title = getString(R.string.invite_friends_default_back_to_main_page_title);
        } else {
            title = getString(R.string.invite_friends_bonus_title);
        }

        if ("freesms".equals(type)) {
            description = getString(R.string.invite_friends_bonus_free_sms_description);
        } else if ("adfree".equals(type)) {
            description = getString(R.string.invite_friends_bonus_ad_free_description);
        } else if ("unlocktheme".equals(type)) {
            description = getString(R.string.invite_friends_bonus_unlock_theme_description);
        } else {
            description = getString(R.string.invite_friends_default_back_to_main_page_description);
        }

        titleTv.setText(title);
        descriptionTv.setText(description);
    }
}
