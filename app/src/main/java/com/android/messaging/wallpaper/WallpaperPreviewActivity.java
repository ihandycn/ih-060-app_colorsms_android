package com.android.messaging.wallpaper;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperPreviewActivity extends AppCompatActivity {

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

    private void setPreviewImage(String path) {
        String url = Uri.fromFile(new File(path)).toString();
        GlideApp.with(WallpaperPreviewActivity.this)
                .asBitmap()
                .load(url)
                .into(new ImageViewTarget<Bitmap>(mWallpaperPreviewImg) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Threads.postOnMainThread(() -> {
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

        mWallpaperPreviewImg = findViewById(R.id.wallpaper_preview);

        RecyclerView wallpaperChooser = findViewById(R.id.wallpaper_chooser_container);
        wallpaperChooser.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        wallpaperChooser.setAdapter(new WallpaperChooserAdapter(this, WallpaperManager.getWallpaperChooserList()));
        wallpaperChooser.setItemViewCacheSize(15);

//        RecyclerView messageList = findViewById(R.id.wallpaper_chooser_message_list);
//        final LinearLayoutManager manager = new LinearLayoutManager(this);
//        //manager.setStackFromEnd(true);
//        manager.setReverseLayout(false);
//        messageList.setHasFixedSize(true);
//        messageList.setLayoutManager(manager);
//        WallpaperPreviewMessageAdapter adapter = new WallpaperPreviewMessageAdapter(this);
//        messageList.setAdapter(adapter);

        String threadId = getIntent().getStringExtra("thread_id");
        if (threadId != null) {
            mThreadId = threadId;
            BugleAnalytics.logEvent("SMS_ChatBackground_Show","from","Options");
        } else {
            BugleAnalytics.logEvent("SMS_ChatBackground_Show","from","Menu");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListeners.clear();
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

            Intent intent = WallpaperEditActivity.getLaunchIntent(this, data);
            startActivity(intent);
            finish();
        }
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
            if (item.getItemType() == WallpaperChooserItem.TYPE_ADD_PHOTO) {
                view.setOnClickListener(v -> {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
                    Navigations.startActivityForResultSafely((Activity) mContext, chooserIntent, REQUEST_CODE_PICK_WALLPAPER);
                    BugleAnalytics.logEvent("SMS_ChatBackground_AddPhotos_Clicked");
                });
            } else if (item.getItemType() == WallpaperChooserItem.TYPE_EMPTY) {
                addListener(view);
                view.setOnClickListener(v -> {
                    onItemSelected(view);
                    mWallpaperPreviewImg.setImageBitmap(null);
                    WallpaperManager.setWallpaperPath("");
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked");
                });
            } else {
                addListener(view);
                view.setOnClickListener(v -> {
                    onItemSelected(view);
                    if (item.isDownloaded()) {
                        if (view.isItemSelected()) {
                            view.onItemSelected();
                            setPreviewImage(item.getLocalPath());
                            if (mThreadId != null) {
                                WallpaperManager.setWallpaperPath(mThreadId, item.getAbsolutePath());
                            } else {
                                WallpaperManager.setWallpaperPath(item.getAbsolutePath());
                            }
                            BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied");
                        }
                    } else {
                        view.onLoadingStart();
                        WallpaperDownloader.download(new WallpaperDownloader.WallpaperDownloadListener() {
                            @Override
                            public void onDownloadSuccess(String path) {
                                Threads.postOnMainThread(() -> {
                                    view.onLoadingSuccess();
                                    setPreviewImage(item.getLocalPath());
                                    if (mThreadId != null) {
                                        WallpaperManager.setWallpaperPath(mThreadId, item.getAbsolutePath());
                                    } else {
                                        WallpaperManager.setWallpaperPath(item.getAbsolutePath());
                                    }
                                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Applied");
                                });
                            }

                            @Override
                            public void onDownloadFailed() {

                            }
                        }, item.getRemoteUrl());
                    }
                    BugleAnalytics.logEvent("SMS_ChatBackground_Backgrounds_Clicked");
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
