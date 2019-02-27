package com.android.messaging.ui.customize;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;

public class BubbleDrawableViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private CustomMessageHost mHost;
    private String mConversationId;

    BubbleDrawableViewHolder(final Context context, String conversationId) {
        mContext = context;
        mConversationId = conversationId;
    }

    void setHost(CustomMessageHost host) {
        mHost = host;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.bubble_customize_style_layout,
                null /* root */,
                false /* attachToRoot */);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setHasFixedSize(true);
        BubbleDrawableAdapter adapter = new BubbleDrawableAdapter(mContext, mConversationId);
        adapter.setOnSelectedBubbleChangeListener((int index) -> mHost.previewCustomBubbleDrawable(index));
        recyclerView.setAdapter(adapter);
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
