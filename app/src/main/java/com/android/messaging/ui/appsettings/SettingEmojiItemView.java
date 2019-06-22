package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class SettingEmojiItemView extends BaseItemView {
    public SettingEmojiItemView(Context context) {
        super(context);
    }

    public SettingEmojiItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingEmojiItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);

        final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
        if (widgetFrame != null) {
            TextView emojiView = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(Dimensions.pxFromDp(7));
            widgetFrame.addView(emojiView, params);
            emojiView.setTextSize(22f);
            emojiView.setText(String.valueOf(Character.toChars(Integer.parseInt("1f590", 16))));
        }

        setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick();
            }
        });
    }
}
