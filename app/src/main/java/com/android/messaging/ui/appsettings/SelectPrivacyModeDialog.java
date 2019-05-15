package com.android.messaging.ui.appsettings;

import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

import static com.android.messaging.ui.appsettings.PrivacyModeSettings.HIDE_CONTACT_AND_MESSAGE;
import static com.android.messaging.ui.appsettings.PrivacyModeSettings.HIDE_MESSAGE_ONLY;
import static com.android.messaging.ui.appsettings.PrivacyModeSettings.NONE;

public class SelectPrivacyModeDialog extends BaseDialogFragment {
    private static final String BUNDLE_KEY_CONVERSATION_ID = "conversation_id";

    private String mConversationId;
    private View mContentView;

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
        mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_choose_privacy_mode, null);
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[] {
                        0xffa5abb1
                        , PrimaryColors.getPrimaryColor(),
                }
        );

        RadioGroup radioGroup = mContentView.findViewById(R.id.radio_group);
        Typeface font = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_MEDIUM);

        int mode = PrivacyModeSettings.getPrivacyMode(mConversationId);

        switch (mode) {
            case NONE:
                radioGroup.check(R.id.mode_disable);
                break;
            case HIDE_MESSAGE_ONLY:
                radioGroup.check(R.id.mode_hide_message_only);
                break;
            case HIDE_CONTACT_AND_MESSAGE:
                radioGroup.check(R.id.mode_hide_contact_and_message);
                break;
        }

        for (int i = 0; i < 3; i++) {
            AppCompatRadioButton rb = ((AppCompatRadioButton) radioGroup.getChildAt(i));
            rb.setTypeface(font);
            if (OsUtil.isAtLeastL()) {
                rb.getCompoundDrawables()[0].setTintList(colorStateList); // Applying tint to drawable at left. '0' to get drawable at bottom
            }

            rb.setOnClickListener(v -> Threads.postOnMainThreadDelayed(this::dismissAllowingStateLoss, 340L));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.mode_disable:
                    PrivacyModeSettings.setPrivacyMode(mConversationId, NONE);
                    break;
                case R.id.mode_hide_message_only:
                    PrivacyModeSettings.setPrivacyMode(mConversationId, PrivacyModeSettings.HIDE_MESSAGE_ONLY);
                    break;
                case R.id.mode_hide_contact_and_message:
                    PrivacyModeSettings.setPrivacyMode(mConversationId, PrivacyModeSettings.HIDE_CONTACT_AND_MESSAGE);
                    break;
            }

            if (TextUtils.isEmpty(mConversationId)) {
                BugleAnalytics.logEvent("SMS_Settings_Privacy_Click", false, true,
                        "type", PrivacyModeSettings.getPrivacyModeDescription(mConversationId));
            } else {
                BugleAnalytics.logEvent("SMS_DetailsPage_Privacy_Click", false, true,
                        "type", PrivacyModeSettings.getPrivacyModeDescription(mConversationId));
            }
        });

        return mContentView;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
