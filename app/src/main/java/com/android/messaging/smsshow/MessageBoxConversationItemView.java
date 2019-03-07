package com.android.messaging.smsshow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;

import com.android.messaging.R;
import com.android.messaging.datamodel.media.AvatarRequestDescriptor;
import com.android.messaging.datamodel.media.ImageResource;
import com.android.messaging.datamodel.media.MediaRequest;
import com.android.messaging.datamodel.media.MediaResourceManager;
import com.android.messaging.util.OsUtil;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class MessageBoxConversationItemView extends FrameLayout {

    private MessageBoxListItemAdapter mAdapter;
    private ImageView mAvatar;

    public MessageBoxConversationItemView(Context context,
                                          String message,
                                          Uri avatarUri,
                                          String conversationName) {
        super(context, null);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.message_box_conversation_item, this, true);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mAvatar = findViewById(R.id.avatar);
        TextView mConversationName = findViewById(R.id.conversation_name);

        mConversationName.setText(conversationName);
        loadAvatar(avatarUri);

        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        llm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(llm);

        ArrayList<String > mMessages = new ArrayList<>();
        mMessages.add(message);
        mAdapter = new MessageBoxListItemAdapter(mMessages);
        mRecyclerView.setAdapter(mAdapter);
    }

    void addNewMessage(String message) {
        mAdapter.addNewIncomingMessage(message);
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
