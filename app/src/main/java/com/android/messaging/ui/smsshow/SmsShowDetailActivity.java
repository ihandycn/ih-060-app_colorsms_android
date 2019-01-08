package com.android.messaging.ui.smsshow;

import android.graphics.Bitmap;
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
import com.bumptech.glide.integration.webp.decoder.WebpDrawable;
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;

public class SmsShowDetailActivity extends HSAppCompatActivity {
    private Animatable mSmsShowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_show_detail_activity);
        TextView applyButton = findViewById(R.id.apply_button);
        ImageView imageView = findViewById(R.id.sms_show_image);

        String url = getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_SMS_SHOW_URL);
        int id = getIntent().getIntExtra(UIIntents.UI_INTENT_EXTRA_SMS_SHOW_ID, 0);

        Transformation<Bitmap> centerInside = new CenterInside();
        GlideApp.with(this)
                .load(url)
                .optionalTransform(centerInside)
                .optionalTransform(WebpDrawable.class, new WebpDrawableTransformation(centerInside))
                .into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                imageView.setImageDrawable(resource);
                if (resource instanceof Animatable) {
                    mSmsShowView = ((Animatable) resource);
                    mSmsShowView.start();
                }
                applyButton.setVisibility(View.VISIBLE);
                applyButton.animate().alpha(1f).setDuration(240L).start();
            }
        });

        applyButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color), 0, true));
        applyButton.setOnClickListener(v -> {
            SmsShowUtils.setSmsShowAppliedId(id);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSmsShowView != null && !mSmsShowView.isRunning()) {
            mSmsShowView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSmsShowView != null) {
            mSmsShowView.stop();
        }
    }
}
