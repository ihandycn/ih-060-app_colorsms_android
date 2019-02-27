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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.appsettings.ChangeFontActivity;
import com.android.messaging.ui.appsettings.ChooseFontDialog;
import com.android.messaging.ui.appsettings.ThemeSelectActivity;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.CustomBubblesActivity;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.EmojiStoreActivity;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.ui.wallpaper.WallpaperPreviewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.Trace;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.TypefacedTextView;

import org.qcode.fontchange.FontManager;

public class ConversationListActivity extends AbstractConversationListActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String EVENT_MAINPAGE_RECREATE = "event_mainpage_recreate";

    private static final String PREF_SHOW_EMOJI_GUIDE = "pref_show_emoji_guide";
    private static final String PREF_KEY_MAIN_DRAWER_OPENED = "pref_key_main_drawer_opened";

    private static boolean sIsRecreate = false;

    private static final int DRAWER_INDEX_NONE = -1;
    private static final int DRAWER_INDEX_THEME_COLOR = 0;
    private static final int DRAWER_INDEX_BUBBLE = 1;
    private static final int DRAWER_INDEX_CHAT_BACKGROUND = 2;
    private static final int DRAWER_INDEX_SETTING = 3;
    private static final int DRAWER_INDEX_RATE = 4;
    private static final int DRAWER_INDEX_CHANGE_FONT = 5;

    private int drawerClickIndex = DRAWER_INDEX_NONE;

    // Drawer related stuff
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView mTitleTextView;
    private View mEmojiStoreIconView;
    private View mEmojiStoreCircleView;
    private ViewGroup mGuideContainer;
    private View mTriangleShape;

    private boolean mShowRateAlert = false;

    private Handler mAnimHandler;

    private static boolean mIsNoActionBack = true;
    private boolean mIsRealCreate = false;
    private boolean isScreenOn;

    private enum AnimState {
        NONE,
        APPEAR,
        SHOWING,
        DISAPPEAR,
    }

    private AnimState mAnimState = AnimState.NONE;


    private boolean mIsEmojiStoreClickable = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        super.onCreate(savedInstanceState);
        if (Factory.sIsRedirectToWelcome) {
            return;
        }
        mIsRealCreate = true;
        setContentView(R.layout.conversation_list_activity);
        configAppBar();
        showEmojiStoreGuide();
        mIsNoActionBack = true;

        if (sIsRecreate) {
            sIsRecreate = false;
        } else {
            Preferences.get(FiveStarRateDialog.DESKTOP_PREFS).incrementAndGetInt(FiveStarRateDialog.PREF_KEY_MAIN_ACTIVITY_SHOW_TIME);
            FiveStarRateDialog.showFiveStarWhenMainPageShowIfNeed(this);
        }

        if (getIntent() != null && getIntent().getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true);
        }

        setupDrawer();

        String bgPath = WallpaperManager.getWallpaperPathByThreadId(null);
        String backgroundStr;
        if (TextUtils.isEmpty(bgPath)) {
            backgroundStr = "default";
        } else if (bgPath.contains("_1.png")) {
            backgroundStr = "customize";
        } else {
            backgroundStr = "colorsms";
        }
        HSGlobalNotificationCenter.addObserver(EVENT_MAINPAGE_RECREATE, this);
        BugleAnalytics.logEvent("SMS_Messages_Show", true,
                "themeColor", String.valueOf(ThemeSelectActivity.getSelectedIndex()),
                "background", backgroundStr,
                "bubbleStyle", String.valueOf(BubbleDrawables.getSelectedIdentifier()),
                "received bubble color", ConversationColors.get().getConversationColorEventType(true, true),
                "sent bubble color", ConversationColors.get().getConversationColorEventType(false, true),
                "received text color", ConversationColors.get().getConversationColorEventType(true, false),
                "sent text color", ConversationColors.get().getConversationColorEventType(false, false));

        BugleAnalytics.logEvent("SMS_Messages_Show_1", true,
                "font", Preferences.getDefault().getString(TypefacedTextView.MESSAGE_FONT_FAMILY, "Default"),
                "size", getResources().getString(ChangeFontActivity.sTextSizeRes[BuglePrefs.getApplicationPrefs().getInt(FontManager.MESSAGE_FONT_SCALE, 2)])
        );

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
            mEmojiStoreIconView.setVisibility(View.VISIBLE);
        }
        //update statusBar color
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));

        super.updateActionBar(actionBar);

        setDrawerMenuIcon();
    }

    private void setupDrawer() {
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setPadding(0, Dimensions.getStatusBarInset(this), 0, 0);

        drawerLayout = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {

                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerClickIndex = DRAWER_INDEX_NONE;
                Preferences.getDefault().putBoolean(PREF_KEY_MAIN_DRAWER_OPENED, true);
                setDrawerMenuIcon();
                BugleAnalytics.logEvent("Menu_Show", true);
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                switch (drawerClickIndex) {
                    case DRAWER_INDEX_THEME_COLOR:
                        BugleAnalytics.logEvent("Menu_ThemeColor_Click");
                        Navigations.startActivity(ConversationListActivity.this, ThemeSelectActivity.class);
                        break;
                    case DRAWER_INDEX_BUBBLE:
                        BugleAnalytics.logEvent("Menu_Bubble_Click");
                        Navigations.startActivity(ConversationListActivity.this, CustomBubblesActivity.class);
                        break;
                    case DRAWER_INDEX_CHAT_BACKGROUND:
                        BugleAnalytics.logEvent("Menu_ChatBackground_Click");
                        WallpaperPreviewActivity.startWallpaperPreview(ConversationListActivity.this);
                        break;
                    case DRAWER_INDEX_CHANGE_FONT:
                        BugleAnalytics.logEvent("Menu_ChangeFont_Click");
                        Intent intent = new Intent(ConversationListActivity.this, ChangeFontActivity.class);
                        startActivity(intent);
                        break;
                    case DRAWER_INDEX_SETTING:
                        UIIntents.get().launchSettingsActivity(ConversationListActivity.this);
                        BugleAnalytics.logEvent("Menu_Settings_Click", true);
                        break;
                    case DRAWER_INDEX_RATE:
                        FiveStarRateDialog.showFiveStarFromSetting(ConversationListActivity.this);
                        BugleAnalytics.logEvent("Menu_FiveStart_Click", true);
                        break;
                    case DRAWER_INDEX_NONE:
                    default:
                        break;
                }

                drawerClickIndex = DRAWER_INDEX_NONE;
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        View navigationContent = getLayoutInflater().inflate(R.layout.layout_main_navigation, navigationView, false);
        navigationView.addView(navigationContent);

        navigationContent.findViewById(R.id.navigation_item_theme_color).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_bubble).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_chat_background).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_change_font).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_setting).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_rate).setOnClickListener(this);

        setDrawerMenuIcon();
    }

    @SuppressWarnings("RestrictedApi")
    private void setDrawerMenuIcon() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable drawable;
        if (!Preferences.getDefault().getBoolean(PREF_KEY_MAIN_DRAWER_OPENED, false)) {
            drawable = AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_navigation_drawer_dot);
        } else {
            drawable = AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_navigation_drawer);
        }
        getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FiveStarRateDialog.dismissDialogs();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
            return;
        }

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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (mActionMode != null &&
                mActionMode.getCallback().onActionItemClicked(mActionMode, menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (mActionMode != null) {
                    dismissActionMode();
                    return true;
                } else {
                    drawerLayout.openDrawer(navigationView);
                    return true;
                }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        mTitleTextView.setVisibility(View.GONE);
        stopEmojiStoreGuide();
        mEmojiStoreIconView.setVisibility(View.GONE);
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
        invalidateActionBar();

        setupToolbarUI();
    }

    private void setupToolbarUI() {
        mTitleTextView = findViewById(R.id.toolbar_title);
        mAnimHandler = new Handler();
        mGuideContainer = findViewById(R.id.emoji_store_guide_content);
        mTriangleShape = findViewById(R.id.emoji_store_guide_triangle);
        mEmojiStoreIconView = findViewById(R.id.emoji_store_icon);
        mEmojiStoreIconView.setScaleX(1f);
        mEmojiStoreIconView.setScaleY(1f);
        mEmojiStoreIconView.setOnClickListener(v -> {
            if (!mIsEmojiStoreClickable) {
                return;
            }
            logFirstComeInClickEvent("emojistore");
            BugleAnalytics.logEvent("SMS_Messages_Emojistore_Click", true);
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
        mEmojiStoreIconView.setOnTouchListener(new View.OnTouchListener() {
            boolean touch = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!touch) {
                            touch = true;
                            mEmojiStoreIconView.animate().cancel();
                            mEmojiStoreIconView.animate()
                                    .scaleX(0.7f)
                                    .scaleY(0.7f)
                                    .setDuration(200L)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (touch) {
                            touch = false;
                            mEmojiStoreIconView.animate().cancel();
                            mEmojiStoreIconView.animate()
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
        mEmojiStoreCircleView = findViewById(R.id.emoji_store_circle);

        mAnimHandler.postDelayed(() -> {
            mEmojiStoreCircleView.setVisibility(View.VISIBLE);
            mEmojiStoreCircleView.setScaleX(0.9f);
            mEmojiStoreCircleView.setScaleY(0.9f);
            mEmojiStoreCircleView.setAlpha(1f);
            mEmojiStoreCircleView.animate()
                    .scaleX(2.2f)
                    .scaleY(2.2f)
                    .alpha(0f)
                    .setDuration(1000L)
                    .setInterpolator(PathInterpolatorCompat.create(0.1f, 0.95f))
                    .withEndAction(() -> mEmojiStoreCircleView.setVisibility(View.GONE))
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
            mGuideContainer.setVisibility(View.GONE);
            mTriangleShape.setVisibility(View.GONE);
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

    public static void logFirstComeInClickEvent(String type) {
        if (!type.equals("no_action")) {
            mIsNoActionBack = false;
        }
        Preferences.getDefault().doOnce(new Runnable() {
            @Override
            public void run() {
                BugleAnalytics.logEvent("SMS_Messages_First_Click", true, "type", type);
            }
        }, "pref_first_come_in_click_event");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mIsRealCreate) {
            return;
        }
        if (mIsNoActionBack) {
            logFirstComeInClickEvent("no_action");
        }
    }

    private void stopEmojiStoreGuide() {
        if (mAnimState != AnimState.NONE) {
            if (mGuideContainer != null) {
                mGuideContainer.setVisibility(View.GONE);
            }
            if (mTriangleShape != null) {
                mTriangleShape.setVisibility(View.GONE);
            }
            if (mEmojiStoreCircleView != null) {
                mEmojiStoreCircleView.setVisibility(View.GONE);
            }
            mAnimState = AnimState.NONE;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation_item_theme_color:
                drawerClickIndex = DRAWER_INDEX_THEME_COLOR;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_bubble:
                drawerClickIndex = DRAWER_INDEX_BUBBLE;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_chat_background:
                drawerClickIndex = DRAWER_INDEX_CHAT_BACKGROUND;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_change_font:
                drawerClickIndex = DRAWER_INDEX_CHANGE_FONT;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_setting:
                drawerClickIndex = DRAWER_INDEX_SETTING;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_rate:
                drawerClickIndex = DRAWER_INDEX_RATE;
                drawerLayout.closeDrawer(navigationView);
                break;
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_MAINPAGE_RECREATE:
                sIsRecreate = true;
                recreate();
                break;
        }
    }
}
