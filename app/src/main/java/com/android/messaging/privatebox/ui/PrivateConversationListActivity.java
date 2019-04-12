package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Choreographer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.messaging.R;
import com.android.messaging.privatebox.MessagesMoveManager;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.privatebox.ui.addtolist.AddToListDialog;
import com.android.messaging.privatebox.ui.addtolist.ContactsSelectActivity;
import com.android.messaging.privatebox.ui.addtolist.ConversationSelectActivity;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

import java.util.Arrays;
import java.util.List;

public class PrivateConversationListActivity extends MultiSelectConversationListActivity {

    private static final String PREF_KEY_ADD_BUTTON_CLICKED = "pref_key_private_toolbar_add_button_clicked";
    private PrivateConversationListFragment mConversationListFragment;
    private View mStatusBarInset;
    private View mTitle;

    private volatile boolean mIsMessageMoving;
    private View mProcessBarContainer;
    private ProgressBar mProgressBar;

    private long mStartTime;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_conversation_list_activity);
        configActionBar();
        mConversationListFragment = (PrivateConversationListFragment) getFragmentManager().
                findFragmentById(R.id.private_conversation_list_fragment);

        BugleAnalytics.logEvent("PrivateBox_Homepage_Show", true,
                "HideTheIcon", String.valueOf(PrivateSettingManager.isPrivateBoxIconHidden()));
        if (getIntent().hasExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST)) {
            String[] conversationList = getIntent().getStringArrayExtra(ConversationListActivity.INTENT_KEY_PRIVATE_CONVERSATION_LIST);
            addAndMoveConversations(Arrays.asList(conversationList));
        }
    }

    private void addAndMoveConversations(List<String> conversationList) {
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
        if (conversationList.size() > 0) {
            MessagesMoveManager.moveConversations(conversationList, false,
                    new MessagesMoveManager.MessagesMoveListener() {
                        @Override
                        public void onMoveStart() {
                            if (mIsMessageMoving) {
                                return;
                            }
                            mStartTime = System.currentTimeMillis();
                            mIsMessageMoving = true;
                            mProcessBarContainer.setVisibility(View.VISIBLE);
                            mChoreographer.postFrameCallback(mFrameCallback);
                        }

                        @Override
                        public void onMoveEnd() {
                            if (!mIsMessageMoving) {
                                return;
                            }
                            mIsMessageMoving = false;
                            mChoreographer.removeFrameCallback(mFrameCallback);
                            mProcessBarContainer.setVisibility(View.GONE);
                            Toasts.showToast(R.string.private_box_add_success);
                        }
                    });
        }
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        mStatusBarInset.setBackgroundColor(PrimaryColors.getPrimaryColor());

        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(PrimaryColors.getPrimaryColor()));
        actionBar.show();
        if (mTitle != null && mActionMode == null) {
            mTitle.setVisibility(View.VISIBLE);
        }

        if (mActionMode == null && getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mActionMode != null) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(R.menu.private_list_conversation_list_menu, menu);
        if (!Preferences.getDefault().getBoolean(PREF_KEY_ADD_BUTTON_CLICKED, false)) {
            menu.findItem(R.id.private_action_add).setIcon(R.drawable.private_add_btn_unclick);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else if (mIsMessageMoving) {

        } else {
            super.onBackPressed();
        }
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

    @Override
    public List<SnackBarInteraction> getSnackBarInteractions() {
        return mConversationListFragment.getSnackBarInteractions();
    }

    private void configActionBar() {
        mStatusBarInset = findViewById(R.id.status_bar_inset);
        ViewGroup.LayoutParams layoutParams = mStatusBarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(this);
        mStatusBarInset.setLayoutParams(layoutParams);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        mTitle = findViewById(R.id.private_conversation_title);
        toolbar.setBackgroundDrawable(new ColorDrawable(PrimaryColors.getPrimaryColor()));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
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
                finish();
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
                        addToBlackListDialog.dismiss();
                    }

                    @Override
                    public void onFromContactsClick() {
                        BugleAnalytics.logEvent("PrivateBox_AddContactAlert_BtnClick", "type", "contact");
                        Navigations.startActivitySafely(PrivateConversationListActivity.this,
                                new Intent(PrivateConversationListActivity.this, ContactsSelectActivity.class));
                        addToBlackListDialog.dismiss();
                    }
                });

                addToBlackListDialog.show();
                Preferences.getDefault().putBoolean(PREF_KEY_ADD_BUTTON_CLICKED, true);
                menuItem.setIcon(R.drawable.private_add_btn);
                break;
            case R.id.private_action_setting:
                Navigations.startActivitySafely(this, new Intent(this, PrivateSettingActivity.class));
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
