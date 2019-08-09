package com.android.messaging.ui.appsettings;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

public class SelectLedColorDialog extends BaseDialogFragment {

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


    public static SelectLedColorDialog newInstance(String conversationId) {
        SelectLedColorDialog dialog = new SelectLedColorDialog();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private View createBodyView() {
        mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_choose_led_color, null);
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

        RadioGroup radioGroup = mContentView.findViewById(R.id.radio_group);
        Typeface font = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_MEDIUM);

        int mode = LedSettings.getLedColor(mConversationId);

        switch (mode) {
            case LedSettings.NONE:
                radioGroup.check(R.id.led_color_none);
                break;
            case LedSettings.COLOR_WHITE:
                radioGroup.check(R.id.led_color_white);
                break;
            case LedSettings.COLOR_RED:
                radioGroup.check(R.id.led_color_red);
                break;
            case LedSettings.COLOR_BLUE:
                radioGroup.check(R.id.led_color_blue);
                break;
            case LedSettings.COLOR_YELLOW:
                radioGroup.check(R.id.led_color_yellow);
                break;
            case LedSettings.COLOR_GREEN:
                radioGroup.check(R.id.led_color_green);
                break;
            case LedSettings.COLOR_PURPLE:
                radioGroup.check(R.id.led_color_purple);
                break;
            case LedSettings.COLOR_CYAN:
                radioGroup.check(R.id.led_color_cyan);
                break;
        }

        for (int i = 0; i < 8; i++) {
            AppCompatRadioButton rb = ((AppCompatRadioButton) radioGroup.getChildAt(i));
            rb.setTypeface(font);
            if (OsUtil.isAtLeastL()) {
                rb.getCompoundDrawables()[0].setTintList(colorStateList); // Applying tint to drawable at left. '0' to get drawable at bottom
            }

            rb.setOnClickListener(v -> Threads.postOnMainThreadDelayed(this::dismissAllowingStateLoss, 340L));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int changedMode = LedSettings.COLOR_WHITE;
            switch (checkedId) {
                case R.id.led_color_none:
                    changedMode = LedSettings.NONE;
                    break;
                case R.id.led_color_white:
                    changedMode = LedSettings.COLOR_WHITE;
                    break;
                case R.id.led_color_red:
                    changedMode = LedSettings.COLOR_RED;
                    break;
                case R.id.led_color_blue:
                    changedMode = LedSettings.COLOR_BLUE;
                    break;
                case R.id.led_color_yellow:
                    changedMode = LedSettings.COLOR_YELLOW;
                    break;
                case R.id.led_color_green:
                    changedMode = LedSettings.COLOR_GREEN;
                    break;
                case R.id.led_color_purple:
                    changedMode = LedSettings.COLOR_PURPLE;
                    break;
                case R.id.led_color_cyan:
                    changedMode = LedSettings.COLOR_CYAN;
                    break;
            }
            LedSettings.setLedColor(mConversationId, changedMode);

            if (TextUtils.isEmpty(mConversationId)) {
                BugleAnalytics.logEvent("SMS_Settings_LEDColor_Set", "type", LedSettings.getLedDescription(""));
            } else {
                BugleAnalytics.logEvent("SMS_Detailspage_Settings_LEDColor_Set", "type", LedSettings.getLedDescription(""));
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
