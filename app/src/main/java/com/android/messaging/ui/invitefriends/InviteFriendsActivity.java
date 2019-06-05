package com.android.messaging.ui.invitefriends;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
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
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.font.FontUtils;
import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;
import com.android.messaging.ui.PlainTextEditText;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.TextUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.UriUtil;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import java.util.ArrayList;

import static com.android.messaging.datamodel.data.ConversationListItemData.INDEX_CONVERSATION_ICON;

public class InviteFriendsActivity extends AppCompatActivity implements ConversationListData.ConversationListDataListener {

    public static final String INTENT_KEY_FROM = "from";

    static final int REQUEST_CODE_ADD_FRIENDS = 12;

    @VisibleForTesting
    final Binding<ConversationListData> mBinding = BindingBase.createBinding(this);

    private InviteFriendsListAdapter mAdapter;
    private PlainTextEditText mEditText;
    private TextView mInviteButton;
    private String mDescription;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        initToolBar();
        initEditText();
        initRecyclerView();

        mInviteButton = findViewById(R.id.invite_button);
        mInviteButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(6.7f), true));

        mInviteButton.setVisibility(View.GONE);
        mInviteButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mDescription)) {
                return;
            }

            String message = mDescription + InviteFriendsTest.getSendLink();
            for (CallAssistantUtils.ContactInfo contactInfo : mAdapter.getContactInfos()) {
                if (!TextUtils.isEmpty(contactInfo.number)) {
                    InsertNewMessageAction.insertNewMessage(ParticipantData.DEFAULT_SELF_SUB_ID, contactInfo.number,
                            message, "");
                }
            }
            Toasts.showToast(R.string.invite_friends_success_toast);

            BugleAnalytics.logEvent("Invite_SendPage_Invite_Click",
                    "from", getIntent().getStringExtra(INTENT_KEY_FROM),
                    "num", String.valueOf(mAdapter.getItemCount()));
            BugleAnalytics.logEvent("Invite_SMS_Send", "link", InviteFriendsTest.getSendLink(),
                    "isModified", "" + !TextUtils.equals(mDescription, InviteFriendsTest.getSendDescription()));
            InviteFriendsTest.logInviteFriendsClick();
            InviteFriendsTest.logInviteSmsSent();
            finish();
        });

        BugleAnalytics.logEvent("Invite_SendPage_Show", true, "from", getIntent().getStringExtra(INTENT_KEY_FROM));
    }

    private void initEditText() {
        mDescription = InviteFriendsTest.getSendDescription();
        MessagesTextView autoLinkMessagesTextView = findViewById(R.id.invite_friends_message_auto_link);
        autoLinkMessagesTextView.setText(InviteFriendsTest.getSendLink());
        mEditText = findViewById(R.id.invite_friends_message_text);
        mEditText.setTypeface(FontUtils.getTypeface());
        mEditText.setText(mDescription);
        mEditText.setEnabled(false);

        View editButton = findViewById(R.id.edit_button);
        View cancelButton = findViewById(R.id.invite_friends_invite_cancel_button);
        View saveButton = findViewById(R.id.invite_friends_invite_save_button);

        editButton.setOnClickListener(v -> {
            makeEditTextEditable(editButton, saveButton, cancelButton);
            ImeUtil.get().showImeKeyboard(InviteFriendsActivity.this, mEditText);
            BugleAnalytics.logEvent("Invite_SendPage_Edit_Click");
        });

        cancelButton.setOnClickListener(v -> {
            makeEditTextUneditable(editButton, saveButton, cancelButton, false);
        });

        saveButton.setOnClickListener(v -> {
            makeEditTextUneditable(editButton, saveButton, cancelButton, true);
            BugleAnalytics.logEvent("Invite_SendPage_Save_Click");
        });
    }

    private void makeEditTextEditable(View editButton, View saveButton, View cancelButton) {
        editButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        mEditText.setEnabled(true);
        mEditText.requestFocus();
        mEditText.setSelection(mEditText.getText().length());
    }

    private void makeEditTextUneditable(View editButton, View saveButton, View cancelButton, boolean save) {
        editButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        if (save) {
            mDescription = mEditText.getText().toString();
        } else {
            mEditText.setText(mDescription);
        }
        mEditText.setEnabled(false);
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

        mAdapter.setOnItemCountChangeListener(() -> {
            mInviteButton.setText(String.format(getString(R.string.invite_friends_invite), mAdapter.getItemCount() - 1));
            mInviteButton.setVisibility(View.VISIBLE);
        });

        if (ContactUtil.hasReadContactsPermission()) {
            mBinding.bind(DataModel.get().createConversationListData(this, this, false));
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

    @Override
    public void onConversationListCursorUpdated(ConversationListData data, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            ArrayList<CallAssistantUtils.ContactInfo> contactInfos = new ArrayList<>();
            int count = HSConfig.optInteger(5, "Application", "CheckContactNum");

            int dataCount = 0;
            do {
                ConversationListItemData itemData = new ConversationListItemData();
                itemData.bind(cursor);
                if (itemData.getIsGroup() || TextUtils.isEmpty(itemData.getOtherParticipantNormalizedDestination())) {
                    continue;
                }

                String uri = "";
                if (!TextUtils.isEmpty(itemData.getIcon())) {
                    Uri primaryUri = AvatarUriUtil.getPrimaryUri(Uri.parse(itemData.getIcon()));
                    if (primaryUri != null) {
                        uri = primaryUri.toString();
                    }
                }
                contactInfos.add(new CallAssistantUtils.ContactInfo(itemData.getName(),
                        itemData.getOtherParticipantNormalizedDestination(), uri));
                dataCount++;
                if (dataCount == count) {
                    break;
                }
                HSLog.d("InviteFriends, onConversationListCursorUpdated");
            } while (cursor.moveToNext());


            if (dataCount > 0) {
                mAdapter.initData(contactInfos.subList(0, dataCount));
            }
        }
    }

    @Override
    public void setBlockedParticipantsAvailable(boolean blockedAvailable) {

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
