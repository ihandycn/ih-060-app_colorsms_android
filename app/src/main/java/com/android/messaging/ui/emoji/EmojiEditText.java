package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.android.messaging.ui.PlainTextEditText;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.emoispan.EmojiSpannableWorker;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

public class EmojiEditText extends PlainTextEditText implements INotificationObserver {
    private float emojiSize;

    public EmojiEditText(Context context) {
        this(context, null);
    }

    public EmojiEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        FontMetrics fontMetrics = getPaint().getFontMetrics();
        emojiSize = fontMetrics.descent - fontMetrics.ascent;

        setText(getText());
    }

    @Override
    protected void onAttachedToWindow() {
        HSGlobalNotificationCenter.addObserver(EmojiSpannableWorker.NOTIFICATION, this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        HSGlobalNotificationCenter.removeObserver(EmojiSpannableWorker.NOTIFICATION, this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (!useSystemEmoji()) {
            if (EmojiSpannableWorker.getInstance().getIsInstalled()) {
                EmojiSpannableWorker.replaceWithImages(getText(), EmojiEditText.this.emojiSize);
            }
        }
    }

    public void backspace() {
        dispatchKeyEvent(new KeyEvent(0, 0, 0, 67, 0, 0, 0, 0, 6));
    }

    public void input(EmojiInfo emojiInfo) {
        if (emojiInfo != null) {
            int selectionStart = getSelectionStart();
            int selectionEnd = getSelectionEnd();
            if (selectionStart < 0) {
                append(emojiInfo.mEmoji);
                return;
            }
            getText().replace(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd), emojiInfo.mEmoji, 0, emojiInfo.mEmoji.length());
        }
    }

    private boolean useSystemEmoji() {
        return EmojiManager.isSystemEmojiStyle();
    }

    public final void setEmojiSize(int i) {
        setEmojiSize(i, true);
    }

    public final void setEmojiSize(int i, boolean z) {
        this.emojiSize = (float) i;
        if (z) {
            setText(getText());
        }
    }

    public final void setEmojiSizeRes(int i) {
        setEmojiSizeRes(i, true);
    }

    public final void setEmojiSizeRes(int i, boolean z) {
        setEmojiSize(getResources().getDimensionPixelSize(i), z);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        setText(getText());
    }
}
