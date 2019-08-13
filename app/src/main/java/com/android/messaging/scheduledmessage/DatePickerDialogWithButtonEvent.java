package com.android.messaging.scheduledmessage;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DatePickerDialogWithButtonEvent extends DatePickerDialog {
    private OnClickListener mPositiveButtonListener;
    private OnClickListener mNegativeButtonListener;

    public DatePickerDialogWithButtonEvent(@NonNull Context context, @Nullable OnDateSetListener listener, int year, int month, int dayOfMonth) {
        super(context, listener, year, month, dayOfMonth);
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
}
