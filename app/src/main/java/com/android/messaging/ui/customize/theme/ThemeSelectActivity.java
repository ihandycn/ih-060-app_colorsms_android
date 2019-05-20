package com.android.messaging.ui.customize.theme;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

import java.util.List;

public class ThemeSelectActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_select);
        mRecyclerView = findViewById(R.id.theme_select_recycler_view);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) / 2 == 0) {
                    outRect.left = Dimensions.pxFromDp(16);
                    outRect.right = Dimensions.pxFromDp(6);

                } else {
                    outRect.left = Dimensions.pxFromDp(6);
                    outRect.right = Dimensions.pxFromDp(16);
                }
                outRect.top = Dimensions.pxFromDp(6.3f);
                outRect.bottom = Dimensions.pxFromDp(15);
                super.getItemOffsets(outRect, view, parent, state);
            }
        });
        mRecyclerView.setAdapter(new ThemeAdapter(ThemeInfo.getAllThemes()));
    }

    class ThemeAdapter extends RecyclerView.Adapter<ThemePreviewViewHolder> {
        List<ThemeInfo> mDataList;

        public ThemeAdapter(List<ThemeInfo> dataList) {
            mDataList = dataList;
        }

        @NonNull
        @Override
        public ThemePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ThemePreviewItemView view = (ThemePreviewItemView) LayoutInflater.from(getBaseContext())
                    .inflate(R.layout.theme_preview_item_view, null, false);
            return new ThemePreviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ThemePreviewViewHolder holder, int position) {
            holder.mView.setThemeData(mDataList.get(0));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }

    class ThemePreviewViewHolder extends RecyclerView.ViewHolder {
        ThemePreviewItemView mView;

        public ThemePreviewViewHolder(View itemView) {
            super(itemView);
            mView = (ThemePreviewItemView) itemView;
        }
    }
}
