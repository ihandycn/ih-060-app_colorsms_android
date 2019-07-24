package com.android.messaging.ui.appsettings;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.superapps.util.Dimensions;

public class SystemEmojiStylePreview extends Drawable {
        private String[] mUnicodes = new String[]{
                new String(Character.toChars(0x1f600)),
                new String(Character.toChars(0x1f618)),
                new String(Character.toChars(0x1f61c)),
                new String(Character.toChars(0x1f614))
        };
        private Paint mPaint = new Paint();


        @Override
        public void draw(Canvas canvas) {
            float textSize = Dimensions.pxFromDp(17f);
            float margin = Dimensions.pxFromDp(1f);
            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setTextSize(textSize);
            float textWidth = mPaint.measureText(mUnicodes[0]);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            canvas.drawText(mUnicodes[0], margin, textWidth - fontMetrics.descent + margin, mPaint);
            canvas.drawText(mUnicodes[1], textWidth + margin, textWidth - fontMetrics.descent + margin, mPaint);
            canvas.drawText(mUnicodes[2], margin, 2 * textWidth - fontMetrics.descent + margin, mPaint);
            canvas.drawText(mUnicodes[3], textWidth + margin, 2 * textWidth - fontMetrics.descent + margin, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }