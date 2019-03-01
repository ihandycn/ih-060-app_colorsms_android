package com.android.messaging.ui.wallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import org.qcode.fontchange.impl.FontManagerImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperPreviewActivity extends BaseActivity implements WallpaperManager.WallpaperChangeListener {

    public static final int REQUEST_CODE_PICK_WALLPAPER = 2;

    public static void startWallpaperPreviewByThreadId(Context context, String threadId) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        intent.putExtra("thread_id", threadId);
        Navigations.startActivitySafely(context, intent);
    }

    public static void startWallpaperPreview(Context context) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        Navigations.startActivitySafely(context, intent);
    }

    private List<WallpaperChooserItemView> mListeners = new ArrayList<>();
    private ImageView mWallpaperPreviewImg;
    private String mThreadId;

    public void addListener(WallpaperChooserItemView listener) {
        mListeners.add(listener);
    }

    public void removeListener(WallpaperChooserItemView listener) {
        mListeners.remove(listener);
    }

    public void onItemSelected(WallpaperChooserItemView view) {
        for (WallpaperChooserItemView v : mListeners) {
            if (v.equals(view)) {
                v.onItemSelected();
            } else {
                v.onItemDeselected();
            }
        }
    }

    public void onItemPreSelected(WallpaperChooserItemView view) {
        for (WallpaperChooserItemView v : mListeners) {
            if (v.isItemSelected()) {
                continue;
            }
            if (v.equals(view)) {
                v.onItemPreSelected();
            } else {
                v.onItemDeselected();
            }
        }
    }

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
                            mWallpaperPreviewImg.setVisibility(View.INVISIBLE);
                            mWallpaperPreviewImg.setImageBitmap(resource);
                            ObjectAnimator animator = ObjectAnimator.ofFloat(mWallpaperPreviewImg, "alpha", 0f, 1f);
                            animator.setDuration(200);
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    mWallpaperPreviewImg.setVisibility(View.VISIBLE);
                                }
                            });
                            animator.start();
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.wallpaper_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));

        RecyclerView wallpaperChooser = findViewById(R.id.wallpaper_chooser_container);
        wallpaperChooser.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        wallpaperChooser.setAdapter(new WallpaperChooserAdapter(this, WallpaperManager.getWallpaperChooserList()));
        wallpaperChooser.setItemViewCacheSize(15);

        String threadId = getIntent().getStringExtra("thread_id");
        if (threadId != null) {
            mThreadId = threadId;
            BugleAnalytics.logEvent("SMS_ChatBackground_Show", true, "from", "Options");
        } else {
            BugleAnalytics.logEvent("SMS_ChatBackground_Show", true, "from", "Menu");
        }

        mWallpaperPreviewImg = findViewById(R.id.wallpaper_preview);
        String wallpaperPath = WallpaperManager.getWallpaperPathByThreadId(mThreadId);
        if (!TextUtils.isEmpty(wallpaperPath)) {
            mWallpaperPreviewImg.setImageURI(Uri.fromFile(new File(wallpaperPath)));
        }

        WallpaperManager.addWallpaperChangeListener(this);
        FontManagerImpl.getInstance().applyFont(findViewById(R.id.wallpaper_chooser_message_list), true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListeners.clear();
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

            Intent intent = WallpaperEditActivity.getLaunchIntent(this, data, mThreadId);
            startActivity(intent);
        }
    }

    @Override
    public void onWallpaperChanged() {
        if (TextUtils.isEmpty(mThreadId)) {
            String wallpaperPath = WallpaperManager.getWallpaperPathByThreadId(mThreadId);
            if (!TextUtils.isEmpty(wallpaperPath)) {
                mWallpaperPreviewImg.setImageURI(Uri.fromFile(new File(wallpaperPath)));
            }
            onItemSelected(null);
        } else {
            finish();
        }
    }

    @Override
    public void onOnlineWallpaperChanged() {

    }

    class WallpaperChooserAdapter extends RecyclerView.Adapter<WallpaperChooserViewHolder> {
        private List<WallpaperChooserItem> wallpaperInfoList;
        private Context mContext;

        WallpaperChooserAdapter(Context context, List<WallpaperChooserItem> wallpaperInfos) {
            mContext = context;
            wallpaperInfoList = wallpaperInfos;
        }

        @NonNull
        @Override
        public WallpaperChooserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new WallpaperChooserViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.wallpaper_chooser_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull WallpaperChooserViewHolder holder, int position) {
            WallpaperChooserItem item = wallpaperInfoList.get(position);
            WallpaperChooserItemView view = (WallpaperChooserItemView) holder.itemView;
            view.setChooserItem(item);
            String wallpaperPath = WallpaperManager.getWallpaperPathByThreadId(mThreadId);
            if (item.getItemType() == WallpaperChooserItem.TYPE_ADD_PHOTO) {
                view.setBackgroundResource(R.drawable.wallpaper_add_photo_bg);
                view.setOnClickListener(v -> {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
                    Navigations.startActivityForResultSafely((Activity) mContext, chooserIntent, REQUEST_CODE_PICK_WALLPAPER);
                    BugleAnalytics.logEvent("SMS_ChatBackground_AddPhotos_Clicked", true,
                            "from", TextUtils.isEmpty(mThreadId) ? "Menu" : "Options");
                });
            } else if (item.getItemType() == WallpaperChooserItem.TYPE_EMPTY) {
                addListener(view);
                view.setOnClickListener(v -> {
                    if (view.isItemSelected()) {
                        return;
                    }
                    onItemSelected(view);
                    mWallpaperPreviewImg.setImageBitmap(null);
                    if (mThreadId == null) {
                        WallpaperManager.setWallpaperPath(null, "");
                    } else {
                        WallpaperManager.setWallpaperPath(mThreadId, "empty");
                    }
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked", true,
                            "from", TextUtils.isEmpty(mThreadId) ? "Menu" : "Options");
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied", true,
                            "from", TextUtils.isEmpty(mThreadId) ? "Menu" : "Options");
                });
                if (TextUtils.isEmpty(wallpaperPath)) {
                    onItemSelected(view);
                }
            } else {
                addListener(view);
                if (wallpaperPath != null && wallpaperPath.equals(item.getAbsolutePath())) {
                    onItemSelected(view);
                }
                view.setOnClickListener(v -> {
                    if (view.isItemSelected() || view.isItemPreSelected()) {
                        return;
                    }
                    if (item.isDownloaded()) {
                        onItemSelected(view);
                        if (view.isItemSelected()) {
                            view.onItemSelected();
                            setPreviewImage(item.getLocalPath());
                            WallpaperManager.setWallpaperPath(mThreadId, item.getAbsolutePath());
                            WallpaperManager.onOnlineWallpaperChanged();
                            BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied", true,
                                    "from", TextUtils.isEmpty(mThreadId) ? "Menu" : "Options");
                        }
                    } else {
                        view.onLoadingStart();
                        onItemPreSelected(view);
                        WallpaperDownloader.download(new WallpaperDownloader.WallpaperDownloadListener() {
                            @Override
                            public void onDownloadSuccess(String path) {
                                Threads.postOnMainThread(() -> {
                                    if (view.isItemPreSelected()) {
                                        onItemSelected(view);
                                    }
                                    view.onLoadingDone();
                                    if (view.isItemSelected()) {
                                        if (isDestroyed()) {
                                            return;
                                        }
                                        setPreviewImage(item.getLocalPath());
                                        WallpaperManager.setWallpaperPath(mThreadId, item.getAbsolutePath());
                                        WallpaperManager.onOnlineWallpaperChanged();
                                        BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied", true,
                                                "from", TextUtils.isEmpty(mThreadId) ? "Menu" : "Options");
                                    }
                                });
                            }

                            @Override
                            public void onDownloadFailed() {
                                view.onItemDeselected();
                                view.onLoadingDone();
                            }
                        }, item.getRemoteUrl());
                    }
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked", true,
                            "from", TextUtils.isEmpty(mThreadId) ? "Menu" : "Options");
                });
            }
        }

        @Override
        public int getItemCount() {
            return wallpaperInfoList.size();
        }
    }

    class WallpaperChooserViewHolder extends RecyclerView.ViewHolder {
        WallpaperChooserViewHolder(View itemView) {
            super(itemView);
        }
    }
}
