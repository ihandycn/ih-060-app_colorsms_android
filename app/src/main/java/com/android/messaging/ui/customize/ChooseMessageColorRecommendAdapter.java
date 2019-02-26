package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.request.RequestOptions;

public class ChooseMessageColorRecommendAdapter extends RecyclerView.Adapter<ChooseMessageColorRecommendAdapter.ViewHolder> {

    private Context mContext;
    private OnColorChangedListener mListener;

    private int mItemCount;
    private int mSelectedPosition = -1;

    ChooseMessageColorRecommendAdapter(Context context) {
        mContext = context;
        mItemCount = ConversationColors.COLORS.length;
    }

    void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    void reset() {
        int position = mSelectedPosition;
        mSelectedPosition = -1;
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.choose_bubble_color_grid_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.mBackground.setOnClickListener(v1 -> {
            int position = viewHolder.getAdapterPosition();
            if (position != mSelectedPosition) {
                mListener.onColorChanged(ConversationColors.COLORS[position]);
                notifyItemChanged(position);
                if (mSelectedPosition != -1) {
                    notifyItemChanged(mSelectedPosition);
                }
                mSelectedPosition = position;
            }

        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlideApp.with(mContext)
                .load(new ColorDrawable(ConversationColors.COLORS[position]))
                .apply(RequestOptions.circleCropTransform())
                .into(holder.mBackground);

        if (position == mSelectedPosition) {
            holder.mCheckmark.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckmark.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    private int findPositionByColor(@ColorInt int color) {
        for (int i = 0; i < mItemCount; i++) {
            if (ConversationColors.COLORS[i] == color) {
                return i;
            }
        }
        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mBackground;
        ImageView mCheckmark;

        public ViewHolder(View itemView) {
            super(itemView);
            mBackground = itemView.findViewById(R.id.background);
            mCheckmark = itemView.findViewById(R.id.check_mark);

        }
    }
}
