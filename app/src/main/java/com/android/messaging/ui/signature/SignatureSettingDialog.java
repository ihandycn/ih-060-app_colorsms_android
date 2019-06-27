package com.android.messaging.ui.signature;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.appsettings.SettingGeneralActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.EmojiPickerFragment;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class SignatureSettingDialog extends DialogFragment {

    public static final String PREF_KEY_SIGNATURE_CONTENT = "pref_key_signature_content";
    private View mEmojiContainer;
    private InterceptBackKeyEditText mInputEditText;
    private boolean mIsEmojiShow, mIsKeyboardShow;
    private Set<String> mInputEmojiSet = new HashSet<>();
    private View root;
    private WeakReference<Activity> mActivityReference;

    private EmojiPickerFragment mEmojiPickerFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFullScreen);
        mActivityReference = new WeakReference<>(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Threads.postOnMainThreadDelayed(this::showKeyboard, 300);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActivityReference.get() != null && mActivityReference.get() instanceof SettingGeneralActivity) {
            ((SettingGeneralActivity) mActivityReference.get()).clearBackPressedListener();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mActivityReference.get() != null && mActivityReference.get() instanceof SettingGeneralActivity) {
            ((SettingGeneralActivity) mActivityReference.get()).clearBackPressedListener();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        root = inflater.inflate(R.layout.activity_signature_setting, container, false);

        if (getActivity() instanceof SettingGeneralActivity) {
            ((SettingGeneralActivity) getActivity()).addBackPressListener(this::onBackPressed);
        }

        mEmojiContainer = root.findViewById(R.id.signature_emoji_container);
        mEmojiContainer.setBackgroundColor(Color.WHITE);

        root.findViewById(R.id.signature_input_container).setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
        View cancelBtn = root.findViewById(R.id.signature_cancel_btn);
        cancelBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffebeef3, Dimensions.pxFromDp(3.3f), true));
        cancelBtn.setOnClickListener(v -> dismiss());

        View saveBtn = root.findViewById(R.id.signature_save_btn);
        saveBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(3.3f), true));
        saveBtn.setOnClickListener(v -> {
            String signature = mInputEditText.getText().toString();
            Preferences.getDefault().putString(PREF_KEY_SIGNATURE_CONTENT, signature);

            boolean hasEmoji = false;
            for (String s : mInputEmojiSet) {
                if (signature.contains(s)) {
                    hasEmoji = true;
                    break;
                }
            }
            BugleAnalytics.logEvent("SMS_Signature_Change", true, "with_emoji", String.valueOf(hasEmoji));

            if (mActivityReference.get() != null && mActivityReference.get() instanceof SettingGeneralActivity) {
                ((SettingGeneralActivity) mActivityReference.get()).refreshSignature();
            }
            dismiss();
        });

        ImageView emojiBtn = root.findViewById(R.id.signature_emoji_btn);
        emojiBtn.getDrawable().mutate().setColorFilter(0xff3b3e43, PorterDuff.Mode.SRC_ATOP);
        emojiBtn.setOnClickListener(v -> {
            if (!mIsKeyboardShow && !mIsEmojiShow) {
                hideKeyboard();
                showEmoji();
            } else if (mIsKeyboardShow) {
                hideKeyboard();
                showEmoji();
            } else {
                hideEmoji();
                showKeyboard();
            }
        });

        mInputEditText = root.findViewById(R.id.signature_input);
        mInputEditText.addBackListener(this::onBackPressed);
        mInputEditText.setOnClickListener(v -> {
            if (!mIsKeyboardShow) {
                hideEmoji();
                mEmojiContainer.setVisibility(View.INVISIBLE);
                showKeyboard();
            }
        });

        String signature = Preferences.getDefault().getString(PREF_KEY_SIGNATURE_CONTENT, null);
        if (!TextUtils.isEmpty(signature)) {
            mInputEditText.setText(signature);
            mInputEditText.setSelection(signature.length());
        }

        BugleAnalytics.logEvent("SMS_Signature_Show", true);
        mEmojiContainer.setVisibility(View.INVISIBLE);
        initEmoji();
        return root;
    }

    public void onBackPressed() {
        mEmojiContainer.setVisibility(View.GONE);
        if (mIsEmojiShow) {
            mIsEmojiShow = false;
            return;
        }
        if (mIsKeyboardShow) {
            hideKeyboard();
            return;
        }
        dismiss();
    }

    private void showEmoji() {
        mEmojiContainer.setVisibility(View.VISIBLE);
        mIsEmojiShow = true;
    }

    private void hideEmoji() {
        mEmojiContainer.setVisibility(View.INVISIBLE);
        mIsEmojiShow = false;
    }

    private void showKeyboard() {
        ImeUtil.get().showImeKeyboard(HSApplication.getContext(), mInputEditText);
        mIsKeyboardShow = true;
    }

    private void hideKeyboard() {
        ImeUtil.get().hideImeKeyboard(HSApplication.getContext(), mInputEditText);
        mIsKeyboardShow = false;
    }

    private void initEmoji() {
        EmojiPickerFragment.OnEmojiPickerListener listener = new EmojiPickerFragment.OnEmojiPickerListener() {
            @Override
            public void addEmoji(String emojiStr) {
                if (mInputEditText != null) {
                    int start = mInputEditText.getSelectionStart();
                    int end = mInputEditText.getSelectionEnd();
                    mInputEditText.getText().replace(start, end, emojiStr);
                    mInputEmojiSet.add(emojiStr);
                }
            }

            @Override
            public void deleteEmoji() {
                mInputEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }

            @Override
            public void prepareSendSticker(Collection<MessagePartData> items) {

            }

            @Override
            public boolean isContainMessagePartData(Uri uri) {
                return false;
            }
        };

        int keyboardHeight = UiUtils.getKeyboardHeight();
        if(keyboardHeight != 0){
            mEmojiContainer.getLayoutParams().height = keyboardHeight;
        }

        mEmojiPickerFragment = new EmojiPickerFragment();
        mEmojiPickerFragment.setOnlyEmojiPage(true);
        mEmojiPickerFragment.setOnEmojiPickerListener(listener);
        getChildFragmentManager().beginTransaction().replace(
                R.id.signature_emoji_container,
                mEmojiPickerFragment,
                EmojiPickerFragment.FRAGMENT_TAG).commitAllowingStateLoss();
        mEmojiPickerFragment.onAnimationFinished();

    }
}
