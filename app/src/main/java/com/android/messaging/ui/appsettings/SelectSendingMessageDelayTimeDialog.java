package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.OsUtil;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

public class SelectSendingMessageDelayTimeDialog extends BaseDialogFragment {

    private static final String BUNDLE_KEY_CONVERSATION_ID = "conversation_id";
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


    public static SelectSendingMessageDelayTimeDialog newInstance(String conversationId) {
        SelectSendingMessageDelayTimeDialog dialog = new SelectSendingMessageDelayTimeDialog();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private View createBodyView() {
        mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_choose_send_delay, null);
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

        int sendDelayInSecs = SendDelaySettings.getSendDelayInSecs();

        switch (sendDelayInSecs) {
            case 0:
                radioGroup.check(R.id.no_delay);
                break;
            case 1:
                radioGroup.check(R.id.one_second);
                break;
            case 2:
                radioGroup.check(R.id.two_seconds);
                break;
            case 3:
                radioGroup.check(R.id.three_seconds);
                break;
            case 4:
                radioGroup.check(R.id.four_seconds);
                break;
            case 5:
                radioGroup.check(R.id.five_seconds);
                break;
            case 6:
                radioGroup.check(R.id.six_seconds);
                break;
            case 7:
                radioGroup.check(R.id.seven_seconds);
                break;
            case 8:
                radioGroup.check(R.id.eight_seconds);
                break;
            case 9:
                radioGroup.check(R.id.nine_seconds);
                break;
        }

        Context context = Factory.get().getApplicationContext();
        for (int i = 0; i < 10; i++) {
            AppCompatRadioButton rb = ((AppCompatRadioButton) radioGroup.getChildAt(i));
            if(i == 0){
                rb.setText(context.getString(R.string.send_delay_no_delay));
            } else {
                rb.setText(context.getResources().getQuantityString(R.plurals.send_delay_seconds, i, i));
            }
            rb.setTypeface(font);
            if (OsUtil.isAtLeastL()) {
                rb.getCompoundDrawables()[0].setTintList(colorStateList); // Applying tint to drawable at left. '0' to get drawable at bottom
            }

            rb.setOnClickListener(v -> Threads.postOnMainThreadDelayed(this::dismissAllowingStateLoss, 340L));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.no_delay:
                    SendDelaySettings.setSendDelay(0);
                    break;
                case R.id.one_second:
                    SendDelaySettings.setSendDelay(1);
                    break;
                case R.id.two_seconds:
                    SendDelaySettings.setSendDelay(2);
                    break;
                case R.id.three_seconds:
                    SendDelaySettings.setSendDelay(3);
                    break;
                case R.id.four_seconds:
                    SendDelaySettings.setSendDelay(4);
                    break;
                case R.id.five_seconds:
                    SendDelaySettings.setSendDelay(5);
                    break;
                case R.id.six_seconds:
                    SendDelaySettings.setSendDelay(6);
                    break;
                case R.id.seven_seconds:
                    SendDelaySettings.setSendDelay(7);
                    break;
                case R.id.eight_seconds:
                    SendDelaySettings.setSendDelay(8);
                    break;
                case R.id.nine_seconds:
                    SendDelaySettings.setSendDelay(9);
                    break;
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
