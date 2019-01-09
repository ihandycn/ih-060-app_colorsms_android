package com.android.messaging.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.android.messaging.R;

/**
 * Created by lizhe on 2019/1/9.
 */

public class ColorFilterImageView extends ImageView {

    private final int mColorPressedId;

    public ColorFilterImageView(Context context) {
        this(context, null);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources resources = context.getResources();
        mColorPressedId = resources.getColor(R.color.contact_avatar_pressed_color);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            setColorFilter(mColorPressedId);
        } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
            clearColorFilter();
        }
        return super.onTouchEvent(event);
    }
}
