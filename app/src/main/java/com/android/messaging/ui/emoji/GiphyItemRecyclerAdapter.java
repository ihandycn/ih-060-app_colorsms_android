package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.GiphyListManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.ui.emoji.utils.GiphyListManager.BUCKET_COUNT;

public class GiphyItemRecyclerAdapter extends RecyclerView.Adapter<GiphyItemRecyclerAdapter.ViewHolder> {

    private List<GiphyInfo> mDataList = new ArrayList<>(20);
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;


    private int mOffset;
    private String mCategory;

    private GPHApi mClient = new GPHApiClient("6eDzBQcmlIqYEtuulH1o3TvQja0oLnBs");

    GiphyItemRecyclerAdapter(EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener, String category) {
        mOnEmojiClickListener = emojiClickListener;
        mCategory = category;
        GiphyListManager.getInstance().getGiphyList(mCategory, 0, giphyList -> updateData(giphyList));
    }

    void loadMore() {
        mOffset += BUCKET_COUNT;
        GiphyListManager.getInstance().getGiphyList(mCategory, mOffset, giphyList -> updateData(giphyList));
    }

    void updateData(List<Media> list) {
        if (list.isEmpty()) {
            return;
        }
        int preCount = mDataList.size();
        int totalCount = list.size();
        for (int i = preCount; i < totalCount; i++) {
            GiphyInfo giphyInfo = new GiphyInfo();
            Image image = list.get(i).getImages().getFixedWidth();
            giphyInfo.mFixedWidthGifUrl = image.getGifUrl();
            giphyInfo.mGifOriginalWidth = image.getWidth();
            giphyInfo.mGifOriginalHeight = image.getHeight();
            mDataList.add(giphyInfo);
        }
        notifyItemRangeInserted(preCount, totalCount - preCount);
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

                GiphyInfo info = mDataList.get(holder.getAdapterPosition());
                info.mStartRect = rect;
                info.mGifWidth = v1.getWidth();
                info.mGifHeight = v1.getHeight();

                mOnEmojiClickListener.gifClick(info);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Context context = holder.itemView.getContext();

        int width = mDataList.get(position).mGifOriginalWidth;
        int height = mDataList.get(position).mGifOriginalHeight;
        holder.mGif.getLayoutParams().height = (Dimensions.getPhoneWidth(context) - Dimensions.pxFromDp(23))
                * height / width / 2;
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new FitCenter(),
                new RoundedCorners((int) context.getResources().getDimension(R.dimen.giphy_list_item_radius)));

        GiphyInfo giphyInfo = mDataList.get(position);
        GlideApp.with(context)
                .asGif()
                .placeholder(R.drawable.gif_item_placehoder)
                .load(giphyInfo.mFixedWidthGifUrl)
                .apply(requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
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

