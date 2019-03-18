package com.android.messaging.ui.signature;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiItemPagerAdapter;
import com.android.messaging.ui.emoji.EmojiPackagePagerAdapter;
import com.android.messaging.ui.emoji.StickerInfo;
import com.android.messaging.ui.emoji.ViewPagerDotIndicatorView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ImeUtil;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SignatureSettingActivity extends HSAppCompatActivity {
    public static final String PREF_KEY_SIGNATURE_CONTENT = "pref_key_signature_content";
    private View mEmojiContainer;
    private EditText mInputEditText;
    private boolean mIsEmojiShow, mIsKeyboardShow;
    private Set<String> mInputEmojiSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_signature_setting);

        mEmojiContainer = findViewById(R.id.signature_emoji_container);
        mEmojiContainer.setBackgroundColor(Color.WHITE);

        findViewById(R.id.signature_input_container).setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
        View cancelBtn = findViewById(R.id.signature_cancel_btn);
        cancelBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffebeef3, Dimensions.pxFromDp(3.3f), true));
        cancelBtn.setOnClickListener(v -> finish());

        View saveBtn = findViewById(R.id.signature_save_btn);
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
            finish();
        });

        ImageView emojiBtn = findViewById(R.id.signature_emoji_btn);
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

        mInputEditText = findViewById(R.id.signature_input);
        mInputEditText.setOnClickListener(v -> {
            if (mIsKeyboardShow || mIsEmojiShow) {
                hideKeyboard();
                hideEmoji();
                mEmojiContainer.setVisibility(View.GONE);
            } else {
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
        // initEditText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Threads.postOnMainThreadDelayed(() -> showKeyboard(), 300);
    }

    @Override
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
        super.onBackPressed();
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
        ImeUtil.get().showImeKeyboard(this, mInputEditText);
        mIsKeyboardShow = true;
    }

    private void hideKeyboard() {
        ImeUtil.get().hideImeKeyboard(this, mInputEditText);
        mIsKeyboardShow = false;
    }

    private void initEmoji() {
        EmojiPackagePagerAdapter.OnEmojiClickListener listener = new EmojiPackagePagerAdapter.OnEmojiClickListener() {
            @Override
            public void emojiClick(EmojiInfo emojiInfo) {
                if (mInputEditText != null) {
                    mInputEditText.getText().append(emojiInfo.mEmoji);
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

        ViewPagerFixed itemPager = findViewById(R.id.emoji_item_pager);
        ViewPagerDotIndicatorView dotIndicatorView = findViewById(R.id.dot_indicator_view);
        itemPager.addOnPageChangeListener(dotIndicatorView);
        PagerAdapter adapter = new EmojiItemPagerAdapter(getEmojiList(), listener);
        itemPager.setAdapter(adapter);
        dotIndicatorView.initDot(adapter.getCount(), 0);
    }
}
