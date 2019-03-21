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

package com.android.messaging.ui.conversationlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ConversationListItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an interface to expose Conversation List Cursor data to a UI widget like a ListView.
 */
public class ConversationListAdapter
        extends RecyclerView.Adapter<ConversationListAdapter.ConversationListViewHolder> {

    private final ConversationListItemView.HostInterface mClivHostInterface;

    private List<ConversationListItemData> dataList = new ArrayList<>();
    private Context context;

    public ConversationListAdapter(final Context context,
                                   final ConversationListItemView.HostInterface clivHostInterface) {
        this.context = context;
        mClivHostInterface = clivHostInterface;
        setHasStableIds(true);
    }

    public void setDataList(List<ConversationListItemData> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ConversationListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final ConversationListItemView itemView =
                (ConversationListItemView) layoutInflater.inflate(
                        R.layout.conversation_list_item_view, null);
        return new ConversationListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationListViewHolder holder, int position) {
        final ConversationListItemView conversationListItemView = holder.mView;
        conversationListItemView.bind(dataList.get(position), mClivHostInterface);
    }

    @Override public int getItemCount() {
        return dataList.size();
    }

    /**
     * ViewHolder that holds a ConversationListItemView.
     */
    public static class ConversationListViewHolder extends RecyclerView.ViewHolder {
        final ConversationListItemView mView;

        public ConversationListViewHolder(final ConversationListItemView itemView) {
            super(itemView);
            mView = itemView;
        }
    }
}
