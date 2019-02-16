package com.android.messaging.ui;


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

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public abstract class BaseDialogFragment extends DialogFragment {
    private OnDismissOrCancelListener mOnDismissOrCancelListener;
    private View.OnClickListener mNegativeClickListener;
    private View.OnClickListener mPositiveClickListener;

    public void setOnDismissOrCancelListener(OnDismissOrCancelListener onDismissOrCancelListener) {
        this.mOnDismissOrCancelListener = onDismissOrCancelListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog);
    }

    protected abstract CharSequence getTitle();
    protected abstract CharSequence getMessages();

    protected abstract CharSequence getNegativeButtonText();
    protected abstract CharSequence getPositiveButtonText();

    protected View getContentView() {
        return null;
    }

    public void setOnNegativeButtonClickListener(View.OnClickListener listener) {
        mNegativeClickListener = listener;
    }

    public void setOnPositiveButtonClickListener(View.OnClickListener listener) {
        mPositiveClickListener = listener;
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
        ((TextView) root.findViewById(R.id.dialog_title)).setText(getTitle());

        if (TextUtils.isEmpty(getMessages())) {
            FrameLayout contentViewContainer = root.findViewById(R.id.content_view);
            contentViewContainer.removeAllViews();
            View contentView = getContentView();
            if (contentView != null) {
                contentViewContainer.addView(contentView);
            }
        } else {
            ((TextView) root.findViewById(R.id.dialog_content)).setText(getMessages());
        }

        final TextView negativeButton = root.findViewById(R.id.negative_btn);
        if (TextUtils.isEmpty(getNegativeButtonText())) {
            negativeButton.setVisibility(View.GONE);
        } else {
            negativeButton.setText(getNegativeButtonText());
        }
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNegativeClickListener.onClick(negativeButton);
            }
        });
        negativeButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.dialog_negative_button_color),
                Dimensions.pxFromDp(3.3f),true));


        final TextView positiveButton = root.findViewById(R.id.ok_btn);
        positiveButton.setText(getPositiveButtonText());
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPositiveClickListener.onClick(positiveButton);
            }
        });
        positiveButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f),true));

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
