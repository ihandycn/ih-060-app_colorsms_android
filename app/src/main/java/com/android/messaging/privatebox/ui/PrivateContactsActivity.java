package com.android.messaging.privatebox.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.privatebox.MoveConversationToTelephonyAction;
import com.android.messaging.privatebox.ui.view.PrivateContactsAdapter;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.datamodel.data.ConversationListData.SORT_ORDER;

public class PrivateContactsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, PrivateContactsAdapter.PrivateContactsHost {
    private static final int CONVERSATION_LIST_LOADER = 1;
    private PrivateContactsAdapter mAdapter;
    private LoaderManager mLoaderManager;
    private List<String> mRemoveConversationList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_contact);
        mAdapter = new PrivateContactsAdapter();
        BugleAnalytics.logEvent("PrivateBox_PrivateContacts_Show");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.private_contacts));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        RecyclerView recyclerView = findViewById(R.id.private_contact_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setHost(this);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(CONVERSATION_LIST_LOADER, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoaderManager.destroyLoader(CONVERSATION_LIST_LOADER);
    }

    @Override
    protected void onStart() {
        AppPrivateLockManager.getInstance().checkLockStateAndSelfVerify();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeConversationsFromPrivateBox(mRemoveConversationList);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void removeConversationsFromPrivateBox(List<String> addList) {
        MoveConversationToTelephonyAction.moveToTelephony((ArrayList<String>) addList,
                null, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = new CursorLoader(PrivateContactsActivity.this,
                MessagingContentProvider.CONVERSATIONS_URI,
                ConversationListItemData.PROJECTION,
                "",
                null,
                SORT_ORDER);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            List<ConversationListItemData> dataList = new ArrayList<>(cursor.getCount());
            do {
                ConversationListItemData itemData = new ConversationListItemData();
                itemData.bind(cursor);
                if (itemData.isPrivate()) {
                    dataList.add(itemData);
                }
            } while (cursor.moveToNext());
            mAdapter.updateData(dataList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onPrivateContactsRemoveButtonClick(ConversationListItemData conversationListItemData) {
        mRemoveConversationList.add(conversationListItemData.getConversationId());
    }
}
