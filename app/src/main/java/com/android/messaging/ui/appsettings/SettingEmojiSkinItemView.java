package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

    private ImageView mEmojiView;
    private static int[] mSkinResource = new int[]{
            R.drawable.emoji_1f590,
            R.drawable.emoji_1f590_1f3fb,
            R.drawable.emoji_1f590_1f3fc,
            R.drawable.emoji_1f590_1f3fd,
            R.drawable.emoji_1f590_1f3fe,
            R.drawable.emoji_1f590_1f3ff,
    };

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);

        final ViewGroup widgetFrame = mRootView.findViewById(R.id.widget_frame);
        if (widgetFrame != null) {
            mEmojiView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Dimensions.pxFromDp(25f), Dimensions.pxFromDp(25f));
            params.setMarginEnd(Dimensions.pxFromDp(7));
            widgetFrame.addView(mEmojiView, params);
        }

        setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick();
            }
        });
    }

    public static int[] getSkinResource(){
        return mSkinResource;
    }

    public void updateSkin(int pos) {
        if (mEmojiView != null)
            mEmojiView.setImageResource(mSkinResource[pos]);
    }

    public void setDefault(int pos) {
        if (mEmojiView != null) {
            mEmojiView.setImageResource(mSkinResource[pos]);
        }
    }

}
