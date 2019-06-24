package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ChooseEmojiSkinAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private class ChooseEmojiSkinViewHolder extends RecyclerView.ViewHolder {
        TextView mSkin;
        ViewGroup mContainer;

        ChooseEmojiSkinViewHolder(View itemView) {
            super(itemView);
            mSkin = itemView.findViewById(R.id.emoji_skin);
            mContainer = itemView.findViewById(R.id.container);
        }
    }

    interface SkinChooseListener {
        void onSkinChooseListener(int index);
    }

    private String[] mEmojiSkins;

    private SkinChooseListener mListener;

    private int mChoose = 0;
    private Context mContext;

    public ChooseEmojiSkinAdapter(String[] mEmojiSkins, int choose, SkinChooseListener listener) {
        this.mEmojiSkins = mEmojiSkins;
        this.mListener = listener;
        this.mChoose = choose;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_emoji_skin, parent, false);
        return new ChooseEmojiSkinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        TextView skin = ((ChooseEmojiSkinViewHolder) holder).mSkin;
        ViewGroup container = ((ChooseEmojiSkinViewHolder) holder).mContainer;
        skin.setText(mEmojiSkins[position]);
        if (position != mChoose) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(0xf5f7f9ff);
            container.setBackground(drawable);
        } else {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setStroke(Dimensions.pxFromDp(2), PrimaryColors.getPrimaryColor());
            drawable.setColor(0xf5f7f9ff);
            container.setBackground(drawable);
        }
        skin.setBackground(BackgroundDrawables.createBackgroundDrawable(
                0xffffff, 0xe7ecf1ff, Dimensions.pxFromDp(28), false, true));
        skin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mChoose = position;
                    notifyDataSetChanged();
                    mListener.onSkinChooseListener(position);
                    mListener = null;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEmojiSkins.length;
    }
}
