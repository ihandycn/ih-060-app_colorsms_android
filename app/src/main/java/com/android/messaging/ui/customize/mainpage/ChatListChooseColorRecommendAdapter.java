package com.android.messaging.ui.customize.mainpage;

import android.content.Context;
import android.graphics.Color;
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
import com.android.messaging.ui.customize.OnColorChangedListener;
import com.bumptech.glide.request.RequestOptions;

public class ChatListChooseColorRecommendAdapter extends RecyclerView.Adapter<ChatListChooseColorRecommendAdapter.ViewHolder> {

    private Context mContext;
    private OnColorChangedListener mListener;

    private int mItemCount = 8;
    private int mSelectedPosition = -1;

    private int[] mData = {0xffffffff, 0xffdce3e8, 0xffb8c0c8, 0xff777d88,
            0xff386ce0, 0xffe1368e, 0xff744fdc, 0xffc94c1b};
    private ColorDrawable[] mColorDrawables;

    ChatListChooseColorRecommendAdapter(Context context) {
        mContext = context;
        mColorDrawables = new ColorDrawable[mItemCount];
    }

    void updatePresetColors(@ColorInt int recommendColor, @ColorInt int selectedColor) {
        int lastRecommendColor = mData[0];
        if (lastRecommendColor != recommendColor) {
            mData[0] = recommendColor;
            mColorDrawables[0] = new ColorDrawable(recommendColor);
            notifyItemChanged(0);
        }

        int lastSelectedPosition = mSelectedPosition;
        mSelectedPosition = -1;
        for (int i = 0; i < mItemCount; i++) {
            if (mData[i] == selectedColor) {
                mSelectedPosition = i;
            }
        }

        notifyItemChanged(lastSelectedPosition);
        if (mSelectedPosition >= 0) {
            notifyItemChanged(mSelectedPosition);
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.chat_list_choose_color_grid_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.mColor.setOnClickListener(v1 -> {
            int position = viewHolder.getAdapterPosition();
            if (position != mSelectedPosition) {
                mListener.onColorChanged(mData[position]);
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

        boolean isSelected = position == mSelectedPosition;

        holder.mCheckMark.setImageResource(R.drawable.icon_customize_bubble_checkmark);
        holder.mCheckMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        if ((mData[position] & 0x00ffffff) == 0x00ffffff) {
            GlideApp.with(mContext).load(R.drawable.bubble_customize_color_ring).into(holder.mColor);
            holder.mCheckMark.setColorFilter(Color.BLACK);
        } else {
            if (mColorDrawables[position] == null) {
                mColorDrawables[position] = new ColorDrawable(mData[position]);
            }
            GlideApp.with(mContext)
                    .load(mColorDrawables[position])
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.mColor);
            holder.mCheckMark.clearColorFilter();
        }
    }


    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mColor;
        ImageView mCheckMark;

        public ViewHolder(View itemView) {
            super(itemView);
            mColor = itemView.findViewById(R.id.background);
            mCheckMark = itemView.findViewById(R.id.check_mark);
        }
    }
}
