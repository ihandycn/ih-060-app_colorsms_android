package com.android.messaging.font;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class ChooseFontItemView extends FrameLayout {
    interface IFontDownloadListener {
        void onFontDownloadSuccess(ChooseFontItemView view);
    }

    private String mFontFamily;
    private RadioButton mRadioBtn;
    private boolean isSelected;
    private boolean mIsPreSelected;
    private FontInfo mFontInfo;
    private View mDownloadBtn;
    private ImageView mPreviewView;
    private ProgressBar mProgressBar;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;
    private boolean mIsFontDownloading;
    private IFontDownloadListener mListener;

    public ChooseFontItemView(Context context, FontInfo fontInfo) {
        super(context);
        mFontInfo = fontInfo;
        initView();
    }

    public ChooseFontItemView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ChooseFontItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChooseFontItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.font_chooser_radio_item_view, this);
        mRadioBtn = root.findViewById(R.id.font_choose_item_radio_button);
        mDownloadBtn = root.findViewById(R.id.font_choose_item_download);
        mPreviewView = root.findViewById(R.id.font_choose_item_font_name_iv);
        mProgressBar = root.findViewById(R.id.font_choose_item_progress_bar);
        TextView previewTextView = root.findViewById(R.id.font_choose_item_font_name_tv);

        ColorStateList colorStateList = ColorStateList.valueOf(0xffcdd2de);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mProgressBar.setIndeterminateTintList(colorStateList);
        }
        mRadioBtn.setClickable(false);
        mFontFamily = mFontInfo.getFontName();

        if (mFontInfo != null) {
            if (FontUtils.MESSAGE_FONT_SYSTEM.equals(mFontInfo.getFontName())) {
                previewTextView.setText(R.string.system_font_display_name);
            } else {
                mPreviewView.setImageDrawable(FontDownloadManager.getDrawableByName(mFontFamily));
                if (!mFontInfo.isFontDownloaded()) {
                    mDownloadBtn.setVisibility(VISIBLE);
                    mPreviewView.setImageAlpha((int) (0.5 * 256));
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            int radioHeight = mRadioBtn.getHeight();
            int radioWidth = mRadioBtn.getWidth();
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mRadioBtn.getLayoutParams();
            lp.setMarginStart(Dimensions.pxFromDp(24) - (radioWidth - radioHeight) / 2);
            mRadioBtn.setLayoutParams(lp);
        }
    }

    public void addFontDownloadedListener(IFontDownloadListener listener) {
        mListener = listener;
    }

    public void setSelected(boolean isSelected) {
        mRadioBtn.setChecked(isSelected);
        this.isSelected = isSelected;
    }

    public String getFontFamily() {
        return mFontFamily;
    }

    public boolean setPreSelect(boolean isSelected) {
        this.mIsPreSelected = isSelected;
        if (mIsPreSelected) {
            if (mFontInfo.isFontDownloaded()) {
                return true;
            } else {
                if (!mIsFontDownloading) {
                    downloadFont();
                }
            }
        }
        return false;
    }

    private void downloadFont() {
        mIsFontDownloading = true;
        mDownloadBtn.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
        FontDownloadManager.downloadFont(mFontInfo, new FontDownloadManager.FontDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                if (mChoreographer != null) {
                    mChoreographer.removeFrameCallback(mFrameCallback);
                }
                mPreviewView.setImageAlpha(255);
                mProgressBar.setVisibility(GONE);
                if (mIsPreSelected && mListener != null) {
                    mListener.onFontDownloadSuccess(ChooseFontItemView.this);
                }
                mIsFontDownloading = false;
            }

            @Override
            public void onDownloadFailed() {
                if (mChoreographer != null) {
                    mChoreographer.removeFrameCallback(mFrameCallback);
                }
                mDownloadBtn.setVisibility(VISIBLE);
                mProgressBar.setVisibility(GONE);
                mIsFontDownloading = false;
            }

            @Override
            public void onDownloadUpdate(float rate) {

            }
        });
        mFrameCallback = frameTimeNanos -> {
            if (mIsFontDownloading) {
                mProgressBar.setProgress((int) (System.currentTimeMillis() % 1000 / 10));
                mChoreographer.postFrameCallback(mFrameCallback);
            }
        };
        mChoreographer = Choreographer.getInstance();
        mChoreographer.postFrameCallback(mFrameCallback);
    }

    public void checkSettingFont(String fontName) {
        if (fontName.equals(mFontFamily)) {
            mRadioBtn.setChecked(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mChoreographer != null && mFrameCallback != null) {
            mChoreographer.removeFrameCallback(mFrameCallback);
            mIsFontDownloading = false;
        }
        FontDownloadManager.removeListener(mFontInfo.getFontName());
    }

    public boolean isSelected() {
        return isSelected;
    }
}
