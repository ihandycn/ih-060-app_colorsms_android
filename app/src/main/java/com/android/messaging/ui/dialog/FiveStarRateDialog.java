package com.android.messaging.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.Cancellable;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.android.messaging.R;
import com.android.messaging.feedback.FeedbackActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.TypefacedTextView;

import java.util.concurrent.RejectedExecutionException;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@SuppressLint("ViewConstructor")
public class FiveStarRateDialog extends DefaultButtonDialog2 implements View.OnClickListener {

    private static final String TAG = FiveStarRateDialog.class.getSimpleName();

    public static final String DESKTOP_PREFS = HSApplication.getContext().getPackageName() + "_desktop"; // Main process
    public static final String PREF_KEY_FIVE_STAR_SHOWED_COUNT = "PREF_KEY_FIVE_STAR_SHOWED_COUNT";
    public static final String PREF_KEY_FIVE_STAR_SHOWED_TIME = "PREF_KEY_FIVE_STAR_SHOWED_TIME";
    public static final String PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_MSG = "PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_MSG";
    public static final String PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_EMOJI = "PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_EMOJI";
    public static final String PREF_KEY_HAD_FIVE_STAR_RATE = "pref_key_had_five_star_rate";
    public static final String PREF_KEY_BACK_TO_DESKTOP_TIMES = "PREF_KEY_BACK_TO_DESKTOP_TIMES";
    public static final String PREF_KEY_BACK_TO_DESKTOP_SHOW_COUNT = "PREF_KEY_BACK_TO_DESKTOP_SHOW_COUNT";
    public static final String PREF_KEY_BACK_TO_DESKTOP_SHOW_TIMES = "PREF_KEY_BACK_TO_DESKTOP_SHOW_TIMES";
    private static final long CHANGE_DURATION = 500;
    private static final long ONE_STEP_DURATION = 200;
    private static final long ANIM_DELAY = 200;
    private static final long ALL_STEPS_DURATION = 5 * ONE_STEP_DURATION;

    private static String sFiveStarRateTipFrom;

    public enum From {
        SEND_MESSAGE(0),
        SEND_EMOJI(1),
        QUIT_APP(2),
        SETTING(3);

        private int code = 0;

        From(int code) {
            this.code = code;
        }

        public int value() {
            return this.code;
        }

        public String toString() {
            switch (this.code) {
                case 0:
                    return "From Send First Message";
                case 1:
                    return "From Send First Emoji";
                case 2:
                    return "From Quit App";
                case 3:
                    return "From Settings";
                default:
                    return "Others";
            }
        }
    }

    private From mFrom;

    private static final int MAX_ANIM_COUNT = 2;
    private static final int INVALID_POSITION = -1;
    private static final int STAR_COUNT = 5;
    private static final int MAX_POSITION = STAR_COUNT - 2;
    private static final int MIN_POSITION = 0;
    private static final float PERCENT_ZERO = 0.0f;
    private static final float PERCENT_ONE_STAR = 0.342f;
    private static final float PERCENT_TWO_STAR = 0.684f;
    private static final float PERCENT_THREE_STAR = 0.763f;
    private static final float PERCENT_FOUR_STAR = 0.842f;
    private static final float PERCENT_FIVE_STAR = 1.0f;
    private static final float[] mTimePoint = {
            PERCENT_ZERO, PERCENT_ONE_STAR, PERCENT_TWO_STAR, PERCENT_THREE_STAR, PERCENT_FOUR_STAR, PERCENT_FIVE_STAR
    };
    private static final int[] mDescTexts = {
            R.string.five_star_one_text, R.string.five_star_two_text, R.string.five_star_three_text,
            R.string.five_star_four_text, R.string.five_star_five_text
    };
    private static final int[] mDialogDescTexts = {
            R.string.liked_it_desc,
            R.string.liked_it_desc,
            R.string.liked_it_desc,
            R.string.liked_it_desc,
            R.string.loved_it_desc
    };

    private boolean mAnimViewShowed = false;
    private DisplayMetrics mMetrics;
    private AppCompatImageView[] mStarViews;
    private AppCompatImageView[] mGuideStarViews;
    private LinearLayout mGuideLayout;
    private FiveStarLayout mRateLayout;
    private LottieAnimationView mAnimationView;
    private Cancellable mAnimationLoadTask;
    private ImageView mStillView;
    private TypefacedTextView mStarDescView;
    private ImageView mHandImageView;
    private int mCurrentPosition = INVALID_POSITION;
    private TextView mDescTv;
    private View mRateButton;
    private AnimatorSet mAnimatorSet;
    private int mAnimCount;

