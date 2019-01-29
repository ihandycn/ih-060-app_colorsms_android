package org.qcode.fontchange.impl;

import android.widget.TextView;

import org.qcode.fontchange.FontSizeAttr;

/**
 * View设置管理
 * qqliu
 * 2016/10/8.
 */

public class FontViewHelperImpl {

    private TextView mTxtView;

    public FontViewHelperImpl(TextView view) {
        mTxtView = view;
    }

    //=========================interfaces================================//

    public FontViewHelperImpl setFontSize(int fontSize) {
        if(fontSize <= 0) {
            return this;
        }

        FontSizeAttr attr = new FontSizeAttr(fontSize);
        ViewFontTagHelper.setFontAttr(mTxtView, attr);
        return this;
    }

    public void applyFont(String fontPath) {
        FontManagerImpl.getInstance().applyFont(mTxtView);
    }
}
