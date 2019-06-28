package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;

public class EmojiItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_TEXT = 2;
    private Context mContext;
    private Map<String, EmojiDrawable> cache = new HashMap<>();

    private List<BaseEmojiInfo> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    EmojiItemRecyclerAdapter(List<BaseEmojiInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        mData = data;
        mOnEmojiClickListener = emojiClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == TYPE_IMAGE) {
            return new EmojiImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_delete_layout, parent, false));
        } else {
            return new EmojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_layout, parent, false));
        }
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmojiImageViewHolder) {
            ((EmojiImageViewHolder) holder).imageView.setOnClickListener(v -> {
                if (mOnEmojiClickListener != null) {
                    mOnEmojiClickListener.deleteEmoji();
                }
            });
        } else if (holder instanceof EmojiViewHolder) {
            final EmojiViewHolder emojiHolder = (EmojiViewHolder) holder;
            final BaseEmojiInfo info = mData.get(position);
            if (info.mEmojiType == EmojiType.EMOJI_EMPTY) {
                emojiHolder.emojiView.setVisibility(View.INVISIBLE);
            } else {
                final EmojiInfo emojiInfo = (EmojiInfo) info;
                emojiHolder.itemView.setTag(emojiInfo);

                if (cache.containsKey(emojiInfo.mEmoji)) {
                    emojiHolder.emojiView.setImageDrawable(cache.get(emojiInfo.mEmoji));
                } else {
                    Threads.postOnThreadPoolExecutor(new Runnable() {
                        @Override
                        public void run() {
                            EmojiDrawable emojiDrawable = new EmojiDrawable(((EmojiInfo) info).mEmoji);
                            Threads.postOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    emojiHolder.emojiView.setImageDrawable(emojiDrawable);
                                }
                            });
                        }
                    });
                }
                emojiHolder.emojiView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        mContext.getResources().getColor(android.R.color.white), Dimensions.pxFromDp(16), true));

                emojiHolder.emojiView.setOnClickListener(v -> {
                    if (mOnEmojiClickListener != null) {
                        mOnEmojiClickListener.emojiClick(emojiInfo);
                    }
                });

                if (emojiInfo.hasVariant()) {
                    emojiHolder.emojiView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mOnEmojiClickListener != null) {
                                EmojiInfo info = (EmojiInfo) emojiHolder.itemView.getTag();
                                mOnEmojiClickListener.emojiLongClick(emojiHolder.emojiView, info);
                            }
                            return true;
                        }
                    });
                    emojiHolder.moreView.setVisibility(View.VISIBLE);
                } else {
                    emojiHolder.emojiView.setOnLongClickListener(null);
                    emojiHolder.moreView.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        BaseEmojiInfo info = mData.get(position);
        if (info.mEmojiType == EmojiType.EMOJI_DELETE) {
            return TYPE_IMAGE;
        } else {
            return TYPE_TEXT;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        private ImageView emojiView;
        private ImageView moreView;

        EmojiViewHolder(View itemView) {
            super(itemView);
            emojiView = itemView.findViewById(R.id.emoji_view);
            moreView = itemView.findViewById(R.id.emoji_more);
        }
    }

    static class EmojiImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        EmojiImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.emoji_delete_btn);
        }
    }

    public static class EmojiDrawable extends Drawable {

        private Paint mPaint;
        private String mUnicode;

        public EmojiDrawable(String unicode) {
            mPaint = new Paint();
            mUnicode = unicode;
        }

        @DebugLog
        @Override
        public void draw(Canvas canvas) {
            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setTextSize(Dimensions.pxFromDp(26));
//            Rect bounds = new Rect();
//            mPaint.getTextBounds(mUnicode, 0, mUnicode.length(), bounds);
            canvas.drawText(mUnicode, Dimensions.pxFromDp(1.5f), Dimensions.pxFromDp(23), mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        public void initView(Context context, ImageView view) {
            GlideApp.with(context)
                    .load(this)
                    .override(Dimensions.pxFromDp(5), Dimensions.pxFromDp(5))
                    .placeholder(R.drawable.emoji_category_people)
                    .into(view);
        }
    }

}
