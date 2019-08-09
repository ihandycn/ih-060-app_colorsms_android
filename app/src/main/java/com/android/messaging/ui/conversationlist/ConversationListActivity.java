package com.android.messaging.ui.conversationlist;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.Cancellable;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.android.messaging.BuildConfig;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ad.AdConfig;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.android.messaging.backup.BackupAutopilotUtils;
import com.android.messaging.backup.ui.BackupGuideDialogActivity;
import com.android.messaging.backup.ui.BackupRestoreActivity;
import com.android.messaging.backup.ui.ChooseBackupViewHolder;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DataModelImpl;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.GetUnreadMessageCountAction;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.font.ChangeFontActivity;
import com.android.messaging.font.FontStyleManager;
import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.privatebox.MoveConversationToPrivateBoxAction;
import com.android.messaging.privatebox.PrivateBoxSettings;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.privatebox.ui.PrivateBoxSetPasswordActivity;
import com.android.messaging.privatebox.ui.SelfVerifyActivity;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.CreateShortcutActivity;
import com.android.messaging.ui.DragHotSeatActivity;
import com.android.messaging.ui.ThemeUpgradeActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.ui.appsettings.ChooseThemeColorRecommendViewHolder;
import com.android.messaging.ui.appsettings.LedSettings;
import com.android.messaging.ui.appsettings.SendDelaySettings;
import com.android.messaging.ui.appsettings.ThemeColorSelectActivity;
import com.android.messaging.ui.appsettings.VibrateSettings;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.CustomBubblesActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.mainpage.ChatListCustomizeActivity;
import com.android.messaging.ui.customize.mainpage.ChatListCustomizeManager;
import com.android.messaging.ui.customize.theme.ThemeSelectActivity;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.EmojiStoreActivity;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
import com.android.messaging.ui.messagebox.MessageBoxSettings;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.android.messaging.ui.smspro.BillingActivity;
import com.android.messaging.ui.wallpaper.WallpaperDownloader;
import com.android.messaging.ui.wallpaper.WallpaperInfos;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.ui.wallpaper.WallpaperPreviewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.BuglePrefsKeys;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.CreateShortcutUtils;
import com.android.messaging.util.ExitAdAutopilotUtils;
import com.android.messaging.util.PhoneUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Calendars;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import hugo.weaving.DebugLog;

import static com.android.messaging.ad.BillingManager.BILLING_VERIFY_SUCCESS;
import static com.android.messaging.ui.conversation.ConversationActivity.PREF_KEY_WIRE_AD_SHOW_TIME_FOR_EXIT_WIRE_AD;
import static com.android.messaging.ui.conversationlist.LightWeightCustomizeGuideController.PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE;
import static com.android.messaging.ui.dialog.FiveStarRateDialog.DESKTOP_PREFS;
import static com.android.messaging.ui.dialog.FiveStarRateDialog.PREF_KEY_MAIN_ACTIVITY_SHOW_TIME;
import static com.ihs.app.framework.HSApplication.getContext;

