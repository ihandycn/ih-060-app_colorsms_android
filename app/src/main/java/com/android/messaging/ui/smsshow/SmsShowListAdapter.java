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
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.data.SmsShowListItemData;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.smsshow.SmsShowUtils;
import com.android.messaging.ui.UIIntents;

import java.util.ArrayList;

public class SmsShowListAdapter extends RecyclerView.Adapter<SmsShowListAdapter.ViewHolder> {
    private ArrayList<SmsShowListItemData> mData;
    private Context mContext;

    private static final String[] COLORS = new String[]{
            "#ff9af6e1",
            "#fffae997",
            "#ffa4ffb1",
            "#ffffb7a4",
            "#ffa4efff",
            "#ffa4c0ff",
    };

    private int mSelectedPosition;

    public SmsShowListAdapter(Context context) {
        mContext = context;
        mData = DataModel.get().createSmsShowListData().getData();
        mSelectedPosition = getSelectedPosition();
    }

    public void updateData() {
        mData = DataModel.get().createSmsShowListData().getData();
        notifyDataSetChanged();
    }

    void updateSelectedTheme() {
        int position = getSelectedPosition();
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
            SmsShowListItemData itemData = mData.get(holder.getAdapterPosition());
            UIIntents.get().launchSmsShowDetailActivity(mContext, itemData.getId(), itemData.getSmsShowUrl());
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = mSelectedPosition == position
//                ? mData.get(position).getSmsShowUrl()
                ? "http://uploads.5068.com/allimg/1712/151-1G225092K3.jpg"
                : mData.get(position).getMainPagePreviewUrl();

        GlideApp.with(mContext)
                .load(url)
                .placeholder(getThemePreviewDrawable(position))
                .into(holder.mSmsShowImage);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private Drawable getThemePreviewDrawable(int position) {
        int colorIndex = position % COLORS.length;
        return new ColorDrawable(Color.parseColor(COLORS[colorIndex]));
    }

    private int getSelectedPosition() {
        int position = -1;
        int currentId = SmsShowUtils.getSmsShowAppliedId();
        int count = mData.size();
        for (int i = 0; i < count; i++) {
            if (mData.get(i).getId() == currentId) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mSmsShowImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mSmsShowImage = itemView.findViewById(R.id.sms_show_image);
        }
    }


}
