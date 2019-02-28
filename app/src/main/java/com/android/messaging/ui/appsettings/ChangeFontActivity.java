package com.android.messaging.ui.appsettings;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.font.MessageFontManager;
import com.android.messaging.ui.customize.CustomMessagePreviewView;
import com.android.messaging.ui.view.LevelSeekBar;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;
import com.superapps.view.TypefacedTextView;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.ActivityFontEventHandlerImpl;
import org.qcode.fontchange.impl.FontManagerImpl;

import java.io.File;

public class ChangeFontActivity extends BaseActivity implements LevelSeekBar.OnLevelChangeListener {

    private View mChangeFontContainer;
    private TextView mTextFontFamily;
    private TextView mTextFontSize;
    public static int[] sTextSizeRes = {R.string.setting_text_size_hint_small,
            R.string.setting_text_size_hint_medium_small,
            R.string.setting_text_size_hint_normal,
            R.string.setting_text_size_hint_medium_large,
            R.string.setting_text_size_hint_large,
            R.string.setting_text_size_hint_larger};

    private ActivityFontEventHandlerImpl mFontEventHandler;
    private boolean mFirstTimeApplyFont = true;
    private int mPrefFontLevel = 999, mCurrentFontLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_font);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.change_font));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String bgPath = WallpaperManager.getWallpaperPathByThreadId(null);
        if (!TextUtils.isEmpty(bgPath)) {
            ((ImageView) findViewById(R.id.change_font_bg)).setImageURI(Uri.fromFile(new File(bgPath)));
        }

        CustomMessagePreviewView customMessagePreviewView = findViewById(R.id.message_preview_view);
        customMessagePreviewView.setIsFontPreview();

        LevelSeekBar mSeekBar = findViewById(R.id.seek_bar);
        mTextFontFamily = findViewById(R.id.setting_text_font_name);
        mTextFontSize = findViewById(R.id.setting_text_size_info);
        mChangeFontContainer = findViewById(R.id.change_font_container);
        View changeFontItem = findViewById(R.id.change_font_item);

        mPrefFontLevel = BuglePrefs.getApplicationPrefs().getInt(FontManager.MESSAGE_FONT_SCALE, 2);
        String fontFamily = Preferences.getDefault().getString(TypefacedTextView.MESSAGE_FONT_FAMILY, "Default");

        mTextFontFamily.setText(fontFamily);
        mTextFontSize.setText(getResources().getString(sTextSizeRes[mPrefFontLevel]));
        mSeekBar.setOnLevelChangeListener(this);
        mSeekBar.setLevel(mPrefFontLevel);

        changeFontItem.setOnClickListener(v -> {
            if (!Networks.isNetworkAvailable(-1)) {
                Toasts.showToast(R.string.network_error);
                BugleAnalytics.logEvent("Customize_TextFont_Click", true, "request", "fail");
            } else {
                // open a dialog to choose font
                new ChooseFontDialog(ChangeFontActivity.this, mListener).show();
                BugleAnalytics.logEvent("Customize_TextFont_Click", true, "request", "success");
            }
        });
        BugleAnalytics.logEvent("Customize_Font_Show", true);

        initFontHandler();
    }

    private void initFontHandler() {
        mFontEventHandler = FontManager.newActivityFontEventHandler()
                .setSupportFontChange(isSupportFontChange())
                .setSwitchFontImmediately(isSwitchFontImmediately())
                .setNeedDelegateViewCreate(false);
        mFontEventHandler.onCreate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFontEventHandler.onDestroy();
        if (mPrefFontLevel != mCurrentFontLevel) {
            BugleAnalytics.logEvent("Customize_TextSize_Change", true, "size", getResources().getString(sTextSizeRes[mCurrentFontLevel]));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mFirstTimeApplyFont) {
            mFontEventHandler.onViewCreated();
            mFirstTimeApplyFont = false;
        }

        mFontEventHandler.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private IFontChangeListener mListener = new IFontChangeListener() {
        @Override
        public void onLoadStart(float scale) {
        }

        @Override
        public void onLoadStart(String fontPath) {
        }

        @Override
        public void onLoadSuccess(float scale) {
            String fontFamily = Preferences.getDefault().getString(TypefacedTextView.MESSAGE_FONT_FAMILY, "Default");
            mTextFontFamily.setText(fontFamily);
        }

        @Override
        public void onLoadSuccess(String fontPath) {

        }

        @Override
        public void onLoadFail(float scale) {
        }

        @Override
        public void onLoadFail(String fontPath) {

        }
    };

    @Override
    public void onLevelChanged(LevelSeekBar seekBar, int oldLevel, int newLevel, boolean fromUser) {
        MessageFontManager.setFontScale(newLevel);
        //FontManagerImpl.getInstance().changeFontSize(MessageFontManager.getFontScaleByLevel(newLevel), null);
        FontManagerImpl.getInstance().changeFontSize(MessageFontManager.getFontScaleByLevel(newLevel), mChangeFontContainer, true);
        mTextFontSize.setText(getResources().getString(sTextSizeRes[newLevel]));
        // need modify view height
        mChangeFontContainer.requestLayout();
        mCurrentFontLevel = newLevel;
    }
}
