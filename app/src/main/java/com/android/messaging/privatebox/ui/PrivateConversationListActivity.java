package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Choreographer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.messaging.R;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.privatebox.MoveConversationToPrivateBoxAction;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.privatebox.ui.addtolist.AddToListDialog;
import com.android.messaging.privatebox.ui.addtolist.ContactsSelectActivity;
import com.android.messaging.privatebox.ui.addtolist.ConversationSelectActivity;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.mainpage.ChatListCustomizeManager;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateConversationListActivity extends MultiSelectConversationListActivity {
    private static final String NOTIFICATION_KEY_MESSAGES_MOVE_START = "conversations_move_to_private_start";
    private static final String NOTIFICATION_KEY_MESSAGES_MOVE_END = "conversations_move_to_private_end";

    private static final String PREF_KEY_ADD_BUTTON_CLICKED = "pref_key_private_toolbar_add_button_clicked";
    private PrivateConversationListFragment mConversationListFragment;
    private View mTitle;

    private volatile boolean mIsMessageMoving;
    private View mProcessBarContainer;
    private ProgressBar mProgressBar;

    private long mStartTime;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;
    private INotificationObserver mNotificationObserver;
    private boolean mHasTheme;
    private boolean mIsActivityFirstStart = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_conversation_list_activity);
        mConversationListFragment = (PrivateConversationListFragment) getFragmentManager().
                findFragmentById(R.id.private_conversation_list_fragment);
        configActionBar();

        BugleAnalytics.logEvent("PrivateBox_Homepage_Show", true,
                "HideTheIcon", String.valueOf(PrivateSettingManager.isPrivateBoxIconHidden()));
        if (getIntent().hasExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST)) {
            String[] conversationList = getIntent().getStringArrayExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST);
            List<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(conversationList));
            addAndMoveConversations(list);
        }

        mProcessBarContainer = findViewById(R.id.private_progress_bar_container);
        mProgressBar = findViewById(R.id.private_move_progress_bar);

        mChoreographer = Choreographer.getInstance();
        mFrameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (mIsMessageMoving) {
                    mProgressBar.setProgress((int) ((System.currentTimeMillis() - mStartTime) / 10));
                    mChoreographer.postFrameCallback(this);
                }
            }
        };

        mNotificationObserver = (s, hsBundle) -> {
            switch (s) {
                case NOTIFICATION_KEY_MESSAGES_MOVE_START:
                    if (mIsMessageMoving) {
                        return;
                    }
                    Threads.postOnMainThread(() -> {
                        if (mIsMessageMoving) {
                            return;
                        }
                        mStartTime = System.currentTimeMillis();
                        mIsMessageMoving = true;
                        mProcessBarContainer.setVisibility(View.VISIBLE);
                        mChoreographer.postFrameCallback(mFrameCallback);
                    });
                    break;
                case NOTIFICATION_KEY_MESSAGES_MOVE_END:
                    Threads.postOnMainThread(() -> {
                        mIsMessageMoving = false;
                        mChoreographer.removeFrameCallback(mFrameCallback);
                        mProcessBarContainer.setVisibility(View.GONE);
                    });
                    break;
            }
        };
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_KEY_MESSAGES_MOVE_START, mNotificationObserver);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_KEY_MESSAGES_MOVE_END, mNotificationObserver);
    }

    @Override
    protected void onStart() {
        if (!mIsActivityFirstStart) {
            AppPrivateLockManager.getInstance().checkLockStateAndSelfVerify();
        }
        mIsActivityFirstStart = false;
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        mIsMessageMoving = false;
        mChoreographer.removeFrameCallback(mFrameCallback);
        HSGlobalNotificationCenter.removeObserver(mNotificationObserver);
        super.onDestroy();
    }

    private void addAndMoveConversations(List<String> conversationList) {
        if (conversationList.size() > 0) {
            MoveConversationToPrivateBoxAction.moveAndUpdatePrivateContact(conversationList,
                    NOTIFICATION_KEY_MESSAGES_MOVE_START, NOTIFICATION_KEY_MESSAGES_MOVE_END);
        }
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        //mStatusBarInset.setBackgroundColor(PrimaryColors.getPrimaryColor());

        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setBackgroundDrawable(new ColorDrawable(PrimaryColors.getPrimaryColor()));
        actionBar.show();
        if (mTitle != null && mActionMode == null) {
            mTitle.setVisibility(View.VISIBLE);
        }

        if (getActionMode() == null) {
            findViewById(R.id.selection_mode_bg).setVisibility(View.INVISIBLE);
        }

        if (mActionMode == null && getSupportActionBar() != null) {
            Drawable drawable = HSApplication.getContext().getResources().getDrawable(R.drawable.ic_back).mutate();
            ChatListCustomizeManager.changeDrawableColorIfNeed(drawable);
            getSupportActionBar().setHomeAsUpIndicator(drawable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mActionMode != null) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(R.menu.private_list_conversation_list_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            ChatListCustomizeManager.changeDrawableColorIfNeed(menu.getItem(i).getIcon());
        }
        if (!Preferences.getDefault().getBoolean(PREF_KEY_ADD_BUTTON_CLICKED, false)) {
            Drawable drawable = menu.findItem(R.id.private_action_add).getIcon();
            ShapeDrawable markDrawable = new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {
                    float x = getWidth() * 25.3f / 30;
                    float y = getHeight() * 4.7f / 30;
                    paint.setStyle(Paint.Style.FILL);
                    paint.setAntiAlias(true);
                    paint.setDither(true);
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(x, y, Dimensions.pxFromDp(3), paint);
                    paint.setColor(Color.RED);
                    canvas.drawCircle(x, y, Dimensions.pxFromDp(2), paint);
                }
            });
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable, markDrawable});
            menu.findItem(R.id.private_action_add).setIcon(layerDrawable);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else if (mIsMessageMoving) {

        } else {
            if (!showUninstallNoticeDialog()) {
                super.onBackPressed();
            }
        }
    }

    public boolean showUninstallNoticeDialog() {
        if (mConversationListFragment != null &&
                !mConversationListFragment.isConversationListEmpty() &&
                !Preferences.getDefault().getBoolean("pref_key_uninstall_dialog_shown", false)) {
            new BaseAlertDialog.Builder(this)
                    .setTitle(R.string.tips)
                    .setMessage(R.string.private_box_uninstall_notice)
                    .setPositiveButton(R.string.welcome_set_default_button, (dialog, button) -> finish())
                    .show();
            Preferences.getDefault().putBoolean("pref_key_uninstall_dialog_shown", true);
            return true;
        }
        return false;
    }

    @Override
    public void updateUi() {
        mConversationListFragment.updateUi();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        if (mTitle != null) {
            mTitle.setVisibility(View.GONE);
        }
        if (mHasTheme) {
            findViewById(R.id.selection_mode_bg).setVisibility(View.VISIBLE);
        }
        return super.startActionMode(callback);
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
        if (hasFocus && mConversationListFragment != null) {
            mConversationListFragment.setScrolledToNewestConversationIfNeeded();
        }
    }

    private void configActionBar() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        ViewGroup.LayoutParams lp = accessoryContainer.getLayoutParams();
        lp.height = Dimensions.getStatusBarHeight(PrivateConversationListActivity.this) + Dimensions.pxFromDp(56);
        accessoryContainer.setLayoutParams(lp);
        Drawable customToolBar = ChatListCustomizeManager.getToolbarDrawable();
        if (customToolBar != null) {
            ImageView ivAccessoryBg = accessoryContainer.findViewById(R.id.accessory_bg);
            ivAccessoryBg.setVisibility(View.VISIBLE);
            ivAccessoryBg.setImageDrawable(customToolBar);
        } else if (ToolbarDrawables.getToolbarBg() != null) {
            mHasTheme = true;
            findViewById(R.id.accessory_bg).setBackground(ToolbarDrawables.getToolbarBg());
        } else {
            accessoryContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());
            findViewById(R.id.accessory_bg).setVisibility(View.GONE);
        }
        mConversationListFragment.onThemeChanged(mHasTheme);

        View mStatusBarInset = findViewById(R.id.status_bar_inset);
        ViewGroup.LayoutParams layoutParams = mStatusBarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(this);
        mStatusBarInset.setLayoutParams(layoutParams);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        mTitle = findViewById(R.id.private_conversation_title);
        ChatListCustomizeManager.changeViewColorIfNeed(mTitle);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        invalidateActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (mActionMode != null &&
                mActionMode.getCallback().onActionItemClicked(mActionMode, menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (mActionMode != null) {
                    dismissActionMode();
                    return true;
                }
                if (!showUninstallNoticeDialog()) {
                    finish();
                }
                return true;
            case R.id.private_action_add:
                BugleAnalytics.logEvent("PrivateBox_Homepage_AddContact_BtnClick");
                final AddToListDialog addToBlackListDialog = new AddToListDialog(PrivateConversationListActivity.this);
                addToBlackListDialog.setOnButtonClickListener(new AddToListDialog.OnButtonClickListener() {
                    @Override
                    public void onFromConversationClick() {
                        BugleAnalytics.logEvent("PrivateBox_AddContactAlert_BtnClick", "type", "conversation");
                        Navigations.startActivitySafely(PrivateConversationListActivity.this,
                                new Intent(PrivateConversationListActivity.this, ConversationSelectActivity.class));
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        addToBlackListDialog.dismiss();
                    }

                    @Override
                    public void onFromContactsClick() {
                        BugleAnalytics.logEvent("PrivateBox_AddContactAlert_BtnClick", "type", "contact");
                        Navigations.startActivitySafely(PrivateConversationListActivity.this,
                                new Intent(PrivateConversationListActivity.this, ContactsSelectActivity.class));
                        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                        addToBlackListDialog.dismiss();
                    }
                });

                addToBlackListDialog.show();
                Preferences.getDefault().putBoolean(PREF_KEY_ADD_BUTTON_CLICKED, true);
                menuItem.setIcon(R.drawable.private_add_btn);
                break;
            case R.id.private_action_setting:
                Navigations.startActivitySafely(this, new Intent(this, PrivateSettingActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
