package com.android.messaging.backup.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class RestoreProcessDialog extends BaseDialogFragment {
    private TextView mTotalView;
    private TextView mRestoresTextView;
    private ProgressBar mProgressbar;
    private TextView mStateView;
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
        return createBodyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View createBodyView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.restore_process_dialog, null);
        mTotalView = view.findViewById(R.id.total_restore_messages);
        mRestoresTextView = view.findViewById(R.id.already_restore_messages);
        mStateView = view.findViewById(R.id.restore_process_hint);
        mProgressbar = view.findViewById(R.id.restore_progress_bar);
        return view;
    }

    public void setTotal(int totalCount) {
        if (mTotalView != null) {
            mTotalView.setText(String.valueOf(totalCount));
        }
        mTotalCount = totalCount;
    }

    public void setProgress(int restoredCount) {
        if (mRestoresTextView != null) {
            mRestoresTextView.setText(String.valueOf(restoredCount));
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
        } else {
            mProgressbar.setVisibility(View.VISIBLE);
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
