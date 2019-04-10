package com.android.messaging.ui.signature;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.appsettings.SettingGeneralActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiItemPagerAdapter;
import com.android.messaging.ui.emoji.EmojiPackagePagerAdapter;
import com.android.messaging.ui.emoji.StickerInfo;
import com.android.messaging.ui.emoji.ViewPagerDotIndicatorView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ImeUtil;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.ViewPagerFixed;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SignatureSettingDialog extends DialogFragment {

    public static final String PREF_KEY_SIGNATURE_CONTENT = "pref_key_signature_content";
    private View mEmojiContainer;
    private InterceptBackKeyEditText mInputEditText;
    private boolean mIsEmojiShow, mIsKeyboardShow;
    private Set<String> mInputEmojiSet = new HashSet<>();
    private View root;
    private WeakReference<Activity> mActivityReference;

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

    private List<BaseEmojiInfo> getEmojiList() {
        List<BaseEmojiInfo> result = new ArrayList<>();
        String[] arrays = getResources().getStringArray(R.array.emoji_faces);
        for (String array : arrays) {
            EmojiInfo info = new EmojiInfo();
            info.mEmoji = new String((Character.toChars(Integer.parseInt(array, 16))));
            result.add(info);
        }
        return result;
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
        EmojiPackagePagerAdapter.OnEmojiClickListener listener = new EmojiPackagePagerAdapter.OnEmojiClickListener() {
            @Override
            public void emojiClick(EmojiInfo emojiInfo) {
                if (mInputEditText != null) {
                    int start = mInputEditText.getSelectionStart();
                    int end = mInputEditText.getSelectionEnd();
                    mInputEditText.getText().replace(start, end, emojiInfo.mEmoji);
                    mInputEmojiSet.add(emojiInfo.mEmoji);
                }
            }

            @Override
            public void stickerClickExcludeMagic(@NonNull StickerInfo info) {

            }

            @Override
            public void deleteEmoji() {
                mInputEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        };

        ViewPagerFixed itemPager = root.findViewById(R.id.emoji_item_pager);
        ViewPagerDotIndicatorView dotIndicatorView = root.findViewById(R.id.dot_indicator_view);
        itemPager.addOnPageChangeListener(dotIndicatorView);
        PagerAdapter adapter = new EmojiItemPagerAdapter(getEmojiList(), listener);
        itemPager.setAdapter(adapter);
        dotIndicatorView.initDot(adapter.getCount(), 0);
    }
}
