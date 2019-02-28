package com.android.messaging.ui.customize;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.emoji.EmojiItemDecoration;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import static android.view.View.OVER_SCROLL_NEVER;


public class ChooseMessageColorRecommendViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private OnColorChangedListener mListener;
    private ChooseMessageColorRecommendAdapter mAdapter;

    ChooseMessageColorRecommendViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.choose_custom_bubble_color_recommend,
                null /* root */,
                false /* attachToRoot */);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));

        float size = Dimensions.getPhoneWidth(mContext) * 0.125f;
        recyclerView.setPadding(Dimensions.pxFromDp(47f), Dimensions.pxFromDp(10f), Dimensions.pxFromDp(47f), Dimensions.pxFromDp(10f));
        recyclerView.addItemDecoration(new RecommendColorItemDecoration(4, 3, (int)size, (int)size));
        mAdapter = new ChooseMessageColorRecommendAdapter(mContext);
        mAdapter.setOnColorChangedListener(color -> mListener.onColorChanged(color));
        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    void update(@ColorInt int firstPositionColor, @ColorInt int secondPositionColor, @ColorInt int selectedColor) {
        Threads.postOnMainThread(() -> mAdapter.updatePresetColors(firstPositionColor, secondPositionColor, selectedColor));
    }

    void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void setHasOptionsMenu() {

    }


    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_color_recommend);
    }
}

