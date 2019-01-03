package com.android.messaging.ui.conversation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.BaseDialog;
import com.android.messaging.ui.UIIntents;

public class DeleteConversationDialog extends BaseDialog {

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
        return null;
    }

    @Override
    protected CharSequence getTitle() {
        return null;
    }

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        setOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        setOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                UIIntents.get().launchAddContactActivity(getActivity(), mNormalizedDestination);
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
