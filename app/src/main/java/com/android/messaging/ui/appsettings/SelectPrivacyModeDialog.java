package com.android.messaging.ui.appsettings;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

public class SelectPrivacyModeDialog extends BaseDialogFragment {
    private static final String BUNDLE_KEY_CONVERSATION_ID = "conversation_id";

    private String mConversationId;

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Override
    protected CharSequence getTitle() {
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


    public static SelectPrivacyModeDialog newInstance(String conversationId) {
        SelectPrivacyModeDialog dialog = new SelectPrivacyModeDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_CONVERSATION_ID, conversationId);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected View getContentView() {
        return createBodyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversationId = getArguments() != null ? getArguments().getString(BUNDLE_KEY_CONVERSATION_ID) : null;
    }

    private View createBodyView() {
        final View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_choose_privacy_mode, null);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        0xffa5abb1
                        , PrimaryColors.getPrimaryColor(),
                }
        );

        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        Typeface font = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD);
        for (int i = 0; i < 3; i++) {
            AppCompatRadioButton rb = ((AppCompatRadioButton) radioGroup.getChildAt(i));
            rb.setTypeface(font);
            rb.setSupportButtonTintList(colorStateList);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.mode_disable:
                    PrivacyModeSettings.setPrivacyMode(mConversationId, PrivacyModeSettings.NONE);
                    break;
                case R.id.mode_hide_message_only:
                    PrivacyModeSettings.setPrivacyMode(mConversationId, PrivacyModeSettings.HIDE_MESSAGE_ONLY);
                    break;
                case R.id.mode_hide_contact_and_message:
                    PrivacyModeSettings.setPrivacyMode(mConversationId, PrivacyModeSettings.HIDE_CONTACT_AND_MESSAGE);
                    break;
            }
            Threads.postOnMainThreadDelayed(this::dismiss, 340L);
        });
        return view;
    }
}
