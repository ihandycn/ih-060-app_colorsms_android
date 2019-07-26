package com.android.messaging.ui.wallpaper;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.customize.CustomMessagePreviewView;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.UiUtils;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.List;

public class WallpaperPreviewActivity extends BaseActivity implements WallpaperManager.WallpaperChangeListener {

    public static final int REQUEST_CODE_PICK_WALLPAPER = 2;

    public static void startWallpaperPreviewByConversationId(Context context, String threadId) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        intent.putExtra("conversation_id", threadId);
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        }
    }

    public static void startWallpaperPreview(Context context) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        }
    }

    private ImageView mWallpaperPreviewImg;
    private String mConversationId;
    private CustomMessagePreviewView mBubbleView;
    private WallpaperChooserAdapter mAdapter;

    private void setPreviewImage(String path) {
        String url = Uri.fromFile(new File(path)).toString();
        GlideApp.with(WallpaperPreviewActivity.this)
                .asBitmap()
                .load(url)
                .into(new ImageViewTarget<Bitmap>(mWallpaperPreviewImg) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Threads.postOnMainThread(() -> {
                            if (isDestroyed()) {
                                return;
                            }
                            mWallpaperPreviewImg.setImageBitmap(resource);
                            startPreviewTransitionAnimation();
                        });
                    }

                    @Override
                    protected void setResource(@Nullable Bitmap resource) {

                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);

                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                    }
                });
        mBubbleView.updateBackgroundState(mConversationId, true);
    }

    public void setPreviewDrawable(Drawable drawable) {
        mWallpaperPreviewImg.setImageDrawable(drawable);
        mBubbleView.updateBackgroundState(mConversationId, false);
        startPreviewTransitionAnimation();
    }

    private void startPreviewTransitionAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mWallpaperPreviewImg, "alpha", 0f, 1f);
        animator.setDuration(200);
        animator.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_chat_background));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        UiUtils.setTitleBarBackground(toolbar, this);
        RecyclerView wallpaperChooser = findViewById(R.id.wallpaper_chooser_container);
        wallpaperChooser.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new WallpaperChooserAdapter(this, WallpaperManager.getWallpaperChooserList());
        wallpaperChooser.setAdapter(mAdapter);

        String conversationId = getIntent().getStringExtra("conversation_id");
        if (conversationId != null) {
            mConversationId = conversationId;
            BugleAnalytics.logEvent("SMS_ChatBackground_Show", true, "from", "Options");
            BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Show",  "from", "Options");
        } else {
            BugleAnalytics.logEvent("SMS_ChatBackground_Show", true, "from", "Menu");
            BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Show", "from", "Menu");
        }
        mWallpaperPreviewImg = findViewById(R.id.wallpaper_preview);
        WallpaperManager.setWallPaperOnView(mWallpaperPreviewImg, mConversationId);

        mBubbleView = findViewById(R.id.wallpaper_chooser_add_preview_message);
        mBubbleView.updateBubbleDrawables(mConversationId, WallpaperManager.hasCustomWallpaper(mConversationId));

        WallpaperManager.addWallpaperChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WallpaperManager.removeWallpaperChangeListener(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_PICK_WALLPAPER
                && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toasts.showToast("error");
                return;
            }

            Intent intent = WallpaperEditActivity.getLaunchIntent(this, data, mConversationId);
            startActivity(intent);
        }
    }

    @Override
    public void onWallpaperChanged() {
        if (TextUtils.isEmpty(mConversationId)) {
            String wallpaperPath = WallpaperManager.getWallpaperPathByConversationId(mConversationId);
            if (!TextUtils.isEmpty(wallpaperPath)) {
                mWallpaperPreviewImg.setImageURI(Uri.fromFile(new File(wallpaperPath)));
            }
            mBubbleView.updateBackgroundState(mConversationId, true);
            mAdapter.onItemSelected(null);
        } else {
            finish();
        }
    }

    @Override
    public void onOnlineWallpaperChanged() {

    }

    class WallpaperChooserAdapter extends RecyclerView.Adapter<WallpaperChooserViewHolder> {
        private List<WallpaperChooserItem> mWallpaperInfoList;
        private Context mContext;
        String mWallpaperPath = WallpaperManager.getWallpaperPathByConversationId(mConversationId);

        WallpaperChooserAdapter(Context context, List<WallpaperChooserItem> wallpaperInfos) {
            mContext = context;
            mWallpaperInfoList = wallpaperInfos;

            if (TextUtils.isEmpty(mWallpaperPath)) {
                for (int i = 0; i < mWallpaperInfoList.size(); i++) {
                    if (mWallpaperInfoList.get(i).getItemType() == WallpaperChooserItem.TYPE_EMPTY) {
                        mWallpaperInfoList.get(i).setSelectedState(true);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < mWallpaperInfoList.size(); i++) {
                    WallpaperChooserItem item = mWallpaperInfoList.get(i);
                    if (item.getItemType() == WallpaperChooserItem.TYPE_NORMAL_WALLPAPER
                            && mWallpaperPath.equals(item.getWallpaperLocalPath())) {
                        item.setSelectedState(true);
                        break;
                    }
                }
            }

            for (int i = 0; i < mWallpaperInfoList.size(); i++) {
                WallpaperChooserItem item = mWallpaperInfoList.get(i);
                if (item.getItemType() == WallpaperChooserItem.TYPE_NORMAL_WALLPAPER
                        && !item.isDownloaded()) {
                    item.setDownloadListener(
                            new WallpaperDownloader.WallpaperDownloadListener() {
                                @Override
                                public void onDownloadSuccess() {
                                    if (item.isItemChecked()) {
                                        onItemSelected(item);
                                        if (isDestroyed()) {
                                            return;
                                        }
                                        setPreviewImage(item.getWallpaperLocalPath());
                                        WallpaperManager.setWallpaperPath(mConversationId, item.getWallpaperLocalPath());

                                        WallpaperManager.onOnlineWallpaperChanged();
                                        BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied", true,
                                                "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                                        BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied",
                                                "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");

                                        BugleAnalytics.logEvent("SMS_ChatBackground_Change", true,
                                                "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                                        BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Change",
                                                "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                                    }
                                }

                                @Override
                                public void onDownloadFailed() {
                                }
                            }
                    );
                }
            }
        }

        public void onItemSelected(WallpaperChooserItem selectedItem) {
            for (WallpaperChooserItem item : mWallpaperInfoList) {
                item.setSelectedState(item.equals(selectedItem));
                item.setPreSelectState(false);
            }
        }

        void onItemPreSelected(WallpaperChooserItem selectedItem) {
            for (WallpaperChooserItem item : mWallpaperInfoList) {
                item.setPreSelectState(item.equals(selectedItem));
            }
        }

        @NonNull
        @Override
        public WallpaperChooserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new WallpaperChooserViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.wallpaper_chooser_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull WallpaperChooserViewHolder holder, int position) {
            WallpaperChooserItem item = mWallpaperInfoList.get(position);
            WallpaperChooserItemView view = holder.mItemView;
            item.bindView(view);

            if (item.getItemType() == WallpaperChooserItem.TYPE_ADD_PHOTO) {
                view.setBackgroundResource(R.drawable.wallpaper_add_photo_bg);
                view.setOnClickListener(v -> {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    Intent chooserIntent = Intent.createChooser(pickIntent, getResources().getString(R.string.select_image));
                    Navigations.startActivityForResultSafely((Activity) mContext, chooserIntent, REQUEST_CODE_PICK_WALLPAPER);
                    BugleAnalytics.logEvent("SMS_ChatBackground_AddPhotos_Clicked", true,
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_AddPhotos_Clicked",
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                });
            } else if (item.getItemType() == WallpaperChooserItem.TYPE_EMPTY) {
                view.setOnClickListener(v -> {
                    if (item.isItemChecked()) {
                        return;
                    }
                    onItemSelected(item);
                    if (mConversationId == null) {
                        WallpaperManager.setWallpaperPath(null, "");
                        WallpaperManager.onOnlineWallpaperChanged();
                    } else {
                        WallpaperManager.setWallpaperPath(mConversationId, "empty");
                    }
                    setPreviewDrawable(WallpaperDrawables.getConversationWallpaperBg());
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked", true,
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked",
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied", true,
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied",
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleAnalytics.logEvent("SMS_ChatBackground_Change", true,
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Change",
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                });
            } else {
                view.setOnClickListener(v -> {
                    if (!item.isItemDownloading() && item.isDownloaded()) {
                        if (item.isItemChecked()) {
                            onItemSelected(item);
                            return;
                        }
                        onItemSelected(item);
                        if (item.isItemChecked()) {
                            setPreviewImage(item.getWallpaperLocalPath());
                            WallpaperManager.setWallpaperPath(mConversationId, item.getWallpaperLocalPath());
                            WallpaperManager.onOnlineWallpaperChanged();
                            BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied", true,
                                    "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                            BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied",
                                    "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");

                            BugleAnalytics.logEvent("SMS_ChatBackground_Change", true,
                                    "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                            BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Change",
                                    "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                        }
                    } else {
                        onItemPreSelected(item);
                        item.downloadWallpaper();
                    }
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked", true,
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                    BugleFirebaseAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked",
                            "from", TextUtils.isEmpty(mConversationId) ? "Menu" : "Options");
                });
            }
        }

        @Override
        public void onViewRecycled(@NonNull WallpaperChooserViewHolder holder) {
            mWallpaperInfoList.get(holder.getAdapterPosition()).bindView(null);
        }

        @Override
        public int getItemCount() {
            return mWallpaperInfoList.size();
        }
    }

    class WallpaperChooserViewHolder extends RecyclerView.ViewHolder {
        private WallpaperChooserItemView mItemView;

        WallpaperChooserViewHolder(View itemView) {
            super(itemView);
            mItemView = (WallpaperChooserItemView) itemView;
        }
    }
}
