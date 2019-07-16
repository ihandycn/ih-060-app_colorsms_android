/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.mainpage.ChatListDrawableManager;
import com.android.messaging.ui.view.MessagesTextView;

/**
 * A common reusable view that shows a hint image and text for an empty list view.
 */
public class ListEmptyView extends LinearLayout {
    private ImageView mEmptyImageHint;
    private MessagesTextView mEmptyTextHint;
    private LottieAnimationView mLoadingAnimationView;

    public ListEmptyView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEmptyImageHint = (ImageView) findViewById(R.id.empty_image_hint);
        mEmptyTextHint = (MessagesTextView) findViewById(R.id.empty_text_hint);
        mEmptyTextHint.setTextColor(ConversationColors.get().getListTitleColor());
        ChatListDrawableManager.changeViewColorIfNeed(mEmptyTextHint);
        mLoadingAnimationView = (LottieAnimationView) findViewById(R.id.loading_image_hint);
    }

    public void setImageHint(final int resId) {
        mEmptyImageHint.setImageResource(resId);
    }

    public void setTextHint(final int resId) {
        mEmptyTextHint.setText(getResources().getText(resId));
    }

    public void setTextHint(final CharSequence hintText) {
        mEmptyTextHint.setText(hintText);
    }

    public void setIsImageVisible(final boolean isImageVisible) {
        mEmptyImageHint.setVisibility(isImageVisible ? VISIBLE : GONE);
    }

    public void setIsLoadingAnimationVisible(final boolean isLoadingAnimationVisible) {
        if (isLoadingAnimationVisible) {
            setIsImageVisible(false);
            mLoadingAnimationView.setVisibility(VISIBLE);
            mLoadingAnimationView.playAnimation();
        } else {
            mLoadingAnimationView.setVisibility(GONE);
            mLoadingAnimationView.cancelAnimation();
        }
    }

    public void setIsVerticallyCentered(final boolean isVerticallyCentered) {
        int gravity =
                isVerticallyCentered ? Gravity.CENTER : Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        ((LinearLayout.LayoutParams) mEmptyImageHint.getLayoutParams()).gravity = gravity;
        ((LinearLayout.LayoutParams) mEmptyTextHint.getLayoutParams()).gravity = gravity;
        getLayoutParams().height =
                isVerticallyCentered ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
        requestLayout();
    }
}
