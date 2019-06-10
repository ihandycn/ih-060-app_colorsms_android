package com.android.messaging.backup.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class RestoreProcessDialog extends BaseDialogFragment {
    private TextView mTotalView;
    private TextView mRestoredTextView;
    private ProgressBar mProgressbar;
    private TextView mStateView;
    private int mTotalCount;
    private TextView mCountDivideView;

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
        mProgressbar = view.findViewById(R.id.restore_progress_bar);
        mCountDivideView = view.findViewById(R.id.restore_messages_divider);
        return view;
    }

    public void setTotal(int totalCount) {
        if (mTotalView != null) {
            mTotalView.setText(String.valueOf(totalCount));
        }
        mTotalCount = totalCount;
    }

    public void setProgress(int restoredCount) {
        if (mRestoredTextView != null) {
            mRestoredTextView.setText(String.valueOf(restoredCount));
        }
        if (mProgressbar != null) {
            int current = mProgressbar.getProgress();
            int target = (int) (restoredCount * 1.0 / mTotalCount * 100);
            if (target > current) {
                mProgressbar.setProgress(target);
            }
        }
    }

    public void hideProgressBar(boolean hide) {
        if (mProgressbar == null) {
            return;
        }
        if (hide) {
            mProgressbar.setVisibility(View.INVISIBLE);
            mCountDivideView.setVisibility(View.INVISIBLE);
            mTotalView.setVisibility(View.INVISIBLE);
            mRestoredTextView.setVisibility(View.INVISIBLE);
        } else {
            mProgressbar.setVisibility(View.VISIBLE);
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
    }
}
