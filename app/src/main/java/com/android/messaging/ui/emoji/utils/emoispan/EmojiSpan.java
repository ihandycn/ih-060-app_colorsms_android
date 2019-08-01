package com.android.messaging.ui.emoji.utils.emoispan;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.style.ImageSpan;

import com.android.messaging.R;
import com.ihs.app.framework.HSApplication;

public final class EmojiSpan extends ImageSpan {
    private final float size;

    public EmojiSpan(Drawable drawable, float emojiSize) {
        super(drawable);
        this.size = emojiSize;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = super.getDrawable();
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(HSApplication.getContext(), R.drawable.ic_emoji);
        }
        drawable.setBounds(0, 0, (int) this.size, (int) this.size);
        return drawable;
    }

    @Override
    public int getSize(Paint paint, CharSequence charSequence, int i, int i2, FontMetricsInt fontMetricsInt) {
        if (fontMetricsInt != null) {
            FontMetrics fontMetrics = paint.getFontMetrics();
            float f = fontMetrics.ascent + ((fontMetrics.descent - fontMetrics.ascent) / 2.0f);
            fontMetricsInt.ascent = (int) (f - (this.size / 2.0f));
            fontMetricsInt.top = fontMetricsInt.ascent;
            fontMetricsInt.bottom = (int) (f + (this.size / 2.0f));
            fontMetricsInt.descent = fontMetricsInt.bottom;
        }
        return (int) this.size;
    }

    @Override
    public void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint) {
        Drawable drawable = getDrawable();
        FontMetrics fontMetrics = paint.getFontMetrics();
        float f2 = ((((float) i4) + fontMetrics.descent) - ((fontMetrics.descent - fontMetrics.ascent) / 2.0f)) - (this.size / 2.0f);
        canvas.save();
        canvas.translate(f, f2);
        drawable.draw(canvas);
        canvas.restore();
    }
}
