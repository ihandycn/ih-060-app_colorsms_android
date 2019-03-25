package com.android.messaging.ui.appsettings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
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
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ViewUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.font.FontStyleManager;
import com.superapps.font.FontUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseFontDialog implements View.OnClickListener{

    private static final String[] sSupportGoogleFonts = {
            FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE,
            "Krub", "Mali", "ExpletusSans", "SourceSerifPro"
    };

    private static final String TAG = "ChooseFontDialog";
    private Activity mActivity;
    private WeakReference<ChangeFontActivity> mWeakActivityReference;
    private Dialog mDialog;
    private View mRootView;
    private String mFontFamily;
    private List<ChooseFontItem> mItemViewList = new ArrayList<>();

    ChooseFontDialog(Context activity) {
        this.mActivity = (Activity) activity;
        mWeakActivityReference = new WeakReference<>((ChangeFontActivity)activity);
        mFontFamily = FontStyleManager.getInstance().getFontFamily();
    }

    private void configDialog(Dialog builder) {
        mDialog = builder;
        builder.setContentView(mRootView);
        builder.setCancelable(true);
        builder.setOnDismissListener(dialog -> HSLog.d(TAG, "onDismiss"));
        builder.setOnCancelListener(dialog -> {
            HSLog.d(TAG, "onCancel");
            dismissSafely();
        });
        mDialog.setOnShowListener(dialog -> HSLog.d(TAG, "OnShow"));
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

        List<String> choicesList = Arrays.asList(sSupportGoogleFonts);
        int checkedIndex = choicesList.indexOf(mFontFamily);
        if (checkedIndex == -1) {
            checkedIndex = 0;
        }

        LinearLayout scrollContent = view.findViewById(R.id.scroll_content);
        for (int i = 0; i < choicesList.size(); i++) {
            ChooseFontItem item = (ChooseFontItem) LayoutInflater.from(mActivity).inflate(R.layout.new_dialog_select_item, scrollContent, false);
            item.setFontFamily(choicesList.get(i));
            if (checkedIndex == i) {
                item.setSelected(true);
            }
            item.loadFont();
            mItemViewList.add(item);
            item.setOnClickListener(this);

            scrollContent.addView(item);
        }
    }

    private void dismissSafely() {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
       for (int i = 0 ; i < mItemViewList.size(); i ++) {
           ChooseFontItem view = mItemViewList.get(i);
           if (v == view) {
               String fontFamily = sSupportGoogleFonts[i];
               mFontFamily = fontFamily;

               view.setSelected(true);
               view.refreshRadioStatus();

               FontStyleManager.getInstance().setFontFamily(fontFamily);
               ChangeFontActivity activity = mWeakActivityReference.get();
               if(activity != null && !activity.isDestroyed()) {
                   activity.onFontChange();
               }
               new Handler().postDelayed(this::dismissSafely, 1);
               BugleAnalytics.logEvent("Customize_TextFont_Change", true, "font", fontFamily);
               HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
           } else {
               view.setSelected(false);
               view.refreshRadioStatus();
           }
       }
    }
}
