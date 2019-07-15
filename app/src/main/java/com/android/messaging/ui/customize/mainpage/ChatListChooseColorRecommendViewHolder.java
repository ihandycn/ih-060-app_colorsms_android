package com.android.messaging.ui.customize.mainpage;

import android.annotation.SuppressLint;
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
import com.android.messaging.ui.customize.OnColorChangedListener;
import com.android.messaging.ui.customize.RecommendColorItemDecoration;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

public class ChatListChooseColorRecommendViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private OnColorChangedListener mListener;
    private ChatListChooseColorRecommendAdapter mAdapter;
    private Integer mInitSelectedColor;

    ChatListChooseColorRecommendViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View view = inflater.inflate(
                R.layout.choose_custom_bubble_color_recommend, null, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));

        float size = Dimensions.pxFromDp(44.3f);
        recyclerView.setPadding(Dimensions.pxFromDp(47f), Dimensions.pxFromDp(10f), Dimensions.pxFromDp(47f), Dimensions.pxFromDp(10f));
        recyclerView.addItemDecoration(new RecommendColorItemDecoration(4, 2, (int) size, (int) size));
        mAdapter = new ChatListChooseColorRecommendAdapter(mContext);
        mAdapter.setOnColorChangedListener(color -> mListener.onColorChanged(color));
        if (mInitSelectedColor != null) {
            mAdapter.updatePresetColors(mInitSelectedColor, mInitSelectedColor);
        }
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    void update(@ColorInt int recommendColor, @ColorInt int selectedColor) {
        if (mAdapter != null) {
            Threads.postOnMainThread(() -> mAdapter.updatePresetColors(recommendColor, selectedColor));
        } else {
            mInitSelectedColor = recommendColor;
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_color_recommend);
    }

    @Override
    public void onPageSelected() {

    }
}

