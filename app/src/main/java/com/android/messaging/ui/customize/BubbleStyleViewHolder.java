package com.android.messaging.ui.customize;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomHeaderPagerViewHolder;

public class BubbleStyleViewHolder extends BasePagerViewHolder implements CustomHeaderPagerViewHolder {
    private Context mContext;

    public BubbleStyleViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.bubble_customize_style_layout,
                null /* root */,
                false /* attachToRoot */);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new BubbleStyleAdapter(mContext));
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));

        return view;
    }


    @Override
    protected void setHasOptionsMenu() {

    }


    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_tab_style);
    }

}
