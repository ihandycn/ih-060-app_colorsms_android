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
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.WebViewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleAnimUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.view.AdvancedPageIndicator;
import com.android.messaging.util.view.IndicatorMark;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.view.MessagesTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static com.ihs.app.framework.HSApplication.getContext;

public class WelcomeStartActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = WelcomeStartActivity.class.getSimpleName();

    public static final String PREF_KEY_START_BUTTON_CLICKED = "PREF_KEY_START_BUTTON_CLICKED";
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;

    private final static float[] LOTTIE_ANIMATION_FORWARD_POSITION =
            new float[]{0.317f, 0.561f, 0.920f};
    private final static float[] LOTTIE_ANIMATION_FORWARD_DRAG_POSITION =
            new float[]{0, 0.368f, 0.620f, 1.0f};

    private final static float[] LOTTIE_ANIMATION_BACKWARD_POSITION =
            new float[]{1, 0.316f, 0};

    private final static int LOTTIE_ANIMATION_FORWARD_DURATION = 6830;
    private final static int LOTTIE_ANIMATION_BACKWARD_DURATION = 4533;

    private final Interpolator mInterpolator =
            PathInterpolatorCompat.create(0.32f, 0.94f, 0.6f, 1f);

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
    private boolean mIsActivityPaused = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.getDefault().getBoolean(PREF_KEY_START_BUTTON_CLICKED, false)) {
            UIIntents.get().launchConversationListActivity(this);
            finish();
        }

        setContentView(R.layout.activity_welcome_start);

        findViewById(R.id.welcome_start_button).setOnClickListener(this);
        findViewById(R.id.welcome_start_button).setBackgroundDrawable(
                BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.welcome_button_dark_green), Dimensions.pxFromDp(6.7f), true));

        MessagesTextView serviceText = findViewById(R.id.welcome_start_service);
        serviceText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        serviceText.setOnClickListener(this);

        MessagesTextView policyText = findViewById(R.id.welcome_start_policy);
        policyText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        policyText.setOnClickListener(this);

        initTextPager();
        initImages();

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
    }

    @Override
    public void onStart() {
        super.onStart();
        BugleAnalytics.logEvent("Start_WelcomePage_Show", true, true);
    }

    @Override protected void onResume() {
        super.onResume();
        mIsActivityPaused = false;
    }

    @Override protected void onPause() {
        super.onPause();
        mIsActivityPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelForwardAnimationLoadTask();
        cancelBackwardAnimationLoadTask();
    }

    @Override
    public void onBackPressed() {
        if (mAllowBackKey) {
            super.onBackPressed();
        }
        BugleAnalytics.logEvent("Start_DetailPage_Back", true);
    }

    private void initTextPager() {
        WelcomePagerAdapter pagerAdapter = new WelcomePagerAdapter(this);
        mPagerAdapter = pagerAdapter;

        mTextPager = findViewById(R.id.welcome_guide_viewpager);
        mTextPager.setAdapter(pagerAdapter);

        mViewPageIndicator = findViewById(R.id.welcome_guide_viewpager_indicator);

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
                    BugleAnalytics.logEvent("Start_DetailPage_Slide");
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

        mForwardLottieView = findViewById(R.id.welcome_guide_lottie_anim_forward);
        mForwardLottieView.setVisibility(View.VISIBLE);
        loadForwardAnimation();

        mBackwardLottieView = findViewById(R.id.welcome_guide_lottie_anim_backward);
        mBackwardLottieView.setVisibility(View.INVISIBLE);
        loadBackwardAnimation();
    }

    private void loadForwardAnimation() {
        cancelForwardAnimationLoadTask();
        try {
            mForwardAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                    "lottie/welcome_guide_anim_forward.json", lottieComposition -> {
                        mForwardLottieView.setComposition(lottieComposition);
                        mForwardLottieView.setProgress(0f);

                        findViewById(R.id.root_view).setBackgroundColor(
                                getResources().getColor(android.R.color.white));
                        findViewById(R.id.root_view).setVisibility(View.VISIBLE);

                        mForwardLottieView.postDelayed(() -> playForwardDropAnimation(0), 200);

                        if (!mIsActivityPaused) {
                            BugleAnalytics.logEvent("Start_DetailPage_Show", true, true);
                        }
                    });
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadBackwardAnimation() {
        cancelBackwardAnimationLoadTask();
        try {
            mBackwardAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                    "lottie/welcome_guide_anim_backward.json", lottieComposition -> {
                        mBackwardLottieView.setComposition(lottieComposition);
                        mBackwardLottieView.setProgress(0f);
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

        if (position > LOTTIE_ANIMATION_FORWARD_POSITION.length - 1 || position < 0) {
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
        mForwardLottieAnimator.addUpdateListener(valueAnimator -> {
            if (mForwardLottieView != null) {
                mForwardLottieView.setProgress(
                        startProgress + totalProgress * valueAnimator.getAnimatedFraction());
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
        mAutoPlayViewPagerSlideAnimator.setInterpolator(mInterpolator);
        mAutoPlayViewPagerSlideAnimator.addUpdateListener(valueAnimator -> {
            mViewPageIndicator.onScrolling(mViewPagerCurrentPosition - 1, valueAnimator.getAnimatedFraction());
            int dx = (int) (Dimensions.getPhoneWidth(getContext()) * (mViewPagerCurrentPosition - 1 + valueAnimator.getAnimatedFraction()));
            mTextPager.scrollTo(dx, 0);
        });

        mForwardLottieView.postDelayed(() -> {
            if (mIsViewPagerAutoSlide) {
                playForwardDropAnimation(++mViewPagerCurrentPosition);
                mAutoPlayViewPagerSlideAnimator.start();
            } else {
                mAutoPlayViewPagerSlideAnimator.cancel();
            }
        }, 1800);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welcome_start_button:
                BugleAnalytics.logEvent("Start_DetailPage_Click", true, true, "Page", String.valueOf(mViewPagerCurrentPosition));
                Preferences.getDefault().putBoolean(PREF_KEY_START_BUTTON_CLICKED, true);
                final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(WelcomeStartActivity.this);
                startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
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

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                if (OsUtil.hasRequiredPermissions()) {
                    Factory.get().onDefaultSmsSetAndPermissionsGranted();
                    UIIntents.get().launchConversationListActivity(this);
                } else {
                    UIIntents.get().launchWelcomePermissionActivity(this);
                }
                BugleAnalytics.logEvent("Start_SetAsDefault_Success", true, true, "step", "detail page");
                finish();
            } else {
                Intent intent = new Intent(WelcomeStartActivity.this, WelcomeSetAsDefaultActivity.class);
                intent.putExtra(WelcomeSetAsDefaultActivity.EXTRA_FROM_WELCOME_START, true);
                Navigations.startActivitySafely(WelcomeStartActivity.this, intent);
                finish();
            }
        }
    }
}
