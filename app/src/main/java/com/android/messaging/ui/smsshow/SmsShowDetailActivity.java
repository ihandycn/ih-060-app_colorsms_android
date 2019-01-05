package com.android.messaging.ui.smsshow;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.smsshow.SmsShowUtils;
import com.android.messaging.ui.UIIntents;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class SmsShowDetailActivity extends HSAppCompatActivity {
    private Animatable mAnimatable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_show_detail_activity);

        TextView applyButton = findViewById(R.id.apply_button);
        LottieAnimationView loadingAnimView = findViewById(R.id.loading_lottie_animation);
        ImageView imageView = findViewById(R.id.sms_show_image);

        String url = getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_SMS_SHOW_URL);
        int id = getIntent().getIntExtra(UIIntents.UI_INTENT_EXTRA_SMS_SHOW_ID, 0);

        GlideApp.with(this).load(url).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                imageView.setImageDrawable(resource);
                if (resource instanceof Animatable) {
                    mAnimatable = ((Animatable) resource);
                    mAnimatable.start();
                }
                loadingAnimView.cancelAnimation();
                loadingAnimView.setVisibility(View.GONE);
                applyButton.setVisibility(View.VISIBLE);
                applyButton.animate().alpha(1f).setDuration(240L).start();
            }
        });

        applyButton.setOnClickListener(v -> {
            SmsShowUtils.setSmsShowAppliedId(id);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAnimatable != null && !mAnimatable.isRunning()) {
            mAnimatable.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAnimatable != null) {
            mAnimatable.stop();
        }
    }
}
