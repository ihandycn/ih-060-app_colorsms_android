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
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.CursorRecyclerAdapter;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbExpressAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbContentLayout;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.util.List;

/**
 * Provides an interface to expose Conversation List Cursor data to a UI widget like a ListView.
 */
public class ConversationListAdapter
        extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {

    public static final String SMS_HOMEPAGE_BANNERAD = "SMSHomepageBannerAd";
    private final ConversationListItemView.HostInterface mClivHostInterface;
    private static final int TYPE_HEADER_VIEW = 0;
    private static final int TYPE_NORMAL = 1;
    private AcbExpressAdView expressAdView;
    private View headerView;

    public ConversationListAdapter(final Context context, final Cursor cursor,
                                   final ConversationListItemView.HostInterface clivHostInterface) {
        super(context, cursor, 0);
        mClivHostInterface = clivHostInterface;
        setHasStableIds(true);
    }

    /**
     * @see com.android.messaging.ui.CursorRecyclerAdapter#bindViewHolder(
     *android.support.v7.widget.RecyclerView.ViewHolder, android.content.Context,
     * android.database.Cursor)
     */
    @Override
    public void bindViewHolder(RecyclerView.ViewHolder holder, Context context, Cursor cursor) {
        if (holder instanceof ConversationListViewHolder) {
            ConversationListViewHolder conversationListViewHolder = (ConversationListViewHolder) holder;
            final ConversationListItemView conversationListItemView = conversationListViewHolder.mView;
            conversationListItemView.bind(cursor, mClivHostInterface);
        } else if (holder instanceof ConversationListHeaderViewHolder) {
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ConversationListViewHolder) {
            if (showHeader()) {
                super.onBindViewHolder(holder, position - 1);
            } else {
                super.onBindViewHolder(holder, position);
            }
        }

    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(final Context context,
                                                    final ViewGroup parent, final int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        if (viewType == TYPE_HEADER_VIEW && showHeader()) {
            return new ConversationListAdapter.ConversationListHeaderViewHolder(headerView);
        } else {
            final ConversationListItemView itemView =
                    (ConversationListItemView) layoutInflater.inflate(
                            R.layout.conversation_list_item_view, null);
            return new ConversationListViewHolder(itemView);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (expressAdView != null) {
            expressAdView.destroy();
            expressAdView = null;
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

    private boolean isHeader(int position) {
        return position == 0;

    }

    private boolean showHeader() {
        return HSConfig.optBoolean(true, "Application", "SMSAd", "SMSHomepageBannerAd") && headerView != null;

    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            if (showHeader())
                return mCursor.getCount() + 1;
            else
                return mCursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * @see android.support.v7.widget.RecyclerView.Adapter#getItem(int)
     */
    public Object getItem(final int position) {
        if (mDataValid && mCursor != null) {
            if (showHeader()) {
                mCursor.moveToPosition(position - 1);
            } else {
                mCursor.moveToPosition(position);
            }
            return mCursor;
        } else {
            return null;
        }
    }

    /**
     * @see android.support.v7.widget.RecyclerView.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(final int position) {
        if (mDataValid && mCursor != null) {
            if (showHeader()) {
                if (mCursor.moveToPosition(position - 1)) {
                    return mCursor.getLong(mRowIDColumn);
                } else {
                    return 0;
                }
            } else {
                if (mCursor.moveToPosition(position)) {
                    return mCursor.getLong(mRowIDColumn);
                } else {
                    return 0;
                }
            }

        } else {
            return 0;
        }
    }

    public void setHeader(View inflate) {
        headerView = inflate;
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

    /**
     * ViewHolder that holds a ConversationListItemView.
     */
    private static class ConversationListHeaderViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        public ConversationListHeaderViewHolder(final View frameLayout) {
            super(frameLayout);
            mView = frameLayout;
        }
    }
}
