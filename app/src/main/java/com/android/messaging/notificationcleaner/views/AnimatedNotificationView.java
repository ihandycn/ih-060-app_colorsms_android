package com.android.messaging.notificationcleaner.views;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.messaging.BugleFiles;
import com.android.messaging.R;
import com.android.messaging.datamodel.NotificationServiceV18;
import com.android.messaging.notificationcleaner.NotificationBarUtil;
import com.android.messaging.notificationcleaner.NotificationCleanerConstants;
import com.android.messaging.notificationcleaner.activity.NCPermissionGuideActivity;
import com.android.messaging.notificationcleaner.activity.NotificationBlockedActivity;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.android.messaging.ui.welcome.NotificationAccessGuideActivity;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import static com.android.messaging.ui.welcome.NotificationAccessGuideActivity.DELAY_START_TO_PERMISSION_CHECK;
import static com.android.messaging.ui.welcome.NotificationAccessGuideActivity.DURATION_PERMISSION_CHECK_CONTINUED;
import static com.android.messaging.ui.welcome.NotificationAccessGuideActivity.INTERVAL_PERMISSION_CHECK;
import static com.android.messaging.ui.welcome.NotificationAccessGuideActivity.MSG_WHAT_NOTIFICATION_LISTENING_CANCEL;
import static com.android.messaging.ui.welcome.NotificationAccessGuideActivity.MSG_WHAT_NOTIFICATION_LISTENING_CHECK;

public class AnimatedNotificationView extends RelativeLayout {
    private static final int DELAY_ANIMATION_START = 200;
    private static final int DELAY_HORIZONTAL_ICONS_COLLAPSE = 200;
    private static final int DELAY_SHIELD_APPEAR = 300;

    private static final float RATIO_ANIMATION_CONTAINER_LEFT_START = 0.025f;
    private static final float RATIO_ANIMATION_CONTAINER_TOP_START = 0.1f;
    private static final float RATIO_ANIMATION_CONTAINER_WIDTH = 0.94f;
    private static final float RATIO_ANIMATION_CONTAINER_HEIGHT = 0.75f;
    private static final float RATIO_ANIMATED_SHIELD_WIDTH = 0.3f;
    private static final float RATIO_ANIMATED_SHIELD_HEIGHT = 0.3f;

    private AnimatedHorizontalIcons animatedHorizontalIcons;
    private AnimatedNotificationHeader animatedNotificationHeader;
    private AnimatedNotificationGroup animateNotificationGroup;
    private AnimatedShield animatedShield;

    private View animatedPhoneFrameView;
    private ImageView phoneBackgroundImageView;
    private LinearLayout animationContainerLayout;
    private FlashButton mStartButton;
    private boolean mIsMainPageGuide;
    private boolean shouldButtonFlash = true;

    @SuppressLint("HandlerLeak") // This handler holds activity reference for no longer than 120s
    private Handler handler = new Handler();

