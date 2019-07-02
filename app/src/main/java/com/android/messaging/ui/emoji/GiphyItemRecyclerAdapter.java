package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.GiphyListManager;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.ui.emoji.utils.GiphyListManager.BUCKET_COUNT;

public class GiphyItemRecyclerAdapter extends RecyclerView.Adapter<GiphyItemRecyclerAdapter.ViewHolder> {

    interface OnDataFetchedListener {
        void onFetched();
    }

    private List<BaseEmojiInfo> mDataList = new ArrayList<>(20);
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;
    private OnDataFetchedListener mOnDataFetchedListener;

    private int mOffset;
    private String mCategory;

    private boolean mIsRecentPage;

    GiphyItemRecyclerAdapter(EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener, OnDataFetchedListener onDataFetchedListener, String category) {
        mOnEmojiClickListener = emojiClickListener;
        mOnDataFetchedListener = onDataFetchedListener;
        mCategory = category;
        GiphyListManager.getInstance().getGiphyList(mCategory, 0, giphyList -> updateData(giphyList));
    }

    GiphyItemRecyclerAdapter(EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener, OnDataFetchedListener onDataFetchedListener, List<BaseEmojiInfo> data) {
        mOnEmojiClickListener = emojiClickListener;
        mOnDataFetchedListener = onDataFetchedListener;
        mIsRecentPage = true;
        mDataList = data;
        notifyDataSetChanged();
        if (mOnDataFetchedListener != null) {
            mOnDataFetchedListener.onFetched();
        }
    }

    void loadMore() {
        if (!mIsRecentPage) {
            mOffset += BUCKET_COUNT;
            GiphyListManager.getInstance().getGiphyList(mCategory, mOffset, giphyList -> updateData(giphyList));
        }
    }

    void updateData(List<Media> list) {
        if (list.isEmpty()) {
            return;
        }

        int preCount = mDataList.size();
        int totalCount = list.size();
        for (int i = preCount; i < totalCount; i++) {
            GiphyInfo giphyInfo = new GiphyInfo();

            Image image = list.get(i).getImages().getFixedWidthDownsampled();
            giphyInfo.mFixedWidthGifUrl = image.getGifUrl();
            giphyInfo.mGifOriginalWidth = image.getWidth();
            giphyInfo.mGifOriginalHeight = image.getHeight();

            mDataList.add(giphyInfo);
        }
        notifyItemRangeInserted(preCount, totalCount - preCount);
        if (mOnDataFetchedListener != null) {
            mOnDataFetchedListener.onFetched();
            HSLog.d("GiphyItemPagerAdapter", "mOnDataFetchedListener onFetched: " + mCategory);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_giphy_list_item, parent, false);
        ViewHolder holder = new ViewHolder(v);

        holder.mGif.setOnClickListener(v1 -> {
            if (mOnEmojiClickListener != null) {
                Rect rect = new Rect();
                v1.getGlobalVisibleRect(rect);

                GiphyInfo info = (GiphyInfo) mDataList.get(holder.getAdapterPosition());
                info.mStartRect = rect;
                info.mGifWidth = v1.getWidth();
                info.mGifHeight = v1.getHeight();
                mOnEmojiClickListener.gifClick(info);
                BugleAnalytics.logEvent("SMSEmoji_GIF_Click", "type", mCategory);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Context context = holder.itemView.getContext();
        GiphyInfo giphyInfo = (GiphyInfo) mDataList.get(position);

        int width = giphyInfo.mGifOriginalWidth;
        int height = giphyInfo.mGifOriginalHeight;
        holder.mGif.getLayoutParams().height = (Dimensions.getPhoneWidth(context) - Dimensions.pxFromDp(23))
                * height / width / 2;
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new FitCenter(),
                new RoundedCorners((int) context.getResources().getDimension(R.dimen.giphy_list_item_radius)));

        GlideApp.with(context)
                .asGif()
                .placeholder(R.drawable.gif_item_placehoder)
                .load(giphyInfo.mFixedWidthGifUrl)
                .apply(requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.mGif);
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mGif;

        ViewHolder(View itemView) {
            super(itemView);
            mGif = itemView.findViewById(R.id.gif_image_view);
        }
    }

}

