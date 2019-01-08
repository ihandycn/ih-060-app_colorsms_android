package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.messaging.R;

public class SettingItemView extends FrameLayout {
    public static final int SWITCH = 1;
    public static final int NORMAL = 3;
    public static final int WITH_TRIANGLE = 2;

    @IntDef({SWITCH, NORMAL, WITH_TRIANGLE})
    @interface SettingViewType {
    }

    public interface OnSettingItemClickListener {
        void onClick();
    }

    private int mViewType;

    private TextView mTitleView, mSummaryView;
    private ImageView mIconView;
    private Switch mSwitchView;
    private ImageView mTriangleView;
    private OnSettingItemClickListener mListener;

    private View mRootView;

    public SettingItemView(Context context) {
        this(context, null);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        mRootView = layoutInflater.inflate(R.layout.setting_item_layout, this);
        mTitleView = findViewById(R.id.title);
        mSummaryView = findViewById(R.id.summary);
        mIconView = findViewById(R.id.icon);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);

        int titleRes = a.getResourceId(R.styleable.SettingItemView_title, 0);
        if (titleRes != 0) {
            mTitleView.setText(getResources().getString(titleRes));
        }

        int summaryRes = a.getResourceId(R.styleable.SettingItemView_summary, 0);
        if (summaryRes != 0) {
            mSummaryView.setText(getResources().getString(summaryRes));
        } else {
            mSummaryView.setVisibility(GONE);
        }

        int iconRes = a.getResourceId(R.styleable.SettingItemView_icon, 0);
        if (iconRes != 0) {
            mIconView.setImageResource(iconRes);
        } else {
            mIconView.setVisibility(GONE);
        }

        int type = a.getInt(R.styleable.SettingItemView_type, 0);
        a.recycle();
        if (mViewType > 0) {
            type = mViewType;
        }
        if (type == 1) {
            mViewType = SWITCH;
            final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
            if (widgetFrame != null) {
                layoutInflater.inflate(R.layout.preference_switch_layout, widgetFrame);
                mSwitchView = mRootView.findViewById(R.id.switch_widget);
            }
        } else if (type == 2) {
            mViewType = WITH_TRIANGLE;
            final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
            if (widgetFrame != null) {
                mTriangleView = new ImageView(getContext());
                mTriangleView.setImageResource(R.drawable.more_icon_settings);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                widgetFrame.addView(mTriangleView, params);
            }
        }

        setOnClickListener(v -> {
            if (mSwitchView != null) {
                mSwitchView.setChecked(!mSwitchView.isChecked());
            }

            if (mListener != null) {
                mListener.onClick();
            }
        });
    }

    public void setViewType(@SettingViewType int type) {
        mViewType = type;
        if (mRootView != null) {
            if (type == 1) {
                mViewType = SWITCH;
                if (mSwitchView != null) {
                    return;
                }
                final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
                if (widgetFrame != null) {
                    final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                    layoutInflater.inflate(R.layout.preference_switch_layout, widgetFrame);
                    mSwitchView = mRootView.findViewById(R.id.switch_widget);
                }
            } else if (type == 2) {
                mViewType = WITH_TRIANGLE;
                if (mTriangleView != null) {
                    return;
                }
                final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
                if (widgetFrame != null) {
                    mTriangleView = new ImageView(getContext());
                    mTriangleView.setImageResource(R.drawable.more_icon_settings);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    widgetFrame.addView(mTriangleView, params);
                }
            }
        }
    }

    public boolean isChecked() {
        return mSwitchView != null && mSwitchView.isChecked();
    }

    public void setChecked(boolean isChecked) {
        if (mViewType == SWITCH && mSwitchView != null) {
            mSwitchView.setChecked(isChecked);
        }
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setSummary(String summary) {
        if (mSummaryView.getVisibility() != VISIBLE) {
            mSummaryView.setVisibility(VISIBLE);
        }
        mSummaryView.setText(summary);
    }

    public void setIcon(@DrawableRes int drawableRes) {
        mIconView.setImageResource(drawableRes);
    }

    public void setEnable(boolean clickable) {
        super.setEnabled(clickable);
        if (!clickable) {
            if (mSwitchView != null) {
                mSwitchView.setEnabled(false);
            }
            mTitleView.setTextColor(0xffacaeb5);
            mSummaryView.setTextColor(0xffacaeb5);
        } else {
            if (mSwitchView != null) {
                mSwitchView.setEnabled(true);
            }
            mTitleView.setTextColor(0xff222327);
            mSummaryView.setTextColor(0xff56575c);
        }
    }

    public void setOnItemClickListener(OnSettingItemClickListener listener) {
        mListener = listener;
    }
}
