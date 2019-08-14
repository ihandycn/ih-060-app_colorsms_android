package com.android.messaging.ui.conversation;

import android.content.Context;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.Dimensions;

public class ConversationSettingGuide extends FrameLayout {

    private AnimationEndCallback callback;
    private MessagesTextView tv;

    public ConversationSettingGuide(Context context) {
        super(context);
        initView(context);
    }

    public ConversationSettingGuide(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ConversationSettingGuide(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        BugleAnalytics.logEvent("SMS_Detailspage_Guide_Show", true);
        LayoutInflater.from(context).inflate(R.layout.conversation_setting_guide_view, this, true);
        tv = findViewById(R.id.view);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) tv.getLayoutParams();
        lp.topMargin = Dimensions.pxFromDp(33.3f) + Dimensions.getStatusBarHeight(context);
        lp.rightMargin = Dimensions.pxFromDp(30.3f);
        tv.setLayoutParams(lp);
        if (Dimensions.getPhoneWidth(context) >= 1080) {
            tv.setTextSize(16);
            tv.setMaxWidth(Dimensions.pxFromDp(300));
            tv.setPadding(Dimensions.pxFromDp(30), Dimensions.pxFromDp(18), Dimensions.pxFromDp(30), Dimensions.pxFromDp(14));
        }

        // start animation
        AnimationSet animationSet = new AnimationSet(false);
        Animation alpha = new AlphaAnimation(0, 100);
        alpha.setDuration(80);
        Animation scale = new ScaleAnimation(0.6f, 1.03f, 0.6f, 1.03f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        scale.setDuration(160);
        scale.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.66f, 0.6f, 1f));
        Animation scale2 = new ScaleAnimation(1f, 1 / 1.03f, 1f, 1 / 1.03f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        scale2.setDuration(70);
        scale2.setStartOffset(160);
        animationSet.addAnimation(scale);
        animationSet.addAnimation(scale2);
        animationSet.addAnimation(alpha);
        tv.startAnimation(animationSet);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // end animation
        AnimationSet animationSet = new AnimationSet(false);
        Animation alpha = new AlphaAnimation(100, 0);
        alpha.setDuration(80);
        alpha.setStartOffset(120);
        Animation scale = new ScaleAnimation(1f, 1.06f, 1f, 1.06f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        scale.setDuration(100);
        scale.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.66f, 0.6f, 1f));
        Animation scale2 = new ScaleAnimation(1f, 0.6f / 1.06f, 1f, 0.6f / 1.06f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        scale2.setDuration(100);
        scale2.setStartOffset(100);
        animationSet.addAnimation(scale);
        animationSet.addAnimation(scale2);
        animationSet.addAnimation(alpha);
        tv.startAnimation(animationSet);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                callback.onAnimationEndCallback(tv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return super.onInterceptTouchEvent(ev);
    }

    public void setAimationEndCallback(AnimationEndCallback callback) {
        this.callback = callback;
    }

    interface AnimationEndCallback {
        void onAnimationEndCallback(TextView tv);
    }
}
