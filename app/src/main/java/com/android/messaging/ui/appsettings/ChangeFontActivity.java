package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.view.LevelSeekBar;
import com.android.messaging.util.BuglePrefs;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.FontManagerImpl;

/**
 * Created by zhangjie on 2019/2/20.
 */

public class ChangeFontActivity extends BaseActivity implements LevelSeekBar.OnLevelChangeListener {

    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
    private LevelSeekBar mSeekBar;
    private boolean mFontChange;
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

        mSeekBar = findViewById(R.id.seek_bar);
        mTextFontFamily = findViewById(R.id.setting_text_font_name);
        mTextFontSize = findViewById(R.id.setting_text_size_info);
        mChangeFontContainer = findViewById(R.id.change_font_container);
        View changeFontItem = findViewById(R.id.change_font_item);

        int level = (int) prefs.getLong("font_scale", 2);
        String fontFamily = prefs.getString("font_family", "Default");

        mTextFontFamily.setText(fontFamily);
        mTextFontSize.setText(mTextSizes[level]);
        mSeekBar.setOnLevelChangeListener(this);
        mSeekBar.setLevel(level);

        changeFontItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open a dialog to choose font
                new ChooseFontDialog(ChangeFontActivity.this, mListener).show();
            }
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
            String fontFamily = prefs.getString("font_family", "Roboto");
            mTextFontFamily.setText(fontFamily);
            mFontChange = true;
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

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
//        return resultCode == ConnectionResult.SUCCESS;
        return true;
    }

    @Override
    public void onLevelChanged(LevelSeekBar seekBar, int oldLevel, int newLevel, boolean fromUser) {
        float scale = getScaleFromLevel(newLevel);
        prefs.putLong("font_scale", (long) newLevel);
        FontManagerImpl.getInstance().changeFontSize(scale, mListener);
        mTextFontSize.setText(mTextSizes[newLevel]);
        mFontChange = true;
        // need modify view height
        mChangeFontContainer.requestLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFontChange = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFontChange) {
            HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
        }
    }

    private float getScaleFromLevel(int level) {
        float scale = 1;
        switch (level) {
            case 0:
                scale = 0.72f;
                break;
            case 1:
                scale = 0.85f;
                break;
            case 2:
                // normal
                scale = 1;
                break;
            case 3:
                scale = 1.15f;
                break;
            case 4:
                scale = 1.32f;
                break;
            case 5:
                scale = 1.52f;
                break;
        }
        return scale;
    }
}
