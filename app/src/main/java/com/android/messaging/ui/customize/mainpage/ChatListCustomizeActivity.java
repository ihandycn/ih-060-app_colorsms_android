package com.android.messaging.ui.customize.mainpage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;

public class ChatListCustomizeActivity extends BaseActivity implements INotificationObserver {
    public static final String CHAT_LIST_WALLPAPER_CHANGED = "chat_list_wallpaper_changed";
    public static final String BUNDLE_KEY_WALLPAPER_PATH = "wallpaper_path";
    // this key is just for event
    public static final String PREF_KEY_EVENT_CHANGE_COLOR_TYPE = "pref_key_event_change_color_type";

    public static final int REQUEST_CODE_PICK_WALLPAPER = 2;

    private ImageView mCustomBackground;
    private ImageView mNavigationIcon;
    private View mBgMaskView;
    private ChatListCustomizeControlView mControlView;
    private Group mThemeViewGroup;
    private TextView mTitleView;
    private ChatListItemListView mListView;
    private String mCurrentSelectedWallpaperPath;
    private int mCurrentSelectedColor;
    private int mCurrentRecommendColor;
    private String mPreviousPath;
    private boolean mUseThemeColor;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_customize_activity);

        initView();

        HSGlobalNotificationCenter.addObserver(CHAT_LIST_WALLPAPER_CHANGED, this);
        BugleAnalytics.logEvent("Customize_ChatList_Show", true, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    private void initView() {
        mCustomBackground = findViewById(R.id.chat_list_full_screen_bg);
        mTitleView = findViewById(R.id.toolbar_title);
        mListView = findViewById(R.id.list_view);
        mBgMaskView = findViewById(R.id.list_bg_mask);
        mControlView = findViewById(R.id.chat_list_customize_view);
        mNavigationIcon = findViewById(R.id.chat_list_navigation_icon);
        initThemeBg();

        mThemeViewGroup = findViewById(R.id.chat_list_theme_group);

        //set custom wallpaper
        mPreviousPath = mCurrentSelectedWallpaperPath = ChatListDrawableManager.getListWallpaperPath();
        if (!TextUtils.isEmpty(mCurrentSelectedWallpaperPath)) {
            mThemeViewGroup.setVisibility(View.INVISIBLE);
            setPreviewImage(mCurrentSelectedWallpaperPath, false);
        }
        //set mask opacity
        mBgMaskView.setAlpha(ChatListDrawableManager.getMaskOpacity());
        //set text color
        if (ChatListDrawableManager.shouldUseThemeColor()) {
            useThemeTextColor();
            mUseThemeColor = true;
        } else {
            mCurrentSelectedColor = ChatListDrawableManager.getTextColor();
            mUseThemeColor = false;
            setTextColor(mCurrentSelectedColor);
        }

        mControlView.addCustomizeChangeListener(new ChatListCustomizeControlView.ChatListCustomizeChangeListener() {
            @Override
            public void onWallpaperChange(String path) {
                mCurrentSelectedWallpaperPath = path;
                if (TextUtils.isEmpty(path)) {
                    mCustomBackground.setVisibility(View.INVISIBLE);
                    mThemeViewGroup.setVisibility(View.VISIBLE);

                    useThemeTextColor();
                    mUseThemeColor = true;
                } else {
                    setPreviewImage(path, true);
                    mUseThemeColor = false;
                }
            }

            @Override
            public void onOpacityChange(float opacity) {
                mBgMaskView.setAlpha(opacity);
            }

            @Override
            public void onTextColorChange(int color) {
                mTitleView.setTextColor(color);
                mListView.changeFontColor(color, color, color);
                mUseThemeColor = false;
                mCurrentSelectedColor = color;
            }

            @Override
            public void onBackBtnClick() {
                handleBackEvent();
            }

            @Override
            public void onApplyBtnClick() {
                Threads.postOnThreadPoolExecutor(() -> {
                    BugleAnalytics.logEvent("Customize_ChatList_Save_Click");
                    //this event must log before apply
                    logApplyEvent();
                    ChatListDrawableManager.saveChatListCustomizeInfo(mCurrentSelectedWallpaperPath, mBgMaskView.getAlpha(), mUseThemeColor, mCurrentSelectedColor);
                    Threads.postOnMainThread(() -> HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE));
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_PICK_WALLPAPER
                && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toasts.showToast("error");
                return;
            }

            Intent intent = ChatListWallpaperEditActivity.getLaunchIntent(this, data);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mControlView.handleBackEvent()) {
            handleBackEvent();
        }
    }

    private void handleBackEvent() {
        if (ChatListDrawableManager.isCustomInfoChanged(mCurrentSelectedWallpaperPath,
                mBgMaskView.getAlpha(), mUseThemeColor, mCurrentSelectedColor)) {
            BaseAlertDialog.Builder builder = new BaseAlertDialog.Builder(this);
            builder.setTitle(R.string.chat_list_confirm_dialog_title);
            builder.setMessage(R.string.chat_list_config_dialog_message);
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> finish());
            builder.setPositiveButton(R.string.bubble_customize_save, ((dialog, which) -> {
                //this event must log before apply
                logApplyEvent();
                ChatListDrawableManager.saveChatListCustomizeInfo(mCurrentSelectedWallpaperPath, mBgMaskView.getAlpha(), mUseThemeColor, mCurrentSelectedColor);
                HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
                BugleAnalytics.logEvent("Customize_ChatList_Alert_Click");
                finish();
            }));
            builder.show();
            BugleAnalytics.logEvent("Customize_ChatList_Alert_Show");
        } else {
            finish();
        }
    }

    private void setTextColor(int textColor) {
        mTitleView.setTextColor(textColor);
        mControlView.changeRecommendTextColor(textColor);
        mListView.changeFontColor(textColor, textColor, textColor);
        mControlView.setTextColorBtnColor(textColor);
        mNavigationIcon.setColorFilter(textColor);
    }

    private void useThemeTextColor() {
        ThemeInfo info = ThemeUtils.getCurrentTheme();
        mControlView.changeRecommendTextColor(Color.parseColor(info.listTitleColor));
        mCurrentSelectedColor = Color.parseColor(info.listTitleColor);
        mTitleView.setTextColor(Color.WHITE);
        mControlView.setTextColorBtnColor(Color.parseColor(info.listTitleColor));
        mListView.changeFontColor(Color.parseColor(info.listTitleColor),
                Color.parseColor(info.listSubtitleColor), Color.parseColor(info.listTimeColor));
        mNavigationIcon.setColorFilter(Color.WHITE);
    }

    private void initThemeBg() {
        ImageView themeWallpaperBackground = findViewById(R.id.chat_list_theme_list_bg);
        ImageView themeToolbarBackground = findViewById(R.id.chat_list_theme_toolbar_bg);

        int statusBarHeight = Dimensions.getStatusBarHeight(ChatListCustomizeActivity.this);
        View toolbarContainer = findViewById(R.id.toolbar_container);
        ViewGroup.LayoutParams layoutParams = toolbarContainer.getLayoutParams();
        layoutParams.height = statusBarHeight + Dimensions.pxFromDp(56);
        toolbarContainer.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams lp = themeToolbarBackground.getLayoutParams();
        lp.height = statusBarHeight + Dimensions.pxFromDp(56);
        themeToolbarBackground.setLayoutParams(lp);

        View toolbarColorBg = findViewById(R.id.chat_list_theme_toolbar_color_bg);
        ViewGroup.LayoutParams lp1 = toolbarColorBg.getLayoutParams();
        lp1.height += statusBarHeight;
        toolbarColorBg.setLayoutParams(lp1);

        View statusBarInset = findViewById(R.id.status_bar_inset);
        layoutParams = statusBarInset.getLayoutParams();
        layoutParams.height = statusBarHeight;
        statusBarInset.setLayoutParams(layoutParams);

        if (ToolbarDrawables.getToolbarBg() != null) {
            themeToolbarBackground.setImageDrawable(ToolbarDrawables.getToolbarBg());
        } else {
            themeToolbarBackground.setImageDrawable(null);
            findViewById(R.id.chat_list_theme_toolbar_color_bg).setBackgroundColor(PrimaryColors.getPrimaryColor());
        }
        themeWallpaperBackground.setImageDrawable(WallpaperDrawables.getConversationListWallpaperDrawable());
    }

    private void setPreviewImage(String path, boolean changeTextColor) {
        String url = Uri.fromFile(new File(path)).toString();
        GlideApp.with(this)
                .asBitmap()
                .load(url)
                .into(new ImageViewTarget<Bitmap>(mCustomBackground) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Threads.postOnMainThread(() -> {
                            if (resource == null) {
                                return;
                            }

                            if (isDestroyed()) {
                                return;
                            }

                            mCustomBackground.setVisibility(View.VISIBLE);
                            mThemeViewGroup.setVisibility(View.INVISIBLE);

                            int height = Dimensions.getPhoneHeight(ChatListCustomizeActivity.this)
                                    - Dimensions.getNavigationBarHeight(ChatListCustomizeActivity.this);
                            int width = Dimensions.getPhoneWidth(ChatListCustomizeActivity.this);

                            int bitmapHeight = resource.getHeight();
                            int bitmapWidth = resource.getWidth();

                            int left = 0;
                            int top = 0;
                            int resizeWidth = bitmapWidth;
                            int resizeHeight = bitmapHeight;

                            if (height * bitmapWidth < width * bitmapHeight) {
                                resizeHeight = (int) (bitmapWidth * height * 1.0f / width);
                                top = bitmapHeight / 2 - resizeHeight / 2;
                            } else {
                                resizeWidth = (int) (bitmapHeight * width * 1.0f / height);
                                left = bitmapWidth / 2 - resizeWidth / 2;
                            }

                            Bitmap resizeBitmap = Bitmap.createBitmap(resource, left, top, resizeWidth, resizeHeight);
                            mCustomBackground.setImageBitmap(resizeBitmap);

                            if (changeTextColor) {
                                Palette.Builder builder = Palette.from(resource);
                                builder.generate(palette -> Threads.postOnMainThread(() -> {
                                    Palette.Swatch vibrant = palette.getDominantSwatch();
                                    if (vibrant != null) {
                                        int textColor = calculateTextColor(vibrant.getRgb());
                                        setTextColor(textColor);
                                        mCurrentSelectedColor = textColor;
                                        mCurrentRecommendColor = textColor;
                                    }
                                }));
                            }
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

    private int calculateTextColor(@ColorInt int bgColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(bgColor, hsv);
        if (hsv[2] < 0.5f * hsv[1] + 0.72) {
            return Color.WHITE;
        } else {
            float[] textHSV = new float[3];
            textHSV[0] = hsv[0];
            textHSV[1] = Math.min(2 * hsv[1] + 0.2f, 1);
            textHSV[2] = Math.max(3 * hsv[2] - 2.16f, 0);
            return Color.HSVToColor(textHSV);
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case CHAT_LIST_WALLPAPER_CHANGED:
                String wallpaperPath = hsBundle.getString(BUNDLE_KEY_WALLPAPER_PATH);
                setPreviewImage(wallpaperPath, true);
                mCurrentSelectedWallpaperPath = wallpaperPath;
                mControlView.onItemSelected(null);
                break;
        }
    }

    private void logApplyEvent() {
        String textColorStr;
        if (mUseThemeColor) {
            textColorStr = "theme";
        } else if (mCurrentRecommendColor == Color.WHITE) {
            textColorStr = "white";
        } else {
            textColorStr = "colored";
        }
        String bgString;
        String path = mCurrentSelectedWallpaperPath;
        if (TextUtils.isEmpty(path)) {
            bgString = "theme";
        } else if (path.contains("list_wallpapers")) {
            bgString = "customize";
        } else {
            bgString = "recommend";
        }
        BugleAnalytics.logEvent("Customize_ChatList_Background_Change",
                true, false, "type", bgString, "text", textColorStr);

        String opacityStr;
        float alpha = mBgMaskView.getAlpha();
        if (alpha < 0.1f) {
            opacityStr = "<10%";
        } else {
            int tensNum = Math.min((int) (alpha * 10), 9);
            opacityStr = tensNum + "0%-" + (tensNum + 1) + "0%";
        }
        BugleAnalytics.logEvent("Customize_ChatList_BackgroundOpacity_Change", "type", opacityStr);

        String colorStr = null;
        if (mUseThemeColor) {
            colorStr = "theme";
        } else if (mCurrentSelectedColor == mCurrentRecommendColor) {
            colorStr = "recommend";
        } else {
            //copy from ChatListChooseColorRecommendAdapter mData
            int[] data = {0xffffffff, 0xffdce3e8, 0xffb8c0c8, 0xff777d88,
                    0xff386ce0, 0xffe1368e, 0xff744fdc, 0xffc94c1b};
            for (int i = 1; i < data.length; i++) {
                if (mCurrentSelectedColor == data[i]) {
                    colorStr = "customize_" + i;
                    break;
                }
            }
        }
        if (colorStr == null) {
            colorStr = "advance";
        }
        Preferences.getDefault().putString(PREF_KEY_EVENT_CHANGE_COLOR_TYPE, colorStr);
        BugleAnalytics.logEvent("Customize_ChatList_TextColor_Change", "type", colorStr);

        StringBuilder changeStr = new StringBuilder();
        if (TextUtils.isEmpty(mCurrentSelectedWallpaperPath)) {
            if (!TextUtils.isEmpty(mPreviousPath)) {
                changeStr.append("background ");
            }
        } else if (!mCurrentSelectedWallpaperPath.equals(mPreviousPath)) {
            changeStr.append("background ");
        }

        if (mUseThemeColor) {
            if (!ChatListDrawableManager.shouldUseThemeColor()) {
                changeStr.append("text ");
            }
        } else {
            if (ChatListDrawableManager.shouldUseThemeColor()) {
                changeStr.append("text ");
            } else if (mCurrentSelectedColor != ChatListDrawableManager.getTextColor()) {
                changeStr.append("text ");
            }
        }

        if (Math.abs(mBgMaskView.getAlpha() - ChatListDrawableManager.getMaskOpacity()) > 0.0000001) {
            changeStr.append("opacity");
        }
        BugleAnalytics.logEvent("Customize_ChatList_Change",
                true, false, "type", changeStr.toString());
    }
}
