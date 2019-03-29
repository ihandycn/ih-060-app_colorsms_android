package com.android.messaging.ui.messagebox;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static com.android.messaging.ui.messagebox.MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX;

public class BoxActivity extends BaseActivity implements INotificationObserver, View.OnClickListener {

    @ColorInt
    private int mPrimaryColor;
    @ColorInt
    private int mPrimaryColorDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_activity);
        mPrimaryColor = PrimaryColors.getPrimaryColor();
        mPrimaryColorDark = PrimaryColors.getPrimaryColorDark();
        initActionBarSimulation();
        initQuickActions();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_call:
                break;
            case R.id.action_close:
                break;
            case R.id.action_delete:
                break;
            case R.id.action_unread:
                break;
            case R.id.action_open:
                break;
        }
    }

    private void initActionBarSimulation() {
        ImageView callActionImage = findViewById(R.id.action_call);
        callActionImage.setOnClickListener(this);
        callActionImage.setBackground(BackgroundDrawables.
                createBackgroundDrawable(mPrimaryColor, mPrimaryColorDark, Dimensions.pxFromDp(21), false, true));

        ImageView closeActionImage = findViewById(R.id.action_close);
        closeActionImage.setOnClickListener(this);
        closeActionImage.setBackground(BackgroundDrawables.
                createBackgroundDrawable(mPrimaryColor, mPrimaryColorDark, Dimensions.pxFromDp(21), false, true));
        findViewById(R.id.action_bar_simulation).getBackground().setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);
    }

    private void initQuickActions() {
        TextView actionDelete = findViewById(R.id.action_delete);
        TextView actionUnread = findViewById(R.id.action_unread);
        TextView actionOpen = findViewById(R.id.action_open);

        actionDelete.setOnClickListener(this);
        actionUnread.setOnClickListener(this);
        actionOpen.setOnClickListener(this);

        float radius = getResources().getDimension(R.dimen.message_box_background_radius);
        int rippleColor = getResources().getColor(com.superapps.R.color.ripples_ripple_color);
        actionDelete.setBackground(
                BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, rippleColor, 0f, 0f, 0, radius,
                        false, true));
        actionUnread.setBackground(
                BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, rippleColor, 0f, 0f, 0, 0f,
                        false, true));
        actionOpen.setBackground(
                BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, rippleColor, 0f, 0f, radius, 0,
                        false, true));
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_FINISH_MESSAGE_BOX.equals(s)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugleAnalytics.logEvent("SMS_PopUp_Close", true);
        HSGlobalNotificationCenter.removeObserver(this);
    }
}
