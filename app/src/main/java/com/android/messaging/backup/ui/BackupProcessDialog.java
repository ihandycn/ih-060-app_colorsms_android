package com.android.messaging.backup.ui;

import android.os.Bundle;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class BackupProcessDialog extends BaseDialogFragment {
    public static final int MIN_PROGRESS_TIME = 3000;
    public static final int MIN_UPLOAD_TIME = 2000;

    private ProgressBar mProgressBar;
    private TextView mStateView;
    private TextView mTotalView;
    private TextView mBackedUpView;
    private LottieAnimationView mLottie;
    private int mTotalCount = 1;
    private Choreographer.FrameCallback mCallback;
    private Choreographer mChoreographer;
    private int mTargetCount;
    private long mStartTime;

    private boolean showUploadAnimation;

    @Override
    protected CharSequence getTitle() {
        return null;
    }

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return null;
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return null;
    }

    @Override
    protected View getContentView() {
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.local_backup_process_dialog, null);
        mProgressBar = view.findViewById(R.id.local_backup_progress_bar);
        mStateView = view.findViewById(R.id.local_backup_process_hint);
        mTotalView = view.findViewById(R.id.local_total_backup_messages);
        mBackedUpView = view.findViewById(R.id.local_already_backup_messages);
        mLottie = view.findViewById(R.id.local_backup_process);

        mChoreographer = Choreographer.getInstance();
        mCallback = frameTimeNanos -> {
            long interval = System.currentTimeMillis() - mStartTime;
            int progress;
            if (mTotalCount * interval > mTargetCount * MIN_PROGRESS_TIME) {
                //use actual progress
                progress = Math.min((int) (100.0f * mTargetCount / mTotalCount), 100);
            } else {
                progress = Math.min((int) (100.0f * interval / MIN_PROGRESS_TIME), 100);
            }

            mProgressBar.setProgress(progress);
            mBackedUpView.setText(String.valueOf((int) (mTotalCount * 1.0f * interval / MIN_PROGRESS_TIME)));

            if (progress >= 100) {

            } else {
                mChoreographer.postFrameCallback(mCallback);
            }
        };
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }

    public void setTotal(int totalCount) {
        mTotalCount = totalCount == 0 ? 1 : totalCount;
        if (mTotalView != null) {
            mTotalView.setText(String.valueOf(mTotalCount));
        }
    }

    public void startProgress() {
        mStartTime = System.currentTimeMillis();
        mChoreographer.postFrameCallback(mCallback);
    }

    public void setProgress(int backedUpCount) {
        mTargetCount = backedUpCount;
    }

    public void hideProgressBar(boolean hide) {
        if (mProgressBar == null) {
            return;
        }

        if (hide) {
            mProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void setStateString(String state) {
        if (mStateView != null) {
            mStateView.setText(state);
        }
    }

    public void changeLottie(boolean backing) {
        if (mLottie == null) {
            return;
        }
        mLottie.cancelAnimation();
        if (backing) {
            mLottie.setAnimation("lottie/local_backup_process.json");
        } else {
            mLottie.setAnimation("lottie/cloud_backup_process.json");
        }
        mLottie.playAnimation();
    }
}
