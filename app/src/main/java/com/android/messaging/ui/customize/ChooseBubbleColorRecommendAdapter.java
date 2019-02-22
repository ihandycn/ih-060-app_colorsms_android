package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.request.RequestOptions;

public class ChooseBubbleColorRecommendAdapter extends RecyclerView.Adapter<ChooseBubbleColorRecommendAdapter.ViewHolder> {
    private Context mContext;

    public ChooseBubbleColorRecommendAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.choose_bubble_color_grid_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.mBackground.setOnClickListener(v1 -> {

        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlideApp.with(mContext)
                .load(new ColorDrawable(BubbleBackgroundColors.COLORS[position]))
                .apply(RequestOptions.circleCropTransform())
                .into(holder.mBackground);
    }

    @Override
    public int getItemCount() {
        return BubbleBackgroundColors.COLORS.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mBackground;
        ImageView mCheckmark;

        public ViewHolder(View itemView) {
            super(itemView);
            mBackground = itemView.findViewById(R.id.background);
        }
    }
}