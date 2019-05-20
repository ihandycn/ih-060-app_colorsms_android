package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;


public class ThemePreviewItemView extends ConstraintLayout {
    private ImageView mThemePreviewImg;
    private TextView mThemeNameTv;
    private ImageView mThemeDownload;
    private TextView mThemeDownloadTimes;
    private LottieAnimationView mDownloadSuccessLottie;
    private View mDownloadingView;

    public ThemePreviewItemView(Context context) {
        super(context);
    }

    public ThemePreviewItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemePreviewItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void initView() {
        mThemePreviewImg = findViewById(R.id.theme_thumbnail);
        mThemeNameTv = findViewById(R.id.theme_name);
        mThemeDownloadTimes = findViewById(R.id.theme_downloaded_times);
    }

    public void setThemeData(ThemeInfo info) {
        if (info.isDownloaded()) {

        }
        mThemePreviewImg.setImageDrawable(ThemeUtils.getDrawableFromUrl(info.previewUrl));
        mThemeNameTv.setText(info.name);
        mThemeDownloadTimes.setText("kjblkbl");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }
}
