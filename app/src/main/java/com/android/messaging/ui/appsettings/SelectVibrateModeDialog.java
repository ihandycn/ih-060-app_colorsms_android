package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

import static com.android.messaging.ui.appsettings.VibrateSettings.OFF;
import static com.android.messaging.ui.appsettings.VibrateSettings.VIBRATE_LONG;
import static com.android.messaging.ui.appsettings.VibrateSettings.VIBRATE_MULTIPLE_LONG;
import static com.android.messaging.ui.appsettings.VibrateSettings.VIBRATE_MULTIPLE_SHORT;
import static com.android.messaging.ui.appsettings.VibrateSettings.VIBRATE_NORMAL;
import static com.android.messaging.ui.appsettings.VibrateSettings.VIBRATE_SHORT;

public class SelectVibrateModeDialog extends BaseDialogFragment {

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


    public static SelectVibrateModeDialog newInstance(String conversationId) {
        SelectVibrateModeDialog dialog = new SelectVibrateModeDialog();
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
                R.layout.dialog_choose_vibrate_mode, null);
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

        int mode = VibrateSettings.getVibrateMode(mConversationId);

        switch (mode) {
            case OFF:
                radioGroup.check(R.id.vibrate_off);
                break;
            case VIBRATE_NORMAL:
                radioGroup.check(R.id.vibrate_normal);
                break;
            case VIBRATE_SHORT:
                radioGroup.check(R.id.vibrate_short);
                break;
            case VIBRATE_LONG:
                radioGroup.check(R.id.vibrate_long);
                break;
            case VIBRATE_MULTIPLE_SHORT:
                radioGroup.check(R.id.vibrate_multiple_short);
                break;
            case VIBRATE_MULTIPLE_LONG:
                radioGroup.check(R.id.vibrate_multiple_long);
                break;
        }

        for (int i = 0; i < 6; i++) {
            AppCompatRadioButton rb = ((AppCompatRadioButton) radioGroup.getChildAt(i));
            rb.setTypeface(font);
            if (OsUtil.isAtLeastL()) {
                rb.getCompoundDrawables()[0].setTintList(colorStateList); // Applying tint to drawable at left. '0' to get drawable at bottom
            }

            rb.setOnClickListener(v -> Threads.postOnMainThreadDelayed(this::dismissAllowingStateLoss, 340L));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int changedMode = VIBRATE_NORMAL;
            switch (checkedId) {
                case R.id.vibrate_off:
                    changedMode = OFF;
                    break;
                case R.id.vibrate_normal:
                    changedMode = VIBRATE_NORMAL;
                    break;
                case R.id.vibrate_short:
                    changedMode = VIBRATE_SHORT;
                    break;
                case R.id.vibrate_long:
                    changedMode = VIBRATE_LONG;
                    break;
                case R.id.vibrate_multiple_short:
                    changedMode = VIBRATE_MULTIPLE_SHORT;
                    break;
                case R.id.vibrate_multiple_long:
                    changedMode = VIBRATE_MULTIPLE_LONG;
                    break;
            }
            VibrateSettings.setVibrateMode(mConversationId, changedMode);
            vibrate(VibrateSettings.getViratePattern(changedMode));

            if (TextUtils.isEmpty(mConversationId)) {
                BugleAnalytics.logEvent("SMS_Settings_Vibrate_Set", true, "type", VibrateSettings.getVibrateDescription(""));
            } else {
                BugleAnalytics.logEvent("SMS_Detailspage_Settings_Vibrate_Set", true, "type", VibrateSettings.getVibrateDescription(mConversationId));
            }
        });

        return mContentView;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }

    private Vibrator vibrator;

    private void vibrate(long[] timeInMs) {
        if (null == vibrator) {
            vibrator = (Vibrator) HSApplication.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        try {
            vibrator.vibrate(timeInMs, -1);
        } catch (SecurityException | NullPointerException exception) {
        }
    }
}
