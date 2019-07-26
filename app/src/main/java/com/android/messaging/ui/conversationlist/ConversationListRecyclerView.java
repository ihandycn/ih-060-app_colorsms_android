package com.android.messaging.ui.conversationlist;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Threads;

public class ConversationListRecyclerView extends RecyclerView {
    private boolean mDisplayed;

    public ConversationListRecyclerView(Context context) {
        super(context);
    }

    public ConversationListRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConversationListRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (getAdapter() != null && getAdapter().getItemCount() > 0 && !mDisplayed) {
            Threads.postOnMainThread(() -> HSGlobalNotificationCenter.sendNotification(ConversationListActivity.CONVERSATION_LIST_DISPLAYED));
        }
        mDisplayed = true;
    }
}
