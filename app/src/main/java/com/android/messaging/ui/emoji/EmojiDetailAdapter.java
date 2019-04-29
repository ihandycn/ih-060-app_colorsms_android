package com.android.messaging.ui.emoji;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.view.MessagesTextView;

public class EmojiDetailAdapter extends BaseStickerItemRecyclerAdapter {

    private static final int TYPE_HEADER_VIEW = 0;
    private static final int TYPE_NORMAL = 1;

    private EmojiPackageInfo mEmojiPackageInfo;
    private String mSource;

    EmojiDetailAdapter(EmojiPackageInfo emojiPackageInfo, String source) {
        this.mEmojiPackageInfo = emojiPackageInfo;
        this.mSource = source;
    }

    @Override
    public RecyclerView.ViewHolder createItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER_VIEW) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_detail_header_layout, parent, false));
        } else {
            return new StickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item_layout, parent, false));
        }
    }

    @Override
    public void bindItemViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            setupImageAndText(((HeaderViewHolder) holder).image, ((HeaderViewHolder) holder).text);
            setupGetButton(((HeaderViewHolder) holder).button);
        } else if (holder instanceof StickerViewHolder) {
            BaseEmojiInfo info = mEmojiPackageInfo.mEmojiInfoList.get(position - 1);
            if (info instanceof StickerInfo) {
                bindStickerInfo((StickerViewHolder) holder, (StickerInfo) info);
            }
        }
    }

    private void setupImageAndText(ImageView image, MessagesTextView text) {
        GlideApp.with(image)
                .asBitmap()
                .load(mEmojiPackageInfo.mBannerUrl)
                .placeholder(BackgroundDrawables.createBackgroundDrawable(0xffeaeaea, 0, false))
                .error(BackgroundDrawables.createBackgroundDrawable(0xffeaeaea, 0, false))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(image);
        text.setText(mEmojiPackageInfo.mName);
    }

    private void setupGetButton(MessagesTextView getBtn) {
        Resources res = getBtn.getResources();
        if (EmojiManager.isTabSticker(mEmojiPackageInfo.mName)) {
            getBtn.setText(res.getString(R.string.sms_emoji_added));
            getBtn.setTextColor(0xFFFFFFFF);
            getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(20), true));
        } else {
            getBtn.setTextColor(0xFF333333);
            getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFF4BE3E, 0xFFDAA017, Dimensions.pxFromDp(20), false, true));
            getBtn.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(mSource)) {
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_StoreDetail_Get", true, "name", mEmojiPackageInfo.mName, "type", mSource);
                }
                getBtn.setOnClickListener(null);
                getBtn.setText(res.getString(R.string.sms_emoji_added));
                getBtn.setTextColor(0xFFFFFFFF);
                getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(20), true));
                EmojiManager.addTabSticker(mEmojiPackageInfo.mName);
                HSBundle bundle = new HSBundle();
                bundle.putString(EmojiStoreFragment.NOTIFICATION_BUNDLE_ITEM_NAME, mEmojiPackageInfo.mName);
                HSGlobalNotificationCenter.sendNotification(EmojiStoreFragment.NOTIFICATION_REFRESH_ITEM_STATUS, bundle);

                HSBundle packageBundle = new HSBundle();
                packageBundle.putObject(EmojiPickerFragment.NOTIFICATION_BUNDLE_PACKAGE_INFO, mEmojiPackageInfo);
                HSGlobalNotificationCenter.sendNotification(EmojiPickerFragment.NOTIFICATION_ADD_EMOJI_FROM_STORE, packageBundle);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mEmojiPackageInfo.mEmojiInfoList.size() + 1;
    }

    @Override public int getItemViewType(int position) {
        return isHeader(position) ? TYPE_HEADER_VIEW : TYPE_NORMAL;
    }

    boolean isHeader(int position) {
        return position == 0;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        MessagesTextView text;
        MessagesTextView button;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.emoji_detail_image);
            text = itemView.findViewById(R.id.emoji_detail_text);
            button = itemView.findViewById(R.id.emoji_detail_get_btn);
        }
    }
}
