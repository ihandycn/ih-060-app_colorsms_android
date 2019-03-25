package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.view.RecyclerViewWidthSlideListener;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;


public class EmojiLottieDetailActivity extends BaseActivity {

    private static final String INTENT_EMOJI_PACKAGE_INFO = "intent_emoji_package_info";
    private static final String INTENT_SOURCE = "intent_source";
    private static final int HEADER_BOTTOM = Dimensions.pxFromDp(16.33f);
    private static final int INNER_HORIZONTAL_SPACING = Dimensions.pxFromDp(5);
    private static final int INNER_VERTICAL_SPACING = Dimensions.pxFromDp(13);

    private static final int GALLERY_COLUMNS = 4;

    public static void start(String source, Context context, EmojiPackageInfo packageInfo) {
        Intent starter = new Intent(context, EmojiLottieDetailActivity.class);
        starter.putExtra(INTENT_EMOJI_PACKAGE_INFO, packageInfo);
        starter.putExtra(INTENT_SOURCE, source);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_emoji_detail_layout);
        setupRecyclerView();

        View backBtn = findViewById(R.id.emoji_detail_back_btn);
        backBtn.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerViewWidthSlideListener gallery = findViewById(R.id.emoji_detail_recycler);
        EmojiLottieDetailAdapter adapter = new EmojiLottieDetailAdapter(EmojiConfig.getInstance().getMajicEmoj());
        gallery.setAdapter(adapter);
        gallery.addItemDecoration(new GridItemDecoration());
        GridLayoutManager manager = new GridLayoutManager(this, GALLERY_COLUMNS);
        gallery.setLayoutManager(manager);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? manager.getSpanCount() : 1;
            }
        });
    }

    private class GridItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == 0) {
                outRect.bottom = HEADER_BOTTOM;
                return;
            }

            outRect.left = INNER_HORIZONTAL_SPACING;
            outRect.right = INNER_HORIZONTAL_SPACING;
            outRect.top = INNER_VERTICAL_SPACING;
            outRect.bottom = INNER_VERTICAL_SPACING;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseStickerItemRecyclerAdapter.releaseListener();
    }
}
