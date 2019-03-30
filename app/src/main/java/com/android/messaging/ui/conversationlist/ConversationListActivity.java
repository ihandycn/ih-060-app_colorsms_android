package com.android.messaging.ui.conversationlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.ui.CreateShortcutActivity;
import com.android.messaging.ui.DragHotSeatActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.ui.appsettings.ChangeFontActivity;
import com.android.messaging.ui.appsettings.ThemeSelectActivity;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.CustomBubblesActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.EmojiStoreActivity;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.android.messaging.ui.wallpaper.WallpaperChooserItem;
import com.android.messaging.ui.wallpaper.WallpaperDownloader;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.ui.wallpaper.WallpaperPreviewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.CreateShortcutUtils;
import com.android.messaging.util.MediaUtil;
import com.android.messaging.util.Trace;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.font.FontStyleManager;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Calendars;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Random;

import static com.android.messaging.ui.dialog.FiveStarRateDialog.DESKTOP_PREFS;
import static com.android.messaging.ui.dialog.FiveStarRateDialog.PREF_KEY_MAIN_ACTIVITY_SHOW_TIME;

public class ConversationListActivity extends AbstractConversationListActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String EVENT_MAINPAGE_RECREATE = "event_mainpage_recreate";
    public static final String SHOW_EMOJ = "show_emoj";

    private static final String PREF_SHOW_EMOJI_GUIDE = "pref_show_emoji_guide";
    public static final String PREF_KEY_MAIN_DRAWER_OPENED = "pref_key_main_drawer_opened";

    private static final String PREF_KEY_THEME_COLOR_CLICKED = "pref_key_navigation_theme_color_clicked";
    private static final String PREF_KEY_BUBBLE_CLICKED = "pref_key_navigation_bubble_clicked";
    private static final String PREF_KEY_BACKGROUND_CLICKED = "pref_key_navigation_background_clicked";
    private static final String PREF_KEY_FONT_CLICKED = "pref_key_navigation_font_clicked";

    public static final String EXTRA_FROM_DESKTOP_ICON = "extra_from_desktop_icon";
    public static final String PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN = "pref_key_create_shortcut_guide_shown";

    private static boolean sIsRecreate = false;

    private static final int DRAWER_INDEX_NONE = -1;
    private static final int DRAWER_INDEX_THEME_COLOR = 0;
    private static final int DRAWER_INDEX_BUBBLE = 1;
    private static final int DRAWER_INDEX_CHAT_BACKGROUND = 2;
    private static final int DRAWER_INDEX_SETTING = 3;
    private static final int DRAWER_INDEX_RATE = 4;
    private static final int DRAWER_INDEX_CHANGE_FONT = 5;
    private static final int DRAWER_INDEX_PRIVACY_BOX = 6;

    private int drawerClickIndex = DRAWER_INDEX_NONE;

    // Drawer related stuff
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView mTitleTextView;
    private View mEmojiStoreIconView;
    private LottieAnimationView mGuideContainer;
    private View statusbarInset;

    private boolean mShowRateAlert = false;

    private Handler mAnimHandler;

    private static boolean mIsNoActionBack = true;
    private boolean mIsRealCreate = false;
    private boolean mShowEndAnimation;
    private boolean hideAnimation;
    private boolean shouldShowCreateShortcutGuide;

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
        mIsNoActionBack = true;

        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_FROM_DESKTOP_ICON, false)) {
            BugleAnalytics.logEvent("SMS_Shortcut_Click");
        }

        if (sIsRecreate) {
            sIsRecreate = false;
        } else {
            Preferences.get(DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_MAIN_ACTIVITY_SHOW_TIME);
            FiveStarRateDialog.showFiveStarWhenMainPageShowIfNeed(this);
            if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                BugleAnalytics.logEvent("SMS_Messages_Show_NewUser", true);
            }
        }

        if (getIntent() != null && getIntent().getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true);
        }

        setupDrawer();

        HSGlobalNotificationCenter.addObserver(EVENT_MAINPAGE_RECREATE, this);
        HSGlobalNotificationCenter.addObserver(SHOW_EMOJ, this);
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);

        if (!sIsRecreate) {
            Threads.postOnThreadPoolExecutor(() -> {
                String bgPath = WallpaperManager.getWallpaperPathByThreadId(null);
                String backgroundStr;
                int wallpaperIndex = 99;
                if (TextUtils.isEmpty(bgPath)) {
                    backgroundStr = "default";
                    wallpaperIndex = 90;
                } else if (bgPath.contains("_1.png")) {
                    backgroundStr = "customize";
                } else {
                    for (int i = 0; i < WallpaperChooserItem.sRemoteUrl.length; i++) {
                        if (WallpaperDownloader.getAbsolutePath(WallpaperChooserItem.sRemoteUrl[i]).equals(bgPath)) {
                            wallpaperIndex = i;
                            break;
                        }
                    }
                    backgroundStr = "colorsms_" + wallpaperIndex;
                }

                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                BugleAnalytics.logEvent("SMS_Messages_Show", true,
                        "themeColor", String.valueOf(ThemeSelectActivity.getSelectedIndex()),
                        "background", backgroundStr,
                        "bubbleStyle", String.valueOf(BubbleDrawables.getSelectedIdentifier()),
                        "received bubble color", ConversationColors.get().getConversationColorEventType(true, true),
                        "sent bubble color", ConversationColors.get().getConversationColorEventType(true, false),
                        "received text color", ConversationColors.get().getConversationColorEventType(false, true),
                        "sent text color", ConversationColors.get().getConversationColorEventType(false, false));

                String size;
                switch (FontStyleManager.getInstance().getFontScaleLevel()) {
                    case 0:
                        size = "Smallest";
                        break;
                    case 1:
                        size = "Small";
                        break;
                    case 3:
                        size = "Medium";
                        break;
                    case 4:
                        size = "Large";
                        break;
                    case 5:
                        size = "Largest";
                        break;
                    default:
                        size = "Default";
                }

                BugleAnalytics.logEvent("SMS_Messages_Show_1", true,
                        "font", FontStyleManager.getInstance().getFontFamily(),
                        "size", size,
                        "open time", String.valueOf(hour),
                        "signature", String.valueOf(!TextUtils.isEmpty(Preferences.getDefault().getString(
                                SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null)))
                );

                if (Calendars.getDayDifference(System.currentTimeMillis(), CommonUtils.getAppInstallTimeMillis()) == 1) {
                    final int wallpaper = wallpaperIndex;
                    Preferences.getDefault().doOnce(() -> {
                        Resources res = Factory.get().getApplicationContext().getResources();
                        int bubbleSendFontColor = ConversationColors.get().getMessageTextColor(false);
                        int bubbleSendBgColor = ConversationColors.get().getBubbleBackgroundColor(false);
                        int bubbleRcvFontColor = ConversationColors.get().getMessageTextColor(true);
                        int bubbleRcvBgColor = ConversationColors.get().getBubbleBackgroundColor(true);
                        if (bubbleRcvBgColor != res.getColor(R.color.message_bubble_color_incoming)
                                || bubbleSendBgColor != res.getColor(R.color.message_bubble_color_outgoing)
                                || bubbleSendFontColor != res.getColor(R.color.message_text_color_outgoing)
                                || bubbleRcvFontColor != res.getColor(R.color.message_text_color_incoming)) {
                            //bubble font color or bg color has been changed
                            String fontType = FontStyleManager.getInstance().getFontFamily();
                            int fontSize = FontStyleManager.getInstance().getFontScaleLevel();
                            int bubbleStyle = BubbleDrawables.getSelectedIdentifier();
                            BugleAnalytics.logEvent("Customize_Analysis", true,
                                    "group" + new Random().nextInt(7),
                                    bubbleSendFontColor + "|" + bubbleSendBgColor + "|"
                                            + bubbleRcvFontColor + "|" + bubbleRcvBgColor + "|"
                                            + fontType + "|" + fontSize + "|" + bubbleStyle + "|"
                                            + wallpaper + "|" + ThemeSelectActivity.getSelectedIndex());
                        }
                    }, "pref_key_customize_config_has_send");
                }
            });
        }

        Trace.endSection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BugleAnalytics.logEvent("SMS_Messages_Show_Corrected", true);
        Preferences.getDefault().incrementAndGetInt(CustomizeGuideController.PREF_KEY_MAIN_PAGE_SHOW_TIME);
        WeakReference<AppCompatActivity> activity = new WeakReference<>(this);
        Threads.postOnMainThreadDelayed(() -> {
            if (!isFinishing() && activity.get() != null) {
                CustomizeGuideController.showGuideIfNeed(activity.get());
            }
        }, 1000);
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        statusbarInset.setBackgroundColor(PrimaryColors.getPrimaryColor());

        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(PrimaryColors.getPrimaryColor()));
        actionBar.show();

        if (mTitleTextView != null && mTitleTextView.getVisibility() == View.GONE) {
            mTitleTextView.setVisibility(View.VISIBLE);
            mEmojiStoreIconView.setVisibility(View.VISIBLE);
        }


        super.updateActionBar(actionBar);

        setDrawerMenuIcon();
    }

    private void setupDrawer() {
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setPadding(0, Dimensions.getStatusBarInset(this), 0, 0);
        View navigationContent = getLayoutInflater().inflate(R.layout.layout_main_navigation, navigationView, false);

        drawerLayout = findViewById(R.id.main_drawer_layout);
        drawerLayout.setBackgroundColor(PrimaryColors.getPrimaryColor());
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
                if (CommonUtils.isNewUser()
                        && Calendars.isSameDay(CommonUtils.getAppInstallTimeMillis(), System.currentTimeMillis())) {
                    BugleAnalytics.logEvent("Menu_Show_NewUser_TestPrivateBox");
                    BugleAnalytics.logEvent("Menu_Show_NewUser", true);
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                switch (drawerClickIndex) {
                    case DRAWER_INDEX_THEME_COLOR:
                        BugleAnalytics.logEvent("Menu_ThemeColor_Click");
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_ThemeColor_Click_NewUser", true);
                        }
                        Navigations.startActivity(ConversationListActivity.this, ThemeSelectActivity.class);
                        navigationContent.findViewById(R.id.navigation_item_theme_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_BUBBLE:
                        BugleAnalytics.logEvent("Menu_Bubble_Click", true);
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_Bubble_Click_NewUser", true);
                        }
                        Navigations.startActivity(ConversationListActivity.this, CustomBubblesActivity.class);
                        navigationContent.findViewById(R.id.navigation_item_bubble_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_CHAT_BACKGROUND:
                        BugleAnalytics.logEvent("Menu_ChatBackground_Click", true);
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_ChatBackground_Click_NewUser", true);
                        }
                        WallpaperPreviewActivity.startWallpaperPreview(ConversationListActivity.this);
                        navigationContent.findViewById(R.id.navigation_item_background_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_CHANGE_FONT:
                        BugleAnalytics.logEvent("Menu_ChangeFont_Click");
                        Intent intent = new Intent(ConversationListActivity.this, ChangeFontActivity.class);
                        navigationContent.findViewById(R.id.navigation_item_font_new_text).setVisibility(View.GONE);
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

        navigationView.addView(navigationContent);

        if (CommonUtils.isNewUser()) {
            if (!Preferences.getDefault().getBoolean(PREF_KEY_THEME_COLOR_CLICKED, false)) {
                View newMark = navigationContent.findViewById(R.id.navigation_item_theme_new_text);
                newMark.setVisibility(View.VISIBLE);
                newMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }

            if (!Preferences.getDefault().getBoolean(PREF_KEY_BUBBLE_CLICKED, false)) {
                View bubbleNewMark = navigationContent.findViewById(R.id.navigation_item_bubble_new_text);
                bubbleNewMark.setVisibility(View.VISIBLE);
                bubbleNewMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }

            if (!Preferences.getDefault().getBoolean(PREF_KEY_BACKGROUND_CLICKED, false)) {
                View newMark = navigationContent.findViewById(R.id.navigation_item_background_new_text);
                newMark.setVisibility(View.VISIBLE);
                newMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }
        }

        navigationContent.findViewById(R.id.navigation_item_theme_color).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_bubble).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_chat_background).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_change_font).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_setting).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_rate).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_privacy_box).setOnClickListener(this);

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

        int mainActivityCreateTime = Preferences.get(DESKTOP_PREFS).getInt(PREF_KEY_MAIN_ACTIVITY_SHOW_TIME, 0);
        if (mainActivityCreateTime >= 2 && !Preferences.getDefault().contains(PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN)) {
            Drawable smsIcon = CreateShortcutUtils.getSystemSMSIcon();
            if (smsIcon != null && ShortcutManagerCompat.isRequestPinShortcutSupported(HSApplication.getContext())) {
                shouldShowCreateShortcutGuide = true;
                Preferences.getDefault().putBoolean(PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN, true);
                Navigations.startActivitySafely(ConversationListActivity.this,
                        new Intent(ConversationListActivity.this, CreateShortcutActivity.class));
                return;
            }
        }
        if (shouldShowCreateShortcutGuide) {
            super.onBackPressed();
            return;
        }

        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            if (mShowRateAlert || !FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
                BugleAnalytics.logEvent("SMS_Messages_Back", true);
                super.onBackPressed();
                overridePendingTransition(0, 0);
                Preferences.getDefault().doOnce(
                        () -> UIIntentsImpl.get().launchDragHotSeatActivity(this),
                        DragHotSeatActivity.SHOW_DRAG_HOTSEAT);
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
        statusbarInset = findViewById(R.id.status_bar_inset);
        ViewGroup.LayoutParams layoutParams = statusbarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationListActivity.this);
        statusbarInset.setLayoutParams(layoutParams);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setContentInsetsRelative(0, 0);
        LayoutInflater.from(this).inflate(R.layout.conversation_list_toolbar_layout, toolbar, true);
        setSupportActionBar(toolbar);
        invalidateActionBar();
        setupToolbarUI();
    }

    private void setupToolbarUI() {
        mTitleTextView = findViewById(R.id.toolbar_title);
        mAnimHandler = new Handler();
        mGuideContainer = findViewById(R.id.emoji_store_guide_content);
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
        mAnimState = AnimState.APPEAR;
        mGuideContainer.setVisibility(View.VISIBLE);
        Preferences.getDefault().putBoolean(PREF_SHOW_EMOJI_GUIDE, false);
        mGuideContainer.setImageAssetsFolder("lottie/show_emoj_bubble/");
        mGuideContainer.setAnimation("lottie/show_emoj_bubble.json");
        mGuideContainer.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimState = AnimState.NONE;
                if (mGuideContainer.getProgress() >= 0.85f) {
                    mGuideContainer.setVisibility(View.GONE);
                }
                if (mShowEndAnimation) {
                    hideStoreGuide();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimState = AnimState.SHOWING;
                if (mGuideContainer.getProgress() <= 0.5f) {
                    MediaUtil.get().playSound(ConversationListActivity.this, R.raw.emoj_show, null /* completionListener */);
                }
            }
        });
        mGuideContainer.setMaxProgress(0.85f);
        mGuideContainer.playAnimation();

    }

    private void hideStoreGuide() {
        hideAnimation = true;
        mAnimState = AnimState.DISAPPEAR;
        mGuideContainer.setMaxProgress(1f);
        mGuideContainer.resumeAnimation();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (mGuideContainer.getVisibility() == View.VISIBLE) {
            if (mAnimState == AnimState.SHOWING) {
                mShowEndAnimation = true;
            } else {
                if (!hideAnimation) {
                    hideStoreGuide();
                }
            }

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
            mAnimState = AnimState.NONE;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation_item_theme_color:
                drawerClickIndex = DRAWER_INDEX_THEME_COLOR;
                drawerLayout.closeDrawer(navigationView);
                Preferences.getDefault().putBoolean(PREF_KEY_THEME_COLOR_CLICKED, true);
                break;
            case R.id.navigation_item_bubble:
                drawerClickIndex = DRAWER_INDEX_BUBBLE;
                drawerLayout.closeDrawer(navigationView);
                Preferences.getDefault().putBoolean(PREF_KEY_BUBBLE_CLICKED, true);
                break;
            case R.id.navigation_item_chat_background:
                drawerClickIndex = DRAWER_INDEX_CHAT_BACKGROUND;
                drawerLayout.closeDrawer(navigationView);
                Preferences.getDefault().putBoolean(PREF_KEY_BACKGROUND_CLICKED, true);
                break;
            case R.id.navigation_item_change_font:
                drawerClickIndex = DRAWER_INDEX_CHANGE_FONT;
                drawerLayout.closeDrawer(navigationView);
                Preferences.getDefault().putBoolean(PREF_KEY_FONT_CLICKED, true);
                break;
            case R.id.navigation_item_setting:
                drawerClickIndex = DRAWER_INDEX_SETTING;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_rate:
                drawerClickIndex = DRAWER_INDEX_RATE;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_privacy_box:
                Toasts.showToast(R.string.menu_privacy_box_coming);
                if (CommonUtils.isNewUser()
                        && Calendars.isSameDay(CommonUtils.getAppInstallTimeMillis(), System.currentTimeMillis())) {
                    BugleAnalytics.logEvent("Menu_PrivateBox_Click_NewUser");
                }
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
            case SHOW_EMOJ:
                mAnimHandler.postDelayed(() -> showEmojiStoreGuide(), 500);
                break;
            default:
                break;
        }
    }
}
