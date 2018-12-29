package com.android.messaging.ui.emoji;

import android.support.annotation.DrawableRes;

import java.util.List;

public class EmojiPackageInfo {

    public EmojiPackageType mEmojiPackageType;

    public @DrawableRes int mTabDrawableIconRes;

    public String mTabIconUrl;

    public List<BaseEmojiInfo> mEmojiInfoList;
}
