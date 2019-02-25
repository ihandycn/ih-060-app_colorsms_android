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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.ImageUtils;

/**
 * A singleton cache that holds tinted drawable resources for displaying messages, such as
 * message bubbles, audio attachments etc.
 */
public class ConversationDrawables {
    private static ConversationDrawables sInstance;

    // Cache the color filtered bubble drawables so that we don't need to create a
    // new one for each ConversationMessageView.
    private Drawable mIncomingBubbleDrawable;
    private Drawable mOutgoingBubbleDrawable;
    private Drawable mIncomingErrorBubbleDrawable;
    private Drawable mIncomingBubbleNoArrowDrawable;
    private Drawable mOutgoingBubbleNoArrowDrawable;
    private Drawable mIncomingAudioPlayButtonDrawable;
    private Drawable mIncomingAudioPauseButtonDrawable;
    private Drawable mOutgoingAudioPlayButtonDrawable;
    private Drawable mOutgoingAudioPauseButtonDrawable;
    private Drawable mIncomingAudioProgressBackgroundDrawable;
    private Drawable mOutgoingAudioProgressBackgroundDrawable;
    private Drawable mAudioProgressForegroundDrawable;
    private Drawable mFastScrollThumbDrawable;
    private Drawable mFastScrollThumbPressedDrawable;
    private Drawable mFastScrollPreviewDrawableLeft;
    private Drawable mFastScrollPreviewDrawableRight;
    private final Context mContext;
    private int mOutgoingBubbleColor;
    private int mIncomingBubbleColor;
    private int mIncomingErrorBubbleColor;
    private int mIncomingSelectedBubbleColor;
    private int mOutgoingSelectedBubbleColor;
    private int mThemeColor;

    public static ConversationDrawables get() {
        if (sInstance == null) {
            sInstance = new ConversationDrawables(Factory.get().getApplicationContext());
        }
        return sInstance;
    }

    private ConversationDrawables(final Context context) {
        mContext = context;
        // Pre-create all the drawables.
        updateDrawables();
    }

    public int getConversationThemeColor() {
        return mThemeColor;
    }

    public void updateDrawables() {
        final Resources resources = mContext.getResources();

        mIncomingBubbleDrawable = resources.getDrawable(R.drawable.message_bubble_incoming_new);
        mIncomingBubbleNoArrowDrawable =
                resources.getDrawable(R.drawable.message_bubble_incoming_no_arrow);
        mIncomingErrorBubbleDrawable = resources.getDrawable(R.drawable.msg_bubble_error);
        mOutgoingBubbleDrawable = resources.getDrawable(R.drawable.message_bubble_outgoing_new);
        mOutgoingBubbleNoArrowDrawable =
                resources.getDrawable(R.drawable.message_bubble_outgoing_no_arrow);
        mIncomingAudioPlayButtonDrawable = resources.getDrawable(R.drawable.ic_audio_play_incoming);
        mIncomingAudioPauseButtonDrawable = resources.getDrawable(R.drawable.ic_audio_pause_incoming);
        mOutgoingAudioPlayButtonDrawable = resources.getDrawable(R.drawable.ic_audio_play_outgoing);
        mOutgoingAudioPauseButtonDrawable = resources.getDrawable(R.drawable.ic_audio_pause_outgoing);
        mIncomingAudioProgressBackgroundDrawable =
                resources.getDrawable(R.drawable.audio_progress_bar_background_incoming);
        mOutgoingAudioProgressBackgroundDrawable =
                resources.getDrawable(R.drawable.audio_progress_bar_background_outgoing);
        mAudioProgressForegroundDrawable =
                resources.getDrawable(R.drawable.audio_progress_bar_progress);
        mFastScrollThumbDrawable = resources.getDrawable(R.drawable.fastscroll_thumb);
        mFastScrollThumbPressedDrawable =
                resources.getDrawable(R.drawable.fastscroll_thumb_pressed);
        mFastScrollPreviewDrawableLeft =
                resources.getDrawable(R.drawable.fastscroll_preview_left);
        mFastScrollPreviewDrawableRight =
                resources.getDrawable(R.drawable.fastscroll_preview_right);
        mOutgoingBubbleColor = PrimaryColors.getPrimaryColor();
        mIncomingBubbleColor = resources.getColor(R.color.message_bubble_color_incoming);
        mIncomingErrorBubbleColor =
                resources.getColor(R.color.message_error_bubble_color_incoming);
        mIncomingSelectedBubbleColor = resources.getColor(R.color.message_bubble_color_selected_incoming);
        float[] hsb = new float[3];
        Color.RGBToHSV(Color.red(mOutgoingBubbleColor), Color.green(mOutgoingBubbleColor),
                Color.blue(mOutgoingBubbleColor), hsb);
        hsb[2] /= 1.2f;
        mOutgoingSelectedBubbleColor = Color.HSVToColor(hsb);
        mThemeColor = PrimaryColors.getPrimaryColor();
    }

