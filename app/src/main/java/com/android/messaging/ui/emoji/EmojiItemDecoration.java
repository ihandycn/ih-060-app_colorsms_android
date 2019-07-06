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
    private int mHorizontalSideSpace;

    public EmojiItemDecoration(int column, int itemWidth, int horizontalSideSpace, int verticalSpacing) {
        mNumColumn = column;
        mItemWidth = itemWidth;
        mVerticalSpacing = verticalSpacing;
        mHorizontalSideSpace = horizontalSideSpace;
    }

    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (!mInitSpacing) {
            mInitSpacing = true;
            int parentWidth = parent.getWidth();

            mItemHorizontalSpacing = (parentWidth - mHorizontalSideSpace * 2
                    - parent.getPaddingLeft() - parent.getPaddingRight() - mItemWidth * mNumColumn) / (mNumColumn - 1);
        }
        int position = parent.getChildAdapterPosition(view);
        int column = position % mNumColumn;


        if (column == 0) {
            outRect.left = mHorizontalSideSpace;
            outRect.right = mItemHorizontalSpacing / 2;
        } else if (column == (mNumColumn - 1)) {
            outRect.left = mItemHorizontalSpacing / 2;
            outRect.right = mHorizontalSideSpace;
        } else {
            outRect.left = mItemHorizontalSpacing / 2;
            outRect.right = mItemHorizontalSpacing / 2;
        }

        if (position < mNumColumn) {
            outRect.top = Dimensions.pxFromDp(12);
        }

        if (position >= mNumColumn) {
            outRect.top = mVerticalSpacing;
        }
    }
}

