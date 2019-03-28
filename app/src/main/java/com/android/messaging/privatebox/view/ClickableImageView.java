package com.android.messaging.privatebox.view;

/**
 * Created by lz on 22/07/2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;

import com.superapps.util.Dimensions;

public class ClickableImageView extends AppCompatImageView {

    private static final int CIRCLE_RADIUS = Dimensions.pxFromDp(35); // dp
    private boolean isTouching;
    private Paint circlePaint;

    public ClickableImageView(Context context, Paint circlePaint) {
        super(context);
        this.circlePaint = circlePaint;
    }

    public void setTouching(boolean isTouching) {
        this.isTouching = isTouching;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isTouching) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
            canvas.drawCircle(centerX, centerY, CIRCLE_RADIUS >= radius ? radius : CIRCLE_RADIUS, circlePaint);
        }
    }
}
