package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.emoispan.EmojiSpannableWorker;
import com.android.messaging.ui.view.MessagesTextView;


public class EmojiTextView extends MessagesTextView {
    public boolean linkHit;
    public boolean useSystemEmoji;
    public float emojiSize;

    public EmojiTextView(Context context) {
        this(context, null);
    }

    public EmojiTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (!isInEditMode()) {
            EmojiSpannableWorker.getInstance().verifyInstalled();
        }
        useSystemEmoji = EmojiManager.isSystemEmojiStyle();

        FontMetrics fontMetrics = getPaint().getFontMetrics();
        emojiSize = fontMetrics.descent - fontMetrics.ascent;

        setText(getText());
    }

    @Override
    public void setText(CharSequence charSequence, BufferType bufferType) {
        if (useSystemEmoji) {
            super.setText(charSequence, bufferType);
            return;
        }
        if (charSequence == null) {
            charSequence = "";
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(charSequence);
        EmojiSpannableWorker.replaceWithImages(spannableStringBuilder, this.emojiSize);
        super.setText(spannableStringBuilder, bufferType);
    }


    @Override
    public boolean performClick() {
        if (this.linkHit) {
            return true;
        }
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.linkHit = false;
        return super.onTouchEvent(motionEvent);
    }
}
