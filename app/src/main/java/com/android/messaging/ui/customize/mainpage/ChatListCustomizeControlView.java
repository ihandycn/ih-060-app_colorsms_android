package com.android.messaging.ui.customize.mainpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.wallpaper.WallpaperChooserItem;
import com.android.messaging.ui.wallpaper.WallpaperChooserItemView;
import com.android.messaging.ui.wallpaper.WallpaperDownloader;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.view.SelectorDrawable;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.ui.customize.mainpage.ChatListCustomizeActivity.REQUEST_CODE_PICK_WALLPAPER;

public class ChatListCustomizeControlView extends ConstraintLayout {

    private List<WallpaperChooserItemView> mListeners = new ArrayList<>();
    private ChatListCustomizeChangeListener mChangeListener;
    private View mTextColorPreview;
    private boolean mIsColorChooseViewShowing;
    private View mTextColorBackIcon;
    private SeekBar mOpacitySeekBar;
    private ChatListChooseColorView mColorChooseView;


    public interface ChatListCustomizeChangeListener {
        void onWallpaperChange(String path);

        void onOpacityChange(float opacity);

        void onTextColorChange(@ColorInt int textColor);

        void onBackBtnClick();

        void onApplyBtnClick();
    }

    public ChatListCustomizeControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean handleBackEvent() {
        if (mIsColorChooseViewShowing) {
            mTextColorBackIcon.performClick();
            return true;
        }
        return false;
    }

    public void addCustomizeChangeListener(ChatListCustomizeChangeListener listener) {
        mChangeListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initTopBackAndApplyBtn();
        initTouchArea();
        initWallpaperChooseView();
        initTextColorChooseView();
        initSeekBar();
    }

    private void initWallpaperChooseView() {
        RecyclerView wallpaperChooser = findViewById(R.id.wallpaper_chooser_container);
        wallpaperChooser.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        wallpaperChooser.setAdapter(new WallpaperChooserAdapter(getContext(), WallpaperManager.getWallpaperChooserList()));
        wallpaperChooser.setItemViewCacheSize(15);
    }

