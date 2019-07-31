package com.android.messaging.ui.welcome;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

public class NotificationGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_guide);
        TextView skipButton = findViewById(R.id.skip_button);
        skipButton.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                getResources().getColor(R.color.notification_guide_skip_button_color),
                Dimensions.pxFromDp(14), false, true));

        TextView confirmButton = findViewById(R.id.notification_guide_confirm_button);
        confirmButton.setBackground(BackgroundDrawables.
                createBackgroundDrawable(getResources().getColor(R.color.notification_guide_confirm_button_color),
                        Dimensions.pxFromDp(6.7f), true));

        TextView guideTitle = findViewById(R.id.notification_guide_title);
        TextView guideContent = findViewById(R.id.notification_guide_content);
        ImageView guideBackground = findViewById(R.id.notification_guide_background);

        Interpolator interpolator = PathInterpolatorCompat.create(0.17f, 0.17f, 0.6f, 1);

        ObjectAnimator titleAlphaAnimator = ObjectAnimator.ofFloat(guideTitle, "alpha", 0, 1);
        titleAlphaAnimator.setDuration(320);
        titleAlphaAnimator.setStartDelay(160);

        ObjectAnimator titleTranslationAnimator = ObjectAnimator.ofFloat(guideTitle, "translationY", Dimensions.pxFromDp(-47), 0);
        titleTranslationAnimator.setDuration(400);
        titleTranslationAnimator.setStartDelay(160);
        titleTranslationAnimator.setInterpolator(interpolator);

        ObjectAnimator contentAlphaAnimator = ObjectAnimator.ofFloat(guideContent, "alpha", 0, 1);
        contentAlphaAnimator.setDuration(320);
        contentAlphaAnimator.setStartDelay(120);

        ObjectAnimator contentTranslationAnimator = ObjectAnimator.ofFloat(guideContent, "translationY", Dimensions.pxFromDp(-47), 0);
        contentTranslationAnimator.setDuration(400);
        contentTranslationAnimator.setStartDelay(120);
        contentTranslationAnimator.setInterpolator(interpolator);

        titleAlphaAnimator.start();
        titleTranslationAnimator.start();
        contentAlphaAnimator.start();
        contentTranslationAnimator.start();
        Threads.postOnMainThreadDelayed(() -> {
            guideBackground.setVisibility(View.VISIBLE);
            skipButton.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
        }, 160);
    }
}
