package com.android.messaging;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public abstract class BaseDialog extends DialogFragment {
    private OnDismissOrCancelListener mOnDismissOrCancelListener;

    public void setOnDismissOrCancelListener(OnDismissOrCancelListener onDismissOrCancelListener) {
        this.mOnDismissOrCancelListener = onDismissOrCancelListener;
    }

    protected abstract CharSequence getTitle();
    protected abstract CharSequence getMessages();

    protected abstract CharSequence getNegativeButtonText();
    protected abstract CharSequence getPositiveButtonText();

    protected View getContentView() {
        return null;
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        transaction.add(this, tag);
        return transaction.commitAllowingStateLoss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.layout_base_dialog, container, false);
//        ((TextView) root.findViewById(R.id.dialog_title)).setText(getTitle());

//        if (TextUtils.isEmpty(getMessages())) {
//            FrameLayout contentViewContainer = root.findViewById(R.id.content_view);
//            contentViewContainer.removeAllViews();
//            View contentView = getContentView();
//            if (contentView != null) {
//                contentViewContainer.addView(contentView);
//            }
//        } else {
//            ((TextView) root.findViewById(R.id.dialog_content)).setText(getMessages());
//        }


//        if (TextUtils.isEmpty(getNegativeButtonText())) {
//            (root.findViewById(R.id.negative_btn)).setVisibility(View.GONE);
//        } else {
//            ((TextView) root.findViewById(R.id.negative_btn)).setText(getNegativeButtonText());
//        }
//
//        ((TextView) root.findViewById(R.id.ok_btn)).setText(getPositiveButtonText());
        return root;

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissOrCancelListener != null) {
            mOnDismissOrCancelListener.onDismiss(dialog);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnDismissOrCancelListener != null) {
            mOnDismissOrCancelListener.onCancel(dialog);
        }
    }

    @Override
    public void dismissAllowingStateLoss() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return;
        }
        super.dismissAllowingStateLoss();
    }

    public interface OnDismissOrCancelListener {
        /**
         * This method will be invoked when the dialog is dismissed.
         *
         * @param dialog the dialog that was dismissed will be passed into the
         *               method
         */
        void onDismiss(DialogInterface dialog);
        /**
         * This method will be invoked when the dialog is canceled.
         *
         * @param dialog the dialog that was canceled will be passed into the
         *               method
         */
        void onCancel(DialogInterface dialog);
    }
}
