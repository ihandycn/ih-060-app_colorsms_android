package com.android.messaging.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import com.android.messaging.R;
import com.superapps.util.Bitmaps;
import com.superapps.util.Dimensions;

public class FontChangePreviewLayout extends View {

    public FontChangePreviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int shadowColor = ContextCompat.getColor(getContext(), R.color.text_shadow);
        mTextPaint.setShadowLayer(Dimensions.pxFromDp(2), Dimensions.pxFromDp(0.7f), Dimensions.pxFromDp(1f), shadowColor);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * Outer container of this preview layout. Responsible for drawing background.
     */
    public static class Container extends FrameLayout {

        public Container(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            if (isInEditMode()) {
                return;
            }

        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
        }
    }
}
