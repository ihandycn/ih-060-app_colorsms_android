package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.font.MessageFontManager;
import com.android.messaging.ui.view.LevelSeekBar;
import com.android.messaging.util.BuglePrefs;
import com.google.android.gms.common.GoogleApiAvailability;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.FontManagerImpl;

public class ChangeFontActivity extends BaseActivity implements LevelSeekBar.OnLevelChangeListener {

    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
    private View mChangeFontContainer;
    private TextView mTextFontFamily;
    private TextView mTextFontSize;
    private String[] mTextSizes = {"Small", "Medium Small", "Normal", "Medium Large", "Large", "Larger"};

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

        LevelSeekBar mSeekBar = findViewById(R.id.seek_bar);
        mTextFontFamily = findViewById(R.id.setting_text_font_name);
        mTextFontSize = findViewById(R.id.setting_text_size_info);
        mChangeFontContainer = findViewById(R.id.change_font_container);
        View changeFontItem = findViewById(R.id.change_font_item);

        int level = BuglePrefs.getApplicationPrefs().getInt(FontManager.MESSAGE_FONT_SCALE, 2);
        String fontFamily = prefs.getString(FontManager.MESSAGE_FONT_FAMILY, "Default");

        mTextFontFamily.setText(fontFamily);
        mTextFontSize.setText(mTextSizes[level]);
        mSeekBar.setOnLevelChangeListener(this);
        mSeekBar.setLevel(level);

        changeFontItem.setOnClickListener(v -> {
            // open a dialog to choose font
            new ChooseFontDialog(ChangeFontActivity.this, mListener).show();
        });
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
            String fontFamily = prefs.getString(FontManager.MESSAGE_FONT_FAMILY, "Roboto");
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
        FontManagerImpl.getInstance().changeFontSize(MessageFontManager.getFontScaleByLevel(newLevel), null);
        mTextFontSize.setText(mTextSizes[newLevel]);
        // need modify view height
        mChangeFontContainer.requestLayout();
    }
}
