package com.android.messaging.ui.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.glide.GlideRequests;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.view.RecyclerViewWidthSlideListener;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.view.RoundImageView;
import com.superapps.view.TypefacedTextView;

import java.util.List;

public class EmojiStoreActivity extends HSAppCompatActivity implements INotificationObserver {

    public static final String NOTIFICATION_REFRESH_ITEM_STATUS = "notificaiton_refresh_item_status";
    public static final String NOTIFICATION_BUNDLE_ITEM_NAME = "notification_bundle_item_position";
    private static final int MAX_COLUMNS = 2;
    private List<EmojiPackageInfo> mStoreEmojiPackageInfoList;
    private StoreAdapter mAdapter;

    public static void start(Context context) {
        Intent starter = new Intent(context, EmojiStoreActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_store_layout);

        mStoreEmojiPackageInfoList = EmojiConfig.getInstance().getStoreEmojiFromConfig();
        RecyclerViewWidthSlideListener recyclerView = findViewById(R.id.emoji_store_list);
        recyclerView.setOnSlideListener(new RecyclerViewWidthSlideListener.OnSlideListener() {
            @Override
            public void slideUp() {
            }

            @Override
            public void slideDown() {
            }
        });
        mAdapter = new StoreAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, MAX_COLUMNS));
        recyclerView.addItemDecoration(new StoreItemDecoration());
        findViewById(R.id.emoji_store_back_btn).setOnClickListener(v -> this.finish());
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REFRESH_ITEM_STATUS, this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case NOTIFICATION_REFRESH_ITEM_STATUS:
                if (hsBundle == null) {
                    return;
                }

                String name = hsBundle.getString(NOTIFICATION_BUNDLE_ITEM_NAME);
                if (TextUtils.isEmpty(name)) {
                    return;
                }

                if (mAdapter != null) {
                    mAdapter.updateItem(name);
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    private class StoreAdapter extends RecyclerView.Adapter<StoreViewHolder> {

        private Context mContext;
        private Drawable mColorDrawable = new ColorDrawable(0xFFF7F7F7);

        StoreAdapter(Context context) {
            mContext = context;
        }

        @Override
        @NonNull
        public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new StoreViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.emoji_store_item, parent, false));
        }

        @Override
        public int getItemCount() {
            return mStoreEmojiPackageInfoList.size();
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
            EmojiPackageInfo packageInfo = mStoreEmojiPackageInfoList.get(position);

            Resources res = getResources();
            if (EmojiManager.isTabSticker(packageInfo.mName)) {
                holder.getBtn.setOnClickListener(null);
                holder.getBtn.setText(res.getString(R.string.emoji_added));
                holder.getBtn.setTextColor(0xFFFFFFFF);
                holder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(15), true));
            } else {
                holder.getBtn.setText(res.getString(R.string.emoji_get));
                holder.getBtn.setTextColor(0xFF333333);
                holder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFF4BE3E, Dimensions.pxFromDp(15), true));
                holder.getBtn.setOnClickListener(v -> {
                    holder.getBtn.setOnClickListener(null);
                    holder.getBtn.setText(res.getString(R.string.emoji_added));
                    holder.getBtn.setTextColor(0xFFFFFFFF);
                    holder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(15), true));
                    EmojiManager.addTabSticker(packageInfo.mName);
                    HSBundle bundle = new HSBundle();
                    bundle.putObject(EmojiPickerFragment.NOTIFICATION_BUNDLE_PACKAGE_INFO, packageInfo);
                    HSGlobalNotificationCenter.sendNotification(EmojiPickerFragment.NOTIFICATION_ADD_EMOJI_FROM_STORE, bundle);
                });
            }

            holder.previewLayout.bindEmojiItems(packageInfo);

            holder.itemView.setOnClickListener(v -> EmojiDetailActivity.start(EmojiStoreActivity.this, packageInfo));

            GlideRequests imageRequest = GlideApp.with(mContext);
            imageRequest.asBitmap()
                    .load(packageInfo.mBannerUrl)
                    .placeholder(mColorDrawable)
                    .error(mColorDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.image);
        }

        void updateItem(String name) {
            for (int i = 0; i < mStoreEmojiPackageInfoList.size(); i++) {
                EmojiPackageInfo packageInfo = mStoreEmojiPackageInfoList.get(i);
                if (packageInfo.mName.equals(name)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    private class StoreViewHolder extends RecyclerView.ViewHolder {

        RoundImageView image;
        TypefacedTextView getBtn;
        EmojiStorePreviewLayout previewLayout;

        StoreViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.emoji_store_item_image);
            getBtn = itemView.findViewById(R.id.emoji_store_item_get_btn);
            previewLayout = itemView.findViewById(R.id.preview_layout);
        }
    }

    private static class StoreItemDecoration extends RecyclerView.ItemDecoration {

        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view);
            int column = position % MAX_COLUMNS;

            int dp3_5 = Dimensions.pxFromDp(3.5f);
            int dp10 = Dimensions.pxFromDp(10);
            int dp9 = Dimensions.pxFromDp(9);

            if (column == 0) {
                outRect.left = dp10;
                outRect.right = dp3_5;
            } else if (column == (MAX_COLUMNS - 1)) {
                outRect.left = dp3_5;
                outRect.right = dp10;
            } else {
                outRect.left = dp3_5;
                outRect.right = dp3_5;
            }

            if (position < MAX_COLUMNS) {
                outRect.top = Dimensions.pxFromDp(14);
                outRect.bottom = dp9;
            } else {
                outRect.bottom = dp9;
            }
        }
    }
}
