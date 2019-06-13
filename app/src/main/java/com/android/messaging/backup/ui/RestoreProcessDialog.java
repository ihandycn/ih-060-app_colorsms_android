package com.android.messaging.backup.ui;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.customize.PrimaryColors;

public class RestoreProcessDialog extends BaseDialogFragment {
    public static final int MIN_PROGRESS_TIME = 3000;
    private TextView mTotalView;
    private TextView mRestoredTextView;
    private ProgressBar mProgressBar;
    private TextView mStateView;
    private TextView mCountDivideView;

    private int mTotalCount = 1;
    private Choreographer.FrameCallback mCallback;
    private Choreographer mChoreographer;
    private int mTargetCount;
    private long mStartTime;

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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.restore_process_dialog, null);
        mTotalView = view.findViewById(R.id.total_restore_messages);
        mRestoredTextView = view.findViewById(R.id.already_restore_messages);
        mStateView = view.findViewById(R.id.restore_process_hint);
        mProgressBar = view.findViewById(R.id.restore_progress_bar);
        mCountDivideView = view.findViewById(R.id.restore_messages_divider);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.background},
                            new int[]{-android.R.attr.background}
                    },
                    new int[]{
                            0xffa5abb1
                            , PrimaryColors.getPrimaryColor(),
                    }
            );
            mProgressBar.setProgressTintList(colorStateList);
        }

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
            mRestoredTextView.setText(String.valueOf((int) (mTotalCount * 1.0f * interval / MIN_PROGRESS_TIME)));

            if (interval < MIN_PROGRESS_TIME && progress < 100 && isResumed()) {
                mChoreographer.postFrameCallback(mCallback);
            }
        };

        return view;
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

    public void setProgress(int restoredCount) {
        mTargetCount = restoredCount;
    }

    public void hideProgressBar(boolean hide) {
        if (mProgressBar == null) {
            return;
        }
        if (hide) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mCountDivideView.setVisibility(View.INVISIBLE);
            mTotalView.setVisibility(View.INVISIBLE);
            mRestoredTextView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mCountDivideView.setVisibility(View.VISIBLE);
            mTotalView.setVisibility(View.VISIBLE);
            mRestoredTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setStateText(String text) {
        if (mStateView != null) {
            mStateView.setText(text);
        }
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
        removeDialogContentVerticalMargin();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mChoreographer.removeFrameCallback(mCallback);
    }

    @Override
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
        mChoreographer.removeFrameCallback(mCallback);
    }
}
