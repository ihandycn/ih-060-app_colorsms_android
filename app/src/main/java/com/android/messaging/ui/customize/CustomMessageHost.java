package com.android.messaging.ui.customize;

import android.support.annotation.ColorInt;

public interface CustomMessageHost {

    void openColorPickerView(@ChooseMessageColorEntryViewHolder.CustomColor int type);

    void closeColorPickerView();

    void previewCustomColor(@ColorInt int color);

    void previewCustomBubbleDrawable(int id);
}
