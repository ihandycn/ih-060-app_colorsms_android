package com.android.messaging.ui;

import android.os.Bundle;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Dimensions;

public class DragHotSeatActivity extends HSAppCompatActivity {

    public static final String SHOW_DRAG_HOTSEAT = "show_drag_hotseat";
    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_hotseat);

        findViewById(R.id.drag_hotseat_btn).setOnClickListener(v -> finish());
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
