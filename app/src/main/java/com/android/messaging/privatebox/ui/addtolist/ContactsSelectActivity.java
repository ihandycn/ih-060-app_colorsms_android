package com.android.messaging.privatebox.ui.addtolist;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Choreographer;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.privatebox.AddPrivateContactAction;
import com.android.messaging.privatebox.MoveConversationToPrivateBoxAction;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsSelectActivity extends HSAppCompatActivity {

    public final static String EVENT_MESSAGES_MOVE_START = "event_contact_messages_move_start";
    public final static String EVENT_MESSAGES_MOVE_END = "event_contact_messages_move_end";

    private ContactsSelectAdapter mAdapter;
    private volatile boolean mIsMessageMoving;
    private View mProcessBarContainer;
    private ProgressBar mProgressBar;
    private INotificationObserver mContactMoveObserver;
    private View mActionButton;

    private long mStartTime;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;
    private long mResponseCode = System.currentTimeMillis();
    private String mMoveStartKey = EVENT_MESSAGES_MOVE_START + mResponseCode;
    private String mMoveEndKey = EVENT_MESSAGES_MOVE_END + mResponseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_blocker);

        startQueryData();

        mAdapter = new ContactsSelectAdapter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.contacts));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.fragment_missed_calls_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        mActionButton = findViewById(R.id.private_box_add_btn);
        mActionButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));

        mActionButton.setOnClickListener(v -> {
            if (Preferences.getDefault().getBoolean(ConversationSelectActivity.PREF_KEY_ADD_PRIVATE_DIALOG_HAS_PROMPT, false)) {
                addContactToPrivate();
            } else {
                new BaseAlertDialog.Builder(ContactsSelectActivity.this)
                        .setTitle(R.string.private_move_tip)
                        .setPositiveButton(R.string.welcome_set_default_button, (dialog, button) -> addContactToPrivate())
                        .setNegativeButton(R.string.delete_conversation_decline_button, null)
                        .show();
                Preferences.getDefault().putBoolean(ConversationSelectActivity.PREF_KEY_ADD_PRIVATE_DIALOG_HAS_PROMPT, true);
            }
        });

        mProcessBarContainer = findViewById(R.id.private_progress_bar_container);
        mProcessBarContainer.setOnClickListener(v -> {

        });
        mProgressBar = findViewById(R.id.private_move_progress_bar);
        mContactMoveObserver = (s, hsBundle) -> {
            if (mMoveStartKey.equals(s)) {
                Threads.runOnMainThread(this::startMessagesMoveProgress);
            } else if (mMoveEndKey.equals(s)) {
                Threads.runOnMainThread(this::stopMessageMoveProgress);
            }
        };

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
        HSGlobalNotificationCenter.addObserver(mMoveStartKey, mContactMoveObserver);
        HSGlobalNotificationCenter.addObserver(mMoveEndKey, mContactMoveObserver);
    }

    @Override
    protected void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(mContactMoveObserver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mIsMessageMoving) {
            return;
        }
        finish();
    }

    private void addContactToPrivate() {
        List<String> addList = new ArrayList<>();
        for (CallAssistantUtils.ContactInfo contactInfo : mAdapter.getRecyclerDataList()) {
            if (contactInfo.customInfo.equals(Boolean.TRUE) && !TextUtils.isEmpty(contactInfo.number)) {
                addList.add(contactInfo.number);
            }
        }

        if (addList.size() <= 0) {
            return;
        }
        mActionButton.setEnabled(false);
        startMessagesMoveProgress();

        //1.update contact in private contact table
        AddPrivateContactAction.addPrivateRecipientsAndMoveMessages(addList);
        //2.move conversations and messages if need
        MoveConversationToPrivateBoxAction.moveByContact(addList,
                EVENT_MESSAGES_MOVE_START + mResponseCode,
                EVENT_MESSAGES_MOVE_END + mResponseCode);
    }

    private void startMessagesMoveProgress() {
        if (mIsMessageMoving) {
            return;
        }
        mStartTime = System.currentTimeMillis();
        mIsMessageMoving = true;
        mProcessBarContainer.setVisibility(View.VISIBLE);
        mChoreographer.postFrameCallback(mFrameCallback);
    }

    private void stopMessageMoveProgress() {
        mIsMessageMoving = false;
        mChoreographer.removeFrameCallback(mFrameCallback);
        mProcessBarContainer.setVisibility(View.GONE);
        Toasts.showToast(R.string.private_box_add_to_success);
        onBackPressed();
    }

    private void startQueryData() {
        Threads.postOnThreadPoolExecutor(() -> {
            final List<CallAssistantUtils.ContactInfo> list = new ArrayList<>();
            List<CallAssistantUtils.ContactInfo> contactsList = CallAssistantUtils.getAllContactsFromPhoneBook();
            // filter contact which is already private
            List<String> privateRecipients = PrivateContactsManager.getInstance().getPrivateRecipientList();
            List<CallAssistantUtils.ContactInfo> filterContactsList = new ArrayList<>();
            for (CallAssistantUtils.ContactInfo info : contactsList) {
                String recipient = PhoneUtils.getDefault().getCanonicalBySimLocale(info.number.trim());
                if (!privateRecipients.contains(recipient)) {
                    filterContactsList.add(info);
                }
            }
            Collections.sort(filterContactsList, (o1, o2) -> Collator.getInstance().compare(o1.toString(), o2.toString()));
            list.addAll(filterContactsList);

            Threads.postOnMainThread(() -> mAdapter.updateData(list));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
