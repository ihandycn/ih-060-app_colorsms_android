package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.superapps.util.Dimensions;

public class EmojiItemDecoration extends RecyclerView.ItemDecoration {

    private int mNumColumn;
    private int mHorizontalSpacing;
    private int mVerticalSpacing;
    private int mItemWidth;
    private int mHorizontalOffsetSpacing;
    private int mItemHorizontalSpacing;
    private boolean mInitSpacing = false;
    int x;

    public EmojiItemDecoration(int column, int itemWidth, int verticalSpacing) {
        mNumColumn = column;
        mItemWidth = itemWidth;
        mVerticalSpacing = verticalSpacing;
    }

    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (!mInitSpacing) {
            mInitSpacing = true;
            int parentWidth = parent.getWidth();

            mItemHorizontalSpacing = (parentWidth - parent.getPaddingLeft() - parent.getPaddingRight()) / mNumColumn - mItemWidth;
        }
        int position = parent.getChildAdapterPosition(view);

        outRect.left = mItemHorizontalSpacing / 2;
        outRect.right = mItemHorizontalSpacing / 2;

        if (position < mNumColumn) {
            outRect.top = Dimensions.pxFromDp(12);
        }

        if (position >= mNumColumn) {
            outRect.top = mVerticalSpacing;
        }
    }
}

