package com.android.messaging.ui.conversationlist;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

public class ConversationListRecyclerView extends RecyclerView {

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
        if (getAdapter() != null && getAdapter().getItemCount() > 0) {
            HSGlobalNotificationCenter.sendNotification(ConversationListActivity.SHOW_MENU_GUIDE);
        }
    }
}
