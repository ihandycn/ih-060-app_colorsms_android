package com.android.messaging.notificationcleaner.resultpage.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.resultpage.ResultPageActivity;
import com.android.messaging.notificationcleaner.resultpage.content.IContent;
import com.android.messaging.notificationcleaner.resultpage.views.FlashCircleView;
import com.android.messaging.util.AnimationUtils;
import com.android.messaging.util.ViewUtils;

import net.appcloudbox.ads.base.AcbInterstitialAd;

public class DefaultTransition extends BaseTransition implements ITransition {

    private IContent content;

    private Object[] params;

    private View transitionView;

    public DefaultTransition(AcbInterstitialAd ad, Object... params) {
        super(ad);

        this.params = params;
    }

    @Override
    public void setContent(IContent iContent) {
        content = iContent;
    }

    // Circle Done
    private static final long START_DELAY_FLASH_CIRCLE = 620;
    private static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 420;

    private TextView mDescriptionTv;

    @Override
    public int getLayoutId() {
        return R.layout.result_page_transition_done_circle;
    }

    @Override
    public void onFinishInflateTransitionView(View transitionView) {
        this.transitionView = transitionView;
        final TextView labelTv = ViewUtils.findViewById(transitionView, R.id.label_title);
        labelTv.setText(R.string.notification_cleaner_deleted);

        mDescriptionTv = ViewUtils.findViewById(transitionView, R.id.description_tv);
        mDescriptionTv.setVisibility(View.VISIBLE);
        mDescriptionTv.setText(transitionView.getResources().getString(R.string.notification_cleaner_cleared_up,
                String.valueOf(params[0])));

        FlashCircleView doneCircle = ViewUtils.findViewById(transitionView, R.id.done_circle);
        doneCircle.setViewListener(new FlashCircleView.ViewListener() {
            @Override
            public void onViewed() {
                doneCircle.postDelayed(doneCircle::startAnimation, START_DELAY_FLASH_CIRCLE);
            }

            @Override
            public void onAnimationEnd() {
                doneCircle.setVisibility(View.GONE);

                popupInterstitialAdIfNeeded();
            }
        });
    }

    @Override
    protected void onInterstitialAdClosed() {
        final TextView optimalTv = ViewUtils.findViewById(transitionView, R.id.label_title);
        final TextView titleAnchor = ViewUtils.findViewById(transitionView, R.id.anchor_title_tv);

        int[] location = new int[2];
        optimalTv.getLocationInWindow(location);
        int oldOptimalTvCenterY = location[1] + optimalTv.getHeight() / 2;
        titleAnchor.getLocationInWindow(location);
        int newOptimalTvCenterY = location[1] + titleAnchor.getHeight() / 2;

        TimeInterpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.79f, 0.37f, 0.28f, 1f);
        optimalTv.animate()
                .translationYBy(newOptimalTvCenterY - oldOptimalTvCenterY)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                .setInterpolator(softStopAccDecInterpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        optimalTv.post(() -> {
                            content.initView(transitionView.getContext());
                            content.startAnimation();
                            ResultPageActivity.sResultPageContentShowTime = System.currentTimeMillis();
                        });
                    }
                })
                .start();

        if (null != mDescriptionTv) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
            alphaAnimation.setDuration(350);
            alphaAnimation.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mDescriptionTv.setVisibility(View.GONE);
                }
            });
            mDescriptionTv.startAnimation(alphaAnimation);
        }
    }
}
