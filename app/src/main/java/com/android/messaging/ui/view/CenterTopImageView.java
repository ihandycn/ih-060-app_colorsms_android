package com.android.messaging.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.android.messaging.R;

/**
 * Created by lizhe on 2019/5/8.
 */
public class CenterTopImageView extends AppCompatImageView {

    private float mRoundRadius = 0f;
    private float mRoundRadiusLeftTop, mRoundRadiusLeftBottom, mRoundRadiusRightTop, mRoundRadiusRightBottom;
    private Path mPath = new Path();
    private float[] mRadii = new float[8];
    private RectF mDrawRect;

    public CenterTopImageView(Context context) {
        super(context);
    }

    public CenterTopImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CenterTopImageView(Context context, AttributeSet attrs,
                              int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setScaleType(ScaleType.MATRIX);
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.CenterTopImageView);

        mRoundRadiusLeftBottom = a.getDimension(R.styleable.CenterTopImageView_radius_leftBottom, mRoundRadius);
        mRoundRadiusLeftTop = a.getDimension(R.styleable.CenterTopImageView_radius_leftTop, mRoundRadius);
        mRoundRadiusRightBottom = a.getDimension(R.styleable.CenterTopImageView_radius_rightBottom, mRoundRadius);
        mRoundRadiusRightTop = a.getDimension(R.styleable.CenterTopImageView_radius_rightTop, mRoundRadius);

        mRadii = new float[]{
                mRoundRadiusLeftTop, mRoundRadiusLeftTop,
                mRoundRadiusRightTop, mRoundRadiusRightTop,
                mRoundRadiusLeftBottom, mRoundRadiusLeftBottom,
                mRoundRadiusRightBottom, mRoundRadiusRightBottom,
        };


        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawRect = new RectF(0, 0, w, h);
    }

    @Override
    protected boolean setFrame(int frameLeft, int frameTop, int frameRight, int frameBottom) {
        if (getDrawable() == null) {
            return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
        }
        float frameWidth = frameRight - frameLeft;
        float frameHeight = frameBottom - frameTop;

        float originalImageWidth = (float)getDrawable().getIntrinsicWidth();
        float originalImageHeight = (float)getDrawable().getIntrinsicHeight();

        float usedScaleFactor = 1;

        if((frameWidth > originalImageWidth) || (frameHeight > originalImageHeight)) {
            // If frame is bigger than image
            // => Crop it, keep aspect ratio and position it at the bottom and center horizontally

            float fitHorizontallyScaleFactor = frameWidth/originalImageWidth;
            float fitVerticallyScaleFactor = frameHeight/originalImageHeight;

            usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);
        }

        float newImageWidth = originalImageWidth * usedScaleFactor;
        float newImageHeight = originalImageHeight * usedScaleFactor;

        Matrix matrix = getImageMatrix();
        matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0); // Replaces the old matrix completly
//comment matrix.postTranslate if you want crop from TOP
//        matrix.postTranslate((frameWidth - newImageWidth) /2, frameHeight - newImageHeight);
        setImageMatrix(matrix);
        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.reset();
        mPath.addRoundRect(mDrawRect, mRadii, Path.Direction.CW);
        canvas.clipPath(mPath);
        super.onDraw(canvas);
    }
}