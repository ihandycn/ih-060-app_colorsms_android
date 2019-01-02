package com.android.messaging.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.commons.utils.HSLog;

public class RecyclerViewWidthSlideListener extends RecyclerView {

    private static final String TAG = RecyclerViewWidthSlideListener.class.getSimpleName();

    private OnSlideListener mOnSlideListener;

    public RecyclerViewWidthSlideListener(Context context) {
        this(context, null);
    }

    public RecyclerViewWidthSlideListener(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewWidthSlideListener(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        OnScrollListener onScrollListener = new OnScrollListener() {

            private int scrollY;
            private int startDraggingScrollY;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        startDraggingScrollY = scrollY;
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (scrollY > startDraggingScrollY) {
                            if (mOnSlideListener != null) {
                                mOnSlideListener.slideUp();
                            }
                            HSLog.d(TAG, "Slide up");
                        } else {
                            if (mOnSlideListener != null) {
                                mOnSlideListener.slideDown();
                            }
                            HSLog.d(TAG, "Slide down");
                        }
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollY += dy;
            }
        };
        addOnScrollListener(onScrollListener);
    }

    public void setOnSlideListener(OnSlideListener listener) {
        mOnSlideListener = listener;
    }

    public interface OnSlideListener {

        void slideUp();

        void slideDown();
    }

}
