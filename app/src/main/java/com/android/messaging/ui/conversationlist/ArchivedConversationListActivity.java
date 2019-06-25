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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.PinConversationAction;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.Dimensions;

import java.util.Collection;
import java.util.List;

public class ArchivedConversationListActivity extends AbstractConversationListActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_archived_conversation);

        configAppBar();

        final ConversationListFragment fragment =
                ConversationListFragment.createArchivedConversationListFragment();
        getFragmentManager().beginTransaction().add(R.id.root_view, fragment).commit();
    }

    private void configAppBar() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        ViewGroup.LayoutParams layoutParams = accessoryContainer.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(this) + Dimensions.pxFromDp(56);
        accessoryContainer.setLayoutParams(layoutParams);
        if (ToolbarDrawables.getToolbarBg() != null) {
            ImageView ivAccessoryBg = accessoryContainer.findViewById(R.id.accessory_bg);
            ivAccessoryBg.setVisibility(View.VISIBLE);
            ivAccessoryBg.setImageDrawable(ToolbarDrawables.getToolbarBg());
        } else {
            accessoryContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());
            accessoryContainer.findViewById(R.id.accessory_bg).setVisibility(View.GONE);
        }

        View statusBarInset = findViewById(R.id.status_bar_inset);
        layoutParams = statusBarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(this);
        statusBarInset.setLayoutParams(layoutParams);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.archived_conversations));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        invalidateActionBar();
        //setupToolbarUI();
    }

    protected void updateActionBar(ActionBar actionBar) {
        if (actionBar == null) {
            return;
        }

        if (getActionMode() == null) {
            findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
            findViewById(R.id.selection_mode_bg).setVisibility(View.INVISIBLE);
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            findViewById(R.id.toolbar_title).setVisibility(View.GONE);
            findViewById(R.id.selection_mode_bg).setVisibility(View.VISIBLE);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.show();

        if (getActionMode() == null) {
            findViewById(R.id.selection_mode_bg).setVisibility(View.INVISIBLE);
        }

        super.updateActionBar(actionBar);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        findViewById(R.id.toolbar_title).setVisibility(View.GONE);
        findViewById(R.id.selection_mode_bg).setVisibility(View.VISIBLE);
        return super.startActionMode(callback);
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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onActionBarHome();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean isArchiveMode() {
        return true;
    }

    @Override
    public void onActionBarHome() {
        onBackPressed();
    }

    @Override
    public void onActionMenu() {

    }

    @Override
    public void onAddToPrivateBox(List<String> conversations) {

    }

    @Override
    public boolean isSwipeAnimatable() {
        return true;
    }

    public static void logUnarchiveEvent(DatabaseWrapper db, String conversationId, String from) {
        //for archive log
        long count = db.queryNumEntries(DatabaseHelper.CONVERSATIONS_TABLE,
                DatabaseHelper.ConversationColumns.ARCHIVE_STATUS + "=1 AND "
                        + DatabaseHelper.ConversationColumns._ID + "=" + conversationId, null);
        if (count == 1) {
            BugleAnalytics.logEvent("SMS_Messages_Unarchive", true, "from", from);
        }
    }
}
