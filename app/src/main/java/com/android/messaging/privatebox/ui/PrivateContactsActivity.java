package com.android.messaging.privatebox.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.datamodel.data.PrivateContactItemData;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.privatebox.MoveConversationToTelephonyAction;
import com.android.messaging.privatebox.MoveRecipientsToTelephonyAction;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.privatebox.ui.view.PrivateContactsAdapter;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

public class PrivateContactsActivity extends BaseActivity implements PrivateContactsAdapter.PrivateContactsHost {
    private PrivateContactsAdapter mAdapter;
    private List<String> mRemoveConversationIdList = new ArrayList<>();
    private View mEmptyListMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_contact);
        startQueryData();
        mEmptyListMessageView = findViewById(R.id.private_contact_empty_container);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        AppPrivateLockManager.getInstance().checkLockStateAndSelfVerify();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeContactsFromPrivateBox(mRemoveConversationIdList);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_null, R.anim.slide_out_to_right_and_fade);
    }

    private void removeContactsFromPrivateBox(List<String> addList) {
        if (addList.size() != 0) {
            MoveConversationToTelephonyAction.moveToTelephony((ArrayList<String>) addList,
                    null, null);
        }
    }

    private void startQueryData() {
        Threads.postOnThreadPoolExecutor(() -> {
            List<String> privateRecipients = PrivateContactsManager.getInstance().getPrivateRecipientList();
            List<PrivateContactItemData> PrivateContactListItemDataList = new ArrayList<>(privateRecipients.size());

            for (String privateRecipient : privateRecipients) {
                Cursor matchingContactCursor = ContactUtil.lookupPhone(this, privateRecipient).performSynchronousQuery();

                PrivateContactItemData data = new PrivateContactItemData();
                if (matchingContactCursor != null && matchingContactCursor.moveToFirst()) {
                    data.bind(matchingContactCursor);
                } else {
                    data.setName(privateRecipient);
                    data.setDestination(privateRecipient);
                }
                PrivateContactListItemDataList.add(data);

                if (matchingContactCursor != null) {
                    matchingContactCursor.close();
                }
            }
            Threads.postOnMainThread(() -> {
                mEmptyListMessageView.setVisibility(PrivateContactListItemDataList.isEmpty() ? View.VISIBLE : View.GONE);
                mAdapter.updateData(PrivateContactListItemDataList);
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrivateContactsRemoveButtonClick(PrivateContactItemData data, boolean isPrivateContactListEmpty) {
        if (isPrivateContactListEmpty) {
            mEmptyListMessageView.setVisibility(View.VISIBLE);
        }
        MoveRecipientsToTelephonyAction.moveToTelephony(data.getDestination().toString());
    }
}
