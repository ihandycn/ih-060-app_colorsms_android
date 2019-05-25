package com.android.messaging.ui.customize.theme;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseBugleFragmentActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class ThemeSelectActivity extends BaseBugleFragmentActivity {
    private ThemeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_select);

        UiUtils.setStatusBarColor(this, Color.WHITE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.theme_store);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView mRecyclerView = findViewById(R.id.theme_select_recycler_view);
        mAdapter = new ThemeAdapter(ThemeInfo.getAllThemes());
        GridLayoutManager layoutManager =
                new GridLayoutManager(getBaseContext(), 2,
                        StaggeredGridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mAdapter.getItemViewType(position);
                switch (viewType) {
                    case ThemeAdapter.THEME:
                        return 1;
                    case ThemeAdapter.BOTTOM:
                        return 2;
                }
                return 0;
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                int viewType = mAdapter.getItemViewType(position);
                if (viewType == ThemeAdapter.THEME) {
                    if (position % 2 == 0) {
                        outRect.left = Dimensions.pxFromDp(16);
                        outRect.right = Dimensions.pxFromDp(6);
                    } else {
                        outRect.left = Dimensions.pxFromDp(6);
                        outRect.right = Dimensions.pxFromDp(16);
                    }
                    outRect.top = Dimensions.pxFromDp(6.3f);
                    outRect.bottom = Dimensions.pxFromDp(15);
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private int scrollDistance = 0;
            private int lastState = RecyclerView.SCROLL_STATE_IDLE;

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                scrollDistance += dy;
            }

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {

                if (lastState != RecyclerView.SCROLL_STATE_IDLE
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (scrollDistance != 0) {
                        BugleAnalytics.logEvent("Customize_ThemeCenter_List_Slide", true);
                    }
                    scrollDistance = 0;
                }
                lastState = newState;
            }
        });

        BugleAnalytics.logEvent("Customize_ThemeCenter_Show", true);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAdapter.refreshThemeState();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ThemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int THEME = 1;
        public static final int BOTTOM = 2;

        class BottomThemeInfo extends ThemeInfo {

        }

        List<ThemeInfo> mDataList;
        List<ThemeSelectItemView> mViewList = new ArrayList<>();
        ThemeUtils.IThemeChangeListener mListener = () -> {
            for (ThemeSelectItemView view : mViewList) {
                view.onThemeChanged();
            }
        };

        ThemeAdapter(List<ThemeInfo> dataList) {
            mDataList = dataList;
            mDataList.add(new BottomThemeInfo());
        }

        void refreshThemeState() {
            mListener.onThemeChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == THEME) {
                @SuppressLint("InflateParams")
                ThemeSelectItemView view = (ThemeSelectItemView) LayoutInflater.from(getBaseContext())
                        .inflate(R.layout.theme_preview_item_view, null, false);
                return new ThemePreviewViewHolder(view);
            } else {
                @SuppressLint("InflateParams")
                View view = LayoutInflater.from(getBaseContext())
                        .inflate(R.layout.theme_preview_item_bottom_view, null, false);
                return new BottomViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ThemePreviewViewHolder) {
                ((ThemePreviewViewHolder) holder).mView.setThemeData(mDataList.get(position));
                ((ThemePreviewViewHolder) holder).mView.addThemeChangeListener(mListener);
                mViewList.add(((ThemePreviewViewHolder) holder).mView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mDataList.get(position) instanceof BottomThemeInfo) {
                return BOTTOM;
            } else {
                return THEME;
            }
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }

    class ThemePreviewViewHolder extends RecyclerView.ViewHolder {
        ThemeSelectItemView mView;

        ThemePreviewViewHolder(View itemView) {
            super(itemView);
            mView = (ThemeSelectItemView) itemView;
        }
    }

    class BottomViewHolder extends RecyclerView.ViewHolder {
        BottomViewHolder(View itemView) {
            super(itemView);
        }
    }
}
