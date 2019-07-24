package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class SettingEmojiSkinItemView extends BaseItemView {
    public SettingEmojiSkinItemView(Context context) {
        super(context);
    }

    public SettingEmojiSkinItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingEmojiSkinItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private TextView mEmojiView;
    private String mBaseEmoji = String.valueOf(Character.toChars(Integer.parseInt("1f590", 16)));

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);

        final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
        if (widgetFrame != null) {
            mEmojiView = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(Dimensions.pxFromDp(7));
            widgetFrame.addView(mEmojiView, params);
            mEmojiView.setTextSize(22f);
            mEmojiView.setText(mBaseEmoji);
            mEmojiView.setTextColor(getResources().getColor(android.R.color.black));
        }

        setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick();
            }
        });
    }

    public void updateSkin(String skin) {
        if (mEmojiView != null)
            mEmojiView.setText(mBaseEmoji + skin);
    }

    public void setDefault(String skin) {
        if (mEmojiView != null) {
            mEmojiView.setText(mBaseEmoji + skin);
        }
    }

}
