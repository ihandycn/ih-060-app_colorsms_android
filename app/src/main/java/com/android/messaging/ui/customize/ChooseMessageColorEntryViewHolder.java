package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ImageUtils;
import com.superapps.util.BackgroundDrawables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_OUTGOING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_OUTGOING;

public class ChooseMessageColorEntryViewHolder extends BasePagerViewHolder implements
        CustomPagerViewHolder, View.OnClickListener {
    @IntDef({BUBBLE_COLOR_INCOMING, BUBBLE_COLOR_OUTGOING,
            TEXT_COLOR_INCOMING, TEXT_COLOR_OUTGOING})
    @Retention(RetentionPolicy.SOURCE)
    @interface CustomColor {
        int BUBBLE_COLOR_INCOMING = 0;
        int BUBBLE_COLOR_OUTGOING = 1;
        int TEXT_COLOR_INCOMING = 2;
        int TEXT_COLOR_OUTGOING = 3;
    }

    private Context mContext;
    private CustomMessageHost mHost;
    private String mConversationId;

    private ImageView mBubbleBackgroundColorIncomingView;
    private ImageView mBubbleBackgroundColorOutgoingView;
    private ImageView mMessageTextColorIncomingView;
    private ImageView mMessageTextColorOutgoingView;

    private LinearLayout mBubbleBackgroundColorIncomingContainer;
    private LinearLayout mBubbleBackgroundColorOutgoingContainer;
    private LinearLayout mMessageTextColorIncomingContainer;
    private LinearLayout mMessageTextColorOutgoingContainer;

    private Drawable mDefaultPreviewDrawable;

    ChooseMessageColorEntryViewHolder(final Context context, String conversationId) {
        mContext = context;
        mConversationId = conversationId;
    }

    public void setHost(CustomMessageHost host) {
        this.mHost = host;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.bubble_customize_color_layout,
                null /* root */,
                false /* attachToRoot */);

        mBubbleBackgroundColorIncomingView = view.findViewById(R.id.bubble_color_incoming);
        mBubbleBackgroundColorOutgoingView = view.findViewById(R.id.bubble_color_outgoing);
        mMessageTextColorIncomingView = view.findViewById(R.id.text_color_incoming);
        mMessageTextColorOutgoingView = view.findViewById(R.id.text_color_outgoing);

        mBubbleBackgroundColorIncomingContainer = view.findViewById(R.id.bubble_color_incoming_container);
        mBubbleBackgroundColorOutgoingContainer = view.findViewById(R.id.bubble_color_outgoing_container);
        mMessageTextColorIncomingContainer = view.findViewById(R.id.text_color_incoming_container);
        mMessageTextColorOutgoingContainer = view.findViewById(R.id.text_color_outgoing_container);

        mBubbleBackgroundColorIncomingContainer.setOnClickListener(this);
        mBubbleBackgroundColorOutgoingContainer.setOnClickListener(this);
        mMessageTextColorIncomingContainer.setOnClickListener(this);
        mMessageTextColorOutgoingContainer.setOnClickListener(this);

        mBubbleBackgroundColorIncomingContainer.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, 0xffd6d7da, 0, false, true));
        mBubbleBackgroundColorOutgoingContainer.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, 0xffd6d7da, 0, false, true));
        mMessageTextColorIncomingContainer.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, 0xffd6d7da, 0, false, true));
        mMessageTextColorOutgoingContainer.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, 0xffd6d7da, 0, false, true));

        mDefaultPreviewDrawable = mContext.getResources().getDrawable(R.drawable.custom_message_color_default_preview_drawable);
        initAppearance();
        return view;
    }

    void previewCustomColor(@CustomColor int type, int color) {
        switch (type) {
            case BUBBLE_COLOR_INCOMING:
                mBubbleBackgroundColorIncomingView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
            case BUBBLE_COLOR_OUTGOING:
                mBubbleBackgroundColorOutgoingView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
            case TEXT_COLOR_INCOMING:
                mMessageTextColorIncomingView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
            case TEXT_COLOR_OUTGOING:
                mMessageTextColorOutgoingView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
        }
    }

    private void initAppearance() {
        ConversationColors conversationColors = ConversationColors.get();

        mBubbleBackgroundColorIncomingView.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getBubbleBackgroundColor(true, mConversationId)));

        mBubbleBackgroundColorOutgoingView.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getBubbleBackgroundColor(false, mConversationId)));

        mMessageTextColorIncomingView.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getMessageTextColor(true, mConversationId)));

        mMessageTextColorOutgoingView.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getMessageTextColor(false, mConversationId)));
    }


    @Override
    public void onClick(View v) {
        if (mBubbleBackgroundColorIncomingContainer.equals(v)) {
            mHost.openColorPickerView(BUBBLE_COLOR_INCOMING);
        } else if (mBubbleBackgroundColorOutgoingContainer.equals(v)) {
            mHost.openColorPickerView(BUBBLE_COLOR_OUTGOING);
        } else if (mMessageTextColorIncomingContainer.equals(v)) {
            mHost.openColorPickerView(TEXT_COLOR_INCOMING);
        } else if (mMessageTextColorOutgoingContainer.equals(v)) {
            mHost.openColorPickerView(TEXT_COLOR_OUTGOING);
        }
    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_tab_color);
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public void onPageSelected() {
        BugleAnalytics.logEvent("Customize_Bubble_Color_Show", false, true);
    }
}
