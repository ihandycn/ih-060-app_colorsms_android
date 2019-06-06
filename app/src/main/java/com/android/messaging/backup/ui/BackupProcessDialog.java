package com.android.messaging.backup.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class BackupProcessDialog extends BaseDialogFragment {

    private ProgressBar mProgressBar;
    private TextView mStateView;
    private TextView mTotalView;
    private TextView mBackedUpView;
    private LottieAnimationView mLottie;
    private int mTotalCount;

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
        if (mTotalView != null) {
            mTotalView.setText(String.valueOf(totalCount));
        }
        mTotalCount = totalCount;
    }

    public void setProgress(int backedUpCount) {
        if (mBackedUpView != null) {
            mBackedUpView.setText(String.valueOf(backedUpCount));
        }
        if (mProgressBar != null) {
            int target = (int) (backedUpCount * 1.0 / mTotalCount * 100);
            mProgressBar.setProgress(target);
        }
    }

    public void setProgress(float percent) {
        if (mBackedUpView != null) {
            mBackedUpView.setText(String.valueOf((int)(percent * mTotalCount)));
        }
        if (mProgressBar != null) {
            mProgressBar.setProgress((int) (100 * percent));
        }
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
