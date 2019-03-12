package com.android.messaging.ui.messagebox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.NoConfirmationSmsSendService;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.datamodel.media.AvatarRequestDescriptor;
import com.android.messaging.datamodel.media.ImageResource;
import com.android.messaging.datamodel.media.MediaRequest;
import com.android.messaging.datamodel.media.MediaResourceManager;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.OsUtil;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;

import static com.android.messaging.datamodel.NoConfirmationSmsSendService.EXTRA_SELF_ID;

@SuppressLint("ViewConstructor")
public class MessageBoxConversationItemView extends FrameLayout {

    private MessageBoxListItemAdapter mAdapter;
    private ImageView mAvatar;
    private String mSelfId;
    private String mConversationId;

    public MessageBoxConversationItemView(Context context, MessageBoxItemData itemData) {
        super(context, null);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.message_box_conversation_item, this, true);

        mSelfId = itemData.getSelfId();
        mConversationId = itemData.getConversationId();

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mAvatar = findViewById(R.id.avatar);
        TextView mConversationName = findViewById(R.id.conversation_name);

        mConversationName.setText(itemData.getConversationName());
        loadAvatar(Uri.parse(itemData.getAvatarUri()));

        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        llm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(llm);

        ArrayList<String > mMessages = new ArrayList<>();
        mMessages.add(itemData.getContent());
        mAdapter = new MessageBoxListItemAdapter(mMessages);
        mRecyclerView.setAdapter(mAdapter);

        setTag(mConversationId);
    }

    void addNewMessage(String message) {
        mAdapter.addNewIncomingMessage(message);
    }

    void replyMessage(String message) {
        Context context = Factory.get().getApplicationContext();
        BugleNotifications.markAllMessagesAsSeen();
        final Intent sendIntent = new Intent(context, NoConfirmationSmsSendService.class);
        sendIntent.setAction(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra(EXTRA_SELF_ID, mSelfId);
        sendIntent.putExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID, mConversationId);
        context.startService(sendIntent);
    }

    private void loadAvatar(Uri avatarUri) {
        Threads.postOnThreadPoolExecutor(() -> {
            final AvatarRequestDescriptor descriptor = new AvatarRequestDescriptor(avatarUri,
                    Dimensions.pxFromDp(38), Dimensions.pxFromDp(38), OsUtil.isAtLeastL());
            final MediaRequest<ImageResource> imageRequest = descriptor.buildSyncMediaRequest(getContext());
            final ImageResource avatarImage =
                    MediaResourceManager.get().requestMediaResourceSync(imageRequest);
            Threads.postOnMainThread(() -> {
                if (avatarImage != null) {
                    mAvatar.setImageBitmap(avatarImage.getBitmap());
                }
            });
        });
    }
}
