package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.emoispan.EmojiSpannableWorker;
import com.android.messaging.ui.view.MessagesTextView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;


public class EmojiTextView extends MessagesTextView implements INotificationObserver {
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HSGlobalNotificationCenter.addObserver(EmojiManager.NOTIFICATION_EMOJI_STYLE_CHANGE, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        HSGlobalNotificationCenter.removeObserver(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        useSystemEmoji = EmojiManager.isSystemEmojiStyle();
        setText(getText());
    }
}
