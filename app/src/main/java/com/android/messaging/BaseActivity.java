package com.android.messaging;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.messaging.util.BuglePrefs;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.IFontChangeActivity;
import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.impl.ActivityFontEventHandlerImpl;
import org.qcode.fontchange.impl.FontManagerImpl;


/**
 * 所有Activity的父类
 */
public abstract class BaseActivity extends AppCompatActivity
        implements IFontChangeActivity {

    private static final String TAG = "BaseActivity";

    private ActivityFontEventHandlerImpl mFontEventHandler;
    private boolean mFirstTimeApplyFont = true;

    private boolean mIsDestroyingFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FontManagerImpl.getInstance().init(BaseActivity.this);
        long level = BuglePrefs.getApplicationPrefs().getLong("font_scale",2);
        String fontFamily = BuglePrefs.getApplicationPrefs().getString("font_family","");
        float scale = getScaleFromLevel((int) level);

        FontManagerImpl.getInstance().changeFontSize(scale , mListener);
        FontManagerImpl.getInstance().changeTypeFaced(fontFamily, mListener);

        initFontHandler();
    }

    private float getScaleFromLevel(int level) {
        float scale =1;
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

    private void initFontHandler() {
        mFontEventHandler = FontManager.newActivityFontEventHandler()
                .setSupportFontChange(isSupportFontChange())
                .setSwitchFontImmediately(isSwitchFontImmediately())
                .setNeedDelegateViewCreate(false);
        mFontEventHandler.onCreate(this);
    }


    @Override
    public boolean isSupportFontChange() {
        return true;
    }

    @Override
    public boolean isSwitchFontImmediately() {
        return true;
    }

    @Override
    public void handleFontChange() {
    }

    @Override
    protected void onDestroy() {
        mIsDestroyingFlag = true;
        super.onDestroy();

        mFontEventHandler.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ////此通知放在此处，尽量让子类的view都添加到view树内
        if(mFirstTimeApplyFont) {
            mFontEventHandler.onViewCreated();
            mFirstTimeApplyFont = false;
        }

        mFontEventHandler.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mFontEventHandler.onWindowFocusChanged(hasFocus);
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
//            Toast.makeText(BaseActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
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

    protected void restartBaseActivity(){
        recreate();
    }
}
