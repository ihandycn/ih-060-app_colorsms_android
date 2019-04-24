package com.android.messaging.ui.messagebox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.android.messaging.R;

public class BottomCropImage extends AppCompatImageView {

    private Path mPath = new Path();
    private float mRadius = getResources().getDimension(R.dimen.message_box_background_radius);

    private float topLeftRadius = mRadius;
    private float topRightRadius = mRadius;
    private float bottomLeftRadius = 0f;
    private float bottomRightRadius = 0f;

    private float[] mRadii = new float[8];
    private RectF mDrawRect;

    public BottomCropImage(Context context) {
        super(context);
        setup();
    }

    public BottomCropImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public BottomCropImage(Context context, AttributeSet attrs,
                           int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        setScaleType(ScaleType.MATRIX);
        post(() -> {

            // bottom crop
            Matrix matrix = getImageMatrix();

            float scale;
            int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            int drawableWidth = getDrawable().getIntrinsicWidth();
            int drawableHeight = getDrawable().getIntrinsicHeight();

            //Get the scale
            if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
                scale = (float) viewHeight / (float) drawableHeight;
            } else {
                scale = (float) viewWidth / (float) drawableWidth;
            }

            //Define the rect to take image portion from
            RectF drawableRect = new RectF(0, drawableHeight - (viewHeight / scale), drawableWidth, drawableHeight);
            RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL);
            setImageMatrix(matrix);
        });

        mRadii = new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomLeftRadius, bottomLeftRadius,
                bottomRightRadius, bottomRightRadius,
        };

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawRect = new RectF(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.reset();
        mPath.addRoundRect(mDrawRect, mRadii, Path.Direction.CW);
        canvas.clipPath(mPath);
        super.onDraw(canvas);

    }
}