    public FiveStarRateDialog(Activity context, From from) {
        super(context);
        mMetrics = context.getResources().getDisplayMetrics();
        mFrom = from;
        mStarViews = new AppCompatImageView[STAR_COUNT];
        mGuideStarViews = new AppCompatImageView[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            mStarViews[i] = new AppCompatImageView(context);
            mStarViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mStarViews[i].setImageResource(R.drawable.star_gray);
            mStarViews[i].setTag(i);
            mStarViews[i].setOnClickListener(this);

            mGuideStarViews[i] = new AppCompatImageView(context);
            mGuideStarViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mGuideStarViews[i].setImageResource(R.drawable.star_gray);
        }
    }

    @Override
    protected View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v = inflater.inflate(R.layout.dialog_five_star, root, false);
        mDescTv = ViewUtils.findViewById(v, R.id.dialog_desc);
        mDescTv.setText(getResources().getString(R.string.five_star_hint_text));

        // Add LottieAnimationView into TopImage container.
        View faceView = inflater.inflate(R.layout.dialog_five_start_face, root, false);
        mStillView = ViewUtils.findViewById(faceView, R.id.still_view);
        mStillView.setVisibility(GONE);
        mAnimationView = ViewUtils.findViewById(faceView, R.id.animation_view);
        ViewGroup imageContainer = ViewUtils.findViewById(mRootView, R.id.dialog_image_container);
        imageContainer.addView(faceView);
        mStarDescView = ViewUtils.findViewById(v, R.id.star_desc);

        mRateLayout = ViewUtils.findViewById(v, R.id.rate_area);
        mRateLayout.setOnMoveListener(new FiveStarLayout.OnMoveListener() {
            @Override
            public void onMove(boolean isToRight, int position, float progress) {
                onMoveChangingView(isToRight, position, progress);
            }

            @Override
            public void onUp(int position) {
                onUpChangingView(position);
            }
        });
        mGuideLayout = ViewUtils.findViewById(v, R.id.guide_rate_area);
        mRateButton = mRootView.findViewById(R.id.ok_btn);

