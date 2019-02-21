package com.android.messaging.ui.appsettings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.FontManagerImpl;

/**
 * Created by zhangjie on 2019/2/20.
 */

public class ChangeFontActivity extends BaseActivity{

    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

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

        // add a dialog for font change
        SeekBar mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float scale = i / 50.0f;
                prefs.putLong("font_scale", (long) i);
                FontManagerImpl.getInstance().changeFontSize(scale, mListener);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Button fontSmall = findViewById(R.id.btn_small);
        Button fontNormal = findViewById(R.id.btn_normal);
        Button fontBig = findViewById(R.id.btn_big);
        Button fontBiggest = findViewById(R.id.btn_biggest);
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
//            Toast.makeText(MainActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadSuccess(String fontPath) {

        }

        @Override
        public void onLoadFail(float scale) {
//            Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadFail(String fontPath) {

        }
    };
}
