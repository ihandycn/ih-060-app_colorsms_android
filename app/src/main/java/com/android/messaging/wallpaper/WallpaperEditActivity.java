package com.android.messaging.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.ViewUtils;
import com.android.messaging.wallpaper.crop.CropImageOptions;
import com.android.messaging.wallpaper.crop.CropOverlayView;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.android.messaging.wallpaper.WallpaperManager.LOCAL_DIRECTORY;

public class WallpaperEditActivity extends HSAppCompatActivity implements View.OnClickListener {

    private static final String TAG = WallpaperEditActivity.class.getSimpleName();
    public static final String INTENT_KEY_WALLPAPER_URI = "wallpaperData";

    private int mBitmapHeight;
    private int mBitmapWidth;

    private ImageView mWallpaperView;
    private TextView mApplyButton;
    private float[] mMatrixValues = new float[9];
    private float mStartX = 0;
    private float mStartY = 0;
    private float mWidth = 0;
    private float mHeight = 0;
    private Bitmap mBitmap;
    private RectF mOverlayBounds;
    private CropOverlayView mCropOverlayView;
    private String mWallpaperPath;
    private boolean mIsCutEventLogged = false;

    private View mResetBtn;

    public static Intent getLaunchIntent(Context context, Intent uriIntent) {
        Intent intent = new Intent(context, WallpaperEditActivity.class);
        intent.putExtra(WallpaperEditActivity.INTENT_KEY_WALLPAPER_URI, uriIntent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_wallpaper_edit_new);
        mWallpaperView = ViewUtils.findViewById(this, R.id.wallpaper_edit_image);
        mCropOverlayView = ViewUtils.findViewById(this, R.id.wallpaper_overlay_view);
        mApplyButton = ViewUtils.findViewById(this, R.id.wallpaper_edit_apply_button);
        mResetBtn = ViewUtils.findViewById(this, R.id.wallpaper_edit_reset_button);
        findViewById(R.id.wallpaper_view_return).setOnClickListener(this);
        init();
        BugleAnalytics.logEvent("SMS_ChatBackground_CutPage_Show");
    }

    private void bindEvents() {
        mResetBtn.setOnClickListener(this);
        mApplyButton.setOnClickListener(this);
        mCropOverlayView.setCropWindowChangeListener(b -> {
            if (b) {
                showResetBtn();
                if (!mIsCutEventLogged) {
                    BugleAnalytics.logEvent("SMS_ChatBackground_CutPage_CutOut");
                    mIsCutEventLogged = true;
                }
            }
        });
    }

    private void showError() {
        Threads.postOnMainThread(() -> {
            Toasts.showToast(R.string.wallpaper_set_failed);
            finish();
        });
    }

