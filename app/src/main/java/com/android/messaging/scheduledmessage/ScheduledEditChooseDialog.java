package com.android.messaging.scheduledmessage;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.superapps.util.BackgroundDrawables;

public class ScheduledEditChooseDialog extends AlertDialog {

    public interface OnButtonClickListener {

        void onSendNowClick();

        void onDeleteClick();

        void onEditClick();
    }

    private OnButtonClickListener listener;

    public ScheduledEditChooseDialog(Context context) {
        super(context, R.style.DefaultCompatDialog);
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.scheduled_edit_dialog_layout);

        View sendNow = findViewById(R.id.scheduled_message_send_now);
        sendNow.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        sendNow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSendNowClick();
            }
        });

        View delete = findViewById(R.id.scheduled_message_delete);
        delete.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        delete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick();
            }
        });

        View edit = findViewById(R.id.scheduled_message_edit);
        edit.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        edit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick();
            }
        });
    }
}