    public Drawable getBubbleDrawable(final boolean selected, final boolean incoming,
                                      final boolean needArrow, final boolean isError) {
        final Drawable protoDrawable;
        if (needArrow) {
            if (incoming) {
                protoDrawable = isError && !selected ?
                        mIncomingErrorBubbleDrawable : mIncomingBubbleDrawable;
            } else {
                protoDrawable = mOutgoingBubbleDrawable;
            }
        } else if (incoming) {
            protoDrawable = mIncomingBubbleNoArrowDrawable;
        } else {
            protoDrawable = mOutgoingBubbleNoArrowDrawable;
        }

        int color;
        if (incoming) {
            if (isError) {
                color = mIncomingErrorBubbleColor;
            } else if (selected) {
                color = mIncomingSelectedBubbleColor;
            } else {
                color = mIncomingBubbleColor;
            }
        } else {
            if (selected) {
                color = mOutgoingSelectedBubbleColor;
            } else {
                color = mOutgoingBubbleColor;
            }
        }

        return ImageUtils.getTintedDrawable(mContext, protoDrawable, color);
    }

    private int getAudioButtonColor(final boolean incoming) {
        return incoming ? mThemeColor : 0xffffffff;
    }

    public Drawable getPlayButtonDrawable(final boolean incoming) {
        return incoming ? mIncomingAudioPlayButtonDrawable : mOutgoingAudioPlayButtonDrawable;
    }

    public Drawable getPauseButtonDrawable(final boolean incoming) {
        return incoming ? mIncomingAudioPauseButtonDrawable : mOutgoingAudioPauseButtonDrawable;
    }

    public Drawable getAudioProgressDrawable(final boolean incoming) {
        return ImageUtils.getTintedDrawable(
                mContext, mAudioProgressForegroundDrawable, getAudioButtonColor(incoming));
    }

    public Drawable getAudioProgressBackgroundDrawable(final boolean incoming) {
        if (incoming) {
            return ImageUtils.getTintedDrawable(
                    mContext, mIncomingAudioProgressBackgroundDrawable, mThemeColor);
        } else {
            return mOutgoingAudioProgressBackgroundDrawable;
        }
    }

    public Drawable getFastScrollThumbDrawable(final boolean pressed) {
        if (pressed) {
            return ImageUtils.getTintedDrawable(mContext, mFastScrollThumbPressedDrawable,
                    mThemeColor);
        } else {
            return mFastScrollThumbDrawable;
        }
    }

    public Drawable getFastScrollPreviewDrawable(boolean positionRight) {
        Drawable protoDrawable = positionRight ? mFastScrollPreviewDrawableRight :
                mFastScrollPreviewDrawableLeft;
        return ImageUtils.getTintedDrawable(mContext, protoDrawable, mThemeColor);
    }
}
