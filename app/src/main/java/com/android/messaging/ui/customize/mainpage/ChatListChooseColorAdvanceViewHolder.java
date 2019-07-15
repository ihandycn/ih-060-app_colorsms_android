package com.android.messaging.ui.customize.mainpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.OnColorChangedListener;
import com.android.messaging.ui.customize.PaletteView;
import com.android.messaging.ui.customize.PrimaryColors;

public class ChatListChooseColorAdvanceViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private OnColorChangedListener mListener;
    private int mInitTextColor = PrimaryColors.getPrimaryColor();

    private PaletteView mColorPickerView;

    ChatListChooseColorAdvanceViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View view = inflater.inflate(
                R.layout.chat_list_choose_color_advance, null , false );

        mColorPickerView = view.findViewById(R.id.color_picker_view);
        mColorPickerView.setColor(mInitTextColor);
        mColorPickerView.setOnColorChangedListener(color -> mListener.onColorChanged(color));

        return view;
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    public void setColor(@ColorInt int color) {
        if (mColorPickerView != null) {
            mColorPickerView.setColor(color);
        } else {
            mInitTextColor = color;
        }
    }

    public void reset() {
        if (mColorPickerView != null) {
            mColorPickerView.reset();
        }
    }

    @Override
    protected void setHasOptionsMenu() {

    }


    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_color_advance);
    }

    @Override
    public void onPageSelected() {

    }
}
