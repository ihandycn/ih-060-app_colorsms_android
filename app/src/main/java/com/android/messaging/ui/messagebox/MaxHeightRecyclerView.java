package com.android.messaging.ui.messagebox;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.android.messaging.R;

/**
 * Created by ihandysoft on 2017/10/12.
 */

public class MaxHeightRecyclerView extends RecyclerView {

    private int maxHeight;
    private int defaultHeight = ViewGroup.LayoutParams.MATCH_PARENT;

    public MaxHeightRecyclerView(Context context) {
        this(context, null);
    }

    public MaxHeightRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        if (!isInEditMode() && attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView);
            //200 is a defualt value
            maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightRecyclerView_maxHeight, defaultHeight);

            styledAttrs.recycle();
        }

    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (maxHeight > 0) {
            heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
