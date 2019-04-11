package com.android.messaging.privatebox.ui.addtolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.superapps.util.BackgroundDrawables;

public class AddToListDialog extends AlertDialog {

    public interface OnButtonClickListener {

        void onFromConversationClick();

        void onFromContactsClick();
    }

    private Activity activity;
    private OnButtonClickListener listener;

    public AddToListDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_to_black_list_dialog_layout);

        ViewGroup fromConversationLayout = findViewById(R.id.from_conversation_layout);
        fromConversationLayout.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        fromConversationLayout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFromConversationClick();
            }
        });

        ViewGroup fromContactsLayout = findViewById(R.id.from_contacts_list_layout);
        fromContactsLayout.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        fromContactsLayout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFromContactsClick();
            }
        });
    }
}
