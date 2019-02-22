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
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.ViewUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.FontManagerImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChooseFontDialog {

    private static final String TAG = "ChooseFontDialog";
    private BuglePrefs mPrefs = BuglePrefs.getApplicationPrefs();
    private Activity activity;
    private Dialog mDialog;
    private View mRootView;
    private FrameLayout mContentView;
    private View dialogContent;
    private View dialogRoot;
    private String mFontFamily;
    private IFontChangeListener mListener;

    public ChooseFontDialog(Context activity, IFontChangeListener listener) {
        this.activity = (Activity) activity;
        mFontFamily = mPrefs.getString("font_family", "Default");;
        mListener = listener;
    }

    private void configDialog(Dialog builder) {
        mDialog = builder;
        builder.setContentView(mRootView);
        builder.setCancelable(true);
        builder.setOnDismissListener(dialog -> {
            HSLog.d(TAG, "onDismiss");
        });
        builder.setOnCancelListener(dialog -> {
            HSLog.d(TAG, "onCancel");
            dismissSafely();
        });
        mDialog.setOnShowListener(dialog -> HSLog.d(TAG, "OnShow"));
        mDialog.setCanceledOnTouchOutside(false);
    }

    @SuppressLint("InflateParams")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(activity);
        Dialog builder = new Dialog(activity, R.style.NewDialogTheme);

        mRootView = mLayoutInflater.inflate(R.layout.dialog_no_btn, null);

        configDialog(builder);
        mContentView = ViewUtils.findViewById(mRootView, R.id.content_view);
        mContentView.addView(createContentView(mLayoutInflater, mContentView));

        //set dialog color and corner
        dialogContent = mRootView.findViewById(R.id.linearLayout);
        assert dialogContent != null;
        dialogContent.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                activity.getResources().getDimension(R.dimen.dialog_corner_radius), false));
        dialogRoot = mRootView.findViewById(R.id.dialog_root_view);
    }

    public final boolean show() {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        init();
        mFontFamily = mPrefs.getString("font_family", "Default");
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
        View header = view.findViewById(R.id.listview_header);
        View footer = view.findViewById(R.id.listview_footer);
        View v = mRootView.findViewById(R.id.content_view);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.leftMargin = 0;
        lp.rightMargin = 0;
        lp.bottomMargin = Dimensions.pxFromDp(32);
        v.setLayoutParams(lp);
        BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

        ListView selectGroup = view.findViewById(R.id.select_radio_group);
        String[] choicesArr = {"Default", "System", "Nunito Sans", "Muli", "Cinzel", "Coiny", "Ubuntu", "Rubik"};
        List<String> choicesList = Arrays.asList(choicesArr);
        int checkedIndex = choicesList.indexOf(mFontFamily);
        if (checkedIndex == -1) {
            checkedIndex = 0;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (CharSequence charSequence : choicesList) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", charSequence);
            list.add(map);
        }
        RadioButtonAdapter adapter = new RadioButtonAdapter(activity, list, checkedIndex);
        if (list.size() > 6) {
            lp = (LinearLayout.LayoutParams) selectGroup.getLayoutParams();
            lp.height = Dimensions.pxFromDp(315);
            selectGroup.setLayoutParams(lp);
            footer.setVisibility(View.VISIBLE);
            selectGroup.setOnScrollListener(new AbsListView.OnScrollListener() {
                private int currentVisibleItemCount;
                private int currentScrollState;
                private int currentFirstVisibleItem;
                private int totalItem;

                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    this.currentScrollState = i;
                    this.isScrollCompleted();
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                    this.currentFirstVisibleItem = i;
                    this.currentVisibleItemCount = i1;
                    this.totalItem = i2;
                }

                private void isScrollCompleted() {
                    if (this.currentScrollState == SCROLL_STATE_IDLE) {
                        if (currentFirstVisibleItem == 0) {
                            // scroll to start
                            footer.setVisibility(View.VISIBLE);
                            header.setVisibility(View.INVISIBLE);
                        } else if (totalItem - currentFirstVisibleItem == currentVisibleItemCount) {
                            // scroll to end
                            footer.setVisibility(View.INVISIBLE);
                            header.setVisibility(View.VISIBLE);
                        } else {
                            // scroll between
                            footer.setVisibility(View.VISIBLE);
                            header.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
        selectGroup.setAdapter(adapter);
        int finalCheckedIndex = checkedIndex;
        selectGroup.setOnItemClickListener((parent, view1, position, id) -> {
            // item click
            View t = parent.getChildAt(finalCheckedIndex);
            RadioButton r = null;
            if (t != null) {
                r = t.findViewById(R.id.dialog_select_btn);
            }
            RadioButton radioButton = view1.findViewById(R.id.dialog_select_btn);
            if (position != finalCheckedIndex) {
                if (r != null) {
                    r.setChecked(false);
                }
                radioButton.setChecked(true);
                String fontFamily = choicesArr[position];
                mFontFamily = fontFamily;
                if (!fontFamily.equals("Default") && !fontFamily.equals("System")) {
                    FontManagerImpl.getInstance().changeTypeFaced(fontFamily, mListener);
                }
                prefs.putString("font_family", fontFamily);
                new Handler().postDelayed(this::dismiss, 1);
            }

        });
    }

    public final void dismiss() {
        if (!activity.isFinishing()) {
            if (dialogRoot != null && dialogContent != null) {
                dialogRoot.animate().setDuration(320).alpha(0).start();
                dialogContent.animate().scaleX(0.97f).scaleY(0.97f).alpha(0).setDuration(280).start();
                new Handler().postDelayed(() -> {
                    if (mDialog.isShowing()) {
                        mListener.onLoadSuccess(100);
                        dismissSafely();
                    }
                }, 320);
            } else {
                dismissSafely();
            }
        }
    }

    private void dismissSafely() {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