    private void initTouchArea() {
        findViewById(R.id.toggle_view).setBackground(BackgroundDrawables.createBackgroundDrawable(0xffe5e8ee,
                Dimensions.pxFromDp(2), false));

        View touchAreaView = findViewById(R.id.chat_list_customize_touch_view);
        View controlViewContainer = findViewById(R.id.select_container);
        controlViewContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, 0,
                Dimensions.pxFromDp(13.3f), Dimensions.pxFromDp(13.3f), 0, 0,
                false, false));
        controlViewContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom - top > 0) {
                    controlViewContainer.setTranslationY(bottom - top - Dimensions.pxFromDp(32));
                    controlViewContainer.removeOnLayoutChangeListener(this);
                    Threads.postOnMainThreadDelayed(() -> {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(controlViewContainer, "translationY", 0);
                        animator.setDuration(600);
                        animator.setInterpolator(PathInterpolatorCompat.create(0.18f, 0.99f, 0.36f, 1));
                        animator.start();
                    }, Build.VERSION.SDK_INT >= 21 ? 380 : 180);
                }
            }
        });
        new ChatListViewSwipeHelper(touchAreaView, controlViewContainer);
    }

    private void initTopBackAndApplyBtn() {
        View backBtn = findViewById(R.id.chat_list_back);
        View applyBtn = findViewById(R.id.chat_list_apply);
        int statusBarHeight = Dimensions.getStatusBarHeight(getContext());
        if (statusBarHeight > 0) {
            ConstraintLayout.LayoutParams params = (LayoutParams) backBtn.getLayoutParams();
            params.topMargin += statusBarHeight;
            backBtn.setLayoutParams(params);

            ConstraintLayout.LayoutParams lp = (LayoutParams) applyBtn.getLayoutParams();
            lp.topMargin += statusBarHeight;
            applyBtn.setLayoutParams(lp);
        }
        backBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getContext().getResources().getColor(R.color.black_60_transparent),
                Dimensions.pxFromDp(33.3f) / 2, true));
        applyBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getContext().getResources().getColor(R.color.black_60_transparent),
                Dimensions.pxFromDp(33.3f) / 2, true));

        backBtn.setOnClickListener(v -> {
            if (mChangeListener != null) {
                mChangeListener.onBackBtnClick();
            }
        });

        applyBtn.setOnClickListener(v -> {
            if (mChangeListener != null) {
                mChangeListener.onApplyBtnClick();
            }
        });
    }

    private void initTextColorChooseView() {
        int phoneWidth = Dimensions.getPhoneWidth(getContext());
        mTextColorPreview = findViewById(R.id.chat_list_text_color_preview);
        View textColorChooseContainer = findViewById(R.id.chat_list_color_select_container);
        textColorChooseContainer.setTranslationX(phoneWidth);

        ObjectAnimator animator = ObjectAnimator.ofFloat(textColorChooseContainer, "translationX", 0);
        animator.setDuration(440);
        animator.setInterpolator(PathInterpolatorCompat.create(0.26f, 1, 0.48f, 1));
        mTextColorPreview.setOnClickListener(v -> {
            textColorChooseContainer.setVisibility(View.VISIBLE);
            animator.start();
            mIsColorChooseViewShowing = true;
        });

        mTextColorBackIcon = findViewById(R.id.chat_list_text_color_back);
        mTextColorBackIcon.setBackground(BackgroundDrawables.createTransparentBackgroundDrawable(
                getContext().getResources().getColor(com.superapps.R.color.ripples_ripple_color),
                Dimensions.pxFromDp(29.3f) / 2));
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(textColorChooseContainer, "translationX", phoneWidth);
        animator1.setDuration(440);
        animator1.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.94f, 0.6f, 1));
        mTextColorBackIcon.setOnClickListener(v -> {
            animator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    textColorChooseContainer.setVisibility(View.GONE);
                }
            });
            animator1.start();
            mIsColorChooseViewShowing = false;
        });

        mColorChooseView = findViewById(R.id.choose_message_color_view);
        mColorChooseView.setOnColorChangeListener(color -> {
            setTextColorBtnColor(color);
            if (mChangeListener != null) {
                mChangeListener.onTextColorChange(color);
            }
        });
    }

    public void resetOpacitySeekBar() {
        mOpacitySeekBar.setProgress(0);
    }

    public void changeRecommendTextColor(@ColorInt int color) {
        mColorChooseView.changeRecommendColor(color);
    }

    public void setTextColorBtnColor(@ColorInt int color) {
        mTextColorPreview.setBackground(createTextPreviewDrawable(color));
    }

    private Drawable createTextPreviewDrawable(int color) {
        int rippleColor = getContext().getResources().getColor(com.superapps.R.color.ripples_ripple_color);
        ShapeDrawable drawable = new ShapeDrawable(new Shape() {

            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setStrokeWidth(Dimensions.pxFromDp(1.3f));
                paint.setDither(true);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(0xffdee0e8);
                float radius = Math.min(getHeight() / 2, getWidth() / 2) - Dimensions.pxFromDp(1.3f);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, Dimensions.pxFromDp(13), paint);
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, null);
        } else {
            return new SelectorDrawable(drawable, rippleColor);
        }
    }

    private void initSeekBar() {
        mOpacitySeekBar = findViewById(R.id.chat_list_seek_bar);

        //set progress bar drawable
        ShapeDrawable seekBarProgressDrawable = new ShapeDrawable();
        Shape shape = new Shape() {
            private int startX = Dimensions.pxFromDp(2);
            private int paintColor = PrimaryColors.getPrimaryColor();

            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(paintColor);
                paint.setStrokeWidth(startX * 2);
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(startX, getHeight() / 2, getWidth() - startX, getHeight() / 2, paint);
            }
        };
        seekBarProgressDrawable.setShape(shape);
        ClipDrawable clipDrawable = new ClipDrawable(seekBarProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL);

        ShapeDrawable seekBarBackgroundDrawable = new ShapeDrawable();
        Shape backgroundShape = new Shape() {
            private int mStartX = Dimensions.pxFromDp(2);
            private int mPaintColor = 0xffeff3f5;

            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(mPaintColor);
                paint.setStrokeWidth(mStartX * 2);
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(mStartX, getHeight() / 2, getWidth() - mStartX, getHeight() / 2, paint);
            }
        };
        seekBarBackgroundDrawable.setShape(backgroundShape);

        LayerDrawable drawable = new LayerDrawable(new Drawable[]{seekBarBackgroundDrawable, clipDrawable});
        drawable.setId(0, android.R.id.background);
        drawable.setId(1, android.R.id.progress);

        mOpacitySeekBar.setProgressDrawable(drawable);

        //bind progress data
        TextView opacityText = findViewById(R.id.chat_list_customize_opacity_text);
        int maxProgressValue = mOpacitySeekBar.getMax();
        mOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                opacityText.setText(String.valueOf(progress + "%"));
                if (mChangeListener != null) {
                    mChangeListener.onOpacityChange(progress * 1.0f / maxProgressValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mOpacitySeekBar.setProgress((int) (ChatListDrawableManager.getMaskOpacity() * maxProgressValue));
    }

    private void onWallpaperChanged(String path) {
        if (mChangeListener != null) {
            mChangeListener.onWallpaperChange(path);
        }
        mOpacitySeekBar.setProgress(0);
    }

    public void addListener(WallpaperChooserItemView listener) {
        mListeners.add(listener);
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

    public class WallpaperChooserAdapter extends RecyclerView.Adapter<WallpaperChooserViewHolder> {
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
                    LayoutInflater.from(mContext).inflate(R.layout.chat_list_wallpaper_choose_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull WallpaperChooserViewHolder holder, int position) {
            WallpaperChooserItem item = wallpaperInfoList.get(position);
            WallpaperChooserItemView view = (WallpaperChooserItemView) holder.itemView;
            view.setChooserItem(item);
            String wallpaperPath = ChatListDrawableManager.getListWallpaperPath();
            if (item.getItemType() == WallpaperChooserItem.TYPE_ADD_PHOTO) {
                view.findViewById(R.id.wallpaper_chooser_add_photo_container).setBackground(
                        BackgroundDrawables.createBackgroundDrawable(0xffeff3f5, Dimensions.pxFromDp(3.3f), true));
                view.setOnClickListener(v -> {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    Intent chooserIntent = Intent.createChooser(pickIntent, getContext().getString(R.string.select_image));
                    Navigations.startActivityForResultSafely((Activity) mContext, chooserIntent, REQUEST_CODE_PICK_WALLPAPER);
                });
            } else if (item.getItemType() == WallpaperChooserItem.TYPE_EMPTY) {
                addListener(view);
                ThemeInfo info = ThemeUtils.getCurrentTheme();
                if (WallpaperDrawables.getConversationWallpaperBg() == null) {
                    ImageView v = view.findViewById(R.id.wallpaper_chooser_item_iv);
                    v.setImageDrawable(null);
                    v.setBackground(
                            BackgroundDrawables.createBackgroundDrawable(Color.WHITE, 0,
                                    2, 0xffe1e7ea, Dimensions.pxFromDp(3.3f),
                                    false, false));
                }
                view.setOnClickListener(v -> {
                    if (view.isItemSelected()) {
                        return;
                    }
                    onItemSelected(view);
                    onWallpaperChanged("");

                    setTextColorBtnColor(Color.parseColor(info.listTitleColor));
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
                            onWallpaperChanged(item.getAbsolutePath());
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
                                        if (getContext() instanceof Activity
                                                && ((Activity) getContext()).isDestroyed()) {
                                            return;
                                        }
                                        onWallpaperChanged(item.getAbsolutePath());
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
