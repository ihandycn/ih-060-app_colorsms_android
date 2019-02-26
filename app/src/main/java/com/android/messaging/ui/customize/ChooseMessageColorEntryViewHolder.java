package com.android.messaging.ui.customize;

import android.content.Context;
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
import com.android.messaging.util.ImageUtils;

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

    private ImageView mBubbleBackgroundColorIncoming;
    private ImageView mBubbleBackgroundColorOutgoing;
    private ImageView mMessageTextColorIncoming;
    private ImageView mMessageTextColorOutgoing;

    private LinearLayout mBubbleBackgroundColorIncomingContainer;
    private LinearLayout mBubbleBackgroundColorOutgoingContainer;
    private LinearLayout mMessageTextColorIncomingContainer;
    private LinearLayout mMessageTextColorOutgoingContainer;

    private Drawable mDefaultPreviewDrawable;

    ChooseMessageColorEntryViewHolder(final Context context) {
        mContext = context;
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

        mBubbleBackgroundColorIncoming = view.findViewById(R.id.bubble_color_incoming);
        mBubbleBackgroundColorOutgoing = view.findViewById(R.id.bubble_color_outgoing);
        mMessageTextColorIncoming = view.findViewById(R.id.text_color_incoming);
        mMessageTextColorOutgoing = view.findViewById(R.id.text_color_outgoing);

        mBubbleBackgroundColorIncomingContainer = view.findViewById(R.id.bubble_color_incoming_container);
        mBubbleBackgroundColorOutgoingContainer = view.findViewById(R.id.bubble_color_outgoing_container);
        mMessageTextColorIncomingContainer = view.findViewById(R.id.text_color_incoming_container);
        mMessageTextColorOutgoingContainer = view.findViewById(R.id.text_color_outgoing_container);

        mBubbleBackgroundColorIncomingContainer.setOnClickListener(this);
        mBubbleBackgroundColorOutgoingContainer.setOnClickListener(this);
        mMessageTextColorIncomingContainer.setOnClickListener(this);
        mMessageTextColorOutgoingContainer.setOnClickListener(this);

        mDefaultPreviewDrawable = mContext.getResources().getDrawable(R.drawable.custom_message_color_default_preview_drawable);
        initAppearance();
        return view;
    }

    void previewCustomColor(@CustomColor int type, int color) {
        switch (type) {
            case BUBBLE_COLOR_INCOMING:
                mBubbleBackgroundColorIncoming.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
            case BUBBLE_COLOR_OUTGOING:
                mBubbleBackgroundColorOutgoing.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
            case TEXT_COLOR_INCOMING:
                mMessageTextColorIncoming.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
            case TEXT_COLOR_OUTGOING:
                mMessageTextColorOutgoing.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                break;
        }
    }


    private void initAppearance() {
        ConversationColors conversationColors = ConversationColors.get();

        mBubbleBackgroundColorIncoming.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getBubbleBackgroundColor(true)));

        mBubbleBackgroundColorOutgoing.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getBubbleBackgroundColor(false)));

        mMessageTextColorIncoming.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getMessageTextColor(true)));

        mMessageTextColorOutgoing.setImageDrawable(ImageUtils
                .getTintedDrawable(mContext, mDefaultPreviewDrawable, conversationColors.getMessageTextColor(false)));
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

}
