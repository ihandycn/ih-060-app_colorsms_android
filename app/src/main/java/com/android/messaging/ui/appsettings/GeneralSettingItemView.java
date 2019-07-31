package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.Dimensions;

public class GeneralSettingItemView extends BaseItemView {
    public static final int SWITCH = 1;
    public static final int NORMAL = 3;
    public static final int WITH_TRIANGLE = 2;

    @IntDef({SWITCH, NORMAL, WITH_TRIANGLE})
    @interface SettingViewType {
    }

    private int mViewType;

    private Switch mSwitchView;
    private ImageView mTriangleView;

    private boolean mBlockSwitchAutoCheck = false;

    public GeneralSettingItemView(Context context) {
        this(context, null);
    }

    public GeneralSettingItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GeneralSettingItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);
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
                mTriangleView.setImageResource(R.drawable.setting_more_icon_new);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(Dimensions.pxFromDp(7));
                widgetFrame.addView(mTriangleView, params);
            }
        }

        if (mSwitchView != null) {
            ColorStateList colorStateList = new ColorStateList(new int[][]{
                    new int[]{android.R.attr.state_checked, android.R.attr.state_enabled},
                    new int[]{}
            }, new int[]{
                    PrimaryColors.getPrimaryColor(),
                    0xffc0c2cb
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSwitchView.getTrackDrawable().setTintList(colorStateList);
                mSwitchView.getThumbDrawable().setTintList(colorStateList);
            }
        }

        setOnClickListener(v -> {
            if (mSwitchView != null) {
                boolean isChecked = !mSwitchView.isChecked();
                if(!mBlockSwitchAutoCheck) {
                    mSwitchView.setChecked(isChecked);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    toggleSwitchViewColorFilter(isChecked);
                }
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
                if (mSwitchView != null) {
                    return;
                }
                final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
                if (widgetFrame != null) {
                    final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                    layoutInflater.inflate(R.layout.preference_switch_layout, widgetFrame);
                    mSwitchView = mRootView.findViewById(R.id.switch_widget);
                }

                if (mSwitchView != null) {
                    ColorStateList colorStateList = new ColorStateList(new int[][]{
                            new int[]{android.R.attr.state_checked, android.R.attr.state_enabled},
                            new int[]{}
                    }, new int[]{
                            PrimaryColors.getPrimaryColor(),
                            0xffc0c2cb
                    });

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mSwitchView.getTrackDrawable().setTintList(colorStateList);
                        mSwitchView.getThumbDrawable().setTintList(colorStateList);
                    }
                }
            } else if (type == 2) {
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
            } else if (type == NORMAL) {
                final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
                if (mSwitchView != null) {
                    widgetFrame.removeView(mSwitchView);
                }
                if (mTriangleView != null) {
                    widgetFrame.removeView(mTriangleView);
                }
            }
        }
    }

    protected void blockSwitchAutoCheck(){
        mBlockSwitchAutoCheck = true;
    }

    public boolean isChecked() {
        return mSwitchView != null && mSwitchView.isChecked();
    }

    public void setChecked(boolean isChecked) {
        if (mViewType == SWITCH && mSwitchView != null) {
            if (mSwitchView.isChecked() == isChecked) {
                return;
            }
            mSwitchView.setChecked(isChecked);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                toggleSwitchViewColorFilter(isChecked);
            }
        }
    }

    @Override
    public void setEnable(boolean clickable) {
        super.setEnabled(clickable);
        if (!clickable) {
            if (mSwitchView != null) {
                mSwitchView.setEnabled(false);
            }
        } else {
            if (mSwitchView != null) {
                mSwitchView.setEnabled(true);
            }
        }
    }

    private void toggleSwitchViewColorFilter(boolean isChecked) {
        if (isChecked) {
            mSwitchView.getThumbDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
            mSwitchView.getTrackDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
        } else {
            mSwitchView.getThumbDrawable().clearColorFilter();
            mSwitchView.getTrackDrawable().clearColorFilter();
        }
    }
}
