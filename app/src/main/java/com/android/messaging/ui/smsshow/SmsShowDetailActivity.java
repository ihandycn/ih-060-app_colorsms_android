package com.android.messaging.ui.smsshow;

import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;

public class SmsShowDetailActivity extends HSAppCompatActivity {

    private Animatable mSmsShowView;
    private TextView mApplyTextView;
    private ViewGroup mApplyBtnContainer;
    private ImageView mCheckMark;

    private ProgressBar mProgressBar;
    private FrameLayout mProgressBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_show_detail_activity);

        mApplyTextView = findViewById(R.id.apply_text);
        mApplyBtnContainer = findViewById(R.id.apply_button_container);
        mCheckMark = findViewById(R.id.apply_checkmark);
        mProgressBar = findViewById(R.id.sms_show_progress_bar);
        mProgressBarContainer = findViewById(R.id.sms_show_progress_bar_container);

        loadSmsShow();
        configApplyButton();
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
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

    private void loadSmsShow() {
        String url = getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_SMS_SHOW_URL);
        ImageView imageView = findViewById(R.id.sms_show_image);
        ImageView messageBox = findViewById(R.id.message_box);
        Transformation<Bitmap> centerInside = new CenterInside();
        ProgressManager.getInstance().addResponseListener(url, new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                mProgressBar.setProgress(progressInfo.getPercent());
                mProgressBarContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(long id, Exception e) {

            }
        });
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
                        mProgressBarContainer.animate().alpha(0f).setDuration(120L).start();
                        showMessageBoxView(messageBox);
                        mApplyBtnContainer.setVisibility(View.VISIBLE);
                        mApplyBtnContainer.animate().alpha(1f).setDuration(240L).start();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Toasts.showToast(R.string.sms_network_error);
                    }
                });

    }

    private void configApplyButton() {
        int id = getIntent().getIntExtra(UIIntents.UI_INTENT_EXTRA_SMS_SHOW_ID, 0);
        boolean applied = SmsShowUtils.getSmsShowAppliedId() == id;

        if (applied) {
            mApplyBtnContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.sms_show_detail_apply_button_applied_bg_color), Dimensions.pxFromDp(29), true));
            mApplyTextView.setTextColor(getResources().getColor(R.color.sms_show_detail_apply_button_applied_text_color));
            mApplyTextView.setText(R.string.sms_show_current_theme);
            mApplyBtnContainer.setOnClickListener(null);
            mCheckMark.setVisibility(View.VISIBLE);
            mProgressBarContainer.setVisibility(View.GONE);
        } else {
            mApplyBtnContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    getResources().getColor(R.color.sms_show_detail_apply_button_bg_color), Dimensions.pxFromDp(29), true));
            mApplyTextView.setTextColor(getResources().getColor(R.color.sms_show_detail_apply_button_text_color));
            mApplyTextView.setText(R.string.sms_show_apply);
            mCheckMark.setVisibility(View.GONE);
            mApplyBtnContainer.setOnClickListener(v -> {
                SmsShowUtils.setSmsShowAppliedId(id);
                configApplyButton();
            });

        }
    }

    private void showMessageBoxView(ImageView messageBox) {
        messageBox.animate().alpha(1f).setDuration(560L);
        messageBox.animate().translationY(0f).setDuration(560L);
    }

}
