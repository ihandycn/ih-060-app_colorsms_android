package com.android.messaging.ui.customize;

import android.content.Context;
import android.content.res.Resources;
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

import static com.android.messaging.ui.appsettings.ChooseThemeColorRecommendViewHolder.COLORS;


public class ChooseMessageColorRecommendAdapter extends RecyclerView.Adapter<ChooseMessageColorRecommendAdapter.ViewHolder> {

    private Context mContext;
    private OnColorChangedListener mListener;

    private int mItemCount;
    private int mSelectedPosition = -1;

    private int[] mData;
    private ColorDrawable[] mColorDrawables;
    private ColorDrawable[] mPresetColorDrawables;

    ChooseMessageColorRecommendAdapter(Context context) {
        mContext = context;
        mItemCount = COLORS.length;
        mData = new int[mItemCount];
        mColorDrawables = new ColorDrawable[mItemCount];
        System.arraycopy(COLORS, 1, mData, 2, mItemCount - 2);
        initPresetColorDrawable();
    }

    void updatePresetColors(@ColorInt int firstPositionColor,
                            @ColorInt int secondPositionColor,
                            @ColorInt int selectedColor) {
        mData[0] = firstPositionColor;
        mData[1] = secondPositionColor;

        mColorDrawables[0] = getDrawableByColor(firstPositionColor);
        mColorDrawables[1] = getDrawableByColor(secondPositionColor);

        int lastSelectedPosition = mSelectedPosition;
        for (int i = 0; i < mItemCount; i++) {
            if (mData[i] == selectedColor) {
                mSelectedPosition = i;
            }
        }

        notifyItemRangeChanged(0, 2);
        notifyItemChanged(lastSelectedPosition);
        notifyItemChanged(mSelectedPosition);
    }

    void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.choose_bubble_color_grid_list_item, parent, false);
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

    private void initPresetColorDrawable() {
        mPresetColorDrawables = new ColorDrawable[4];
        Resources resources = mContext.getResources();
        mPresetColorDrawables[0] = new ColorDrawable(resources.getColor(R.color.message_bubble_color_incoming));
        mPresetColorDrawables[1] = new ColorDrawable(resources.getColor(R.color.message_bubble_color_outgoing));
        mPresetColorDrawables[2] = new ColorDrawable(resources.getColor(R.color.message_text_color_incoming));
        mPresetColorDrawables[3] = new ColorDrawable(resources.getColor(R.color.message_text_color_outgoing));
    }

    private ColorDrawable getDrawableByColor(@ColorInt int color) {
        for (ColorDrawable colorDrawable : mPresetColorDrawables) {
            if (colorDrawable.getColor() == color) {
                return colorDrawable;
            }
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        boolean isLightColor = (mData[position] == mContext.getResources().getColor(R.color.message_text_color_outgoing)
                || mData[position] == mContext.getResources().getColor(R.color.message_bubble_color_incoming));
        boolean isSelected = position == mSelectedPosition;

        holder.mCheckmark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        if (isSelected && isLightColor) {
            holder.mCheckmark.setImageResource(R.drawable.ic_customize_bubble_checkmark_dark);
        } else {
            holder.mCheckmark.setImageResource(R.drawable.icon_customize_bubble_checkmark);
        }

        if (isLightColor) {
            GlideApp.with(mContext).load(R.drawable.bubble_customize_color_ring).into(holder.mColor);
        } else {
            if (mColorDrawables[position] == null) {
                mColorDrawables[position] = new ColorDrawable(mData[position]);
            }
            GlideApp.with(mContext)
                    .load(mColorDrawables[position])
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.mColor);
        }
    }


    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mColor;
        ImageView mCheckmark;

        public ViewHolder(View itemView) {
            super(itemView);
            mColor = itemView.findViewById(R.id.background);
            mCheckmark = itemView.findViewById(R.id.check_mark);

        }
    }
}