    @SuppressLint("HandlerLeak") // This mCheckPermissionHandler holds activity reference for no longer than 120s
    private Handler mCheckPermissionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_NOTIFICATION_LISTENING_CHECK:
                    if (!Permissions.isNotificationAccessGranted()) {
                        sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CHECK, INTERVAL_PERMISSION_CHECK);
                        break;
                    }
                    BugleAnalytics.logEvent("NotificationCleaner_AccessGuide_Success", true);
                    Intent intentSelf = new Intent(HSApplication.getContext(), NotificationBlockedActivity.class);
                    intentSelf.putExtra(NotificationBlockedActivity.START_FROM, NotificationBlockedActivity.START_FROM_GUIDE_BAR);
                    intentSelf.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(intentSelf);
                    break;
                case MSG_WHAT_NOTIFICATION_LISTENING_CANCEL:
                    removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
                    break;
                default:
                    break;
            }
        }
    };

    public AnimatedNotificationView(Context context) {
        this(context, null);
    }

    public AnimatedNotificationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedNotificationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIsMainPageGuide(boolean isMainPageGuide) {
        this.mIsMainPageGuide = isMainPageGuide;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        animatedPhoneFrameView = findViewById(R.id.animated_notification_phone_frame);

        if (getResources().getDisplayMetrics().densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            animatedPhoneFrameView.setScaleX(0.9f);
            animatedPhoneFrameView.setScaleY(0.9f);
        }

        phoneBackgroundImageView = findViewById(R.id.animated_notification_phone_background);

        animationContainerLayout = findViewById(R.id.animated_notification_container);
        animatedHorizontalIcons = findViewById(R.id.horizontal_icons);
        animatedShield = findViewById(R.id.animated_shield);
        animateNotificationGroup = findViewById(R.id.expand_notification_group);
        animatedNotificationHeader = findViewById(R.id.shrink_drawer_notification_header);

        mStartButton = findViewById(R.id.notification_activate_button);
        mStartButton.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color),
                getResources().getColor(R.color.ripples_ripple_color), Dimensions.pxFromDp(3.3f), true, true));
        mStartButton.setOnClickListener(v -> {
            NotificationCleanerProvider.switchNotificationOrganizer(true);
            boolean isNotificationAccessGranted = Permissions.isNotificationAccessGranted();
            BugleAnalytics.logEvent("NotificationCleaner_Guide_BtnClick", true);
            if (isNotificationAccessGranted) {
                NotificationBarUtil.checkToUpdateBlockedNotification();
                sendGetActiveNotificationBroadcast();
                Intent intentBlocked = new Intent(getContext(), NotificationBlockedActivity.class);
                intentBlocked.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Preferences.get(BugleFiles.NOTIFICATION_PREFS)
                        .putLong(NotificationCleanerConstants.NOTIFICATION_CLEANER_USAGE_TIME, System.currentTimeMillis());
                Navigations.startActivitySafely(getContext(), intentBlocked);
                ((Activity) getContext()).finish();
            } else {
//                Utils.requestNotificationListeningPermission(HSApplication.getContext(), () -> {
//                    BugleAnalytics.logEvent("NotificationCleaner_OpenSuccess");
//                    sendGetActiveNotificationBroadcast();
//                    SettingLauncherPadActivity.closeSettingsActivity(getContext());
//                    if (AnimatedNotificationView.this.getContext() instanceof NotificationGuideActivity) {
//                        Intent intentSelf = new Intent(HSApplication.getContext(), NotificationGuideActivity.class);
//                        intentSelf.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
//                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
//                        Navigations.startActivitySafely(getContext(), intentSelf);
//                    }
//                    NCFloatWindowController.getInstance().removePermissionGuideTipWithAnimation();
//                    NCFloatWindowController.getInstance().removePermissionGuideFloatButton();
//                }, NotificationCleanerUtil.NotificationCleanerSourceType.GUIDE_START, "");
                Intent intent = new Intent(NotificationAccessGuideActivity.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                getContext().startActivity(intent);
                Threads.postOnMainThreadDelayed(()-> {
                    Navigations.startActivity(getContext(), NCPermissionGuideActivity.class);
                }, 800);
                mCheckPermissionHandler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
                mCheckPermissionHandler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CHECK, DELAY_START_TO_PERMISSION_CHECK);
                mCheckPermissionHandler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CANCEL, DURATION_PERMISSION_CHECK_CONTINUED);
            }
        });

        phoneBackgroundImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                phoneBackgroundImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int phoneFrameWidth = Dimensions.pxFromDp(230);
                int phoneFrameHeight = Dimensions.pxFromDp(452);

                float containerLeftStart = phoneFrameWidth * RATIO_ANIMATION_CONTAINER_LEFT_START;
                float containerTopStart = phoneFrameHeight * RATIO_ANIMATION_CONTAINER_TOP_START;
                int containerWidth = (int) (phoneFrameWidth * RATIO_ANIMATION_CONTAINER_WIDTH);
                int containerHeight = (int) (phoneFrameHeight * RATIO_ANIMATION_CONTAINER_HEIGHT);

                FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(containerWidth, containerHeight);
                animationContainerLayout.setX(containerLeftStart);
                animationContainerLayout.setY(containerTopStart);
                animationContainerLayout.setLayoutParams(containerParams);

                LayoutParams shieldParams = (LayoutParams) animatedShield.getLayoutParams();
                shieldParams.width = (int) (phoneFrameWidth * RATIO_ANIMATED_SHIELD_WIDTH);
                shieldParams.height = (int) (phoneFrameHeight * RATIO_ANIMATED_SHIELD_HEIGHT);
                animatedShield.setLayoutParams(shieldParams);
            }
        });

        initCallbacks();
    }

    private void initCallbacks() {
        animatedNotificationHeader.setOnHeaderAnimationFinishListener(new AnimatedNotificationHeader.OnHeaderAnimationFinishListener() {
            @Override
            public void onLastItemCollapsed() {
                animateNotificationGroup.collapseStayItems();
                animatedHorizontalIcons.postDelayed(() -> animatedHorizontalIcons.collapseHorizontalIcons(), DELAY_HORIZONTAL_ICONS_COLLAPSE);
            }

            @Override
            public void onHeaderAnimated() {

            }
        });

        animateNotificationGroup.setOnAnimationFinishListener(new AnimatedNotificationGroup.OnAnimationFinishListener() {
            @Override
            public void onExpandFinish() {
                handler.postDelayed(() -> animatedShield.enlargeAndRotateAnimation(), DELAY_SHIELD_APPEAR);
            }

            @Override
            public void onStayItemCollapseFinish() {
                if (mStartButton != null) {
                    mStartButton.setRepeatCount(ValueAnimator.INFINITE);
                    if (shouldButtonFlash) {
                        mStartButton.startFlash();
                    }
                }
            }
        });

        animatedShield.setOnAnimationFinishListener(() -> {
            animatedNotificationHeader.setVisibility(View.VISIBLE);
            animateNotificationGroup.collapseNotificationItems(animatedNotificationHeader);
        });
    }

    public void startAnimations() {
        handler.postDelayed(() -> {
            animatedHorizontalIcons.expandHorizontalIcons();
            animateNotificationGroup.expandNotificationItems();
        }, DELAY_ANIMATION_START);
    }

    public static void sendGetActiveNotificationBroadcast() {
        HSLog.d(NotificationServiceV18.TAG, "NotificationGuideActivity sendGetActiveNotificationBroadcast");
        Intent broadcastReceiverIntent = new Intent(NotificationServiceV18.ACTION_NOTIFICATION_GET_CURRENT_ACTIVE);
        broadcastReceiverIntent.setPackage(HSApplication.getContext().getPackageName());
        HSApplication.getContext().sendBroadcast(broadcastReceiverIntent);
    }

    public void stopButtonFlash() {
        if (mStartButton != null) {
            mStartButton.stopFlash();
            shouldButtonFlash = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCheckPermissionHandler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CANCEL);
        mCheckPermissionHandler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
    }
}
