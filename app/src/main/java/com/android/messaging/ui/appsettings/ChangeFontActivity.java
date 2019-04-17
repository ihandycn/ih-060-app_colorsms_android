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
import com.android.messaging.font.ChangeFontUtils;
import com.android.messaging.ui.customize.CustomMessagePreviewView;
import com.android.messaging.ui.view.LevelSeekBar;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.superapps.font.FontStyleManager;

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

    private int mPrefFontLevel = 999, mCurrentFontLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_font);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_change_font));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        UiUtils.setTitleBarBackground(toolbar, this);

        ImageView background = findViewById(R.id.change_font_bg);
        WallpaperManager.setWallPaperOnView(background, "");

        CustomMessagePreviewView customMessagePreviewView = findViewById(R.id.message_preview_view);
        customMessagePreviewView.setIsFontPreview();
        customMessagePreviewView.updateBubbleDrawables(null);

        LevelSeekBar mSeekBar = findViewById(R.id.seek_bar);
        mTextFontFamily = findViewById(R.id.setting_text_font_name);
        mTextFontSize = findViewById(R.id.setting_text_size_info);
        mChangeFontContainer = findViewById(R.id.change_font_container);
        View changeFontItem = findViewById(R.id.change_font_item);

        mPrefFontLevel = FontStyleManager.getInstance().getFontScaleLevel();
        String fontFamily = FontStyleManager.getInstance().getFontFamily();

        mTextFontFamily.setText(fontFamily);
        mTextFontSize.setText(getResources().getString(sTextSizeRes[mPrefFontLevel]));
        mSeekBar.setOnLevelChangeListener(this);
        mSeekBar.setLevel(mPrefFontLevel);

        changeFontItem.setOnClickListener(v -> {
            // open a dialog to choose font
            new ChooseFontDialog(ChangeFontActivity.this).show();
            BugleAnalytics.logEvent("Customize_TextFont_Click", true, "request", "success");
        });
        BugleAnalytics.logEvent("Customize_Font_Show", true, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPrefFontLevel != mCurrentFontLevel) {
            String size;
            switch (mCurrentFontLevel) {
                case 0:
                    size = "Smallest";
                    break;
                case 1:
                    size = "Small";
                    break;
                case 3:
                    size = "Medium";
                    break;
                case 4:
                    size = "Large";
                    break;
                case 5:
                    size = "Largest";
                    break;
                default:
                    size = "Default";
            }
            BugleAnalytics.logEvent("Customize_TextSize_Change", true, true, "size", size);
        }
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

    @Override
    public void onLevelChanged(LevelSeekBar seekBar, int oldLevel, int newLevel, boolean fromUser) {
        FontStyleManager.getInstance().setFontScaleLevel(newLevel);
        mTextFontSize.setText(getResources().getString(sTextSizeRes[newLevel]));
        ChangeFontUtils.changeFontSize(mChangeFontContainer, FontStyleManager.getScaleByLevel(newLevel));

        mChangeFontContainer.requestLayout();
        mCurrentFontLevel = newLevel;
    }

    public void onFontChange() {
        mTextFontFamily.setText(FontStyleManager.getInstance().getFontFamily());
        ChangeFontUtils.changeFontTypeface(mChangeFontContainer, FontStyleManager.getInstance().getFontFamily());
    }
}
