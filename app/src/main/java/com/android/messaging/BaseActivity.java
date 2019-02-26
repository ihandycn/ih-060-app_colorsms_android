package com.android.messaging;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.messaging.font.MessageFontManager;
import com.android.messaging.util.BuglePrefs;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.IFontChangeActivity;
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

        String fontFamily = BuglePrefs.getApplicationPrefs().getString(FontManager.MESSAGE_FONT_FAMILY,"");
        float scale = MessageFontManager.getFontScale();

        FontManagerImpl.getInstance().changeFontSize(scale , null);
        FontManagerImpl.getInstance().changeTypeFaced(fontFamily, null);

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

    protected void restartBaseActivity(){
        recreate();
    }
}
