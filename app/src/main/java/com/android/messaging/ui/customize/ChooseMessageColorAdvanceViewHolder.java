package com.android.messaging.ui.customize;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;

public class ChooseMessageColorAdvanceViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private OnColorChangedListener mListener;

    private AnyColorPickerView mColorPickerView;

    ChooseMessageColorAdvanceViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.choose_custom_bubble_color_advance,
                null /* root */,
                false /* attachToRoot */);

        mColorPickerView = view.findViewById(R.id.color_picker_view);
        mColorPickerView.setOnColorChangedListener(color -> mListener.onColorChanged(color));

        return view;
    }

    void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    void setColor(@ColorInt int color) {
        mColorPickerView.setColor(color);
    }

    @Override
    protected void setHasOptionsMenu() {

    }


    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_color_advance);
    }
}
