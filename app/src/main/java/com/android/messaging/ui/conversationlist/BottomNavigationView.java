package com.android.messaging.ui.conversationlist;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;

import java.util.ArrayList;

public class BottomNavigationView extends LinearLayout {
    public static final int POSITION_MESSAGING = 0;
    public static final int POSITION_SMS_SHOW = 1;
    public static final int POSITION_EMOJI = 2;

    interface OnItemSelectedListener {
        void onSelected(int position);
    }

    private int mCurrentPosition = -1;
    private OnItemSelectedListener mOnItemSelectedListener;
    private ArrayList<ImageView> mIcons = new ArrayList<>(3);
    private ArrayList<TextView> mTitles = new ArrayList<>(3);

    public BottomNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSelectedPosition(int position) {
        onSelected(position);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        findViewById(R.id.item_messaging).setOnClickListener(v -> onSelected(POSITION_MESSAGING));


        findViewById(R.id.item_smsshow).setOnClickListener(v -> onSelected(POSITION_SMS_SHOW));

        findViewById(R.id.item_emoji).setOnClickListener(v -> {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", true, "type", "tab");
            onSelected(POSITION_EMOJI);
        });

        mIcons.add(findViewById(R.id.item_messaging_icon));
        mIcons.add(findViewById(R.id.item_smsshow_icon));
        mIcons.add(findViewById(R.id.item_emoji_icon));

        mTitles.add(findViewById(R.id.item_messaging_text));
        mTitles.add(findViewById(R.id.item_smsshow_text));
        mTitles.add(findViewById(R.id.item_emoji_text));
    }

    private void onSelected(int position) {
        if (mCurrentPosition == position) {
            return;
        }
        if (mCurrentPosition >= 0) {
            mIcons.get(mCurrentPosition).setSelected(false);
            mTitles.get(mCurrentPosition).setSelected(false);
        }
        mIcons.get(position).setSelected(true);
        mTitles.get(position).setSelected(true);

        mCurrentPosition = position;
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onSelected(position);
        }
    }
}
