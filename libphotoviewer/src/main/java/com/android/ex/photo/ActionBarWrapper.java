package com.android.ex.photo;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import com.superapps.util.Dimensions;

/**
 * Wrapper around {@link ActionBar}.
 */
public class ActionBarWrapper implements ActionBarInterface {

    private final ActionBar mActionBar;
    private TextView mTitle;

    private class MenuVisiblityListenerWrapper implements ActionBar.OnMenuVisibilityListener {

        private final ActionBarInterface.OnMenuVisibilityListener mWrapped;

        public MenuVisiblityListenerWrapper(ActionBarInterface.OnMenuVisibilityListener wrapped) {
            mWrapped = wrapped;
        }

        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
            mWrapped.onMenuVisibilityChanged(isVisible);
        }
    }

    public ActionBarWrapper(ActionBar actionBar, Context context) {
        mActionBar = actionBar;
        int identifier = context.getResources().getIdentifier("photo_view_ic_back", "drawable", context.getPackageName());
        Drawable drawable = context.getResources().getDrawable(identifier);
        drawable.setColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN);
        mActionBar.setHomeAsUpIndicator(drawable);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mTitle = new TextView(context);
        mTitle.setTextColor(0xb3ffffff);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.7f);
        mTitle.setTypeface(Typeface.createFromAsset(context.getApplicationContext().getAssets(),
                "fonts/Custom-Medium.ttf"));
        mTitle.setGravity(Gravity.CENTER);
        mTitle.setPadding(Dimensions.pxFromDp(15), Dimensions.pxFromDp(3), 0, 0);
        mActionBar.setCustomView(mTitle);
    }

    @Override
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        mActionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
    }

    @Override
    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        mActionBar.addOnMenuVisibilityListener(new MenuVisiblityListenerWrapper(listener));
    }

    @Override
    public void setDisplayOptionsShowTitle() {
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
    }

    @Override
    public CharSequence getTitle() {
       return mTitle.getText();
    }

    @Override
    public void setTitle(CharSequence title) {
        //mActionBar.setTitle(title);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        //mActionBar.setSubtitle(subtitle);
        mActionBar.setDisplayShowTitleEnabled(false);
        mTitle.setText(subtitle);
    }

    @Override
    public void show() {
        mActionBar.show();
    }

    @Override
    public void hide() {
        mActionBar.hide();
    }

    @Override
    public void setLogo(Drawable logo) {
        mActionBar.setLogo(logo);
    }

}
