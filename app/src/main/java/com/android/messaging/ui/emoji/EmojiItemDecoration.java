package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.superapps.util.Dimensions;

public class EmojiItemDecoration extends RecyclerView.ItemDecoration {

    private int mNumColumn;
    private int mVerticalSpacing;
    private int mItemWidth;
    private int mItemHorizontalSpacing;
    private boolean mInitSpacing = false;

    public EmojiItemDecoration(int column, int itemWidth, int verticalSpacing) {
        mNumColumn = column;
        mItemWidth = itemWidth;
        mVerticalSpacing = verticalSpacing;
    }

    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (!mInitSpacing) {
            mInitSpacing = true;
            int parentWidth = parent.getWidth();

            mItemHorizontalSpacing = (parentWidth - parent.getPaddingLeft() - parent.getPaddingRight() - mItemWidth * mNumColumn) / (mNumColumn + 1);
        }
        int position = parent.getChildAdapterPosition(view);
        int column = position % mNumColumn;

        outRect.left = (mNumColumn - column) * mItemHorizontalSpacing / mNumColumn;
        outRect.right = (column + 1) * mItemHorizontalSpacing / mNumColumn;

        if (position < mNumColumn) {
            outRect.top = Dimensions.pxFromDp(12);
        }

        if (position >= mNumColumn) {
            outRect.top = mVerticalSpacing;
        }
    }
}

