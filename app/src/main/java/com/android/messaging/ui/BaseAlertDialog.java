package com.android.messaging.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.ihs.app.framework.HSApplication;

public class BaseAlertDialog {


    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(@StringRes int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        public Builder setTitle(@StringRes int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }


        public void show() {
            new BaseAlertDialog(this).show();
        }

    }

    private Dialog dialog;

    BaseAlertDialog(Builder builder) {
        LayoutInflater inflater = (LayoutInflater) HSApplication.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // instantiate the dialog with the custom Theme
        View layout = inflater.inflate(R.layout.layout_base_dialog, null);

        Dialog dialog = new Dialog(builder.context, R.style.BaseDialogTheme);
        dialog.setCancelable(true);
        dialog.setContentView(layout);

        this.dialog = dialog;

        ((TextView) layout.findViewById(R.id.dialog_title)).setText(builder.title);
        // set the confirm button
        if (!TextUtils.isEmpty(builder.positiveButtonText)) {
            TextView positiveButton = layout.findViewById(R.id.ok_btn);
            positiveButton.setText(builder.positiveButtonText);
            positiveButton.setOnClickListener(v -> {
                if (builder.positiveButtonClickListener != null) {
                    builder.positiveButtonClickListener.onClick(this.dialog, DialogInterface.BUTTON_POSITIVE);
                }
                this.dialog.dismiss();
            });
        } else {
            // if no confirm button just set the visibility to GONE
            layout.findViewById(R.id.ok_btn).setVisibility(
                    View.GONE);
        }
        // set the cancel button
        if (!TextUtils.isEmpty(builder.negativeButtonText)) {
            TextView negativeButton = layout.findViewById(R.id.negative_btn);
            negativeButton.setText(builder.negativeButtonText);

            negativeButton.setOnClickListener(v -> {
                if (builder.negativeButtonClickListener != null) {
                    builder.negativeButtonClickListener.onClick(this.dialog, DialogInterface.BUTTON_NEGATIVE);
                }
                this.dialog.dismiss();
            });
        } else {
            // if no confirm button just set the visibility to GONE
            layout.findViewById(R.id.negative_btn).setVisibility(View.GONE);
        }
        // set the content message
        if (builder.message != null) {
            ((TextView) layout.findViewById(R.id.dialog_content)).setText(builder.message);
        } else if (builder.contentView != null) {
            // if no message set
            // add the contentView to the dialog body
            ((FrameLayout) layout.findViewById(R.id.content_view)).removeAllViews();
            ((FrameLayout) layout.findViewById(R.id.content_view)).addView(
                    builder.contentView, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT));
        }

    }

    public void show() {
        dialog.show();
    }


}