        mRateLayout.setGravity(Gravity.CENTER);
        mGuideLayout.setGravity(Gravity.CENTER);
        for (int i = 0; i < STAR_COUNT; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Dimensions.pxFromDp(26.4f), 1);
            mRateLayout.addView(mStarViews[i], params);
            mGuideLayout.addView(mGuideStarViews[i], params);
        }

        mHandImageView = ViewUtils.findViewById(v, R.id.hand_img);
        loadAnimation();
        mAnimCount = 0;
        return v;
    }

    @Override
    protected void initButtons() {
        super.initButtons();
        mRateButton.setActivated(false);
    }

    private void loadAnimation() {
        cancelAnimationLoadTask();
        try {
            mAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                    "lottie/five_star_rating.json", lottieComposition -> {
                        if (!mAnimViewShowed) {
                            mAnimViewShowed = true;
                            mAnimationView.setComposition(lottieComposition);
                            guideAnim(true);
                            show();
                        }
                    });
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getPositiveButtonStringId() {
        return R.string.five_star_positive_text;
    }

    @Override protected int getNegativeButtonStringId() {
        return android.R.string.cancel;
    }

    @Override
    protected void onClickPositiveButton(View v) {
        if (mCurrentPosition >= 0) {
            BugleAnalytics.logEvent("Alert_FiveStar_Submit_BtnClicked", "type", (mCurrentPosition + 1) + "star");
            if (mCurrentPosition >= MAX_POSITION) {
                HSMarketUtils.browseAPP();
                PermissionGuideManager.getInstance().showPermissionGuide(HSApplication.getContext(),
                        PermissionGuideManager.PermissionGuideType.FIVE_STAR_RATE, false);
                logGuideShown();
            } else {
                Intent intent = new Intent(mActivity, FeedbackActivity.class);
                intent.putExtra(FeedbackActivity.INTENT_KEY_LAUNCH_FROM, FeedbackActivity.LAUNCH_FROM_RATING);
                mActivity.startActivity(intent);
            }
            haveDone();
            super.onClickPositiveButton(v);
        } else {
            logSubmitNoStarClick();
            guideAnim(false);
        }
    }

    @Override
    protected void onClickNegativeButton(View v) {
    }

    @Override
    protected void onCanceled() {
        BugleAnalytics.logEvent("Alert_FiveStar_Closed");
    }

    @Override
    protected Drawable getTopImageDrawable() {
        if (mTopImageDrawable == null) {
            mTopImageDrawable = ContextCompat.getDrawable(mActivity, R.drawable.dialog_five_star_top);
        }
        return mTopImageDrawable;
    }

    @Override
    protected boolean fitImageWidth() {
        return true;
    }

    private void onMoveChangingView(boolean isToRight, int position, float progress) {
        if ((isToRight && mCurrentPosition < MAX_POSITION) || (!isToRight && mCurrentPosition > MIN_POSITION)) {
            configureStarViews(position);
            mRateButton.setActivated(true);
            mCurrentPosition = position;
            mAnimationView.setProgress(progress);
        }
    }

    private void onUpChangingView(int position) {
        if (position == mCurrentPosition) {
            logStarClick(position);
            float progress = mTimePoint[getTimePosition(position)];
            mAnimationView.setProgress(progress);
        }
    }

    private void configureStarViews(int position) {
        if (position > mCurrentPosition) {
            for (int k = mCurrentPosition == -1 ? 0 : mCurrentPosition; k <= position; k++) {
                mStarViews[k].setImageResource(R.drawable.star_orange);
            }
        } else {
            for (int i = position + 1; i <= mCurrentPosition; i++) {
                mStarViews[i].setImageResource(R.drawable.star_gray);
            }
        }
        mStarDescView.setText(getResources().getString(mDescTexts[position]));
        mDescTv.setText(getResources().getString(mDialogDescTexts[position]));
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        if (tag == mCurrentPosition) {
            return;
        }
        logStarClick(tag);
        configureStarViews(tag);
        mRateButton.setActivated(true);
        int last = mCurrentPosition < 0 ? 3 : (mCurrentPosition < 2 ? 0 : mCurrentPosition);
        getAnimator(mTimePoint[getTimePosition(last)], mTimePoint[getTimePosition(tag)]).setDuration(CHANGE_DURATION).start();
        mCurrentPosition = tag;
    }

    private int getTimePosition(int position) {
        return position + 1;
    }

    private void initAnim() {
        ObjectAnimator[] objectAnimatorArray = new ObjectAnimator[mGuideStarViews.length];
        for (int i = 0; i < mGuideStarViews.length; i++) {
            objectAnimatorArray[i] = ObjectAnimator.ofFloat(mGuideStarViews[i], "alpha", 0f, 1f).setDuration(ONE_STEP_DURATION);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimCount++;
                mGuideLayout.setVisibility(VISIBLE);
                for (int i = 0; i < mGuideStarViews.length; i++) {
                    mGuideStarViews[i].setImageResource(R.drawable.star_orange);
                    mGuideStarViews[i].setAlpha(0f);
                }
                mHandImageView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mGuideLayout.setVisibility(GONE);
                mHandImageView.setVisibility(GONE);
                if (mAnimCount < MAX_ANIM_COUNT) {
                    guideAnim(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mGuideLayout.setVisibility(GONE);
                mHandImageView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        ValueAnimator animator = getAnimator(PERCENT_ONE_STAR, PERCENT_FIVE_STAR).setDuration(ALL_STEPS_DURATION);
        int dis = Dimensions.pxFromDp(188);
        ObjectAnimator handAnimator = ObjectAnimator.ofFloat(mHandImageView, "translationX", dis).setDuration(ALL_STEPS_DURATION);
        mAnimatorSet.playSequentially(
                objectAnimatorArray[0],
                objectAnimatorArray[1],
                objectAnimatorArray[2],
                objectAnimatorArray[3],
                objectAnimatorArray[4]
        );
        mAnimatorSet.playTogether(animator, handAnimator);
    }

    private void guideAnim(boolean isDelay) {
        if (mAnimatorSet == null) {
            initAnim();
        }
        if (isDelay) {
            Threads.postOnMainThreadDelayed(() -> {
                if (mAnimatorSet != null) {
                    mAnimatorSet.start();
                }
            }, ANIM_DELAY);
        } else {
            mAnimatorSet.start();
        }
    }

    private ValueAnimator getAnimator(final float from, float to) {
        if (mAnimationView == null || mAnimationView.getVisibility() != VISIBLE) {
            return null;
        }
        final float total = to - from;
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.addUpdateListener(animation -> {
            float progress = from + total * animation.getAnimatedFraction();
            mAnimationView.setProgress(progress);
        });
        return animator;
    }

    private void haveDone() {
        Preferences.get(DESKTOP_PREFS).putBoolean(FiveStarRateDialog.PREF_KEY_HAD_FIVE_STAR_RATE, true);
    }

    @Override protected void onShow() {
        super.onShow();

        boolean countThis = true;
        Preferences pref = Preferences.get(DESKTOP_PREFS);
        switch (mFrom) {
            case SEND_MESSAGE:
                pref.putBoolean(FiveStarRateDialog.PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_MSG, true);
                break;
            case SEND_EMOJI:
                pref.putBoolean(FiveStarRateDialog.PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_EMOJI, true);
                break;
            case QUIT_APP:
                pref.incrementAndGetInt(FiveStarRateDialog.PREF_KEY_BACK_TO_DESKTOP_SHOW_COUNT);
                pref.putInt(FiveStarRateDialog.PREF_KEY_BACK_TO_DESKTOP_SHOW_TIMES, pref.getInt(PREF_KEY_BACK_TO_DESKTOP_TIMES, 0));
                break;
            case SETTING:
            default:
                countThis = false;
                break;

        }
        if (countThis) {
            pref.incrementAndGetInt(FiveStarRateDialog.PREF_KEY_FIVE_STAR_SHOWED_COUNT);
        }
        pref.putLong(FiveStarRateDialog.PREF_KEY_FIVE_STAR_SHOWED_TIME, System.currentTimeMillis());
        logAlertFiveStarViewedFrom(mFrom.toString());
    }

    @Override
    protected void onDismissComplete() {
        super.onDismissComplete();
        cancelAnimationLoadTask();
    }

    private void cancelAnimationLoadTask() {
        if (mAnimationLoadTask != null) {
            mAnimationLoadTask.cancel();
            mAnimationLoadTask = null;
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
    }

    private void logStarClick(int position) {
        BugleAnalytics.logEvent("Alert_FiveStar_Star_Clicked", "type", (position + 1) + "star");
    }

    private void logSubmitNoStarClick() {
        BugleAnalytics.logEvent("Alert_FiveStar_Nostar_BtnClicked", "from", sFiveStarRateTipFrom);
    }

    private void logGuideShown() {
        BugleAnalytics.logEvent("Alert_FiveStar_Guide_Shown", "from", sFiveStarRateTipFrom);
    }

    private static void logAlertFiveStarViewedFrom(String from) {
        if (from != null) {
            sFiveStarRateTipFrom = from;
        }
        BugleAnalytics.logEvent("Alert_FiveStar_ViewedFrom", "from", sFiveStarRateTipFrom);
    }

    private static boolean timesCondition() {
        Preferences pref = Preferences.get(DESKTOP_PREFS);
        int times = pref.getInt(PREF_KEY_BACK_TO_DESKTOP_TIMES, 0);
        int lastShowTimes = pref.getInt(PREF_KEY_BACK_TO_DESKTOP_SHOW_TIMES, 0);
        int count = pref.getInt(PREF_KEY_BACK_TO_DESKTOP_SHOW_COUNT, 0);
        int shouldShowTimes;
        switch (count) {
            case 0:
                shouldShowTimes = 1;
                break;
            case 1:
                shouldShowTimes = 2;
                break;
            default:
            case 2:
                shouldShowTimes = 3;
                break;
        }

        return (times - lastShowTimes) >= shouldShowTimes;
    }

    private static boolean isHadFiveStarRate() {
        return Preferences.get(DESKTOP_PREFS).getBoolean(PREF_KEY_HAD_FIVE_STAR_RATE, false);
    }

    private static boolean isShowFiveStarRateTooMaxTimes() {
        return Preferences.get(DESKTOP_PREFS).getInt(PREF_KEY_FIVE_STAR_SHOWED_COUNT, 0) < 3;
    }

    private static boolean isShowFiveStarRateMoreThenInterval() {
        return System.currentTimeMillis() - Preferences.get(DESKTOP_PREFS).getLong(PREF_KEY_FIVE_STAR_SHOWED_TIME, 0)
                >= 30 * DateUtils.MINUTE_IN_MILLIS;
    }

    private static boolean shouldShowThisTime() {
        return !isHadFiveStarRate() && isShowFiveStarRateTooMaxTimes() && isShowFiveStarRateMoreThenInterval();
    }

    public static boolean showShowFiveStarRateDialogOnBackToDesktopIfNeed(Activity context) {
        Preferences.get(DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_BACK_TO_DESKTOP_TIMES);

        if (shouldShowThisTime() && timesCondition()) {
            showFiveStarRateDialog(context, From.QUIT_APP);
            return true;
        }
        return false;
    }

    public static boolean showFiveStarWhenSendMsgIfNeed(Activity context) {
        if (shouldShowThisTime() &&
                !Preferences.get(DESKTOP_PREFS).getBoolean(PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_MSG, false)) {
            showFiveStarRateDialog(context, From.SEND_MESSAGE);
            return true;
        }
        return false;
    }

    public static boolean showFiveStarWhenSendEmojiIfNeed(Activity context) {
        if (shouldShowThisTime() &&
                !Preferences.get(DESKTOP_PREFS).getBoolean(PREF_KEY_FIVE_STAR_SHOWED_AFTER_SEND_EMOJI, false)) {
            showFiveStarRateDialog(context, From.SEND_EMOJI);
            return true;
        }
        return false;
    }


    public static void showFiveStarFromSetting(Activity context) {
        showFiveStarRateDialog(context, From.SETTING);
    }

    private static void showFiveStarRateDialog(Activity context, From from) {
        Threads.postOnMainThreadDelayed(() ->
                new FiveStarRateDialog(context, from).show(), 100);
    }
}
