package com.android.messaging.notificationcleaner.resultpage.content;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.resultpage.ResultPageActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.ViewUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class OptimizedContent implements IContent {

    private static final int FRAME_TIME = 40;

    private View resultView;
    private ViewGroup container;

    private View phone;
    private View screen;
    private View star1;
    private View star2;
    private View star3;
    private View description;
    private ImageView shield;
    private TextView mBtnOk;
    private ClipDrawable tickDrawable;
    private CardOptimizedFlashView flashView;

    public OptimizedContent() {
    }

    @Override
    public void initView(Context context) {
        container = ViewUtils.findViewById((Activity) context, R.id.full_screen_view_container);
        resultView = ViewUtils.findViewById((Activity) context, R.id.result_view);

        ViewGroup optimizedContent = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.result_page_content_card_optimized, container, false);
        container.addView(optimizedContent);

        phone = ViewUtils.findViewById(optimizedContent, R.id.phone);
        screen = ViewUtils.findViewById(optimizedContent, R.id.screen);
        star1 = ViewUtils.findViewById(optimizedContent, R.id.star_1);
        star2 = ViewUtils.findViewById(optimizedContent, R.id.star_2);
        star3 = ViewUtils.findViewById(optimizedContent, R.id.star_3);
        shield = ViewUtils.findViewById(optimizedContent, R.id.shield);
        ImageView tick = ViewUtils.findViewById(optimizedContent, R.id.tick);
        tickDrawable = (ClipDrawable) tick.getDrawable();
        description = ViewUtils.findViewById(optimizedContent, R.id.description);
        mBtnOk = ViewUtils.findViewById(optimizedContent, R.id.btn_ok);
        flashView = ViewUtils.findViewById(optimizedContent, R.id.flash_view);

        mBtnOk.setTextColor(((ResultPageActivity) context).getBackgroundColor());
        mBtnOk.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                context.getResources().getColor(R.color.ripples_ripple_color),
                Dimensions.pxFromDp(3.3f), true, true));
        mBtnOk.setOnClickListener(v -> {
            UIIntents.get().launchConversationListActivity(context);
            ((ResultPageActivity) context).finish();
        });

        phone.setTranslationY(Dimensions.pxFromDp(77));
        phone.setAlpha(0);
        screen.setTranslationY(Dimensions.pxFromDp(77));
        screen.setAlpha(0);
        description.setAlpha(0);
        mBtnOk.setScaleX(0);
        mBtnOk.setAlpha(0);
        tickDrawable.setLevel(0);
        star1.setAlpha(0);
        star2.setAlpha(0);
        star3.setAlpha(0);
    }

    @Override
    public void startAnimation() {
        resultView.setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);

        phone.animate().alpha(1).translationY(0).setDuration(8 * FRAME_TIME).start();
        screen.animate().alpha(1).translationY(0).setDuration(8 * FRAME_TIME).start();
        description.animate().alpha(1).setDuration(7 * FRAME_TIME).setStartDelay(5 * FRAME_TIME).start();
        mBtnOk.animate().alpha(1).scaleX(1).setDuration(8 * FRAME_TIME).setStartDelay(FRAME_TIME).start();

        ValueAnimator tickAnimator = ValueAnimator.ofInt(0, 10000);
        tickAnimator.setDuration(7 * FRAME_TIME);
        tickAnimator.setStartDelay(20 * FRAME_TIME);
        tickAnimator.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            tickDrawable.setLevel(level);
        });
        tickAnimator.start();

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(shield,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f, 1.14f, 1.17f, 1.2f, 1.21f, 1.2f, 1.17f, 1.13f, 1.06f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f, 1.14f, 1.17f, 1.2f, 1.21f, 1.2f, 1.17f, 1.13f, 1.06f, 1f));
        animator.setDuration(10 * FRAME_TIME);
        animator.setStartDelay(21 * FRAME_TIME);
        animator.start();

        ObjectAnimator star1Animator = ObjectAnimator.ofFloat(star1, View.ALPHA, 0f, 1f, 0.15f, 0.89f);
        star1Animator.setDuration(19 * FRAME_TIME);
        star1Animator.setStartDelay(32 * FRAME_TIME);
        star1Animator.start();

        ObjectAnimator star2Animator = ObjectAnimator.ofFloat(star2, View.ALPHA, 0f, 0.8f, 0.1f, 0.8f);
        star2Animator.setDuration(18 * FRAME_TIME);
        star2Animator.setStartDelay(34 * FRAME_TIME);
        star2Animator.start();

        star3.animate().alpha(.6f).setDuration(7 * FRAME_TIME).setStartDelay(41 * FRAME_TIME).start();

        screen.postDelayed(() -> flashView.startFlashAnimation(), 8 * FRAME_TIME);
    }

    @Override
    public void onActivityDestroy() {

    }
}
