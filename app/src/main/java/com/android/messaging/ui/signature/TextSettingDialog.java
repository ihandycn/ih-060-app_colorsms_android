package com.android.messaging.ui.signature;

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
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.EmojiPickerFragment;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public abstract class TextSettingDialog extends DialogFragment {

    private View mEmojiContainer;
    private InterceptBackKeyEditText mInputEditText;
    private MessagesTextView mTitleView;
    private boolean mIsEmojiShow, mIsKeyboardShow;
    private boolean mEnableEmoji = true;
    protected Set<String> mInputEmojiSet = new HashSet<>();
    private View root;
    private EmojiPickerFragment mEmojiPickerFragment;
    private boolean mIsEmojiFragmentCreated = false;
    private boolean mIsReleaseBackPress = false;

    protected TextSettingDialogCallback mHost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFullScreen);
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
        mIsReleaseBackPress = true;
        super.onDestroy();
    }

    @Override
    public void dismiss() {
        mIsReleaseBackPress = true;
        super.dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        root = inflater.inflate(R.layout.activity_signature_setting, container, false);

        mTitleView = root.findViewById(R.id.title);
        mTitleView.setText(getTitle());
        mEmojiContainer = root.findViewById(R.id.signature_emoji_container);
        mEmojiContainer.setBackgroundColor(Color.WHITE);

        root.findViewById(R.id.signature_input_container).setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
        View cancelBtn = root.findViewById(R.id.signature_cancel_btn);
        cancelBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffebeef3, Dimensions.pxFromDp(3.3f), true));
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
                dismiss();
            }
        });

        View saveBtn = root.findViewById(R.id.signature_save_btn);
        saveBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(3.3f), true));
        saveBtn.setOnClickListener(v -> {
            String text = mInputEditText.getText().toString();
            onSave(text);
        });

        ImageView emojiBtn = root.findViewById(R.id.signature_emoji_btn);
        if (mEnableEmoji) {
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
        } else {
            emojiBtn.setVisibility(View.GONE);
        }

        mInputEditText = root.findViewById(R.id.signature_input);
        mInputEditText.addBackListener(this::onBackPressed);
        mInputEditText.setOnClickListener(v -> {
            if (!mIsKeyboardShow) {
                hideEmoji();
                mEmojiContainer.setVisibility(View.INVISIBLE);
                showKeyboard();
            }
        });

        String defaultText = getDefaultText();
        if (!TextUtils.isEmpty(defaultText)) {
            mInputEditText.setText(defaultText);
            mInputEditText.setSelection(defaultText.length());
        }

        mEmojiContainer.setVisibility(View.INVISIBLE);
        return root;
    }

    public void setEnableEmojiShow(boolean enable) {
        this.mEnableEmoji = enable;
    }

    public void setHost(TextSettingDialogCallback host) {
        this.mHost = host;
    }

    public boolean ifReleaseBackPress() {
        return mIsReleaseBackPress;
    }

    public void onBackPressed() {
        if (root == null) {
            return;
        }
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
        if (!mIsEmojiFragmentCreated) {
            initEmoji();
            mIsEmojiFragmentCreated = true;
        }
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
            public void prepareSendMedia(Collection<MessagePartData> items) {

            }

            @Override
            public boolean isContainMessagePartData(Uri uri) {
                return false;
            }
        };

        int keyboardHeight = UiUtils.getKeyboardHeight();
        if (keyboardHeight != 0) {
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

    public abstract void onSave(String text);

    public abstract void onCancel();

    public abstract String getDefaultText();

    public abstract String getTitle();

    public interface TextSettingDialogCallback {
        void onTextSaved(String text);
    }
}
