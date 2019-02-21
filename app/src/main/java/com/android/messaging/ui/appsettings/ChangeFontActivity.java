package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.view.LevelSeekBar;
import com.android.messaging.util.BuglePrefs;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.superapps.util.Toasts;

import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.FontManagerImpl;

/**
 * Created by zhangjie on 2019/2/20.
 */

public class ChangeFontActivity extends BaseActivity implements LevelSeekBar.OnLevelChangeListener{

    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
    private LevelSeekBar mSeekBar;

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
        int level = (int) prefs.getLong("font_scale",4);

        mSeekBar.setOnLevelChangeListener(this);
        mSeekBar.setLevel(level);

        Button fontSmall = findViewById(R.id.btn_small);
        Button fontNormal = findViewById(R.id.btn_normal);
        Button fontBig = findViewById(R.id.btn_big);
        Button fontBiggest = findViewById(R.id.btn_biggest);

        if (isGooglePlayServicesAvailable(ChangeFontActivity.this)){
            fontNormal.setOnClickListener(v -> {
                FontManagerImpl.getInstance().changeTypeFaced("Muli",mListener);
                prefs.putString("font_family","Muli");
            });
            fontSmall.setOnClickListener(v -> {
                FontManagerImpl.getInstance().changeTypeFaced("Cinzel",mListener);
                prefs.putString("font_family","Cinzel");
            });
            fontBig.setOnClickListener(v -> {
                FontManagerImpl.getInstance().changeTypeFaced("Coiny",mListener);
                prefs.putString("font_family","Coiny");
            });
            fontBiggest.setOnClickListener(v -> {
                FontManagerImpl.getInstance().changeTypeFaced("Poppins",mListener);
                prefs.putString("font_family","Poppins");
            });
        } else {
            Toasts.showToast("You need Google service");
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

    private IFontChangeListener mListener = new IFontChangeListener() {
        @Override
        public void onLoadStart(float scale) {
        }

        @Override
        public void onLoadStart(String fontPath) {

        }

        @Override
        public void onLoadSuccess(float scale) {
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

    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onLevelChanged(LevelSeekBar seekBar, int oldLevel, int newLevel, boolean fromUser) {
        float scale = newLevel / 5f;
        prefs.putLong("font_scale", (long) newLevel);
        FontManagerImpl.getInstance().changeFontSize(scale, mListener);
    }
}
