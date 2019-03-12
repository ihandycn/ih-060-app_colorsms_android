package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.ihs.app.framework.HSApplication;
import com.superapps.font.FontUtils;
import com.superapps.util.Dimensions;

public class ChooseFontItem extends LinearLayout {

    private String fontFamily;
    private RadioButton radioBtn;
    private Drawable mCheckedDrawable, mUncheckedDrawable;
    private boolean isSelected;

    public ChooseFontItem(Context context) {
        this(context, null);
    }

    public ChooseFontItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChooseFontItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void loadFont() {
        radioBtn = findViewById(R.id.dialog_select_btn);

        TypedValue out = new TypedValue();
        getResources().getValue(R.dimen.light_dialog_choice_text, out, true);
        float alpha = out.getFloat();
        radioBtn.setAlpha(alpha);
        radioBtn.setText(fontFamily);
        radioBtn.setClickable(false);
        radioBtn.setTypeface(FontUtils.getTypefaceByName(fontFamily, FontUtils.SEMI_BOLD));
        refreshRadioStatus();
    }

    public void refreshRadioStatus() {
        if (isSelected) {
            toggleRadioButtonColorFilter(radioBtn, true);
            radioBtn.setChecked(true);
        } else {
            toggleRadioButtonColorFilter(radioBtn, false);
            radioBtn.setChecked(false);
        }
    }

    public void init() {
        mCheckedDrawable = HSApplication.getContext().getResources().getDrawable(R.drawable.select_icon_click);
        mCheckedDrawable.setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
        mCheckedDrawable.setBounds(0, 0, mCheckedDrawable.getIntrinsicWidth(), mCheckedDrawable.getIntrinsicHeight());
        mUncheckedDrawable = HSApplication.getContext().getResources().getDrawable(R.drawable.select_icon_normal);
        mUncheckedDrawable.setBounds(0, 0, mUncheckedDrawable.getIntrinsicWidth(), mUncheckedDrawable.getIntrinsicHeight());
    }

    private void toggleRadioButtonColorFilter(RadioButton btn, boolean isChecked) {
        if (isChecked) {
            if (Dimensions.isRtl()) {
                btn.setCompoundDrawables(null, null, mCheckedDrawable, null);
            } else {
                btn.setCompoundDrawables(mCheckedDrawable, null, null, null);
            }
        } else {
            if (Dimensions.isRtl()) {
                btn.setCompoundDrawables(null, null, mUncheckedDrawable, null);
            } else {
                btn.setCompoundDrawables(mUncheckedDrawable, null, null, null);
            }
        }
    }

}
