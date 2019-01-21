/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.ui.conversationlist;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.EmojiStoreActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Trace;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

public class ConversationListActivity extends AbstractConversationListActivity {

    private static final String PREF_SHOW_EMOJI_GUIDE = "pref_show_emoji_guide";

    private TextView mTitleTextView;
    private View mSettingsBtn;

    private boolean mShowRateAlert = false;

    private Handler mAnimHandler;

    private enum AnimState {
        NONE,
        APPEAR,
        SHOWING,
        DISAPPEAR,
    }

    private AnimState mAnimState = AnimState.NONE;

    private ViewGroup mGuideContainer;
    private View mTriangleShape;

    private boolean mIsEmojiStoreClickable = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        super.onCreate(savedInstanceState);
        if (mIsRedirectToWelcome) {
            return;
        }
        setContentView(R.layout.conversation_list_activity);
        configAppBar();
        showEmojiStoreGuide();
        BugleAnalytics.logEvent("SMS_Messages_Show", true);
        Trace.endSection();
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();

        if (mTitleTextView != null && mTitleTextView.getVisibility() == View.GONE) {
            mTitleTextView.setVisibility(View.VISIBLE);
            mSettingsBtn.setVisibility(View.VISIBLE);
        }
        //update statusBar color
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));

        super.updateActionBar(actionBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FiveStarRateDialog.dismissDialogs();
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            if (mShowRateAlert || !FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
                BugleAnalytics.logEvent("SMS_Messages_Back", true);
                super.onBackPressed();
            } else {
                mShowRateAlert = true;
            }
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        mTitleTextView.setVisibility(View.GONE);
        mSettingsBtn.setVisibility(View.GONE);
        BugleAnalytics.logEvent("SMS_EditMode_Show", true);
        return super.startActionMode(callback);
    }

    @Override
    public void onActionBarHome() {
        exitMultiSelectState();
    }

    @Override
    public boolean isSwipeAnimatable() {
        return !isInConversationListSelectMode();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final ConversationListFragment conversationListFragment =
                (ConversationListFragment) getFragmentManager().findFragmentById(
                        R.id.conversation_list_fragment);
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
        if (hasFocus && conversationListFragment != null) {
            conversationListFragment.setScrolledToNewestConversationIfNeeded();
        }
    }

    private void configAppBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setContentInsetsRelative(0, 0);
        LayoutInflater.from(this).inflate(R.layout.conversation_list_toolbar_layout, toolbar, true);
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);

        setupToolbarUI();
    }

    private void setupToolbarUI() {
        mTitleTextView = findViewById(R.id.toolbar_title);
        mSettingsBtn = findViewById(R.id.setting_btn);
        mSettingsBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff,
                Dimensions.pxFromDp(20), true));
        mSettingsBtn.setOnClickListener(v -> {
            UIIntents.get().launchSettingsActivity(this);
            BugleAnalytics.logEvent("SMS_Mainpage_Settings_Click", true);
        });

        mAnimHandler = new Handler();
        mGuideContainer = findViewById(R.id.emoji_store_guide_content);
        mTriangleShape = findViewById(R.id.emoji_store_guide_triangle);
        View store = findViewById(R.id.emoji_store_icon);
        store.setScaleX(1f);
        store.setScaleY(1f);
        store.setOnClickListener(v -> {
            if (!mIsEmojiStoreClickable) {
                return;
            }
            if (mAnimState == AnimState.DISAPPEAR) {
                mIsEmojiStoreClickable = false;
                Threads.postOnMainThreadDelayed(() -> {
                    mIsEmojiStoreClickable = true;
                    EmojiStoreActivity.start(ConversationListActivity.this);
                }, 250);
            } else {
                EmojiStoreActivity.start(ConversationListActivity.this);
            }
        });
        store.setOnTouchListener(new View.OnTouchListener() {
            boolean touch = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!touch) {
                            touch = true;
                            store.animate().cancel();
                            store.animate()
                                    .scaleX(0.7f)
                                    .scaleY(0.7f)
                                    .setDuration(200L)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (touch) {
                            touch = false;
                            store.animate().cancel();
                            store.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start();
                        }
                        break;
                }
                return false;
            }
        });

    }

    private void showEmojiStoreGuide() {
        boolean isShowEmojiGuide = Preferences.getDefault().getBoolean(PREF_SHOW_EMOJI_GUIDE, true);
        if (!isShowEmojiGuide) {
            return;
        }
        Preferences.getDefault().putBoolean(PREF_SHOW_EMOJI_GUIDE, false);
        mAnimState = AnimState.APPEAR;
        View circle = findViewById(R.id.emoji_store_circle);

        mAnimHandler.postDelayed(() -> {
            circle.setVisibility(View.VISIBLE);
            circle.setScaleX(0.9f);
            circle.setScaleY(0.9f);
            circle.setAlpha(1f);
            circle.animate()
                    .scaleX(2.2f)
                    .scaleY(2.2f)
                    .alpha(0f)
                    .setDuration(1000L)
                    .setInterpolator(PathInterpolatorCompat.create(0.1f, 0.95f))
                    .withEndAction(() -> circle.setVisibility(View.INVISIBLE))
                    .start();
        }, 800L);

        mAnimHandler.postDelayed(() -> {
            for (int i = 0; i < mGuideContainer.getChildCount(); i++) {
                mGuideContainer.getChildAt(i).setVisibility(View.VISIBLE);
            }
        }, 1750L);

        mAnimHandler.postDelayed(() -> {
            mGuideContainer.setVisibility(View.VISIBLE);
            mGuideContainer.setScaleX(0f);
            mGuideContainer.setScaleY(0.7f);
            mGuideContainer.setPivotX(mGuideContainer.getWidth());
            mGuideContainer.setPivotY(mGuideContainer.getHeight() / 2);

            ValueAnimator widthAnimator = ValueAnimator.ofFloat(0f, .85f);
            widthAnimator.setDuration(350L);
            widthAnimator.setInterpolator(PathInterpolatorCompat.create(.7f, 2.02f, .57f, 1f));
            widthAnimator.addUpdateListener(v -> mGuideContainer.setScaleX((Float) v.getAnimatedValue()));
            widthAnimator.start();

            ValueAnimator heightAnimator = ValueAnimator.ofFloat(0.7f, 0.9f);
            heightAnimator.setDuration(350L);
            heightAnimator.setInterpolator(PathInterpolatorCompat.create(0.35f, 3.56f, .56f, 1f));
            heightAnimator.addUpdateListener(v -> mGuideContainer.setScaleY((Float) v.getAnimatedValue()));
            heightAnimator.start();
        }, 1500L);

        mAnimHandler.postDelayed(() -> {
            mGuideContainer.setPivotX(mGuideContainer.getWidth());
            ValueAnimator widthAnimator = ValueAnimator.ofFloat(.85f, 1f);
            widthAnimator.setDuration(200L);
            widthAnimator.setInterpolator(PathInterpolatorCompat.create(.23f, 0f, .71f, 1.0f));
            widthAnimator.addUpdateListener(v -> mGuideContainer.setScaleX((Float) v.getAnimatedValue()));
            widthAnimator.start();

            ValueAnimator heightAnimator = ValueAnimator.ofFloat(0.9f, 1f);
            heightAnimator.setDuration(200L);
            heightAnimator.setInterpolator(PathInterpolatorCompat.create(.45f, 0f, .66f, 2.0f));
            heightAnimator.addUpdateListener(v -> mGuideContainer.setScaleY((Float) v.getAnimatedValue()));
            heightAnimator.start();

            mTriangleShape.setVisibility(View.VISIBLE);
            mTriangleShape.setScaleX(0);
            mTriangleShape.setPivotX(0);
            ValueAnimator triangleAnimator = ValueAnimator.ofFloat(0f, 1f);
            triangleAnimator.setDuration(200L);
            triangleAnimator.setInterpolator(PathInterpolatorCompat.create(.6f, 1.8f));
            triangleAnimator.addUpdateListener(v -> mTriangleShape.setScaleX((Float) v.getAnimatedValue()));
            triangleAnimator.start();
        }, 1850L);

        mAnimHandler.postDelayed(() -> mAnimState = AnimState.SHOWING, 2050L);
    }

    private void hideStoreGuide() {
        mAnimState = AnimState.DISAPPEAR;
        mAnimHandler.removeCallbacksAndMessages(null);
        mGuideContainer.animate()
                .scaleX(0f)
                .setDuration(250L)
                .setInterpolator(PathInterpolatorCompat.create(.23f, 0f, .71f, 1.0f))
                .start();
        mAnimHandler.postDelayed(() -> {
            for (int i = 0; i < mGuideContainer.getChildCount(); i++) {
                mGuideContainer.getChildAt(i).setVisibility(View.INVISIBLE);
            }
            mTriangleShape.animate()
                    .scaleX(0f)
                    .setDuration(100L)
                    .start();
        }, 150L);
        mAnimHandler.postDelayed(() -> {
            mGuideContainer.setVisibility(View.INVISIBLE);
            mTriangleShape.setVisibility(View.INVISIBLE);
            mAnimState = AnimState.NONE;
        }, 250L);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mAnimState == AnimState.SHOWING) {
            hideStoreGuide();
        }
        return super.dispatchTouchEvent(event);
    }

}
