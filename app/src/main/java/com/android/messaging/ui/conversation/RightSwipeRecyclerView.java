package com.android.messaging.ui.conversation;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.messaging.gesture.SwipeRightGestureDetector;

public class RightSwipeRecyclerView extends RecyclerView {
    SwipeRightGestureDetector mSwipeRightGestureDetector;

    public RightSwipeRecyclerView(Context context) {
        super(context);
    }

    public RightSwipeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public void setActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        mSwipeRightGestureDetector = new SwipeRightGestureDetector(activity,
                eventType -> activity.finish());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mSwipeRightGestureDetector != null) {
            mSwipeRightGestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
