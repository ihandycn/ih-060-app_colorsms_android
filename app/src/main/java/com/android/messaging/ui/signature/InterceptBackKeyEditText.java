package com.android.messaging.ui.signature;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.android.messaging.ui.emoji.EmojiEditText;


public class InterceptBackKeyEditText extends EmojiEditText {

    interface BackEventListener {
        void onBackPressed();
    }

    private BackEventListener mBackListener;

    public InterceptBackKeyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        mBackListener = null;
        super.onDetachedFromWindow();
    }

    public void addBackListener(BackEventListener listener) {
        mBackListener = listener;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // ((Activity)this.getContext()).onBackPressed();
            if (mBackListener != null) {
                mBackListener.onBackPressed();
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
