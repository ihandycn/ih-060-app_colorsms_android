package com.android.messaging.ui.invitefriends;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import com.android.messaging.R;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ContactPickerData;
import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;
import com.android.messaging.ui.customize.RecommendColorItemDecoration;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.ContactUtil;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.util.ArrayList;

public class InviteFriendsActivity extends AppCompatActivity {

    static final int REQUEST_CODE_ADD_FRIENDS = 12;

    private InviteFriendsListAdapter mAdapter;

    @VisibleForTesting
    final Binding<ContactPickerData> mBinding = BindingBase.createBinding(this);

    private static final String BINDING_ID = "bindingId";
    private static final int LOADER_ID = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        MessagesTextView autoLinkMessagesTextView = findViewById(R.id.invite_friends_message_auto_link);
        stripUnderlines(autoLinkMessagesTextView);

        RecyclerView recyclerView = findViewById(R.id.friends_list_recycler_view);
        float size = getResources().getDimensionPixelSize(R.dimen.invite_friends_item_size);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerView.addItemDecoration(new InviteFriendsListItemDecoration(5, (int)size, (int)size));
        mAdapter = new InviteFriendsListAdapter(this);
        recyclerView.setAdapter(mAdapter);


        final Bundle args = new Bundle();
        getLoaderManager().initLoader(LOADER_ID, args, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return ContactUtil.getFrequentContacts(InviteFriendsActivity.this)
                        .createBoundCursorLoader(BINDING_ID);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data == null) {
                    return;
                }
                HSLog.d("ContactPicker_InviteFriendsActivity_", "" + data.getCount());

                ArrayList<CallAssistantUtils.ContactInfo> contactInfos = new ArrayList<>(data.getCount());
                if (data.moveToFirst()) {
                    while (data.moveToNext()) {
                        final String displayName = data.getString(ContactUtil.INDEX_DISPLAY_NAME);
                        HSLog.d("ContactPicker_InviteFriendsActivity_", displayName);

                        final String photoThumbnailUri = data.getString(ContactUtil.INDEX_PHOTO_URI);

                        if (!TextUtils.isEmpty(photoThumbnailUri)) {
                            HSLog.d("ContactPicker_InviteFriendsActivity_", photoThumbnailUri);
                        }
                        final String destination = data.getString(ContactUtil.INDEX_PHONE_EMAIL);
                        contactInfos.add(new CallAssistantUtils.ContactInfo(displayName, destination, photoThumbnailUri));
                    }
                }
                mAdapter.initData(contactInfos);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
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

    private class URLSpanNoUnderline extends URLSpan {
        private URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(LOADER_ID);
    }
}