public class ConversationListActivity extends AbstractConversationListActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String INTENT_KEY_PRIVATE_CONVERSATION_LIST = "conversation_list";
    private static final boolean DEBUGGING_MESSAGE_BOX = false && BuildConfig.DEBUG;

    public static final String EVENT_MAINPAGE_RECREATE = "event_mainpage_recreate";
    public static final String CONVERSATION_LIST_DISPLAYED = "conversation_list_displayed";
    public static final String FIRST_LOAD = "first_load";
    public static final String HAS_PIN_CONVERSATION = "has_pin_conversation";

    public static final String PREF_KEY_LAST_SHOW_TIME = "pref_key_last_show_time";
    public static final String PREF_KEY_TODAY_SHOW_COUNT = "pref_key_today_show_count";

    public static final String PREF_KEY_MAIN_DRAWER_OPENED = "pref_key_main_drawer_opened";
    private static final String PREF_KEY_THEME_CLICKED = "pref_key_navigation_theme_clicked";
    private static final String PREF_KEY_EMOJI_STORE_CLICKED = "pref_key_emoji_store_clicked";
    private static final String PREF_KEY_THEME_COLOR_CLICKED = "pref_key_navigation_theme_color_clicked";
    private static final String PREF_KEY_BUBBLE_CLICKED = "pref_key_navigation_bubble_clicked";
    private static final String PREF_KEY_PRIVATE_BOX_CLICKED = "pref_key_navigation_private_box_clicked";
    private static final String PREF_KEY_BACKGROUND_CLICKED = "pref_key_navigation_background_clicked";
    private static final String PREF_KEY_FONT_CLICKED = "pref_key_navigation_font_clicked";

    public static final String EXTRA_FROM_DESKTOP_ICON = "extra_from_desktop_icon";
    public static final String PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN = "pref_key_create_shortcut_guide_shown";
    public static final String PREF_KEY_EXIT_WIRE_AD_SHOW_TIME = "pref_key_exit_wire_ad_show_time";
    public static final String PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY = "pref_key_exit_wire_ad_show_count_in_one_day";

    private static final String NOTIFICATION_NAME_MESSAGES_MOVE_END = "conversation_list_move_end";
    private static final int REQUEST_PERMISSION_CODE = 1001;

    private static boolean sIsRecreate = false;

    private static final int DRAWER_INDEX_NONE = -1;
    private static final int DRAWER_INDEX_THEME = 0;
    private static final int DRAWER_INDEX_THEME_COLOR = 1;
    private static final int DRAWER_INDEX_BUBBLE = 2;
    private static final int DRAWER_INDEX_CHAT_BACKGROUND = 3;
    private static final int DRAWER_INDEX_SETTING = 4;
    private static final int DRAWER_INDEX_CHANGE_FONT = 6;
    private static final int DRAWER_INDEX_PRIVACY_BOX = 7;
    private static final int DRAWER_INDEX_BACKUP_RESTORE = 9;
    private static final int DRAWER_INDEX_EMOJI_STORE = 10;
    private static final int DRAWER_INDEX_REMOVE_ADS = 11;
    private static final int DRAWER_INDEX_CHAT_LIST = 12;

    private static final int MIN_AD_CLICK_DELAY_TIME = 300;
    private int drawerClickIndex = DRAWER_INDEX_NONE;

    // Drawer related stuff
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView mTitleTextView;

    private static boolean mIsNoActionBack = true;
    private boolean mIsRealCreate = false;
    private String size;
    private View mPrivateBoxEntrance;
    private AcbInterstitialAd mInterstitialAd;
    private long mLastAdClickTime = 0;
    private boolean mIsExitAdShown;
    private boolean mHasInflatedDrawer;
    private boolean isDrawerAutoOpened;

    private boolean mIsMessageMoving;
    private ConstraintLayout mExitAppAnimationViewContainer;
    private LottieAnimationView mLottieAnimationView;
    private LightWeightCustomizeGuideController mCustomizeGuideController;
    private final BuglePrefs mPrefs = Factory.get().getApplicationPrefs();

    @Override
    @DebugLog
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mShouldFinishThisTime) {
            return;
        }

        mIsRealCreate = true;
        setLayout();
        onCreateLogics();
        GetUnreadMessageCountAction.refreshUnreadMessageCount();
    }

    @DebugLog
    private void setLayout() {
        setContentView(R.layout.conversation_list_activity);
    }

    @DebugLog
    private void onCreateLogics() {
        configAppBar();
        mIsNoActionBack = true;

        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_FROM_DESKTOP_ICON, false)) {
            BugleAnalytics.logEvent("SMS_Shortcut_Click");
        }

        if (sIsRecreate) {
            sIsRecreate = false;
        } else {
            Preferences.get(DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_MAIN_ACTIVITY_SHOW_TIME);
            if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                BugleAnalytics.logEvent("SMS_Messages_Show_NewUser", true);
            }
            if (HSApplication.getFirstLaunchInfo().appVersionCode >= 48) {
                BugleAnalytics.logEvent("SMS_Messages_Show_NewUser", true, "SendDelay", "" + SendDelaySettings.getSendDelayInSecs());
            }
        }

        if (getIntent() != null && getIntent().getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true);
            BugleFirebaseAnalytics.logEvent("SMS_Notifications_Clicked");
            AutopilotEvent.logTopicEvent("topic-768lyi3sp", "notification_clicked");
        }

        HSGlobalNotificationCenter.addObserver(EVENT_MAINPAGE_RECREATE, this);
        HSGlobalNotificationCenter.addObserver(CONVERSATION_LIST_DISPLAYED, this);
        HSGlobalNotificationCenter.addObserver(FIRST_LOAD, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_NAME_MESSAGES_MOVE_END, this);
        HSGlobalNotificationCenter.addObserver(BILLING_VERIFY_SUCCESS, this);

        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
        if (!sIsRecreate) {
            Threads.postOnThreadPoolExecutor(() -> {
                String bgPath = WallpaperManager.getWallpaperPathByConversationId(null);
                String backgroundStr = null;
                if (TextUtils.isEmpty(bgPath)) {
                    backgroundStr = "default";
                } else if (bgPath.contains("_1.png")) {
                    backgroundStr = "customize";
                } else {
                    for (int i = 0; i < WallpaperInfos.sRemoteUrl.length; i++) {
                        if (WallpaperDownloader.getWallpaperLocalPath(WallpaperInfos.sRemoteUrl[i]).equals(bgPath)) {
                            backgroundStr = "colorsms_" + i;
                            break;
                        }
                    }
                }

                if (backgroundStr == null) {
                    backgroundStr = "upgrade";
                }

                BugleAnalytics.logEvent("SMS_Messages_Create", true,
                        "themeColor", String.valueOf(ChooseThemeColorRecommendViewHolder.getPrimaryColorType()),
                        "background", backgroundStr,
                        "bubbleStyle", String.valueOf(BubbleDrawables.getSelectedIdentifier()),
                        "received_bubble_color", ConversationColors.get().getConversationColorEventType(true, true),
                        "sent_bubble_color", ConversationColors.get().getConversationColorEventType(true, false),
                        "received_text_color", ConversationColors.get().getConversationColorEventType(false, true),
                        "sent_text_color", ConversationColors.get().getConversationColorEventType(false, false),
                        "theme", ThemeUtils.getCurrentThemeName());
                BugleFirebaseAnalytics.logEvent("SMS_Messages_Create",
                        "themeColor", String.valueOf(ChooseThemeColorRecommendViewHolder.getPrimaryColorType()),
                        "background", backgroundStr,
                        "bubbleStyle", String.valueOf(BubbleDrawables.getSelectedIdentifier()),
                        "received_bubble_color", ConversationColors.get().getConversationColorEventType(true, true),
                        "sent_bubble_color", ConversationColors.get().getConversationColorEventType(true, false),
                        "received_text_color", ConversationColors.get().getConversationColorEventType(false, true),
                        "sent_text_color", ConversationColors.get().getConversationColorEventType(false, false),
                        "theme", ThemeUtils.getCurrentThemeName());

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

                boolean granted = RuntimePermissions.checkSelfPermission(ConversationListActivity.this,
                        Manifest.permission.READ_PHONE_STATE) == RuntimePermissions.PERMISSION_GRANTED;
                String simStatus;
                if (granted) {
                    simStatus = String.valueOf(PhoneUtils.getDefault().getActiveSubscriptionCount());
                } else {
                    simStatus = "No Permission";
                }

                BugleAnalytics.logEvent("SMS_HomePage_Show", true,
                        "SIM", simStatus,
                        "Signal", String.valueOf(DataModelImpl.get().getConnectivityUtil().getSignalLevel(0)),
                        "Popups", String.valueOf(MessageBoxSettings.isSMSAssistantModuleEnabled()),
                        "DeliveryReport", String.valueOf(Preferences.getDefault().getBoolean(getString(R.string.delivery_reports_pref_key),
                                getResources().getBoolean(R.bool.delivery_reports_pref_default))));

                if (Compats.IS_HUAWEI_DEVICE) {
                    BugleFirebaseAnalytics.logEvent("Device_HUAWEI", new HashMap<>());
                } else if (Compats.IS_MOTOROLA_DEVICE || Compats.IS_LGE_DEVICE) {
                    BugleFirebaseAnalytics.logEvent("Device_MOTOLG", new HashMap<>());
                } else if (Compats.IS_SAMSUNG_DEVICE) {
                    BugleFirebaseAnalytics.logEvent("Device_Samsung", new HashMap<>());
                } else if (Compats.IS_VIVO_DEVICE) {
                    BugleFirebaseAnalytics.logEvent("Device_Vivo", new HashMap<>());
                }
            });
        }

        navigationView = findViewById(R.id.navigation_view);
        navigationView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Threads.postOnMainThread(() -> onPostPageVisible());
                navigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    private void onPostPageVisible() {

        if (mHasInflatedDrawer) {
            return;
        }
        mHasInflatedDrawer = true;

        setupDrawer();
    }

    @DebugLog
    @Override
    protected void onStart() {
        super.onStart();
        HSGlobalNotificationCenter.sendNotification(MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX);
    }

    @Override
    @DebugLog
    protected void onResume() {
        super.onResume();
        if (mIsExitAdShown) {
            showExitAppAnimation();
            return;
        }
        AppPrivateLockManager.getInstance().lockAppLock();
        if (mPrivateBoxEntrance != null) {
            if (PrivateSettingManager.isPrivateBoxIconHidden()) {
                mPrivateBoxEntrance.setVisibility(View.GONE);
            } else {
                mPrivateBoxEntrance.setVisibility(View.VISIBLE);
            }
        }

        if (!Calendars.isSameDay(System.currentTimeMillis(),
                Preferences.getDefault().getLong(PREF_KEY_LAST_SHOW_TIME, -1))) {
            Preferences.getDefault().putInt(PREF_KEY_TODAY_SHOW_COUNT, 0);
        }
        int todayShowCount = Preferences.getDefault().incrementAndGetInt(PREF_KEY_TODAY_SHOW_COUNT);
        Preferences.getDefault().putLong(PREF_KEY_LAST_SHOW_TIME, System.currentTimeMillis());
        if (todayShowCount > 20) {
            BugleFirebaseAnalytics.logEvent("SMS_Messages_Show_Positive");
        }

        BugleAnalytics.logEvent("SMS_Messages_Show_Corrected", true);
        BugleFirebaseAnalytics.logEvent("SMS_Messages_Show_Corrected");
        AutopilotEvent.logTopicEvent("topic-768lyi3sp", "homepage_show");
        showThemeUpgradeDialog();

        if (drawerLayout != null) {
            View v = drawerLayout.findViewById(R.id.navigation_item_backup_restore_new_text);
            if (v.getVisibility() == View.VISIBLE
                    && Preferences.getDefault().getBoolean(BackupRestoreActivity.PREF_KEY_BACKUP_ACTIVITY_SHOWN, false)) {
                v.setVisibility(View.GONE);
            }
        }
        logMainPageShowEvent();
    }

    private void logMainPageShowEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("WidthDp", String.valueOf(Dimensions.dpFromPx(Dimensions.getPhoneWidth(this))));
        params.put("HeightDp", String.valueOf(Dimensions.dpFromPx(Dimensions.getPhoneHeight(this))));
        params.put("DensityDpi", String.valueOf(getResources().getDisplayMetrics().densityDpi));
        params.put("ScreenSize", getScreenSize());
        BugleAnalytics.logEvent("Main_Page_Shown", true, params);
    }

    private static String getScreenSize() {
        int screenWidth = Dimensions.getPhoneWidth(getContext());
        int screenHeight = Dimensions.getPhoneHeight(getContext());
        return screenWidth + "x" + screenHeight;
    }

    private void showThemeUpgradeDialog() {
        if (Preferences.getDefault().getBoolean(BuglePrefsKeys.PREFS_KEY_THEME_CLEARED_TO_DEFAULT, false)) {
            Navigations.startActivitySafely(ConversationListActivity.this,
                    new Intent(ConversationListActivity.this, ThemeUpgradeActivity.class));
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            Preferences.getDefault().putBoolean(BuglePrefsKeys.PREFS_KEY_THEME_CLEARED_TO_DEFAULT, false);
        }
    }

    private void showExitAppAnimation() {
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
        }
        if (mLottieAnimationView != null) {
            mLottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishWithoutOverridePendingTransition();
                    overridePendingTransition(0, 0);
                    int mainActivityCreateTime = Preferences.get(DESKTOP_PREFS).getInt(PREF_KEY_MAIN_ACTIVITY_SHOW_TIME, 0);
                    if (mainActivityCreateTime >= 2) {
                        Preferences.getDefault().doOnce(
                                () -> UIIntentsImpl.get().launchDragHotSeatActivity(ConversationListActivity.this),
                                DragHotSeatActivity.SHOW_DRAG_HOTSEAT);
                    }
                }
            });
            mLottieAnimationView.playAnimation();
        }
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.show();

        if (mTitleTextView != null && mTitleTextView.getVisibility() == View.GONE) {
            mTitleTextView.setVisibility(View.VISIBLE);
        }

        if (getActionMode() == null) {
            findViewById(R.id.selection_mode_bg).setVisibility(View.INVISIBLE);
        }

        super.updateActionBar(actionBar);

        setDrawerMenuIcon();
    }

    @DebugLog
    private void setupDrawer() {
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setPadding(0, Dimensions.getStatusBarInset(this), 0, 0);
        View navigationContent = getLayoutInflater().inflate(R.layout.layout_main_navigation, navigationView, false);

        drawerLayout = findViewById(R.id.main_drawer_layout);
        //drawerLayout.setBackgroundColor(PrimaryColors.getPrimaryColor());
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
                BugleAnalytics.logEvent("Menu_Show_NewVersion", true);
                BugleAnalytics.logEvent("Menu_Show", true);
                BugleFirebaseAnalytics.logEvent("Menu_Show");
                BackupAutopilotUtils.logMenuShow();
                if (CommonUtils.isNewUser()
                        && Calendars.isSameDay(CommonUtils.getAppInstallTimeMillis(), System.currentTimeMillis())) {
                    BugleAnalytics.logEvent("Menu_Show_NewUser", true);
                    BugleAnalytics.logEvent("Menu_Show_NewUser_Backup", true);
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (isDrawerAutoOpened) {
                    BugleAnalytics.logEvent("Menu_Click_AfterGuide");
                    isDrawerAutoOpened = false;
                }

                switch (drawerClickIndex) {
                    case DRAWER_INDEX_THEME:
                        BugleAnalytics.logEvent("Menu_Theme_Click");
                        Navigations.startActivity(ConversationListActivity.this, ThemeSelectActivity.class);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        navigationContent.findViewById(R.id.navigation_item_theme_new_text).setVisibility(View.GONE);
                        break;

                    case DRAWER_INDEX_THEME_COLOR:
                        BugleAnalytics.logEvent("Menu_ThemeColor_Click", true);
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_ThemeColor_Click_NewUser", true);
                        }
                        Navigations.startActivity(ConversationListActivity.this, ThemeColorSelectActivity.class);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        navigationContent.findViewById(R.id.navigation_item_theme_color_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_BUBBLE:
                        BugleAnalytics.logEvent("Menu_Bubble_Click", true);
                        BugleFirebaseAnalytics.logEvent("Menu_Bubble_Click");
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_Bubble_Click_NewUser", true);
                        }

                        if (DEBUGGING_MESSAGE_BOX) {
                            UIIntents.get().launchMessageBoxActivity(getApplicationContext(), new MessageBoxItemData("asd",
                                    "asd", "asd", "asd", "asd", "heihei", 123L));
                        } else {
                            Navigations.startActivity(ConversationListActivity.this, CustomBubblesActivity.class);
                            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        }
                        break;
                    case DRAWER_INDEX_CHAT_BACKGROUND:
                        BugleAnalytics.logEvent("Menu_ChatBackground_Click", true);
                        BugleFirebaseAnalytics.logEvent("Menu_ChatBackground_Click");
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_ChatBackground_Click_NewUser", true);
                        }
                        WallpaperPreviewActivity.startWallpaperPreview(ConversationListActivity.this);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        navigationContent.findViewById(R.id.navigation_item_background_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_CHAT_LIST:
                        Navigations.startActivitySafely(ConversationListActivity.this, ChatListCustomizeActivity.class);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        break;
                    case DRAWER_INDEX_CHANGE_FONT:
                        BugleAnalytics.logEvent("Menu_ChangeFont_Click");
                        Navigations.startActivity(ConversationListActivity.this, ChangeFontActivity.class);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        navigationContent.findViewById(R.id.navigation_item_font_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_EMOJI_STORE:
                        BugleAnalytics.logEvent("Menu_EmojiStore_Click", true);
                        Navigations.startActivity(ConversationListActivity.this, EmojiStoreActivity.class);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        navigationContent.findViewById(R.id.navigation_item_emoji_store_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_BACKUP_RESTORE:
                        BugleAnalytics.logEvent("Menu_BackupRestore_Click", true);
                        BackupAutopilotUtils.logMenuBackupClick();
                        BackupRestoreActivity.startBackupRestoreActivity(ConversationListActivity.this, BackupRestoreActivity.ENTRANCE_MENU);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        navigationContent.findViewById(R.id.navigation_item_backup_restore_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_PRIVACY_BOX:
                        BugleAnalytics.logEvent("Menu_PrivateBox_Click", true);
                        if (PrivateBoxSettings.isAnyPasswordSet()) {
                            Intent intent1 = new Intent(ConversationListActivity.this, SelfVerifyActivity.class);
                            intent1.putExtra(SelfVerifyActivity.INTENT_KEY_ACTIVITY_ENTRANCE,
                                    SelfVerifyActivity.ENTRANCE_MENU);
                            Navigations.startActivitySafely(ConversationListActivity.this, intent1);
                            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        } else {
                            Navigations.startActivitySafely(ConversationListActivity.this,
                                    new Intent(ConversationListActivity.this, PrivateBoxSetPasswordActivity.class));
                            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        }
                        Preferences.getDefault().putBoolean(PREF_KEY_PRIVATE_BOX_CLICKED, true);
                        navigationContent.findViewById(R.id.navigation_item_private_box_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_SETTING:
                        boolean granted = RuntimePermissions.checkSelfPermission(ConversationListActivity.this,
                                Manifest.permission.READ_PHONE_STATE) == RuntimePermissions.PERMISSION_GRANTED;
                        if (granted) {
                            UIIntents.get().launchSettingsActivity(ConversationListActivity.this);
                            BugleAnalytics.logEvent("Menu_Settings_Click", true);
                            BugleFirebaseAnalytics.logEvent("Menu_Settings_Click");
                        } else {
                            RuntimePermissions.requestPermissions(ConversationListActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION_CODE);
                        }
                        break;
                    case DRAWER_INDEX_REMOVE_ADS:
                        Intent goSmsProIntent = new Intent(ConversationListActivity.this, BillingActivity.class);
                        ActivityOptionsCompat options =
                                ActivityOptionsCompat.makeCustomAnimation(ConversationListActivity.this, R.anim.fade_in, R.anim.anim_null);
                        startActivity(goSmsProIntent, options.toBundle());
                        BugleAnalytics.logEvent("SMS_Menu_Subscription_Click", true);

                        BugleAnalytics.logEvent("Subscription_Analysis", "Menu_Subscription_Click", "true");
                        BugleFirebaseAnalytics.logEvent("Subscription_Analysis", "Menu_Subscription_Click", "true");
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
            if (!Preferences.getDefault().getBoolean(PREF_KEY_THEME_CLICKED, false)) {
                View newMark = navigationContent.findViewById(R.id.navigation_item_theme_new_text);
                newMark.setVisibility(View.VISIBLE);
                newMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }

            if (!Preferences.getDefault().getBoolean(PREF_KEY_EMOJI_STORE_CLICKED, false)
                    && HSApplication.getFirstLaunchInfo().appVersionCode > 58) {
                View newMark = navigationContent.findViewById(R.id.navigation_item_emoji_store_new_text);
                newMark.setVisibility(View.VISIBLE);
                newMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }

            if (!Preferences.getDefault().getBoolean(PREF_KEY_PRIVATE_BOX_CLICKED, false)) {
                View bubbleNewMark = navigationContent.findViewById(R.id.navigation_item_private_box_new_text);
                bubbleNewMark.setVisibility(View.VISIBLE);
                bubbleNewMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }

            if (!Preferences.getDefault().getBoolean(BackupRestoreActivity.PREF_KEY_BACKUP_ACTIVITY_SHOWN, false)) {
                View bubbleNewMark = navigationContent.findViewById(R.id.navigation_item_backup_restore_new_text);
                bubbleNewMark.setVisibility(View.VISIBLE);
                bubbleNewMark.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffea6126,
                        Dimensions.pxFromDp(8.7f), false));
            }
        }

        navigationContent.findViewById(R.id.navigation_item_theme).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_theme_color).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_bubble).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_chat_background).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_change_font).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_setting).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_emoji_store).setOnClickListener(this);
        navigationContent.findViewById(R.id.navigation_item_chat_list).setOnClickListener(this);

        View backupEntrance = navigationContent.findViewById(R.id.navigation_item_backup_restore);
        backupEntrance.setOnClickListener(this);
        if (HSApplication.getFirstLaunchInfo().appVersionCode >= 68) {
            if (!BackupAutopilotUtils.getIsBackupSwitchOn()) {
                backupEntrance.setVisibility(View.GONE);
            } else {
                backupEntrance.setVisibility(View.VISIBLE);
            }
        }

        if (HSConfig.optBoolean(false, "Application", "Subscription", "Enabled")
                && !BillingManager.isPremiumUser()) {
            navigationContent.findViewById(R.id.navigation_item_remove_ads).setOnClickListener(this);
        } else {
            navigationContent.findViewById(R.id.navigation_item_remove_ads).setVisibility(View.GONE);
        }

        //test code
        //this item is used to delete dirty mms parts in telephony
        navigationContent.findViewById(R.id.navigation_item_clear_private_parts).setVisibility(View.GONE);
        navigationContent.findViewById(R.id.navigation_item_clear_private_parts).setOnClickListener(v -> {
            Threads.postOnSingleThreadExecutor(() -> {
                List<Integer> privatePartIdList = new ArrayList<>();
                final Cursor c = SqliteWrapper.query(getContext(), getContext().getContentResolver(),
                        Uri.parse("content://mms/part"),
                        new String[]{Telephony.Mms.Part._ID, Telephony.Mms.Part.MSG_ID},
                        Telephony.Mms.Part.MSG_ID + "< 0",
                        null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        privatePartIdList.add(c.getInt(0));
                        HSLog.d("---->>>>", "find private message " + c.getString(1) + " parts : \n part id is :"
                                + c.getInt(0));
                    }
                    c.close();
                }
                for (int i = 0; i < privatePartIdList.size(); i++) {
                    int k = SqliteWrapper.delete(getContext(),
                            getContext().getContentResolver(),
                            Uri.parse("content://mms/part/" + privatePartIdList.get(i)), null, null);
                    if (k > 0) {
                        HSLog.d("---->>>>", "delete part : id = " + privatePartIdList.get(i));
                    }
                }
            });

        });
        mPrivateBoxEntrance = navigationContent.findViewById(R.id.navigation_item_privacy_box);
        mPrivateBoxEntrance.setOnClickListener(this);
        if (PrivateSettingManager.isPrivateBoxIconHidden()) {
            mPrivateBoxEntrance.setVisibility(View.GONE);
        }

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
            ChatListCustomizeManager.changeDrawableColorIfNeed(drawable);
        }
        getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        RuntimePermissions.onRequestPermissionsResult(ConversationListActivity.this,
                requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
        }
        FiveStarRateDialog.dismissDialogs();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
            return;
        }

        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
            return;
        }

        if (!Preferences.getDefault().contains(PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN)
                && HSConfig.optBoolean(false, "Application", "ShortcutLikeSystemSMS")) {
            Drawable smsIcon = CreateShortcutUtils.getSystemSMSIcon();
            if (smsIcon != null && ShortcutManagerCompat.isRequestPinShortcutSupported(getContext())) {
                Preferences.getDefault().putBoolean(PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN, true);
                Navigations.startActivitySafely(ConversationListActivity.this,
                        new Intent(ConversationListActivity.this, CreateShortcutActivity.class));
                return;
            }
        }

        int mainActivityCreateTime = Preferences.get(DESKTOP_PREFS).getInt(PREF_KEY_MAIN_ACTIVITY_SHOW_TIME, 0);
        // show backup full guide
        if (mainActivityCreateTime >= 2 && CommonUtils.isNewUser()
                && !Preferences.getDefault().getBoolean(BackupRestoreActivity.PREF_KEY_BACKUP_ACTIVITY_SHOWN, false)
                && !Preferences.getDefault()
                .getBoolean(BackupGuideDialogActivity.PREF_KEY_BACKUP_FULL_GUIDE_SHOWN, false)
                && BackupAutopilotUtils.getIsBackupSwitchOn()
                && (HSConfig.optBoolean(false, "Application", "BackupRestore", "RecommendFull")
                || BackupAutopilotUtils.getIsBackupFullScreenGuideSwitchOn())) {
            Intent intent = new Intent(this, BackupGuideDialogActivity.class);
            Navigations.startActivitySafely(this, intent);
            return;
        }

        if (FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
            return;
        }

        if (showInterstitialAd()) {
            return;
        }
        ExitAdAutopilotUtils.logSmsExitApp();
        super.onBackPressed();
        overridePendingTransition(0, 0);
        if (mainActivityCreateTime >= 2) {
            Preferences.getDefault().doOnce(
                    () -> UIIntentsImpl.get().launchDragHotSeatActivity(this),
                    DragHotSeatActivity.SHOW_DRAG_HOTSEAT);
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

    private boolean showInterstitialAd() {
        int exitAdShownCountInOneDay = mPrefs.getInt(PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY, 0);
        if (BillingManager.isPremiumUser()) {
            BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "premiumuser");
            return false;
        }
        if (!(HSConfig.optBoolean(true, "Application", "SMSAd", "SMSExitAd", "Enabled")
                && ExitAdAutopilotUtils.getIsExitAdSwitchOn())) {
            BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "configdisabled");
            return false;
        }
        if (Calendars.isSameDay(System.currentTimeMillis(), mPrefs.getLong(PREF_KEY_EXIT_WIRE_AD_SHOW_TIME, -1))) {
            if (exitAdShownCountInOneDay == ExitAdAutopilotUtils.getExitAdShowMaxTimes()) {
                BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "maxtimes");
                return false;
            }
        } else {
            exitAdShownCountInOneDay = 0;
        }
        if (System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                <= HSConfig.optInteger(2, "Application", "SMSAd", "SMSExitAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS) {
            BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "newuser");
            return false;
        }
        if (System.currentTimeMillis() - mPrefs.getLong(PREF_KEY_EXIT_WIRE_AD_SHOW_TIME, -1)
                < HSConfig.optInteger(5, "Application", "SMSAd", "SMSExitAd", "MinInterval") * DateUtils.MINUTE_IN_MILLIS) {
            BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "exitadinterval");
            return false;
        }
        if (System.currentTimeMillis() - mPrefs.getLong(PREF_KEY_WIRE_AD_SHOW_TIME_FOR_EXIT_WIRE_AD, -1)
                < 20 * DateUtils.SECOND_IN_MILLIS) {
            BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "fulladinterval");
            return false;
        }
        BugleAnalytics.logEvent("SMS_Messages_Back", true, "type", "exitadchance");
        BugleAnalytics.logEvent("SMS_ExitAd_Chance", true);

        BugleFirebaseAnalytics.logEvent("SMS_Ad", "type", "exitad_chance");
        ExitAdAutopilotUtils.logExitAdChance();
        List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(AdPlacement.AD_EXIT_WIRE, 1);
        if (ads.size() > 0) {
            ExitAdAutopilotUtils.logSmsExitApp();
            mInterstitialAd = ads.get(0);
            mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {

                private ViewStub mExitAppAnimationViewStub;
                private Cancellable mAnimationLoadTask;

                @Override
                public void onAdDisplayed() {

                    Threads.postOnMainThreadDelayed(() -> {
                        if (mExitAppAnimationViewContainer == null) {
                            mExitAppAnimationViewStub = findViewById(R.id.exit_app_stub);
                            mExitAppAnimationViewContainer = (ConstraintLayout) mExitAppAnimationViewStub.inflate();
                            mLottieAnimationView = findViewById(R.id.exit_app_lottie);
                            mLottieAnimationView.useHardwareAcceleration();
                            loadAnimation();
                        } else {
                            mExitAppAnimationViewContainer.setVisibility(View.VISIBLE);
                        }
                        mIsExitAdShown = true;
                    }, 200);
                }

                @Override
                public void onAdClicked() {
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (currentTime - mLastAdClickTime > MIN_AD_CLICK_DELAY_TIME) {
                        BugleAnalytics.logEvent("SMS_ExitAd_Click", true);
                        BugleAnalytics.logEvent("SMS_Ad", "type", "exitad_click");
                        BugleFirebaseAnalytics.logEvent("SMS_Ad", "type", "exitad_click");
                        ExitAdAutopilotUtils.logExitAdClick();
                        mLastAdClickTime = currentTime;
                    }
                }

                @Override
                public void onAdClosed() {

                }

                @Override
                public void onAdDisplayFailed(AcbError acbError) {

                }

                private void loadAnimation() {
                    cancelAnimationLoadTask();
                    try {
                        mAnimationLoadTask = LottieComposition.Factory.fromAssetFileName(getContext(),
                                "lottie/exit_app.json", lottieComposition -> {
                                    if (lottieComposition != null) {
                                        mLottieAnimationView.setComposition(lottieComposition);
                                        mLottieAnimationView.setProgress(0f);
                                    }
                                });
                    } catch (RejectedExecutionException e) {
                        e.printStackTrace();
                    }
                }

                private void cancelAnimationLoadTask() {
                    if (mAnimationLoadTask != null) {
                        mAnimationLoadTask.cancel();
                        mAnimationLoadTask = null;
                    }
                }
            });

            mInterstitialAd.setSoundEnable(false);
            mInterstitialAd.show();
            BugleAnalytics.logEvent("SMS_ExitAd_Show", true);
            BugleAnalytics.logEvent("SMS_Ad", "type", "exitad_show");
            BugleFirebaseAnalytics.logEvent("SMS_Ad", "type", "exitad_show");

            ExitAdAutopilotUtils.logExitAdShow();
            mPrefs.putInt(PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY, exitAdShownCountInOneDay + 1);
            mPrefs.putLong(PREF_KEY_EXIT_WIRE_AD_SHOW_TIME, System.currentTimeMillis());
            return true;
        } else {
            return false;
        }
    }

    boolean getExitAdShown() {
        return mIsExitAdShown;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mIsExitAdShown) {
            Intent intent = new Intent(this, ConversationListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            if (mInterstitialAd != null) {
                mInterstitialAd.release();
            }
            mIsExitAdShown = false;
            if (mExitAppAnimationViewContainer != null) {
                mExitAppAnimationViewContainer.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        mTitleTextView.setVisibility(View.GONE);
        findViewById(R.id.selection_mode_bg).setVisibility(View.VISIBLE);
        BugleAnalytics.logEvent("SMS_EditMode_Show", true);
        BugleFirebaseAnalytics.logEvent("SMS_EditMode_Show");
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        return super.startActionMode(callback);
    }

    @Override
    public void dismissActionMode() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.dismissActionMode();
    }

    void openDrawer() {
        isDrawerAutoOpened = true;
        drawerLayout.openDrawer(navigationView);
    }

    @Override
    public boolean isArchiveMode() {
        return false;
    }

    @Override
    public void onActionBarHome() {
        exitMultiSelectState();
    }

    @Override
    public void onActionMenu() {
        BugleAnalytics.logEvent("SMS_EditMode_More_Click");
    }

    @Override
    public void onAddToPrivateBox(List<String> conversations) {
        if (!PrivateBoxSettings.isAnyPasswordSet()) {
            new BaseAlertDialog.Builder(ConversationListActivity.this)
                    .setTitle(R.string.tips)
                    .setMessage(R.string.private_start_box_tip)
                    .setPositiveButton(R.string.welcome_set_default_button, (dialog, which) -> {
                        Intent intent = new Intent(ConversationListActivity.this, PrivateBoxSetPasswordActivity.class);
                        intent.putExtra(INTENT_KEY_PRIVATE_CONVERSATION_LIST, conversations.toArray(new String[conversations.size()]));
                        Navigations.startActivitySafely(ConversationListActivity.this, intent);
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                    })
                    .setNegativeButton(R.string.delete_conversation_decline_button, null)
                    .show();
        } else {
            mIsMessageMoving = true;
            MoveConversationToPrivateBoxAction.moveAndUpdatePrivateContact(conversations,
                    null, NOTIFICATION_NAME_MESSAGES_MOVE_END);
        }
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

    @DebugLog
    private void configAppBar() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        ViewGroup.LayoutParams layoutParams = accessoryContainer.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationListActivity.this) + Dimensions.pxFromDp(56);
        accessoryContainer.setLayoutParams(layoutParams);
        Drawable customToolBar = ChatListCustomizeManager.getToolbarDrawable();
        if (customToolBar != null) {
            ImageView ivAccessoryBg = accessoryContainer.findViewById(R.id.accessory_bg);
            ivAccessoryBg.setVisibility(View.VISIBLE);
            ivAccessoryBg.setImageDrawable(customToolBar);
        } else if (ToolbarDrawables.getToolbarBg() != null) {
            ImageView ivAccessoryBg = accessoryContainer.findViewById(R.id.accessory_bg);
            ivAccessoryBg.setVisibility(View.VISIBLE);
            ivAccessoryBg.setImageDrawable(ToolbarDrawables.getToolbarBg());
        } else {
            accessoryContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());
            accessoryContainer.findViewById(R.id.accessory_bg).setVisibility(View.GONE);
        }

        View statusBarInset = findViewById(R.id.status_bar_inset);
        layoutParams = statusBarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationListActivity.this);
        statusBarInset.setLayoutParams(layoutParams);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setContentInsetsRelative(0, 0);
        LayoutInflater.from(this).inflate(R.layout.conversation_list_toolbar_layout, toolbar, true);
        ChatListCustomizeManager.changeViewColorIfNeed(toolbar.findViewById(R.id.toolbar_title));
        setSupportActionBar(toolbar);
        invalidateActionBar();
        setupToolbarUI();
    }

    private void setupToolbarUI() {
        mTitleTextView = findViewById(R.id.toolbar_title);
    }

    public static void logFirstComeInClickEvent(String type) {
        if (!type.equals("no_action")) {
            mIsNoActionBack = false;
        }
        Preferences.getDefault().doOnce(new Runnable() {
            @Override
            public void run() {
                BugleAnalytics.logEvent("SMS_Messages_First_Click", true, "type", type);
                BugleFirebaseAnalytics.logEvent("SMS_Messages_First_Click", "type", type);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation_item_theme:
                drawerClickIndex = DRAWER_INDEX_THEME;
                drawerLayout.closeDrawer(navigationView);
                Preferences.getDefault().putBoolean(PREF_KEY_THEME_CLICKED, true);
                break;

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
            case R.id.navigation_item_emoji_store:
                drawerClickIndex = DRAWER_INDEX_EMOJI_STORE;
                drawerLayout.closeDrawer(navigationView);
                Preferences.getDefault().putBoolean(PREF_KEY_EMOJI_STORE_CLICKED, true);
                break;
            case R.id.navigation_item_backup_restore:
                drawerClickIndex = DRAWER_INDEX_BACKUP_RESTORE;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_privacy_box:
                drawerClickIndex = DRAWER_INDEX_PRIVACY_BOX;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_remove_ads:
                drawerClickIndex = DRAWER_INDEX_REMOVE_ADS;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_chat_list:
                drawerClickIndex = DRAWER_INDEX_CHAT_LIST;
                drawerLayout.closeDrawer(navigationView);
                break;
        }
    }

    private void preloadAds() {
        if (AdConfig.isHomepageBannerAdEnabled()) {
            AcbNativeAdManager.preload(1, AdPlacement.AD_BANNER);
        }

        if (AdConfig.isDetailpageTopAdEnabled()) {
            AcbNativeAdManager.preload(1, AdPlacement.AD_DETAIL_NATIVE);
        }

        if (AdConfig.isExitAdEnabled()) {
            AcbInterstitialAdManager.preload(1, AdPlacement.AD_EXIT_WIRE);
        }
    }


    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_MAINPAGE_RECREATE:
                sIsRecreate = true;
                recreate();
                break;
            case CONVERSATION_LIST_DISPLAYED:
                if (Preferences.getDefault().getBoolean(PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE, true)) {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (mCustomizeGuideController == null) {
                            if (!isFinishing()) {
                                mCustomizeGuideController = new LightWeightCustomizeGuideController();
                                mCustomizeGuideController.showGuideIfNeed(this);
                            }
                        }
                    }, 200);
                }
                preloadAds();
                break;
            case FIRST_LOAD:
                if (!sIsRecreate && hsBundle != null) {
                    boolean hasPinConversation = hsBundle.getBoolean(HAS_PIN_CONVERSATION);
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    Threads.postOnThreadPoolExecutor(() -> {
                        DatabaseWrapper db = DataModel.get().getDatabase();
                        Cursor cursor = db.query(DatabaseHelper.CONVERSATIONS_TABLE, new String[]{"COUNT(*)"},
                                DatabaseHelper.ConversationColumns.ARCHIVE_STATUS + " =1 ", null,
                                null, null, null);
                        int archivedCount = 0;
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                archivedCount = cursor.getInt(0);
                            }
                            cursor.close();
                        }

                        BugleAnalytics.logEvent("SMS_Messages_Show_1", true,
                                "font", FontStyleManager.getInstance().getFontFamily(),
                                "size", size,
                                "open time", String.valueOf(hour),
                                "signature", String.valueOf(!TextUtils.isEmpty(Preferences.getDefault().getString(
                                        SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null))),
                                "pin", String.valueOf(hasPinConversation),
                                "backup", String.valueOf(Preferences.getDefault().getBoolean(
                                        ChooseBackupViewHolder.PREF_KEY_BACKUP_SUCCESS_FOR_EVENT, false)),
                                "archive", String.valueOf(archivedCount > 0),
                                "emojiskintone", String.valueOf(EmojiManager.getSkinDefault() + 1));

                        String bgString;
                        String path = ChatListCustomizeManager.getListWallpaperPath();
                        if (TextUtils.isEmpty(path)) {
                            bgString = "theme";
                        } else if (path.contains("list_wallpapers")) {
                            bgString = "customize";
                        } else {
                            bgString = "recommend";
                        }
                        String opacityStr;
                        float alpha = 1 - ChatListCustomizeManager.getMaskOpacity();
                        if (alpha < 0.1f) {
                            opacityStr = "<10%";
                        } else if (Math.abs(1 - alpha) < 0.0000001) {
                            opacityStr = "100%";
                        } else {
                            int tensNum = Math.min((int) (alpha * 10), 9);
                            opacityStr = tensNum + "0%-" + (tensNum + 1) + "0%";
                        }
                        BugleAnalytics.logEvent("SMS_Messages_Show_2", true,
                                "subscription", String.valueOf(BillingManager.isPremiumUser()),
                                "type", EmojiManager.getEmojiStyle(),
                                "chat_list_background", bgString,
                                "chat_list_text_color", Preferences.getDefault()
                                        .getString(ChatListCustomizeActivity.PREF_KEY_EVENT_CHANGE_COLOR_TYPE, "theme"),
                                "chat_list_opacity", opacityStr,
                                "vibrate", VibrateSettings.getVibrateDescription(""),
                                "led", LedSettings.getLedDescription(""));

                        BugleFirebaseAnalytics.logEvent("SMS_Messages_Show_2",
                                "subscription", String.valueOf(BillingManager.isPremiumUser()),
                                "type", EmojiManager.getEmojiStyle(),
                                "chat_list_background", bgString,
                                "chat_list_text_color", Preferences.getDefault()
                                        .getString(ChatListCustomizeActivity.PREF_KEY_EVENT_CHANGE_COLOR_TYPE, "theme"),
                                "chat_list_opacity", opacityStr,
                                "vibrate", VibrateSettings.getVibrateDescription(""),
                                "led", LedSettings.getLedDescription(""));
                    });
                }
                break;
            case NOTIFICATION_NAME_MESSAGES_MOVE_END:
                if (mIsMessageMoving) {
                    mIsMessageMoving = false;
                    Toasts.showToast(R.string.private_box_add_to_success);
                }
                break;
            case BILLING_VERIFY_SUCCESS:
                final ConversationListFragment conversationListFragment =
                        (ConversationListFragment) getFragmentManager().findFragmentById(
                                R.id.conversation_list_fragment);
                if (conversationListFragment != null) {
                    conversationListFragment.disableTopNativeAd();
                }
                View navigationItemRemoveAds = findViewById(R.id.navigation_item_remove_ads);
                if (navigationItemRemoveAds != null) {
                    navigationItemRemoveAds.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }
}
