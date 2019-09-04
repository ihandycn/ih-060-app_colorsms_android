package com.android.messaging.ui.ringtone;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.OsUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import java.io.IOException;
import java.util.List;

public class RingtoneSettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RingtoneInfo> mDataList;
    private int mCurPos;

    private ValueAnimator mLastAnimator;
    private OnAppRingtoneSelected mHost;

    private ColorStateList colorStateList = new ColorStateList(
            new int[][]{
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked}
            },
            new int[]{
                    0xffa5abb1
                    , PrimaryColors.getPrimaryColor(),
            }
    );

    public RingtoneSettingAdapter(List<RingtoneInfo> dataList, RingtoneInfo curRingtone) {
        this.mDataList = dataList;
        mCurPos = -1;
        if (curRingtone.type == RingtoneInfo.TYPE_APP) {
            for (int i = 0; i < dataList.size(); i++) {
                RingtoneInfo item = dataList.get(i);
                if (item.uri.equals(curRingtone.uri)) {
                    mCurPos = i;
                    break;
                }
            }
        }
    }

    public void setHost(OnAppRingtoneSelected host) {
        this.mHost = host;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ringtone_choose, parent, false);
        return new RingtoneChooseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int p) {
        int position = h.getAdapterPosition();
        RingtoneChooseViewHolder holder = (RingtoneChooseViewHolder) h;
        RingtoneInfo info = mDataList.get(position);
        holder.nameText.setText(info.name);
        if (position == mCurPos) {
            holder.radioBtn.setChecked(true);
        } else {
            holder.radioBtn.setChecked(false);
        }
        if (OsUtil.isAtLeastL()) {
            holder.radioBtn.setButtonTintList(colorStateList);
        }
        holder.animationView.getLayoutParams().width = 0;
        holder.animationView.requestLayout();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lastPos = mCurPos;

                if (mHost != null) {
                    mHost.onAppRingtoneSelected(info);
                }
                if(lastPos != position) {
                    notifyItemChanged(lastPos);
                }
                mCurPos = position;

                holder.radioBtn.setChecked(true);

                if (mLastAnimator != null) {
                    mLastAnimator.cancel();
                }

                int maxWidth = holder.itemView.getWidth();
                ValueAnimator animator = ValueAnimator.ofFloat(0, maxWidth);
                mLastAnimator = animator;
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        holder.animationView.getLayoutParams().width = value == maxWidth ? 0 : (int) value;
                        holder.animationView.requestLayout();
                    }
                });

                MediaPlayer player = new MediaPlayer();
                try {
                    player.setDataSource(HSApplication.getContext(), Uri.parse(info.uri));
                    Threads.postOnThreadPoolExecutor(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                player.prepare();
                                Threads.postOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        animator.setDuration(player.getDuration());
                                        animator.start();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        player.start();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        player.release();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        };
        holder.itemView.setOnClickListener(listener);
        holder.radioBtn.setOnClickListener(listener);
    }

    public void onActivityChange() {
        if (mLastAnimator != null) {
            mLastAnimator.cancel();
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class RingtoneChooseViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioBtn;
        MessagesTextView nameText;
        View animationView;

        public RingtoneChooseViewHolder(View itemView) {
            super(itemView);
            radioBtn = itemView.findViewById(R.id.radio);
            nameText = itemView.findViewById(R.id.name);
            animationView = itemView.findViewById(R.id.animation_view);
        }
    }

    public interface OnAppRingtoneSelected {
        void onAppRingtoneSelected(RingtoneInfo info);
    }

    public void clearChoose(){
        int last = mCurPos;
        mCurPos = -1;
        notifyItemChanged(last);
    }
}
