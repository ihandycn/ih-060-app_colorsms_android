package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.EmojiTextView;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;

public class SignatureItemView extends FrameLayout {
    public interface OnSettingItemClickListener {
        void onClick();
    }

    protected TextView mTitleView;
    private EmojiTextView mSummaryView;
    protected OnSettingItemClickListener mListener;

    protected View mRootView;

    public SignatureItemView(Context context) {
        this(context, null);
    }

    public SignatureItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignatureItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    protected void initView(Context context, AttributeSet attrs) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        mRootView = layoutInflater.inflate(R.layout.base_setting_item_layout, this);
        mTitleView = findViewById(R.id.title);
        mSummaryView = findViewById(R.id.summary);

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

        boolean hideLine = a.getBoolean(R.styleable.SettingItemView_hideDivideLine, false);
        if (hideLine) {
            findViewById(R.id.divide_line).setVisibility(GONE);
        }

        a.recycle();

        setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff,
                HSApplication.getContext().getResources().getColor(com.superapps.R.color.ripples_ripple_color),
                0, true, true));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onClick();
                }
            }
        });
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setSummary(String summary) {
        if (TextUtils.isEmpty(summary)) {
            mSummaryView.setVisibility(GONE);
        } else {
            if (mSummaryView.getVisibility() != VISIBLE) {
                mSummaryView.setVisibility(VISIBLE);
            }
            mSummaryView.setText(summary);
        }
    }

    public void setEnable(boolean clickable) {
        super.setEnabled(clickable);
        if (!clickable) {
            mTitleView.setTextColor(0xffacaeb5);
            mSummaryView.setTextColor(0xffacaeb5);
        } else {
            mTitleView.setTextColor(0xff222327);
            mSummaryView.setTextColor(0xff56575c);
        }
    }

    public void setOnItemClickListener(OnSettingItemClickListener listener) {
        mListener = listener;
    }

    public void hideDivideLine(boolean hideLine) {
        if (hideLine) {
            findViewById(R.id.divide_line).setVisibility(GONE);
        } else {
            findViewById(R.id.divide_line).setVisibility(VISIBLE);
        }
    }
}
