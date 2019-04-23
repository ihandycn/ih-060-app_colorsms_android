package com.android.messaging.ui.messagebox;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;

public class MessageBoxIndicatorView extends LinearLayout {

    public interface OnIndicatorClickListener {
        void onClickLeft();
        void onClickRight();
    }

    private AppCompatImageView mLeftIndicator;
    private TextView mTextIndicator;
    private AppCompatImageView mRightIndicator;

    private OnIndicatorClickListener mOnIndicatorClickListener;

    public MessageBoxIndicatorView(Context context) {
        super(context);
    }

    @SuppressLint("RestrictedApi")
    public MessageBoxIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.message_box_indicator_layout, this, true);

        mLeftIndicator = findViewById(R.id.left_indicator);
        mTextIndicator = findViewById(R.id.indicator_text);
        mRightIndicator = findViewById(R.id.right_indicator);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_pressed},
                new int[]{android.R.attr.state_pressed}
        };

        int[] colors = new int[]{
                Color.WHITE,
                getResources().getColor(R.color.white_40_transparent),
                Color.WHITE,
                getResources().getColor(R.color.white_40_transparent),

        };
        ColorStateList state = new ColorStateList(states, colors);
        mLeftIndicator.setSupportImageTintList(state);
        mRightIndicator.setSupportImageTintList(state);

        mLeftIndicator.setOnClickListener(v -> {
            if (mOnIndicatorClickListener != null) {
                mOnIndicatorClickListener.onClickLeft();
                BugleAnalytics.logEvent("SMS_PopUp_MultiUser_Next_Click");
            }

        });

        mRightIndicator.setOnClickListener(v -> {
            if (mOnIndicatorClickListener != null) {
                mOnIndicatorClickListener.onClickRight();
                BugleAnalytics.logEvent("SMS_PopUp_MultiUser_Next_Click");
            }
        });
        setVisibility(GONE);
    }

    void setOnIndicatorClickListener(OnIndicatorClickListener listener) {
        mOnIndicatorClickListener = listener;
    }


    void updateIndicator(int position, int totalCount) {
        mTextIndicator.setText(String.format(getResources().getString(R.string.message_box_indicator), position + 1, totalCount));

        if (position  == 0) {
            mLeftIndicator.setEnabled(false);
        } else {
            mLeftIndicator.setEnabled(true);
        }

        if (position == totalCount - 1) {
            mRightIndicator.setEnabled(false);
        } else {
            mRightIndicator.setEnabled(true);
        }

        if (totalCount == 1) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

}

