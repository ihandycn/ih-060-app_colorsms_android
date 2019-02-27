package com.android.messaging;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.messaging.font.MessageFontManager;
import com.android.messaging.util.BuglePrefs;
import com.superapps.util.Preferences;
import com.superapps.view.TypefacedTextView;

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

        String fontFamily = Preferences.getDefault().getString(TypefacedTextView.MESSAGE_FONT_FAMILY, "Default");
        float scale = MessageFontManager.getFontScale();

        FontManagerImpl.getInstance().changeFontSize(scale, null);
//        if (fontFamily != null && !fontFamily.equals("Default") && !fontFamily.equals("System")) {
//            FontManagerImpl.getInstance().loadAndSetTypeface(fontFamily, null);
//        }
//        initFontHandler();
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
//
//        mFontEventHandler.onDestroy();
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

//        if (mFirstTimeApplyFont) {
//            mFontEventHandler.onViewCreated();
//            mFirstTimeApplyFont = false;
//        }
//
//        mFontEventHandler.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

      //  mFontEventHandler.onWindowFocusChanged(hasFocus);
    }

    protected void restartBaseActivity() {
        recreate();
    }
}
