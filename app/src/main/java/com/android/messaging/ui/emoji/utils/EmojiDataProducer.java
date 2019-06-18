package com.android.messaging.ui.emoji.utils;

import android.content.Context;
import android.net.Uri;

import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.android.messaging.ui.emoji.EmojiPackageType;
import com.android.messaging.ui.emoji.EmojiType;
import com.android.messaging.ui.emoji.utils.emoji.Emoji;
import com.android.messaging.ui.emoji.utils.emoji.EmojiCategory;
import com.android.messaging.ui.emoji.utils.emoji.EmojiProvider;

import java.util.ArrayList;
import java.util.List;

public class EmojiDataProducer {
    private static final String TAG = "emoji_data_producer";

    public static List<EmojiPackageInfo> getInitStickerData(Context context) {
        List<EmojiPackageInfo> result = new ArrayList<>();

        String packageName = context.getPackageName();
        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
        recentInfo.mName = "recent";
        recentInfo.mEmojiPackageType = EmojiPackageType.RECENT;
        recentInfo.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_recent_tab_icon", "drawable", packageName)).toString();
        recentInfo.mEmojiInfoList = EmojiManager.getRecentInfo(EmojiPackageType.STICKER);
        result.add(recentInfo);

        result.addAll(EmojiConfig.getInstance().getAddedEmojiFromConfig());
        return result;
    }

    public static List<EmojiPackageInfo> getInitEmojiData(Context context) {
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiCategory[] categoryList = EmojiProvider.getCategories();

        String packageName = context.getPackageName();
        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
        recentInfo.mName = "recent";
        recentInfo.mEmojiPackageType = EmojiPackageType.RECENT;
        recentInfo.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_recent_tab_icon", "drawable", packageName)).toString();
        recentInfo.mEmojiInfoList = EmojiManager.getRecentInfo(EmojiPackageType.EMOJI);
        result.add(recentInfo);

        for (EmojiCategory category : categoryList) {
            EmojiPackageInfo info = new EmojiPackageInfo();
            info.mEmojiPackageType = EmojiPackageType.EMOJI;
            info.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                    context.getResources().getIdentifier(category.getIcon() + "", "drawable", packageName)).toString();
            List<BaseEmojiInfo> emojiList = new ArrayList<>();
            for (Emoji emoji : category.getEmojis()) {
                EmojiInfo emojiInfo = new EmojiInfo();
                emojiInfo.mEmojiType = EmojiType.EMOJI;
                emojiInfo.mEmoji = emoji.getUnicode();
                emojiList.add(emojiInfo);

            }
            info.mEmojiInfoList = emojiList;
            result.add(info);
        }
        return result;
    }
}
