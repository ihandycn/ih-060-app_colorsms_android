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
package com.android.messaging.ui.conversation;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.messaging.ui.conversation.ConversationMessageView.ConversationMessageViewHost;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.util.ViewUtils;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an interface to expose Conversation Message Cursor data to a UI widget like a
 * RecyclerView.
 */
public class ConversationMessageAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ConversationMessageViewHost mHost;
    private final AsyncImageViewDelayLoader mImageViewDelayLoader;
    private final ConversationMessageClickListener mViewClickListener;
    private boolean mOneOnOne;
    private static boolean multiSelectMode;
    public static final int NORMAL = 1000;
    public static final int SLIDE = 2000;
    public static int mState;

    private List<ConversationMessageData> mDataList = new ArrayList<>();

    public ConversationMessageAdapter(final ConversationMessageViewHost host,
                                      final AsyncImageViewDelayLoader imageViewDelayLoader,
                                      final ConversationMessageClickListener viewClickListener) {
        mHost = host;
        mViewClickListener = viewClickListener;
        mImageViewDelayLoader = imageViewDelayLoader;
        setHasStableIds(true);
    }

    public void setDataList(List<ConversationMessageData> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public void openItemAnimation() {
        mState = SLIDE;
        notifyDataSetChanged();
    }

    public void closeItemAnimation() {
        mState = NORMAL;
        notifyDataSetChanged();
        setMultiSelectMode(false);
    }

    public void setOneOnOne(final boolean oneOnOne, final boolean invalidate) {
        if (mOneOnOne != oneOnOne) {
            mOneOnOne = oneOnOne;
            if (invalidate) {
                notifyDataSetChanged();
            }
        }
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        this.multiSelectMode = multiSelectMode;
    }

    public static boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            final ConversationMessageView conversationMessageView = (ConversationMessageView)
                    layoutInflater.inflate(R.layout.conversation_message_view, null);
            conversationMessageView.setHost(mHost);
            conversationMessageView.setImageViewDelayLoader(mImageViewDelayLoader);
            return new ConversationMessageViewHolder(conversationMessageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final ConversationMessageView conversationMessageView =
                    (ConversationMessageView) ((ConversationMessageViewHolder) holder).mView;
            ImageView checkbox = conversationMessageView.findViewById(R.id.check_box);
            conversationMessageView.bind((ConversationMessageData) mDataList.get(position), mOneOnOne, multiSelectMode);
            ConversationMessageData data = conversationMessageView.getData();

            conversationMessageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkbox.setSelected(!checkbox.isSelected());
                    mViewClickListener.onConversationMessageClick((ConversationMessageData) mDataList.get(position));
                }
            });
            conversationMessageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    checkbox.setSelected(true);
                    mViewClickListener.onConversationMessageLongClick((ConversationMessageData) mDataList.get(position));
                    return true;
                }
            });
            if (multiSelectMode && !ConversationFragment.getSelectMessageIds().isEmpty()) {
                checkbox.setVisibility(View.VISIBLE);
                if (ConversationFragment.getSelectMessageIds().contains(data.getMessageId())) {
                    checkbox.setImageResource(R.drawable.ic_choosen);
                } else {
                    checkbox.setImageResource(R.drawable.ic_choose);
                }
            } else {
                checkbox.setImageResource(R.drawable.ic_choose);
            }
        ((ConversationMessageViewHolder) holder).bind();
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public long getItemId(int position) {
        if (mDataList.get(position) instanceof ConversationMessageData) {
            ConversationMessageData messageData = (ConversationMessageData) mDataList.get(position);
            return Long.parseLong(messageData.getMessageId());
        } else {
            return 0;
        }
    }

    /**
     * SmsViewHolder that holds a ConversationMessageView.
     */
    public static class ConversationMessageViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        public ConversationMessageViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void bind() {
            ConversationMessageView messageView = (ConversationMessageView) mView;
            switch (mState) {
                case NORMAL:
                    messageView.close();
                    break;
                case SLIDE:
                    messageView.open();
                    break;
            }
        }
    }

    public interface ConversationMessageClickListener{
        void onConversationMessageClick(ConversationMessageData data);
        void onConversationMessageLongClick(ConversationMessageData data);
    }
}
