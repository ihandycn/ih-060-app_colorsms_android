package com.android.messaging.ui.signature;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.android.messaging.ui.PlainTextEditText;


public class InterceptBackKeyEditText extends PlainTextEditText {
    public InterceptBackKeyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            ((Activity)this.getContext()).onBackPressed();
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
