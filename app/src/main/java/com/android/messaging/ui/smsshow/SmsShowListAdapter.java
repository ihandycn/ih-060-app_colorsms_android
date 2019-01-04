package com.android.messaging.ui.smsshow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.libwebp.WebpUtils;

import java.util.ArrayList;

public class SmsShowListAdapter extends RecyclerView.Adapter<SmsShowListAdapter.ViewHolder> {

    private ArrayList<String> mData = new ArrayList<>(20);
    private Context mContext;

    private static final String[] COLORS = new String[]{
            "#ff9af6e1",
            "#fffae997",
            "#ffa4ffb1",
            "#ffffb7a4",
            "#ffa4efff",
            "#ffa4c0ff",
    };

    private int mSelectedPosition = 2;

    public SmsShowListAdapter(Context context) {
        mContext = context;
        for (int i = 0; i < 16; i++) {
            mData.add(WebpUtils.getWebpPath("boost_anim_" + i + ".webp"));
        }
    }

    public SmsShowListAdapter(ArrayList<String> data) {
        mData = data;
    }

    public void updateData(ArrayList<String> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setSelected(int position) {
        if (mSelectedPosition == position) {
            return;
        }

        int preSelectedPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(preSelectedPosition);
        notifyItemChanged(mSelectedPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sms_show_list_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.mSmsShowImage.setOnClickListener(v1 -> {
            // start activity
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlideApp.with(mContext).load(getThemePreviewDrawable(position)).into(holder.mSmsShowImage);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private Drawable getThemePreviewDrawable(int position) {
        int colorIndex = position % COLORS.length;
        return new ColorDrawable(Color.parseColor(COLORS[colorIndex]));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mSmsShowImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mSmsShowImage = itemView.findViewById(R.id.sms_show_image);
        }
    }


}
