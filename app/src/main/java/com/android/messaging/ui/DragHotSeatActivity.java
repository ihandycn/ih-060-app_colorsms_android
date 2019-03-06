package com.android.messaging.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.view.TypefacedTextView;

public class DragHotSeatActivity extends HSAppCompatActivity {

    public static final String SHOW_DRAG_HOTSEAT = "show_drag_hotseat";
    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_hotseat);
        BugleAnalytics.logEvent("SMS_DockGuide_Show", true);
        TypefacedTextView gotIt = findViewById(R.id.drag_hotseat_btn);
        gotIt.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color), getResources().getDimensionPixelSize(R.dimen.dialog_btn_corner_radius), true));
        gotIt.setOnClickListener(v -> {
            finish();
            BugleAnalytics.logEvent("SMS_DockGuide_BtnClick", true);

        });
        lottieAnimationView = findViewById(R.id.lottie_view);
        ViewGroup.LayoutParams layoutParams = lottieAnimationView.getLayoutParams();
        layoutParams.width = (int) (Dimensions.getPhoneWidth(this) * 0.82);
        layoutParams.height = layoutParams.width * 15 / 22;
        lottieAnimationView.setLayoutParams(layoutParams);
        lottieAnimationView.setImageAssetsFolder("lottie/drag_hotseat_images/");
        lottieAnimationView.setAnimation("lottie/drag_hotseat.json");
        lottieAnimationView.loop(true);
        lottieAnimationView.playAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (lottieAnimationView != null) {
            lottieAnimationView.cancelAnimation();
        }
    }
}
