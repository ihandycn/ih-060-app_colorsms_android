package com.android.messaging.ui.invitefriends;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.action.InsertNewMessageAction;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ContactPickerData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;
import com.android.messaging.ui.PlainTextEditText;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import java.util.ArrayList;

public class InviteFriendsActivity extends AppCompatActivity implements ContactPickerData.ContactPickerDataListener {

    public static final String INTENT_KEY_FROM = "from";

    static final int REQUEST_CODE_ADD_FRIENDS = 12;

    @VisibleForTesting
    final Binding<ContactPickerData> mBinding = BindingBase.createBinding(this);


    private InviteFriendsListAdapter mAdapter;
    private PlainTextEditText mEditText;
    private TextView mInviteButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        initToolBar();

        MessagesTextView autoLinkMessagesTextView = findViewById(R.id.invite_friends_message_auto_link);
        stripUnderlines(autoLinkMessagesTextView);
        mEditText = findViewById(R.id.invite_friends_message_text);
        mEditText.setText(InviteFriendsTest.getSendDescription());
        mEditText.clearFocus();

        initRecyclerView();
        mInviteButton = findViewById(R.id.invite_button);
        mInviteButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(6.7f), true));

        mInviteButton.setVisibility(View.GONE);
        mInviteButton.setOnClickListener(v -> {
            InsertNewMessageAction.insertNewMessage(ParticipantData.DEFAULT_SELF_SUB_ID, mAdapter.getRecipients(),
                    mEditText.getText().append(getString(R.string.invite_friends_invite_auto_link_content)).toString(), "");
            Toasts.showToast(R.string.invite_friends_success_toast);

            BugleAnalytics.logEvent("Invite_SendPage_Invite_Click");
            BugleAnalytics.logEvent("Invite_SMS_Send");
            InviteFriendsTest.logInviteFriendsClick();
            InviteFriendsTest.logInviteSmsSent();
            finish();
        });

        BugleAnalytics.logEvent("Invite_SendPage_Show", true, "from", getIntent().getStringExtra(INTENT_KEY_FROM));

    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.friends_list_recycler_view);
        float size = getResources().getDimensionPixelSize(R.dimen.invite_friends_item_size);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerView.addItemDecoration(new InviteFriendsListItemDecoration(5, (int)size, (int)size));
        mAdapter = new InviteFriendsListAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);


        if (ContactUtil.hasReadContactsPermission()) {
            mBinding.bind(DataModel.get().createContactPickerData(this, this));
            mBinding.getData().init(getLoaderManager(), mBinding);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_ADD_FRIENDS:
                if (resultCode == RESULT_OK) {
                    mAdapter.addContact(InviteFriendsList.getAddedInvitedFriendsList());
                }
                break;
        }
    }

    private void stripUnderlines(MessagesTextView textView) {
        if(null!=textView&&textView.getText() instanceof Spannable){
            Spannable s = (Spannable)textView.getText();
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span: spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                s.setSpan(span, start, end, 0);
            }

            textView.setAutoLinkMask(0);
            textView.setText(s);
        }
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.invite_friends_default_back_to_main_page_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onAllContactsCursorUpdated(Cursor data) {

    }

    @Override
    public void onFrequentContactsCursorUpdated(Cursor data) {
        if (data == null) {
            return;
        }

        ArrayList<CallAssistantUtils.ContactInfo> contactInfos = new ArrayList<>(data.getCount());
        if (data.moveToFirst()) {
            while (data.moveToNext()) {
                final String displayName = data.getString(ContactUtil.INDEX_DISPLAY_NAME);
                final String photoThumbnailUri = data.getString(ContactUtil.INDEX_PHOTO_URI);
                final String destination = data.getString(ContactUtil.INDEX_PHONE_EMAIL);
                contactInfos.add(new CallAssistantUtils.ContactInfo(displayName, destination, photoThumbnailUri));
            }
        }

        int count = Math.min(contactInfos.size(), HSConfig.optInteger(5, "Application", "CheckContactNum"));
        if (count > 0) {
            mInviteButton.setText(String.format(getString(R.string.invite_friends_invite), count));
            mInviteButton.setVisibility(View.VISIBLE);
            mAdapter.initData(contactInfos.subList(0, count));
        }
    }

    @Override
    public void onContactCustomColorLoaded(ContactPickerData data) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBinding.isBound()) {
            mBinding.unbind();
        }
    }

    private class URLSpanNoUnderline extends URLSpan {
        private URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
