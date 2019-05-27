package com.android.messaging.ui.conversationlist;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.BuildConfig;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ad.AdConfig;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.action.PinConversationAction;
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
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.ui.appsettings.ChooseThemeColorRecommendViewHolder;
import com.android.messaging.ui.appsettings.ThemeColorSelectActivity;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.CustomBubblesActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.theme.ThemeSelectActivity;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.EmojiStoreActivity;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
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
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Calendars;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.android.messaging.ui.dialog.FiveStarRateDialog.DESKTOP_PREFS;
import static com.android.messaging.ui.dialog.FiveStarRateDialog.PREF_KEY_MAIN_ACTIVITY_SHOW_TIME;

public class ConversationListActivity extends AbstractConversationListActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String INTENT_KEY_PRIVATE_CONVERSATION_LIST = "conversation_list";
    private static final boolean DEBUGGING_MESSAGE_BOX = false && BuildConfig.DEBUG;

    public static final String EVENT_MAINPAGE_RECREATE = "event_mainpage_recreate";
    public static final String SHOW_EMOJI = "show_emoj";
    public static final String FIRST_LOAD = "first_load";
    public static final String HAS_PIN_CONVERSATION = "has_pin_conversation";

    private static final String PREF_SHOW_EMOJI_GUIDE = "pref_show_emoji_guide";
    public static final String PREF_KEY_MAIN_DRAWER_OPENED = "pref_key_main_drawer_opened";

    private static final String PREF_KEY_THEME_CLICKED = "pref_key_navigation_theme_clicked";
    private static final String PREF_KEY_THEME_COLOR_CLICKED = "pref_key_navigation_theme_color_clicked";
    private static final String PREF_KEY_BUBBLE_CLICKED = "pref_key_navigation_bubble_clicked";
    private static final String PREF_KEY_PRIVATE_BOX_CLICKED = "pref_key_navigation_private_box_clicked";
    private static final String PREF_KEY_BACKGROUND_CLICKED = "pref_key_navigation_background_clicked";
    private static final String PREF_KEY_FONT_CLICKED = "pref_key_navigation_font_clicked";

    public static final String EXTRA_FROM_DESKTOP_ICON = "extra_from_desktop_icon";
    public static final String PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN = "pref_key_create_shortcut_guide_shown";

    private static final String NOTIFICATION_NAME_MESSAGES_MOVE_END = "conversation_list_move_end";
    private static final int REQUEST_PERMISSION_CODE = 1001;

    private static boolean sIsRecreate = false;

    private static final int DRAWER_INDEX_NONE = -1;
    private static final int DRAWER_INDEX_THEME = 0;
    private static final int DRAWER_INDEX_THEME_COLOR = 1;
    private static final int DRAWER_INDEX_BUBBLE = 2;
    private static final int DRAWER_INDEX_CHAT_BACKGROUND = 3;
    private static final int DRAWER_INDEX_SETTING = 4;
    private static final int DRAWER_INDEX_RATE = 5;
    private static final int DRAWER_INDEX_CHANGE_FONT = 6;
    private static final int DRAWER_INDEX_PRIVACY_BOX = 7;

    private int drawerClickIndex = DRAWER_INDEX_NONE;

    // Drawer related stuff
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView mTitleTextView;
    private View mEmojiStoreIconView;
    private LottieAnimationView mGuideContainer;

    private static boolean mIsNoActionBack = true;
    private boolean mIsRealCreate = false;
    private boolean mShowEndAnimation;
    private boolean hideAnimation;
    private boolean shouldShowCreateShortcutGuide;
    private String size;
    private View mPrivateBoxEntrance;

    private boolean mIsMessageMoving;

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

        if (mShouldFinishThisTime) {
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
            if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                BugleAnalytics.logEvent("SMS_Messages_Show_NewUser", true);
            }
        }

        if (getIntent() != null && getIntent().getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true, true);
        }

        setupDrawer();

        HSGlobalNotificationCenter.addObserver(EVENT_MAINPAGE_RECREATE, this);
        HSGlobalNotificationCenter.addObserver(SHOW_EMOJI, this);
        HSGlobalNotificationCenter.addObserver(FIRST_LOAD, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_NAME_MESSAGES_MOVE_END, this);
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

                BugleAnalytics.logEvent("SMS_Messages_Create", true, true,
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
                                            + wallpaper + "|" + ChooseThemeColorRecommendViewHolder.getPrimaryColorType());
                        }
                    }, "pref_key_customize_config_has_send");
                }
            });

            if (AdConfig.isDetailpageTopAdEnabled()) {
                AcbNativeAdManager.preload(1, AdPlacement.AD_DETAIL_NATIVE);
            }
        }

        Trace.endSection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSGlobalNotificationCenter.sendNotification(MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppPrivateLockManager.getInstance().lockAppLock();
        if (mPrivateBoxEntrance != null) {
            if (PrivateSettingManager.isPrivateBoxIconHidden()) {
                mPrivateBoxEntrance.setVisibility(View.GONE);
            } else {
                mPrivateBoxEntrance.setVisibility(View.VISIBLE);
            }
        }

        BugleAnalytics.logEvent("SMS_Messages_Show_Corrected", true, true);
        Preferences.getDefault().incrementAndGetInt(CustomizeGuideController.PREF_KEY_MAIN_PAGE_SHOW_TIME);
        if (Preferences.getDefault().getInt(CustomizeGuideController.PREF_KEY_MAIN_PAGE_SHOW_TIME, 0) == 2) {
            Threads.postOnMainThreadDelayed(() -> showEmojiStoreGuide(), 500);
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
            mEmojiStoreIconView.setVisibility(View.VISIBLE);
        }

        if (getActionMode() == null) {
            findViewById(R.id.selection_mode_bg).setVisibility(View.INVISIBLE);
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
                BugleAnalytics.logEvent("Menu_Show", true, true);
                if (CommonUtils.isNewUser()
                        && Calendars.isSameDay(CommonUtils.getAppInstallTimeMillis(), System.currentTimeMillis())) {
                    BugleAnalytics.logEvent("Menu_Show_NewUser", true);
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                switch (drawerClickIndex) {
                    case DRAWER_INDEX_THEME:
                        BugleAnalytics.logEvent("Menu_Theme_Click");
                        //Navigations.startActivity(ConversationListActivity.this, ChooseThemeActivity.class);
                        Navigations.startActivity(ConversationListActivity.this, ThemeSelectActivity.class);
                        navigationContent.findViewById(R.id.navigation_item_theme_new_text).setVisibility(View.GONE);
                        break;

                    case DRAWER_INDEX_THEME_COLOR:
                        BugleAnalytics.logEvent("Menu_ThemeColor_Click", true);
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_ThemeColor_Click_NewUser", true);
                        }
                        Navigations.startActivity(ConversationListActivity.this, ThemeColorSelectActivity.class);
                        navigationContent.findViewById(R.id.navigation_item_theme_color_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_BUBBLE:
                        BugleAnalytics.logEvent("Menu_Bubble_Click", true, true);
                        if (CommonUtils.isNewUser() && DateUtils.isToday(CommonUtils.getAppInstallTimeMillis())) {
                            BugleAnalytics.logEvent("Menu_Bubble_Click_NewUser", true);
                        }

                        if (DEBUGGING_MESSAGE_BOX) {
                            UIIntents.get().launchMessageBoxActivity(getApplicationContext(), new MessageBoxItemData("asd",
                                    "asd", "asd", "asd", "asd", "heihei", 123L));
                        } else {
                            Navigations.startActivity(ConversationListActivity.this, CustomBubblesActivity.class);
                        }
                        break;
                    case DRAWER_INDEX_CHAT_BACKGROUND:
                        BugleAnalytics.logEvent("Menu_ChatBackground_Click", true, true);
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
                    case DRAWER_INDEX_PRIVACY_BOX:
                        BugleAnalytics.logEvent("Menu_PrivateBox_Click", true);
                        if (PrivateBoxSettings.isAnyPasswordSet()) {
                            Intent intent1 = new Intent(ConversationListActivity.this, SelfVerifyActivity.class);
                            intent1.putExtra(SelfVerifyActivity.INTENT_KEY_ACTIVITY_ENTRANCE,
                                    SelfVerifyActivity.ENTRANCE_MENU);
                            Navigations.startActivitySafely(ConversationListActivity.this, intent1);
                        } else {
                            Navigations.startActivitySafely(ConversationListActivity.this,
                                    new Intent(ConversationListActivity.this, PrivateBoxSetPasswordActivity.class));
                        }
                        navigationContent.findViewById(R.id.navigation_item_private_box_new_text).setVisibility(View.GONE);
                        break;
                    case DRAWER_INDEX_SETTING:
                        boolean granted = RuntimePermissions.checkSelfPermission(ConversationListActivity.this,
                                Manifest.permission.READ_PHONE_STATE) == RuntimePermissions.PERMISSION_GRANTED;
                        if (granted) {
                            UIIntents.get().launchSettingsActivity(ConversationListActivity.this);
                            BugleAnalytics.logEvent("Menu_Settings_Click", true, true);
                        } else {
                            RuntimePermissions.requestPermissions(ConversationListActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION_CODE);
                        }
                        break;
                    case DRAWER_INDEX_RATE:
                        FiveStarRateDialog.showFiveStarFromSetting(ConversationListActivity.this);
                        BugleAnalytics.logEvent("Menu_FiveStart_Click", true, true);
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

            if (!Preferences.getDefault().getBoolean(PREF_KEY_PRIVATE_BOX_CLICKED, false)) {
                View bubbleNewMark = navigationContent.findViewById(R.id.navigation_item_private_box_new_text);
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
        navigationContent.findViewById(R.id.navigation_item_rate).setOnClickListener(this);
        //test code
        //this item is used to delete dirty mms parts in telephony
        navigationContent.findViewById(R.id.navigation_item_clear_private_parts).setVisibility(View.GONE);
        navigationContent.findViewById(R.id.navigation_item_clear_private_parts).setOnClickListener(v -> {
            Threads.postOnSingleThreadExecutor(() -> {
                List<Integer> privatePartIdList = new ArrayList<>();
                final Cursor c = SqliteWrapper.query(HSApplication.getContext(), HSApplication.getContext().getContentResolver(),
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
                    int k = SqliteWrapper.delete(HSApplication.getContext(),
                            HSApplication.getContext().getContentResolver(),
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
            return;
        }

        if (!Preferences.getDefault().contains(PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN)
                && HSConfig.optBoolean(false, "Application", "ShortcutLikeSystemSMS")) {
            Drawable smsIcon = CreateShortcutUtils.getSystemSMSIcon();
            if (smsIcon != null && ShortcutManagerCompat.isRequestPinShortcutSupported(HSApplication.getContext())) {
                shouldShowCreateShortcutGuide = true;
                Preferences.getDefault().putBoolean(PREF_KEY_CREATE_SHORTCUT_GUIDE_SHOWN, true);
                Navigations.startActivitySafely(ConversationListActivity.this,
                        new Intent(ConversationListActivity.this, CreateShortcutActivity.class));
                return;
            }
        }

        if (!shouldShowCreateShortcutGuide
                && FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
            return;
        }

        BugleAnalytics.logEvent("SMS_Messages_Back", true);
        super.onBackPressed();
        overridePendingTransition(0, 0);
        int mainActivityCreateTime = Preferences.get(DESKTOP_PREFS).getInt(PREF_KEY_MAIN_ACTIVITY_SHOW_TIME, 0);
        if (!shouldShowCreateShortcutGuide && mainActivityCreateTime >= 2) {
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

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        mTitleTextView.setVisibility(View.GONE);
        stopEmojiStoreGuide();
        mEmojiStoreIconView.setVisibility(View.GONE);
        findViewById(R.id.selection_mode_bg).setVisibility(View.VISIBLE);
        BugleAnalytics.logEvent("SMS_EditMode_Show", true, true);
        return super.startActionMode(callback);
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
    public void onPin(Collection<MultiSelectActionModeCallback.SelectedConversation> conversations, boolean pin) {
        if (pin) {
            BugleAnalytics.logEvent("SMS_EditMode_Pin_Click", true);
        } else {
            BugleAnalytics.logEvent("SMS_EditMode_Unpin_Click", true);
        }

        for (MultiSelectActionModeCallback.SelectedConversation conversation : conversations) {
            if (pin) {
                PinConversationAction.pinConversation(conversation.conversationId);
            } else {
                PinConversationAction.unpinConversation(conversation.conversationId);
            }
        }
        exitMultiSelectState();
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

    private void configAppBar() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        ViewGroup.LayoutParams layoutParams = accessoryContainer.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationListActivity.this) + Dimensions.pxFromDp(56);
        accessoryContainer.setLayoutParams(layoutParams);
        if (ToolbarDrawables.getToolbarBg() != null) {
            ImageView ivAccessoryBg = accessoryContainer.findViewById(R.id.accessory_bg);
            ivAccessoryBg.setVisibility(View.VISIBLE);
            ivAccessoryBg.setImageDrawable(ToolbarDrawables.getToolbarBg());
        } else {
            accessoryContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());
            accessoryContainer.findViewById(R.id.accessory_bg).setVisibility(View.GONE);
        }

        View statusbarInset = findViewById(R.id.status_bar_inset);
        layoutParams = statusbarInset.getLayoutParams();
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
        mGuideContainer = findViewById(R.id.emoji_store_guide_content);
        mEmojiStoreIconView = findViewById(R.id.emoji_store_icon);
        mEmojiStoreIconView.setScaleX(1f);
        mEmojiStoreIconView.setScaleY(1f);
        mEmojiStoreIconView.setOnClickListener(v -> {
            if (!mIsEmojiStoreClickable) {
                return;
            }
            logFirstComeInClickEvent("emojistore");
            BugleAnalytics.logEvent("SMS_Messages_Emojistore_Click", true, true);
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
                BugleAnalytics.logEvent("SMS_Messages_First_Click", true, true, "type", type);
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
            case R.id.navigation_item_rate:
                drawerClickIndex = DRAWER_INDEX_RATE;
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.navigation_item_privacy_box:
                drawerClickIndex = DRAWER_INDEX_PRIVACY_BOX;
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
            case SHOW_EMOJI:
                WeakReference<AppCompatActivity> activity = new WeakReference<>(this);
                Threads.postOnMainThreadDelayed(() -> {
                    if (!isFinishing() && activity.get() != null) {
                        CustomizeGuideController.showGuideIfNeed(activity.get());
                    }
                }, 1000);
                break;
            case FIRST_LOAD:
                if (!sIsRecreate && hsBundle != null) {
                    boolean hasPinConversation = hsBundle.getBoolean(HAS_PIN_CONVERSATION);
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    BugleAnalytics.logEvent("SMS_Messages_Show_1", true,
                            "font", FontStyleManager.getInstance().getFontFamily(),
                            "size", size,
                            "open time", String.valueOf(hour),
                            "signature", String.valueOf(!TextUtils.isEmpty(Preferences.getDefault().getString(
                                    SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null))),
                            "pin", String.valueOf(hasPinConversation), "defaultlauncher", getPackageName());
                }
                break;
            case NOTIFICATION_NAME_MESSAGES_MOVE_END:
                if (mIsMessageMoving) {
                    mIsMessageMoving = false;
                    Toasts.showToast(R.string.private_box_add_to_success);
                }
                break;
            default:
                break;
        }
    }
}
