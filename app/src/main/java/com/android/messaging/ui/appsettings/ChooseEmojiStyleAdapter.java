package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.EmojiStyleDownloadManager;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class ChooseEmojiStyleAdapter extends RecyclerView.Adapter<ChooseEmojiStyleAdapter.ChooseEmojiStyleHolder> {

    private List<EmojiStyleItem> mDataList;
    private int mLastItem;
    private EmojiStyleItemSelectListener mListener;
    private Context mContext;

    private ColorStateList colorStateList = new ColorStateList(
            new int[][] {
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked}
            },
            new int[] {
                    0xffa5abb1
                    , PrimaryColors.getPrimaryColor(),
            }
    );


    public ChooseEmojiStyleAdapter() {
    }

    @NonNull
    @Override
    public ChooseEmojiStyleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_item_emoji_style, parent, false);
        return new ChooseEmojiStyleHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseEmojiStyleHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        EmojiStyleItem item = mDataList.get(position);
        holder.nameText.setText(item.name);
        if (OsUtil.isAtLeastL()) {
            holder.radioButton.setButtonTintList(colorStateList); // Applying tint to drawable at left. '0' to get drawable at bottom
        }
        if (item.isSystem) {
            holder.sampleImage.setImageDrawable(new SystemEmojiStylePreview());
        } else {
            GlideApp.with(holder.sampleImage).load(item.sampleImageUrl).into(holder.sampleImage);
        }
        if (position == mLastItem) {
            holder.radioButton.setChecked(true);
            holder.downloadText.setTextColor(0xff2d7ab5);
            if (item.isSystem) {
                holder.downloadText.setText(mContext.getResources().getString(R.string.emoji_style_current_set));
            } else {
                if (item.isDownloaded) {
                    holder.downloadText.setText(mContext.getResources().getString(R.string.emoji_style_current_set));
                } else {
                    String text = mContext.getResources().getString(R.string.emoji_style_downloading);
                    text += "  " + item.downloadPercent + " %";
                    holder.downloadText.setText(text);
                }
            }
        } else {
            holder.radioButton.setChecked(false);
            holder.downloadText.setTextColor(0xff858b92);
            if (item.isSystem) {
                holder.downloadText.setText(mContext.getResources().getString(R.string.emoji_style_default));
            } else {
                if (item.isDownloaded) {
                    holder.downloadText.setText(mContext.getResources().getString(R.string.emoji_style_downloaded));
                } else {
                    String text = mContext.getResources().getString(R.string.emoji_style_download);
                    text += " " + item.downloadSize + " MB";
                    holder.downloadText.setText(text);
                }
            }
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lastPos = mLastItem;
                mLastItem = holder.getAdapterPosition();
                notifyItemChanged(holder.getAdapterPosition());
                notifyItemChanged(lastPos);
                // callback for download, when the emoji is downloaded, it's useless.
                mListener.onItemSelected(item, new EmojiStyleDownloadManager.DownloadCallback() {
                    @Override
                    public void onFail(EmojiStyleDownloadManager.EmojiStyleDownloadTask task, String msg) {
                        HSLog.e("emoji_download", msg);
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.emoji_style_download_failed), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(EmojiStyleDownloadManager.EmojiStyleDownloadTask task) {
                        item.isDownloaded = true;
                        EmojiManager.setEmojiStyleDownloaded(item.name);
                        notifyItemChanged(position);
                    }

                    @Override
                    public void onUpdate(long downloadSize, long totalSize) {
                        item.downloadPercent = (int) (downloadSize * 100 / (float) totalSize);
                        notifyItemChanged(position);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                mListener.onItemUnSelected(mDataList.get(lastPos));
            }
        };
        holder.itemView.setOnClickListener(clickListener);
        holder.radioButton.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public EmojiStyleItem getSelectItem() {
        return mDataList.get(mLastItem);
    }

    public void setDataList(List<EmojiStyleItem> dataList) {
        this.mDataList = dataList;
        notifyDataSetChanged();
    }

    public void setDefaultSelectPos(int pos) {
        mLastItem = pos;
    }

    public void setItemSelectListener(EmojiStyleItemSelectListener listener) {
        this.mListener = listener;
    }

    interface EmojiStyleItemSelectListener {
        void onItemSelected(EmojiStyleItem item, EmojiStyleDownloadManager.DownloadCallback callback);

        void onItemUnSelected(EmojiStyleItem item);
    }

    static class ChooseEmojiStyleHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView nameText;
        TextView downloadText;
        ImageView sampleImage;

        public ChooseEmojiStyleHolder(View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_btn);
            nameText = itemView.findViewById(R.id.name);
            downloadText = itemView.findViewById(R.id.download_text);
            sampleImage = itemView.findViewById(R.id.sample_image);
        }
    }

    public static class EmojiStyleItem {
        boolean isSystem;
        boolean isDownloaded;
        String name;
        String downloadSize;
        String sampleImageUrl;
        String downloadUrl;
        int downloadPercent;

        public EmojiStyleItem(String name, String downloadSize, String sampleImageUrl, String downloadUrl) {
            this.name = name;
            this.downloadSize = downloadSize;
            this.sampleImageUrl = sampleImageUrl;
            this.downloadUrl = downloadUrl;
            this.isSystem = false;
        }

        public EmojiStyleItem() {
            this.name = EmojiManager.EMOJI_STYLE_SYSTEM;
            this.isSystem = true;
            this.isDownloaded = true;
        }

        public void setSystem(boolean system) {
            isSystem = system;
        }

        public void setDownloaded(boolean downloaded) {
            isDownloaded = downloaded;
        }

    }
}
