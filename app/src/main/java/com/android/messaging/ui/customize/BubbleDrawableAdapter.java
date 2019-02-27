package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;

public class BubbleDrawableAdapter extends RecyclerView.Adapter<BubbleDrawableAdapter.ViewHolder> {

    public interface OnSelectedBubbleChangeListener {
        void onChange(int index);
    }

    private Context mContext;
    private OnSelectedBubbleChangeListener mListener;

    private int mSelectedPosition;

    BubbleDrawableAdapter(Context context, String conversationId) {
        mContext = context;
        mSelectedPosition = BubbleDrawables.getSelectedIndex(conversationId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.bubble_drawables_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.mBubble.setOnClickListener(v1 -> {
            int position = viewHolder.getAdapterPosition();
            int oldSelectedPosition = mSelectedPosition;
            if (position != oldSelectedPosition) {
                mSelectedPosition = position;
                notifyItemChanged(oldSelectedPosition);
                notifyItemChanged(position);
                mListener.onChange(position);
            }
        });
        return viewHolder;
    }

    void setOnSelectedBubbleChangeListener(OnSelectedBubbleChangeListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mBubble.setBackgroundResource(BubbleDrawables.BUBBLES_INCOMING[position]);
        if (position == mSelectedPosition) {
            holder.mBubble.getBackground().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
            holder.mBubble.setImageResource(R.drawable.ic_checkmark_large_light);
        } else {
            holder.mBubble.getBackground().setColorFilter(null);
            holder.mBubble.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return BubbleDrawables.BUBBLES_INCOMING.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mBubble;

        public ViewHolder(View itemView) {
            super(itemView);
            mBubble = itemView.findViewById(R.id.bubble_item);
        }
    }
}
