package com.android.messaging.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class UserSurveyActivity extends HSAppCompatActivity implements View.OnClickListener {

    public static final String SHOW_USER_SURVEY = "show_user_survey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_survey);
        BugleAnalytics.logEvent("Customize_ThemeColor_FeedbackAlert_Show");
        findViewById(R.id.linearLayout).setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.white), Dimensions.pxFromDp(8), false));
        View close = findViewById(R.id.iv_close);
        close.setOnClickListener(this);
        close.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.close_icon), Dimensions.pxFromDp(15), true));
        View dislike = findViewById(R.id.iv_dislike);
        dislike.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.dislike_icon), Dimensions.pxFromDp(26), true));
        dislike.setOnClickListener(this);
        View like = findViewById(R.id.iv_like);
        like.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.like_icon), Dimensions.pxFromDp(26), true));
        like.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_dislike:
                BugleAnalytics.logEvent("Customize_ThemeColor_FeedbackAlert_Click", "btn", "dislike");
                break;
            case R.id.iv_like:
                BugleAnalytics.logEvent("Customize_ThemeColor_FeedbackAlert_Click", "btn", "like");
                break;
            default:
                break;
        }
        finish();
        overridePendingTransition(0, 0);
    }
}
