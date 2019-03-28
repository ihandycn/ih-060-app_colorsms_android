package com.android.messaging.ui.welcome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.Cancellable;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.WebViewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleAnimUtils;
import com.android.messaging.util.view.AdvancedPageIndicator;
import com.android.messaging.util.view.IndicatorMark;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.view.TypefacedTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static com.ihs.app.framework.HSApplication.getContext;

public class WelcomeStartActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = WelcomeStartActivity.class.getSimpleName();

    private static final String PREF_KEY_START_BUTTON_CLICKED = "PREF_KEY_START_BUTTON_CLICKED";

    private final static float[] LOTTIE_ANIMATION_FORWARD_POSITION =
            new float[]{0, 0.147f, 0.393f, 0.702f, 0.881f};
    private final static float[] LOTTIE_ANIMATION_FORWARD_DRAG_POSITION =
            new float[]{0, 0.042f, 0.207f, 0.453f, 0.747f, 1.0f};

    private final static float[] LOTTIE_ANIMATION_BACKWARD_POSITION =
            new float[]{1, 0.819f, 0.524f, 0.286f, 0};

    private final static int LOTTIE_ANIMATION_FORWARD_DURATION = 9500;
    private final static int LOTTIE_ANIMATION_BACKWARD_DURATION = 3500;

    private Interpolator mFadeInInterpolator = PathInterpolatorCompat.create(0, 0.83f, 0.24f, 1);

    private boolean mAllowBackKey = true;

    private ViewPager mTextPager;
    private WelcomePagerAdapter mPagerAdapter;
    private AdvancedPageIndicator mViewPageIndicator;

    private LottieAnimationView mForwardLottieView;
    private LottieAnimationView mBackwardLottieView;
    private Cancellable mForwardAnimationLoadTask;
    private Cancellable mBackwardAnimationLoadTask;
    private ValueAnimator mForwardLottieAnimator;
    private ValueAnimator mBackwardLottieAnimator;
    private ValueAnimator mAutoPlayViewPagerSlideAnimator;

    private int mViewPagerCurrentPosition = 0;
    private boolean mIsViewPagerAutoSlide = true;
    private boolean mCanViewPagerDrag = true;
    private float mViewPagerEndDragStartX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.getDefault().getBoolean(PREF_KEY_START_BUTTON_CLICKED, false)) {
            UIIntents.get().launchConversationListActivity(this);
            finish();
        }

        setContentView(R.layout.activity_welcome_start);

        findViewById(R.id.welcome_start_button).setOnClickListener(this);

        TypefacedTextView serviceText = findViewById(R.id.welcome_start_service);
        serviceText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        serviceText.setOnClickListener(this);

        TypefacedTextView policyText = findViewById(R.id.welcome_start_policy);
        policyText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        policyText.setOnClickListener(this);

        initTextPager();
        initImages();

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
    }

    private void initTextPager() {
        WelcomePagerAdapter pagerAdapter = new WelcomePagerAdapter(this);
        mPagerAdapter = pagerAdapter;

        mTextPager = findViewById(R.id.welcome_guide_viewpager);
        mTextPager.setVisibility(View.INVISIBLE);
        mTextPager.setAdapter(pagerAdapter);

        mViewPageIndicator = findViewById(R.id.welcome_guide_viewpager_indicator);
        mViewPageIndicator.setVisibility(View.INVISIBLE);

        List<IndicatorMark.MarkerType> markerTypes = new ArrayList<>();
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            markerTypes.add(IndicatorMark.MarkerType.CIRCLE);
        }

        if (pagerAdapter.getCount() > 1) {
            mIsViewPagerAutoSlide = true;
            mViewPageIndicator.addMarkers(markerTypes);
            mTextPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int positionAbsolute, float positionAbsoluteOffset, int positionOffsetPixels) {
                    int position;
                    float positionOffset;
                    if (Dimensions.isRtl()) {
                        position = mPagerAdapter.getCount() - 2 - positionAbsolute;
                        positionOffset = 1 - positionAbsoluteOffset;
                    } else {
                        position = positionAbsolute;
                        positionOffset = positionAbsoluteOffset;
                    }

                    mViewPageIndicator.onScrolling(position, positionOffset);
                }

                @Override
                public void onPageSelected(int positionAbsolute) {
                    if (mIsViewPagerAutoSlide) {
                        return;
                    }
                    if (positionAbsolute > mViewPagerCurrentPosition) {
                        playForwardDropAnimation(positionAbsolute);
                        mBackwardLottieView.setProgress(LOTTIE_ANIMATION_BACKWARD_POSITION[positionAbsolute]);
                    } else {
                        playBackwardAnimation(positionAbsolute);
                        mForwardLottieView.setProgress(LOTTIE_ANIMATION_FORWARD_POSITION[positionAbsolute]);
                    }
                    mViewPagerCurrentPosition = positionAbsolute;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else {
            mIsViewPagerAutoSlide = false;
        }
    }

    private void initImages() {
        HSLog.d(TAG, "initImages()");

        final View lottieAnimationContainer = findViewById(R.id.welcome_guide_lottie_container);
        lottieAnimationContainer.setVisibility(View.INVISIBLE);

        mForwardLottieView = findViewById(R.id.welcome_guide_lottie_anim_forward);
        mForwardLottieView.setLayerType(View.LAYER_TYPE_NONE, null);
        mForwardLottieView.setVisibility(View.VISIBLE);
        loadForwardAnimation(lottieAnimationContainer);

        mBackwardLottieView = findViewById(R.id.welcome_guide_lottie_anim_backward);
        mBackwardLottieView.setLayerType(View.LAYER_TYPE_NONE, null);
        mBackwardLottieView.setVisibility(View.INVISIBLE);
        loadBackwardAnimation();
    }

    private void loadForwardAnimation(View lottieAnimationContainer) {
        cancelForwardAnimationLoadTask();
        try {
            mForwardAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                    "lottie/welcome_guide_anim_forward.json", new OnCompositionLoadedListener() {
                        @Override
                        public void onCompositionLoaded(LottieComposition lottieComposition) {
                            mForwardLottieView.setComposition(lottieComposition);
                            mForwardLottieView.setProgress(0f);

                            mForwardLottieView.post(() -> guideFadeIn(lottieAnimationContainer));
                        }
                    });
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    private void guideFadeIn(View lottieAnimationContainer) {
        HSLog.d(TAG, "guideFadeIn()");

        initFadeIn(lottieAnimationContainer);
        initFadeIn(mTextPager);
        if (mPagerAdapter.getCount() > 1) {
            initFadeIn(mViewPageIndicator);
        }
        fadeInAnimation(lottieAnimationContainer, 600);

        mTextPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeInAnimation(mTextPager, 480);
            }
        }, 120);

    }

    private void initFadeIn(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.setTranslationY(Dimensions.getPhoneHeight(getContext()) - view.getTranslationY());
            view.setAlpha(0);
        }
    }

    private void fadeInAnimation(View view, int duration) {
        fadeInAnimation(view, duration, null);
    }

    private void fadeInAnimation(View view, int duration, Runnable completeRunnable) {
        view.animate()
                .setDuration(duration)
                .setInterpolator(mFadeInInterpolator)
                .translationY(0)
                .alpha(1)
                .withEndAction(() -> {
                    if (completeRunnable != null) {
                        completeRunnable.run();
                    }
                }).start();
    }

    private void loadBackwardAnimation() {
        cancelBackwardAnimationLoadTask();
        try {
            mBackwardAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                    "lottie/welcome_guide_anim_backward.json", new OnCompositionLoadedListener() {
                        @Override
                        public void onCompositionLoaded(LottieComposition lottieComposition) {
                            mBackwardLottieView.setComposition(lottieComposition);
                            mBackwardLottieView.setProgress(0f);
                        }
                    });
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    private void playForwardDragAnimation(int position, float offset) {
        if (mForwardLottieView == null || mBackwardLottieView == null) {
            return;
        }

        if (position >= LOTTIE_ANIMATION_FORWARD_POSITION.length) {
            return;
        }

        mForwardLottieView.setVisibility(View.VISIBLE);
        if (mForwardLottieAnimator != null) {
            mForwardLottieAnimator.cancel();
        }
        mBackwardLottieView.setVisibility(View.INVISIBLE);
        mBackwardLottieView.animate().cancel();
        if (mBackwardLottieAnimator != null) {
            mBackwardLottieAnimator.cancel();
        }

        float totalProgress = LOTTIE_ANIMATION_FORWARD_DRAG_POSITION[position + 1] - LOTTIE_ANIMATION_FORWARD_POSITION[position];
        float offsetProgress = LOTTIE_ANIMATION_FORWARD_POSITION[position] + totalProgress * offset;

        mForwardLottieView.setProgress(offsetProgress);
    }

    private void playForwardDropAnimation(int position) {
        if (mForwardLottieView == null || mBackwardLottieView == null) {
            return;
        }

        if (position > LOTTIE_ANIMATION_FORWARD_POSITION.length - 1 || position <= 0) {
            return;
        }

        mCanViewPagerDrag = false;
        mForwardLottieView.setVisibility(View.VISIBLE);
        mBackwardLottieView.setVisibility(View.INVISIBLE);
        if (mBackwardLottieAnimator != null) {
            mBackwardLottieAnimator.cancel();
        }
        mBackwardLottieView.animate().cancel();

        float startProgress;
        float endProgress;
        float totalProgress;
        int duration;
        startProgress = mForwardLottieView.getProgress();
        endProgress = LOTTIE_ANIMATION_FORWARD_POSITION[position];
        totalProgress = endProgress - startProgress;
        duration = (int) (1.0f * totalProgress * LOTTIE_ANIMATION_FORWARD_DURATION);

        if (mForwardLottieAnimator != null) {
            mForwardLottieAnimator.cancel();
        }
        mForwardLottieAnimator = BugleAnimUtils.ofFloat(mForwardLottieView, 0f, 1f);
        mForwardLottieAnimator.setDuration(Math.abs(duration));
        mForwardLottieAnimator.setInterpolator(new DecelerateInterpolator());
        mForwardLottieAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mForwardLottieView != null) {
                    mForwardLottieView.setProgress(
                            startProgress + totalProgress * valueAnimator.getAnimatedFraction());
                }
            }
        });
        mForwardLottieAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCanViewPagerDrag = true;
                if (mIsViewPagerAutoSlide) {
                    mTextPager.setCurrentItem(position);
                    autoPlayViewPagerSlide();
                }
            }
        });
        mForwardLottieAnimator.start();
    }

    private void playBackwardAnimation(int position) {
        if (mForwardLottieView == null || mBackwardLottieView == null) {
            return;
        }

        if (position >= LOTTIE_ANIMATION_BACKWARD_POSITION.length - 1) {
            return;
        }

        mCanViewPagerDrag = false;
        mForwardLottieView.setVisibility(View.INVISIBLE);
        if (mForwardLottieAnimator != null) {
            mForwardLottieAnimator.cancel();
        }
        mForwardLottieView.animate().cancel();
        mBackwardLottieView.setVisibility(View.VISIBLE);

        float endProgress = LOTTIE_ANIMATION_BACKWARD_POSITION[position];
        float startProgress = LOTTIE_ANIMATION_BACKWARD_POSITION[position + 1];
        float totalProgress = endProgress - startProgress;

        if (mBackwardLottieAnimator != null) {
            mBackwardLottieAnimator.cancel();
        }
        mBackwardLottieAnimator = BugleAnimUtils.ofFloat(mBackwardLottieView, 0f, 1f);
        mBackwardLottieAnimator.setDuration((long) (Math.abs(totalProgress) * LOTTIE_ANIMATION_BACKWARD_DURATION));
        mBackwardLottieAnimator.setInterpolator(new DecelerateInterpolator());
        mBackwardLottieAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mBackwardLottieView != null) {
                    mBackwardLottieView.setProgress(
                            startProgress + totalProgress * valueAnimator.getAnimatedFraction()
                    );
                }
            }
        });
        mBackwardLottieAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCanViewPagerDrag = true;
            }
        });
        mBackwardLottieAnimator.start();
    }

    private void autoPlayViewPagerSlide() {
        if (!mIsViewPagerAutoSlide) {
            return;
        }

        if (mViewPagerCurrentPosition >= mPagerAdapter.getCount() - 1) {
            return;
        }

        if (mAutoPlayViewPagerSlideAnimator != null) {
            mAutoPlayViewPagerSlideAnimator.cancel();
        }
        mAutoPlayViewPagerSlideAnimator = BugleAnimUtils.ofFloat(0f, 1f);
        mAutoPlayViewPagerSlideAnimator.setDuration(800);
        mAutoPlayViewPagerSlideAnimator.setInterpolator(new DecelerateInterpolator());
        mAutoPlayViewPagerSlideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mViewPageIndicator.onScrolling(mViewPagerCurrentPosition - 1, valueAnimator.getAnimatedFraction());
                int dx = (int) (Dimensions.getPhoneWidth(getContext()) * (mViewPagerCurrentPosition - 1 + valueAnimator.getAnimatedFraction()));
                mTextPager.scrollTo(dx, 0);
            }
        });

        mForwardLottieView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsViewPagerAutoSlide) {
                    playForwardDropAnimation(++mViewPagerCurrentPosition);
                    mAutoPlayViewPagerSlideAnimator.start();
                } else {
                    mAutoPlayViewPagerSlideAnimator.cancel();
                }
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelForwardAnimationLoadTask();
        cancelBackwardAnimationLoadTask();
    }

    private void cancelForwardAnimationLoadTask() {
        if (mForwardAnimationLoadTask != null) {
            mForwardAnimationLoadTask.cancel();
            mForwardAnimationLoadTask = null;
        }
    }

    private void cancelBackwardAnimationLoadTask() {
        if (mBackwardAnimationLoadTask != null) {
            mBackwardAnimationLoadTask.cancel();
            mBackwardAnimationLoadTask = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BugleAnalytics.logEvent("SMS_Start_WelcomePage_Show", true);
    }

    @Override
    public void onBackPressed() {
        if (mAllowBackKey) {
            super.onBackPressed();
            BugleAnalytics.logEvent("SMS_Start_WelcomePage_Back", true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welcome_start_button:
                BugleAnalytics.logEvent("SMS_Start_WelcomePage_BtnClick", true);
                Preferences.getDefault().putBoolean(PREF_KEY_START_BUTTON_CLICKED, true);
                Navigations.startActivitySafely(WelcomeStartActivity.this,
                        new Intent(WelcomeStartActivity.this, WelcomeSetAsDefaultActivity.class));
                finish();
                break;

            case R.id.welcome_start_service:
                Intent termsOfServiceIntent = WebViewActivity.newIntent(
                        HSConfig.optString("", "Application", "TermsOfServiceUrl"),
                        false, false);
                startActivity(termsOfServiceIntent);
                break;

            case R.id.welcome_start_policy:
                Intent privacyIntent = WebViewActivity.newIntent(
                        HSConfig.optString("", "Application", "PrivacyPolicyUrl"),
                        false, false);
                startActivity(privacyIntent);
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mForwardLottieView == null) {
            return super.dispatchTouchEvent(ev);
        }

        if (mIsViewPagerAutoSlide) {
            if (mAutoPlayViewPagerSlideAnimator != null) {
                mAutoPlayViewPagerSlideAnimator.cancel();
            }
            mTextPager.setCurrentItem(mViewPagerCurrentPosition);
            mIsViewPagerAutoSlide = false;
        }
        if (0 <= mViewPagerCurrentPosition && mViewPagerCurrentPosition < mPagerAdapter.getCount()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                mViewPagerEndDragStartX = ev.getX();
            }

            float phoneWidth = (float) Dimensions.getPhoneWidth(getContext());
            float positionOffset = 0.8f * (mViewPagerEndDragStartX - ev.getX()) / phoneWidth;
            if (mCanViewPagerDrag && positionOffset > 0) {
                playForwardDragAnimation(mViewPagerCurrentPosition, positionOffset);
            }

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                float startProgress = mForwardLottieView.getProgress();
                float endProgress = LOTTIE_ANIMATION_FORWARD_POSITION[mViewPagerCurrentPosition];
                float totalProgress = endProgress - startProgress;
                if (mForwardLottieAnimator != null) {
                    mForwardLottieAnimator.cancel();
                }
                mForwardLottieAnimator = BugleAnimUtils.ofFloat(mForwardLottieView, 0f, 1f);
                mForwardLottieAnimator.setDuration((long) (Math.abs(totalProgress) * LOTTIE_ANIMATION_FORWARD_DURATION));
                mForwardLottieAnimator.setInterpolator(new LinearInterpolator());
                mForwardLottieAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if (mForwardLottieView != null) {
                            mForwardLottieView.setProgress(
                                    startProgress + totalProgress * valueAnimator.getAnimatedFraction());
                        }
                    }
                });

                mForwardLottieAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCanViewPagerDrag = true;
                    }
                });
                mForwardLottieAnimator.start();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
