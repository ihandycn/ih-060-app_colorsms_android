package com.android.messaging.scheduledmessage;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;

public class TimePickerDialogWithButtonEvent extends TimePickerDialog {
    private OnClickListener mPositiveButtonListener;
    private OnClickListener mNegativeButtonListener;

    public TimePickerDialogWithButtonEvent(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, listener, hourOfDay, minute, is24HourView);
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
        super.setButton(whichButton, text, this);
        if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            mPositiveButtonListener = listener;
        } else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
            mNegativeButtonListener = listener;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (mPositiveButtonListener != null && which == DialogInterface.BUTTON_POSITIVE) {
            mPositiveButtonListener.onClick(this, DialogInterface.BUTTON_POSITIVE);
        }
        if (mNegativeButtonListener != null && which == DialogInterface.BUTTON_NEGATIVE) {
            mNegativeButtonListener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
        }
    }

    /**
     * we don't want the dialog dismiss automatically,
     * but the dismiss() method will be called
     * when positive button click.
     * {@link TimePickerDialog#show()}
     **/
    @Override
    public void dismiss() {

    }

    /**
     * the {@link Dialog#cancel()} method will call
     * {@link TimePickerDialogWithButtonEvent#dismiss()} method which does nothing,
     * so we use super.dismiss{@link Dialog#dismiss()} to cancel the dialog
     **/
    @Override
    public void cancel() {
        super.dismiss();
    }
}
