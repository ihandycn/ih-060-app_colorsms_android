package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.util.ImageUtils;

public class BubbleStyleAdapter extends RecyclerView.Adapter<BubbleStyleAdapter.ViewHolder> {
    private Context mContext;

    public BubbleStyleAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.bubble_drawables_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.mBubble.setOnClickListener(v1 -> {
            int position = viewHolder.getAdapterPosition();
            int oldSelectedPosition = BubbleDrawables.getSelectedIndex();
            if (position != oldSelectedPosition) {
                BubbleDrawables.setSelectedIndex(position);
            }
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(position);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == BubbleDrawables.getSelectedIndex()) {
            Drawable drawable = ImageUtils.getTintedDrawable(mContext,
                    mContext.getResources().getDrawable(BubbleDrawables.BUBBLES[position]),
                    BubbleBackgroundColors.getBubbleBackgroundColor());
            holder.mBubble.setBackground(drawable);
            holder.mBubble.setImageResource(R.drawable.ic_checkmark_large_light);
        } else {
            holder.mBubble.setBackgroundResource(BubbleDrawables.BUBBLES[position]);
            holder.mBubble.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return BubbleDrawables.BUBBLES.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mBubble;

        public ViewHolder(View itemView) {
            super(itemView);
            mBubble = itemView.findViewById(R.id.bubble_item);
        }
    }
}
