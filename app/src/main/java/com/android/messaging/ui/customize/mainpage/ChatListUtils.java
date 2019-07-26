package com.android.messaging.ui.customize.mainpage;

import android.graphics.Color;
import android.widget.TextView;

import com.superapps.util.Dimensions;

public class ChatListUtils {
    private static int sShadowOffset = Dimensions.pxFromDp(0.7f);
    private static int sShadowRadius = Dimensions.pxFromDp(1.7f);

    public static void changeTextViewShadow(TextView view) {
        changeTextViewShadow(view, view.getTextColors().getColorForState(view.getDrawableState(), 0) == Color.WHITE);
    }

    public static void changeTextViewShadow(TextView view, boolean addShadow) {
        if (addShadow) {
            view.setShadowLayer(sShadowRadius, sShadowOffset, sShadowOffset, 0x59000000);
        } else {
            view.setShadowLayer(0, 0, 0, 0);
        }
    }
}
