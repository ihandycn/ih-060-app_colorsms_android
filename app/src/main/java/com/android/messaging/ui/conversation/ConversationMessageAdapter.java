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

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.messaging.ui.CursorRecyclerAdapter;
import com.android.messaging.ui.conversation.ConversationMessageView.ConversationMessageViewHost;
import com.android.messaging.util.Assert;

/**
 * Provides an interface to expose Conversation Message Cursor data to a UI widget like a
 * RecyclerView.
 */
public class ConversationMessageAdapter extends
        CursorRecyclerAdapter<ConversationMessageAdapter.ConversationMessageViewHolder> {

    private final ConversationMessageViewHost mHost;
    private final AsyncImageViewDelayLoader mImageViewDelayLoader;
    private final View.OnClickListener mViewClickListener;
    private final View.OnLongClickListener mViewLongClickListener;
    private boolean mOneOnOne;
    private String mSelectedMessageId;
    private static boolean multiSelectMode;
    public static final int NORMAL = 1000;
    public static final int SLIDE = 2000;
    public static int mState;

    public ConversationMessageAdapter(final Context context, final Cursor cursor,
                                      final ConversationMessageViewHost host,
                                      final AsyncImageViewDelayLoader imageViewDelayLoader,
                                      final View.OnClickListener viewClickListener,
                                      final View.OnLongClickListener longClickListener) {
        super(context, cursor, 0);
        mHost = host;
        mViewClickListener = viewClickListener;
        mViewLongClickListener = longClickListener;
        mImageViewDelayLoader = imageViewDelayLoader;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(final ConversationMessageViewHolder holder,
                               final Context context, final Cursor cursor) {
        Assert.isTrue(holder.mView instanceof ConversationMessageView);
        final ConversationMessageView conversationMessageView =
                (ConversationMessageView) holder.mView;
        ImageView checkbox = conversationMessageView.findViewById(R.id.check_box);
        conversationMessageView.bind(cursor, mOneOnOne, multiSelectMode);
        ConversationMessageData data = conversationMessageView.getData();

        if (multiSelectMode && !ConversationFragment.getSelectMessageIds().isEmpty()) {
            checkbox.setVisibility(View.VISIBLE);
            if (ConversationFragment.getSelectMessageIds().contains(data.getMessageId())) {
                checkbox.setImageResource(R.drawable.ic_choosen);
            } else {
                checkbox.setImageResource(R.drawable.ic_choose);
            }
        } else {
            checkbox.setVisibility(View.GONE);
        }
        holder.bind();
    }

    @Override
    public ConversationMessageViewHolder createViewHolder(final Context context,
                                                          final ViewGroup parent, final int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final ConversationMessageView conversationMessageView = (ConversationMessageView)
                layoutInflater.inflate(R.layout.conversation_message_view, null);
        conversationMessageView.setHost(mHost);
        conversationMessageView.setImageViewDelayLoader(mImageViewDelayLoader);
        ConversationMessageViewHolder conversationMessageViewHolder = new ConversationMessageViewHolder(conversationMessageView,
                mViewClickListener, mViewLongClickListener);
        return conversationMessageViewHolder;
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

    public void setSelectedMessage(final String messageId) {
        mSelectedMessageId = messageId;
        notifyDataSetChanged();
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

    /**
     * ViewHolder that holds a ConversationMessageView.
     */
    public static class ConversationMessageViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        /**
         * @param viewClickListener a View.OnClickListener that should define the interaction when
         *                          an item in the RecyclerView is clicked.
         */
        public ConversationMessageViewHolder(final View itemView,
                                             final View.OnClickListener viewClickListener,
                                             final View.OnLongClickListener viewLongClickListener) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(viewClickListener);
            mView.setOnLongClickListener(viewLongClickListener);


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
}
