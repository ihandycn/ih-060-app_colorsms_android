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
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.ImageUtils;
import com.superapps.util.BackgroundDrawables;

/**
 * A singleton cache that holds tinted drawable resources for displaying messages, such as
 * message bubbles, audio attachments etc.
 */
public class ConversationDrawables {
    private static ConversationDrawables sInstance;

    // Cache the color filtered bubble drawables so that we don't need to create a
    // new one for each ConversationMessageView.

    private Drawable mThemeIncomingBubbleDrawable;
    private Drawable mThemeOutgoingBubbleDrawable;
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
    private int mIncomingErrorBubbleColor;
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

        updateThemeBubbleDrawables();

        mIncomingBubbleNoArrowDrawable =
                resources.getDrawable(R.drawable.message_bubble_incoming_no_arrow);
        mIncomingErrorBubbleDrawable = resources.getDrawable(R.drawable.msg_bubble_error);
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
        mIncomingErrorBubbleColor =
                resources.getColor(R.color.message_error_bubble_color_incoming);
        mThemeColor = PrimaryColors.getPrimaryColor();
    }

    public Drawable getBubbleDrawable(final boolean selected, final boolean incoming,
                                      final boolean needArrow, final boolean isError,
                                      final String conversationId) {
        final Drawable protoDrawable;
        final Resources resources = mContext.getResources();
        if (needArrow) {
            if (incoming) {
                int incomingdDrawableRes = BubbleDrawables.getSelectedDrawable(false, conversationId);
                Drawable incomingDrawable;
                if (incomingdDrawableRes != -1) {
                    incomingDrawable = resources.getDrawable(incomingdDrawableRes);
                } else {
                    incomingDrawable = mThemeIncomingBubbleDrawable;
                }
                protoDrawable = isError && !selected ? mIncomingErrorBubbleDrawable : incomingDrawable;
            } else {
                int drawableRes = BubbleDrawables.getSelectedDrawable(false, conversationId);

                if (drawableRes != -1) {
                    protoDrawable = resources.getDrawable(drawableRes);
                } else {
                    protoDrawable = mThemeOutgoingBubbleDrawable;
                }
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
                // incoming selected bubble color
                color = ConversationColors.get().getBubbleBackgroundColorDark(true, conversationId);
            } else {
                // incoming bubble color
                color = ConversationColors.get().getBubbleBackgroundColor(true, conversationId);
            }
        } else {
            if (selected) {
                // outgoing selected bubble color
                color = ConversationColors.get().getBubbleBackgroundColorDark(false, conversationId);
            } else {
                // outgoing bubble color
                color = ConversationColors.get().getBubbleBackgroundColor(false, conversationId);
            }
        }

        return ImageUtils.getTintedDrawable(mContext, protoDrawable, color);
    }

    private int getAudioButtonColor(final boolean incoming) {
        return incoming ? mThemeColor : 0xffffffff;
    }

    public void updateThemeBubbleDrawables() {
        mThemeIncomingBubbleDrawable = ThemeUtils.getSelectedDrawable(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()).bubbleIncomingUrl);
        mThemeOutgoingBubbleDrawable = ThemeUtils.getSelectedDrawable(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()).bubbleOutgoingUrl);
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
