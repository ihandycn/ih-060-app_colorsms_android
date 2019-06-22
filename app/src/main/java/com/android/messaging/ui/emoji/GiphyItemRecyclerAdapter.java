package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.superapps.util.Dimensions;

import java.util.List;

public class GiphyItemRecyclerAdapter extends RecyclerView.Adapter<GiphyItemRecyclerAdapter.ViewHolder> {

    private List<Media> mDataList;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    GiphyItemRecyclerAdapter(EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        mOnEmojiClickListener = emojiClickListener;

        GPHApi client = new GPHApiClient("D765MXypkvjqjXplqFcuhqPLvCvnJCXt");
        client.search("cats", MediaType.gif, 20, 0, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    // Do what you want to do with the error
                } else {
                    if (result.getData() != null) {
                        updateData(result.getData());

                    } else {
                        Log.e("giphy error", "No results found");
                    }
                }
            }
        });
    }

    public void updateData(List<Media> list) {
        mDataList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_giphy_list_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Context context = holder.itemView.getContext();

        int width = mDataList.get(position).getImages().getFixedWidth().getWidth();
        int height = mDataList.get(position).getImages().getFixedWidth().getHeight();
        holder.mGif.getLayoutParams().height = Dimensions.getPhoneWidth(context) * height / width / 2;

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new FitCenter(), new RoundedCorners(Dimensions.pxFromDp(4)));

        GlideApp.with(context)
                .asGif()
                .load(mDataList.get(position).getImages().getFixedWidth().getGifUrl())
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

        public ViewHolder(View itemView) {
            super(itemView);
            mGif = itemView.findViewById(R.id.gif_image_view);
        }
    }

}

