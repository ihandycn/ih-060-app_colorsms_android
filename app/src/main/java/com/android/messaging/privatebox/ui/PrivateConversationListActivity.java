package com.android.messaging.privatebox.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.Dimensions;

import java.util.List;

public class PrivateConversationListActivity extends MultiSelectConversationListActivity {

    private TextView mToolbarTitle;
    private PrivateConversationListFragment mConversationListFragment;
    private View mToolbarSettingIcon;
    private View mToolbarAddIcon;
    private View mStatusBarInset;

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

        if (mToolbarTitle != null && mToolbarTitle.getVisibility() == View.GONE) {
            mToolbarTitle.setVisibility(View.VISIBLE);
            mToolbarSettingIcon.setVisibility(View.VISIBLE);
            mToolbarAddIcon.setVisibility(View.VISIBLE);
        }
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
        mToolbarTitle.setVisibility(View.GONE);
        mToolbarSettingIcon.setVisibility(View.GONE);
        mToolbarAddIcon.setVisibility(View.GONE);
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
        toolbar.setContentInsetsRelative(0, 0);
        LayoutInflater.from(this).inflate(R.layout.private_conversation_list_toolbar_layout, toolbar, true);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.acb_adadapter_settings);

        mToolbarSettingIcon = findViewById(R.id.private_toolbar_setting_icon);
        mToolbarAddIcon = findViewById(R.id.private_toolbar_add_icon);
        mToolbarTitle = findViewById(R.id.private_toolbar_title);

        mToolbarSettingIcon.setOnClickListener(v -> {

        });

        mToolbarAddIcon.setOnClickListener(v -> {

        });

        invalidateActionBar();
    }
}