    private boolean isGif(byte[] header) {
        StringBuilder stringBuilder = new StringBuilder();
        if (header == null || header.length <= 0) {
            return false;
        }
        for (byte aByte : header) {
            int v = aByte & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        // 47494638 is hex number for string "gif"
        return stringBuilder.toString().toUpperCase().contains("47494638");
    }

    private void errorState(String msg) {
        HSLog.w(msg);
        finish();
    }

    private void init() {
        Intent intent = getIntent();
        if (intent.getExtras() == null) {
            errorState("intent.getExtras() == null");
            return;
        }
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(INTENT_KEY_WALLPAPER_URI)) {
                loadFromGallery();
            }
        }
        hideResetBtn();
    }

    private void loadFromGallery() {
        Threads.postOnThreadPoolExecutor(() -> {
            Uri selectedImage = ((Intent) (getIntent().getParcelableExtra(INTENT_KEY_WALLPAPER_URI))).getData();
            if (selectedImage == null) {
                showError();
                errorState("wallpaper uri not passed correctly");
                return;
            }

            InputStream imageStream;
            byte[] type = new byte[4];
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showError();
                errorState("local wallpaper image file not found");
                return;
            } catch (SecurityException e) {
                e.printStackTrace();
                showError();
                errorState("cannot read local wallpaper image file due to device security model");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showError();
                errorState("other errors when opening local wallpaper image file");
                return;
            }
            try {
                //noinspection ConstantConditions
                int size = imageStream.read(type);
                if (size != 4) {
                    errorState("local wallpaper Cannot get 4 bytes file header.");
                    throw new IOException("Cannot get 4 bytes file header.");
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                showError();
                errorState("local wallpaper IOException | NullPointerException e");
                return;
            }

            if (isGif(type)) {
                //showError(R.string.local_wallpaper_pick_error_gif_not_supported);
                errorState("local wallpaper image file is gif");
                return;
            }

            String fileName = Utils.md5(selectedImage.toString());
            File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);

            if (!Utils.saveInputStreamToFile(type, imageStream, storedWallpaper)) {
                showError();
                errorState("local wallpaper file save failed");
                return;
            }
            final String storedPath = storedWallpaper.getAbsolutePath();
            Threads.postOnMainThread(() -> {

                if (BugleActivityUtil.isDestroyed(WallpaperEditActivity.this)) {
                    return;
                }
                mWallpaperPath = storedPath;
                initData();
            });
        });

    }

    private void saveWallpaperToLocal(Bitmap bitmap) {
        String fileName = Utils.md5(mWallpaperPath + "_1.png");
        File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);

        FileOutputStream out;
        try {
            out = new FileOutputStream(storedWallpaper);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String storedPath = storedWallpaper.getAbsolutePath();
        WallpaperManager.setWallpaperPath(storedPath);
    }

    private void initData() {
        String url;
        url = Uri.fromFile(new File(mWallpaperPath)).toString();
        mWallpaperView.setEnabled(false);
        GlideApp.with(WallpaperEditActivity.this).asBitmap().load(url).into(new ImageViewTarget<Bitmap>(mWallpaperView) {

            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                mWallpaperView.setImageBitmap(resource);
                view.postDelayed(() -> loadBitmapComplete(view, resource), 10);
            }

            @Override
            protected void setResource(@Nullable Bitmap resource) {

            }

            @Override
            public void onLoadFailed(Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                HSLog.w(TAG, "Load local image failed");
                finish();
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                super.onLoadCleared(placeholder);
                HSLog.w(TAG, "Load local image cancelled, finish");
                finish();
            }
        });
        refreshButtonState();
    }

    private void loadBitmapComplete(View view, Bitmap bitmap) {
        HSLog.i("onLoadingComplete " + " " + bitmap + " " + view.getWidth() + " " + view.getMeasuredWidth());
        mBitmap = bitmap;
        mCropOverlayView.setVisibility(View.VISIBLE);
        centerInside();
        updateOverlayView();
        mWallpaperView.setEnabled(true);
        bindEvents();
    }

    private void centerInside() {
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mOverlayBounds = new RectF(0, 0, mWallpaperView.getWidth(), mWallpaperView.getHeight());
        RectF bitmapRect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        Matrix mCurrentMatrix = new Matrix();
        mCurrentMatrix.setRectToRect(bitmapRect, mOverlayBounds, Matrix.ScaleToFit.CENTER);
        mCurrentMatrix.mapRect(mOverlayBounds, bitmapRect);
        mWallpaperView.setImageMatrix(mCurrentMatrix);
        initOverlayView();
        HSLog.i(mOverlayBounds + "");
    }

    private void initOverlayView() {
        //初始化配置
        mCropOverlayView.setInitialAttributeValues(new CropImageOptions());
        //GuideLine显示状态
        mCropOverlayView.setGuidelines(CropImageOptions.Guidelines.OFF);
        //Crop 形状
        mCropOverlayView.setCropShape(CropImageOptions.CropShape.RECTANGLE);
        //是否多点触控
        mCropOverlayView.setMultiTouchEnabled(true);

    }

    private void updateOverlayViewBounds() {
        float[] bounds = new float[8];
        mapRectToPoint(mOverlayBounds, bounds);
        mCropOverlayView.setBounds(bounds, (int) mOverlayBounds.right, (int) mOverlayBounds.bottom);
        mCropOverlayView.setCropWindowLimits(mOverlayBounds.right, mOverlayBounds.bottom, 2, 2);
    }

    private void updateOverlayView() {
        updateOverlayViewBounds();
        PointF ratio = getRatio();
        mCropOverlayView.setAspectRatio((int) ratio.x, (int) ratio.y);
        mCropOverlayView.setCropWindowRect(getOverlayViewRect());
        mCropOverlayView.invalidate();
    }

    private RectF getOverlayViewRect() {
        PointF point = getRatio();
        float k = point.x / point.y;
        float width = mOverlayBounds.width();
        float height = mOverlayBounds.height();
        float overlayHeight;
        float overlayWidth;

        if (height > width / k) {
            overlayWidth = width;
            overlayHeight = width / k;
        } else {
            overlayHeight = height;
            overlayWidth = height * k;
        }

        RectF overlayRect = new RectF(0, 0, overlayWidth, overlayHeight);
        HSLog.i("overlayRect " + overlayRect);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(overlayRect, mOverlayBounds, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(overlayRect);
        return overlayRect;
    }

    private static void mapRectToPoint(RectF rect, float[] points) {
        points[0] = rect.left;
        points[1] = rect.top;
        points[2] = rect.right;
        points[3] = rect.top;
        points[4] = rect.right;
        points[5] = rect.bottom;
        points[6] = rect.left;
        points[7] = rect.bottom;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wallpaper_edit_reset_button:
                reset();
                break;
            case R.id.wallpaper_edit_apply_button:
                apply();
                finish();
                BugleAnalytics.logEvent("SMS_ChatBackground_CutPage_Applied");
                break;
            case R.id.wallpaper_view_return:
                cancel();
                break;
        }
    }

    private void calculateCropParams(Matrix matrix) {
        RectF viewRect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        matrix.mapRect(viewRect);
        RectF cropOverlayRectF = mCropOverlayView.getCropWindowRect();
        matrix.getValues(mMatrixValues);
        float scale = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X] * mMatrixValues[Matrix.MSCALE_Y] -
                mMatrixValues[Matrix.MSKEW_X] * mMatrixValues[Matrix.MSKEW_Y]);
        //Anticlockwise rotation
        if (mMatrixValues[Matrix.MSCALE_X] > 0) {
            //0try
            mStartX = Math.abs(cropOverlayRectF.left - viewRect.left) / scale;
            mStartY = Math.abs(cropOverlayRectF.top - viewRect.top) / scale;

            mWidth = cropOverlayRectF.width() / scale;
            mHeight = cropOverlayRectF.height() / scale;
        } else if (mMatrixValues[Matrix.MSCALE_X] < 0) {
            // 180
            mStartX = Math.abs(cropOverlayRectF.right - viewRect.right) / scale;
            mStartY = Math.abs(cropOverlayRectF.bottom - viewRect.bottom) / scale;

            mWidth = cropOverlayRectF.width() / scale;
            mHeight = cropOverlayRectF.height() / scale;
        } else if (mMatrixValues[Matrix.MSKEW_X] > 0) {
            //90
            mStartX = Math.abs(cropOverlayRectF.bottom - viewRect.bottom) / scale;
            mStartY = Math.abs(cropOverlayRectF.left - viewRect.left) / scale;

            mWidth = cropOverlayRectF.height() / scale;
            mHeight = cropOverlayRectF.width() / scale;
        } else if (mMatrixValues[Matrix.MSKEW_X] < 0) {
            // 270
            mStartX = Math.abs(cropOverlayRectF.top - viewRect.top) / scale;
            mStartY = Math.abs(cropOverlayRectF.right - viewRect.right) / scale;

            mWidth = cropOverlayRectF.height() / scale;
            mHeight = cropOverlayRectF.width() / scale;
        }
    }

    private void apply() {
        mApplyButton.setTextColor(0x80ffffff);
        mApplyButton.setClickable(false);
        saveWallpaperToLocal(tryGetWallpaperToSet());
    }

    private void cancel() {
        finish();
    }

    public void reset() {
        updateOverlayView();
        hideResetBtn();
    }

    private void hideResetBtn() {
        mResetBtn.setVisibility(View.INVISIBLE);
    }

    private void showResetBtn() {
        mResetBtn.setVisibility(View.VISIBLE);
    }

    protected Bitmap tryGetWallpaperToSet() {
        calculateCropParams(mWallpaperView.getImageMatrix());
        HSLog.i("mStart X " + mStartX + " mStartY " + mStartY + " mWidth " + mWidth + " mHeight " + mHeight);
        Bitmap bitmap = mBitmap;

        int startX = Math.round(mStartX);
        int startY = Math.round(mStartY);
        int width = Math.round(mWidth);
        int height = Math.round(mHeight);
        if (startX + width > bitmap.getWidth()) {
            // Clamp to avoid out-of-range error due to rounding
            width = bitmap.getWidth() - startX;
        }
        if (startY + height > bitmap.getHeight()) {
            height = bitmap.getHeight() - startY;
        }
        Matrix matrix = new Matrix();
        Bitmap wallpaper;
        try {
            wallpaper = Bitmap.createBitmap(bitmap, startX, startY, width, height, matrix, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return wallpaper;
    }

    protected void refreshButtonState() {
        mApplyButton.setClickable(true);
        mApplyButton.setAlpha(1.0f);
    }

    private PointF getRatio() {
        //Point point = WallpaperUtils.getWindowSize(this);
        PointF pointF = new PointF();
        pointF.x = 1080;
        pointF.y = 1363;
        return pointF;
    }
}
