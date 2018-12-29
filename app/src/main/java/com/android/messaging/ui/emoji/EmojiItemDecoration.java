package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class EmojiItemDecoration extends RecyclerView.ItemDecoration {

    private int mNumColumn;
    private int mNumRows;
    private int mHorizontalSpacing;
    private int mVerticalSpacing;
    private int mItemWidth;
    private int mItemHeight;
    private int mHorizontalOffsetSpacing;
    private int mItemHorizontalSpacing;
    private boolean mInitSpacing = false;

    public EmojiItemDecoration(int column, int row, int itemWidth, int itemHeight) {
        mNumColumn = column;
        mNumRows = row;
        mItemWidth = itemWidth;
        mItemHeight = itemHeight;
    }

    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (!mInitSpacing) {
            mInitSpacing = true;
            int parentHeight = parent.getHeight();
            int parentWidth = parent.getWidth();

            mItemHorizontalSpacing = (parentWidth - parent.getPaddingLeft() - parent.getPaddingRight()) / mNumColumn - mItemWidth;
            mHorizontalSpacing = (mItemHorizontalSpacing * mNumColumn) / (mNumColumn - 1);
            mHorizontalOffsetSpacing = mHorizontalSpacing - mItemHorizontalSpacing;

            mVerticalSpacing = (parentHeight - parent.getPaddingTop() - parent.getPaddingBottom() - mItemHeight * mNumRows) / (mNumRows - 1);
        }
        int position = parent.getChildAdapterPosition(view);
        int column = position % mNumColumn;

        if (column == 0) {
            outRect.left = 0;
            outRect.right = mItemHorizontalSpacing;
        } else if (column == (mNumColumn - 1)) {
            outRect.left = mItemHorizontalSpacing;
            outRect.right = 0;
        } else {
            outRect.left = column * mHorizontalOffsetSpacing;
            outRect.right = mHorizontalSpacing - outRect.left;
        }

        if (position >= mNumColumn) {
            outRect.top = mVerticalSpacing;
        }
    }
}

