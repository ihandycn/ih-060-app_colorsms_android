package com.android.messaging.ui.dialog;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.android.messaging.R;

public abstract class BasePermissionGuideDialog extends FrameLayout {

    protected static final long SLIDE_IN_ANIM_DURATION = 300;
    public static final long ESTIMATED_ACTIVITY_SWITCH_TIME = 1200;
    protected boolean mIsShowImmediately;

    public BasePermissionGuideDialog(Context context) {
        this(context, null);
    }

    public BasePermissionGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePermissionGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(INVISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean showFloatButton = !shouldDismissCompletelyOnTouch();
//        LauncherFloatWindowManager.getInstance().removePermissionGuide(showFloatButton);
        return false;
    }

    /**
     * Whether this dialog should dismiss completely (without showing a little float button for
     * the user to re-open the dialog) when any touch event occurs on the screen.
     */
    protected abstract boolean shouldDismissCompletelyOnTouch();

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//            LauncherFloatWindowManager.getInstance().removePermissionGuide(true);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void onAddedToWindow() {
        postDelayed(() -> {
            setVisibility(VISIBLE);
            View content = findViewById(R.id.permission_guide_content);
            if (null != content) {
                int contentHeight = content.getHeight();
                content.setTranslationY(contentHeight);
                content.animate().translationY(0f)
                        .setDuration(SLIDE_IN_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }, mIsShowImmediately ? 0 : ESTIMATED_ACTIVITY_SWITCH_TIME);
    }

    public void setShowContentImmediately(boolean isImmediately) {
        mIsShowImmediately = isImmediately;
    }

    public void onRemovedFromWindow() {
        int contentHeight = getHeight();
        ValueAnimator animator = ofFloat(this, 0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(valueAnimator ->
                setTranslationY(valueAnimator.getAnimatedFraction() * contentHeight));
        animator.start();
    }

    public static ValueAnimator ofFloat(View target, float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
//        cancelOnDestroyActivity(anim);
        return anim;
    }
}
