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
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ad.AdConfig;
import com.android.messaging.datamodel.data.AdItemData;
import com.android.messaging.datamodel.data.ConversationListItemData;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Provides an interface to expose Conversation List Cursor data to a UI widget like a ListView.
 */
public class ConversationListAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ConversationListItemView.HostInterface mClivHostInterface;

    private List<Object> dataList = new ArrayList<>();
    private Context context;
    private static final int TYPE_HEADER_VIEW = 0;
    private static final int TYPE_NORMAL = 1;
    private View headerView;

    public boolean hasHeader() {
        return hasHeader;
    }

    private boolean hasHeader;

    public ConversationListAdapter(final Context context,
                                   final ConversationListItemView.HostInterface clivHostInterface) {
        this.context = context;
        mClivHostInterface = clivHostInterface;
        setHasStableIds(true);
    }

    public void setDataList(List<Object> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            final ConversationListItemView itemView =
                    (ConversationListItemView) layoutInflater.inflate(
                            R.layout.conversation_list_item_view, null);
            return new ConversationListViewHolder(itemView);
        } else {
            return new ConversationListAdapter.ConversationListHeaderViewHolder(headerView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ConversationListViewHolder) {
            if (dataList.get(position) instanceof ConversationListItemData) {
                ConversationListItemData conversationListItemData = (ConversationListItemData) dataList.get(position);
                ConversationListViewHolder conversationListViewHolder = (ConversationListViewHolder) holder;
                final ConversationListItemView conversationListItemView = conversationListViewHolder.mView;
                conversationListItemView.bind(conversationListItemData, mClivHostInterface);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        if (dataList.get(position) instanceof ConversationListItemData) {
            ConversationListItemData conversationListItemData = (ConversationListItemData) dataList.get(position);
            return Long.parseLong(conversationListItemData.getConversationId());
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showHeader()) {
            return isHeader(position) ? TYPE_HEADER_VIEW : TYPE_NORMAL;
        } else {
            return TYPE_NORMAL;
        }
    }

    private boolean showHeader() {
        return AdConfig.isHomepageBannerAdEnabled() && headerView != null;
    }

    private boolean isHeader(int position) {
        return dataList.get(position) instanceof AdItemData;
    }

    public void setHeader(View inflate) {
        if (headerView == null) {
            headerView = inflate;
        }
        hasHeader = true;
        dataList.add(0, new AdItemData());
        notifyItemInserted(0);
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

    private static class ConversationListHeaderViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        public ConversationListHeaderViewHolder(final View frameLayout) {
            super(frameLayout);
            mView = frameLayout;
        }
    }

}
