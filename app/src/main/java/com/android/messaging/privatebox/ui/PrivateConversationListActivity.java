package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.privatebox.ui.addtolist.AddToListDialog;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Toasts;

import java.util.List;

public class PrivateConversationListActivity extends MultiSelectConversationListActivity {

    private PrivateConversationListFragment mConversationListFragment;
    private View mStatusBarInset;
    private View mTitle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_conversation_list_activity);
        configActionBar();
        mConversationListFragment = (PrivateConversationListFragment) getFragmentManager().
                findFragmentById(R.id.private_conversation_list_fragment);
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
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
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
                final AddToListDialog addToBlackListDialog = new AddToListDialog(PrivateConversationListActivity.this);
                addToBlackListDialog.setOnButtonClickListener(new AddToListDialog.OnButtonClickListener() {
                    @Override
                    public void onFromConversationClick() {

                        addToBlackListDialog.dismiss();
                    }

                    @Override
                    public void onFromContactsClick() {

                        addToBlackListDialog.dismiss();
                    }
                });

                addToBlackListDialog.show();
                break;
            case R.id.private_action_setting:
                Navigations.startActivitySafely(this, new Intent(this, PrivateSettingActivity.class));
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
