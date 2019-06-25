package com.android.messaging.privatebox.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.privatebox.MoveConversationToTelephonyAction;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.privatebox.ui.view.PrivateContactsAdapter;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Threads;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
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
            final List<String> list = new ArrayList<>();
            List<String> conversationIdList = new ArrayList<>();
            List<String> privateRecipients = PrivateContactsManager.getInstance().getPrivateRecipientList();
            for (String recipient : privateRecipients) {
                String phoneNumBySim = PhoneUtils.getDefault().getCanonicalBySimLocale(recipient);
                String phoneNumBySystem = PhoneUtils.getDefault().getCanonicalBySystemLocale(recipient);

                String participantId = BugleDatabaseOperations.getParticipantIdByName(phoneNumBySim);
                String conversationId =
                        BugleDatabaseOperations.getConversationIdForParticipantsGroup(Collections.singletonList(participantId));
                if (!TextUtils.isEmpty(conversationId)) {
                    conversationIdList.add(conversationId);
                }

                if (!phoneNumBySim.equals(phoneNumBySystem)) {
                    String conversationId1 =
                            BugleDatabaseOperations.getConversationIdForParticipantsGroup(
                                    Collections.singletonList(
                                            BugleDatabaseOperations.getParticipantIdByName(phoneNumBySim)));
                    if (!TextUtils.isEmpty(conversationId1)) {
                        conversationIdList.add(conversationId1);
                    }
                }
            }
            Collections.sort(conversationIdList, (o1, o2) -> Collator.getInstance().compare(o1, o2));
            list.addAll(conversationIdList);

            Threads.postOnMainThread(() -> {
                final DatabaseWrapper db = DataModel.get().getDatabase();
                mEmptyListMessageView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                mAdapter.updateData(list, db);
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
    public void onPrivateContactsRemoveButtonClick(ConversationListItemData conversationListItemData, boolean isPrivateContactListEmpty) {
        mRemoveConversationIdList.add(conversationListItemData.getConversationId());
        if (isPrivateContactListEmpty) {
            mEmptyListMessageView.setVisibility(View.VISIBLE);
        }
    }
}
