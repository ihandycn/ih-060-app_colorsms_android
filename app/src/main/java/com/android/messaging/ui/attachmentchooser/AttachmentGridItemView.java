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
package com.android.messaging.ui.attachmentchooser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.AttachmentPreviewFactory;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.Assert;
import com.android.messaging.util.OsUtil;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

/**
 * Shows an item in the attachment picker grid.
 */
@SuppressWarnings("Convert2Lambda")
public class AttachmentGridItemView extends FrameLayout {
    public interface HostInterface {
        boolean isItemSelected(MessagePartData attachment);
        void onItemCheckedChanged(AttachmentGridItemView view, MessagePartData attachment);
        void onItemClicked(AttachmentGridItemView view, MessagePartData attachment);
    }

    @VisibleForTesting
    MessagePartData mAttachmentData;
    private FrameLayout mAttachmentViewContainer;
    private ImageView mCheckBox;
    private HostInterface mHostInterface;

    public AttachmentGridItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAttachmentViewContainer = findViewById(R.id.attachment_container);
        if (OsUtil.isAtLeastL()){
            ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Dimensions.pxFromDp(3.3f));
                }
            };
            mAttachmentViewContainer.setClipToOutline(true);
            mAttachmentViewContainer.setOutlineProvider(viewOutlineProvider);
        }
        mCheckBox = findViewById(R.id.checkbox);
        mCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mHostInterface.onItemCheckedChanged(AttachmentGridItemView.this, mAttachmentData);
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mHostInterface.onItemClicked(AttachmentGridItemView.this, mAttachmentData);
            }
        });
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Enlarge the clickable region for the checkbox.
                final int touchAreaIncrease = getResources().getDimensionPixelOffset(
                        R.dimen.attachment_grid_checkbox_area_increase);
                final Rect region = new Rect();
                mCheckBox.getHitRect(region);
                region.inset(-touchAreaIncrease, -touchAreaIncrease);
                setTouchDelegate(new TouchDelegate(region, mCheckBox));
            }
        });
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // The grid view auto-fits the columns, so we want to let the height match the width
        // to make the attachment preview square.
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public void bind(final MessagePartData attachment, final HostInterface hostInterface) {
        Assert.isTrue(attachment.isAttachment());
        mHostInterface = hostInterface;
        updateSelectedState();
        if (mAttachmentData == null || !mAttachmentData.equals(attachment)) {
            mAttachmentData = attachment;
            updateAttachmentView();
        }
    }

    public void updateSelectedState() {
       // mCheckBox.setChecked(mHostInterface.isItemSelected(mAttachmentData));
        if (mHostInterface.isItemSelected(mAttachmentData)) {
            mCheckBox.setImageResource(R.drawable.gallery_select_icon);
            mCheckBox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    PrimaryColors.getPrimaryColor(), 0, 5,
                    Color.WHITE, Dimensions.pxFromDp(10), false, false));
        } else {
            mCheckBox.setImageDrawable(null);
            mCheckBox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    HSApplication.getContext().getResources().getColor(R.color.black_20_transparent),
                    0, 5, Color.WHITE,
                    Dimensions.pxFromDp(10), false, false));
        }
    }

    private void updateAttachmentView() {
        mAttachmentViewContainer.removeAllViews();
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View attachmentView = AttachmentPreviewFactory.createAttachmentPreview(inflater,
                mAttachmentData, mAttachmentViewContainer,
                AttachmentPreviewFactory.TYPE_CHOOSER_GRID, true /* startImageRequest */, null);
        mAttachmentViewContainer.addView(attachmentView);
    }
}
