package com.android.messaging.font;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.ViewUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChooseFontDialog {

    private static final String TAG = "ChooseFontDialog";
    private Activity mActivity;
    private WeakReference<ChangeFontActivity> mWeakActivityReference;
    private Dialog mDialog;
    private View mRootView;
    private String mFontFamily;
    private boolean mDialogDismiss;
    private String mDefaultFamily;
    private List<ChooseFontItemView> mItemViewList = new ArrayList<>();

    ChooseFontDialog(Context activity) {
        this.mActivity = (Activity) activity;
        mWeakActivityReference = new WeakReference<>((ChangeFontActivity) activity);
        mFontFamily = mDefaultFamily = FontStyleManager.getInstance().getFontFamily();
    }

    private void configDialog(Dialog builder) {
        mDialog = builder;
        builder.setContentView(mRootView);
        builder.setCancelable(true);
        builder.setOnDismissListener(dialog -> {
            mDialogDismiss = true;
            HSLog.d(TAG, "onDismiss");
            if (!mDefaultFamily.equals(mFontFamily)) {
                HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
                FontUtils.onFontTypefaceChanged();
            }
        });
        builder.setOnCancelListener(dialog -> {
            HSLog.d(TAG, "onCancel");
            dismissSafely();
        });
        mDialog.setCanceledOnTouchOutside(true);

        Window window = mDialog.getWindow();

        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(0x00000000);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

    }

    @SuppressLint("InflateParams")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(mActivity);
        Dialog builder = new Dialog(mActivity, R.style.BaseDialogTheme);

        mRootView = mLayoutInflater.inflate(R.layout.dialog_no_btn, null);
        mRootView.findViewById(R.id.cancel_btn).setVisibility(View.GONE);
        mRootView.setOnClickListener(v -> dismissSafely());

        configDialog(builder);
        FrameLayout mContentView = ViewUtils.findViewById(mRootView, R.id.content_view);
        mContentView.addView(createContentView(mLayoutInflater, mContentView));

        //set dialog color and corner
        View mDialogContent = mRootView.findViewById(R.id.linearLayout);
        assert mDialogContent != null;
        mDialogContent.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                mActivity.getResources().getDimension(R.dimen.dialog_corner_radius), false));
    }

    public final boolean show() {
        if (mActivity == null || mActivity.isFinishing()) {
            return false;
        }
        init();
        try {
            if (mDialog.getWindow() != null) {
                mDialog.getWindow().getAttributes().windowAnimations = Animation.ABSOLUTE;
            }
            mDialog.show();
        } catch (Exception e) {
            return false;
        }

        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return true;
    }

    private View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v = inflater.inflate(R.layout.dialog_content_choice, root, false);
        v.setClickable(true);
        configRadioGroup(v);
        TextView mTitleTv = ViewUtils.findViewById(v, R.id.dialog_title);
        mTitleTv.setText(R.string.setting_text_font);
        return v;
    }

    private void configRadioGroup(View view) {
        View v = mRootView.findViewById(R.id.content_view);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.leftMargin = 0;
        lp.rightMargin = 0;
        lp.bottomMargin = Dimensions.pxFromDp(32);
        v.setLayoutParams(lp);

        List<FontInfo> fontList = new ArrayList<>();
        List<FontInfo> configList = FontDownloadManager.getFontList();

        for (FontInfo info : configList) {
            if (info.isLocalFont()) {
                if (info.getFontName().equals(FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE)) {
                    fontList.add(0, info);
                } else {
                    fontList.add(info);
                }
            }
        }
        fontList.add(1, new FontInfo(FontUtils.MESSAGE_FONT_SYSTEM, new ArrayList<>(), true));

        for (FontInfo info : configList) {
            if (!info.isLocalFont()) {
                fontList.add(info);
            }
        }

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        0xffa5abb1
                        , PrimaryColors.getPrimaryColor(),
                }
        );

        ChooseFontItemView.IFontDownloadListener fontDownloadListener = view12 -> {
            for (ChooseFontItemView itemView : mItemViewList) {
                itemView.setPreSelect(false);
                if (itemView != view12) {
                    itemView.setSelected(false);
                } else {
                    itemView.setSelected(true);
                    if (!mDialogDismiss) {
                        onFontChanged(itemView.getFontFamily());
                    }
                }
            }
        };

        int selectIndex = 0;
        //local
        LinearLayout scrollContent = view.findViewById(R.id.scroll_content);
        for (int i = 0; i < fontList.size(); i++) {
            ChooseFontItemView item = new ChooseFontItemView(mActivity, fontList.get(i));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item.setLayoutParams(layoutParams);

            AppCompatRadioButton radioButton = item.findViewById(R.id.font_choose_item_radio_button);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radioButton.getCompoundDrawablesRelative()[0].setTintList(colorStateList);
            }

            item.checkSettingFont(mFontFamily);
            if (fontList.get(i).getFontName().equals(mFontFamily)) {
                selectIndex = i;
            }
            item.setOnClickListener(view1 -> {
                for (ChooseFontItemView button : mItemViewList) {
                    if (button != item) {
                        button.setPreSelect(false);
                    }
                }

                if (item.setPreSelect(true)) {
                    //font downloaded, change choose state
                    for (ChooseFontItemView button : mItemViewList) {
                        if (button != item) {
                            button.setSelected(false);
                        }
                    }
                    item.setSelected(true);
                    onFontChanged(item.getFontFamily());
                    new Handler().postDelayed(this::dismissSafely, 200);
                }
            });
            item.addFontDownloadedListener(fontDownloadListener);
            mItemViewList.add(item);
            scrollContent.addView(item);
        }

        final int scrollerPosition;
        //view height 38dp; list height 300dp;
        int viewHeight = 38;
        int scrollerHeight = 300;
        int totalHeight = viewHeight * fontList.size();
        int expectedPosition = selectIndex * viewHeight + viewHeight / 2 - scrollerHeight / 2;
        if (expectedPosition < 0) {
            scrollerPosition = 0;
        } else if (expectedPosition > totalHeight - scrollerHeight) {
            scrollerPosition = totalHeight - scrollerHeight;
        } else {
            scrollerPosition = expectedPosition;
        }

        mDialog.setOnShowListener(dialog -> {
            HSLog.d(TAG, "OnShow");
            view.findViewById(R.id.dialog_scroller_view).scrollTo(0, Dimensions.pxFromDp(scrollerPosition));
        });
    }

    private void onFontChanged(String font) {
        mFontFamily = font;
        FontStyleManager.getInstance().setFontFamily(font);
        ChangeFontActivity activity = mWeakActivityReference.get();
        if (activity != null && !activity.isDestroyed()) {
            activity.onFontChange();
        }
        BugleAnalytics.logEvent("Customize_TextFont_Change", true, "font", font);
        BugleFirebaseAnalytics.logEvent("Customize_TextFont_Change", "font", font);
    }

    private void dismissSafely() {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
