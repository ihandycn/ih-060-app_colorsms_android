package com.android.messaging.ui.emoji.utils;

import android.content.Context;
import android.net.Uri;

import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.android.messaging.ui.emoji.EmojiPackageType;
import com.android.messaging.ui.emoji.StickerInfo;
import com.android.messaging.ui.emoji.utils.emoji.Emoji;
import com.android.messaging.ui.emoji.utils.emoji.EmojiCategory;
import com.android.messaging.ui.emoji.utils.emoji.EmojiProvider;
import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.List;

public class EmojiDataProducer {
    private static final String TAG = "emoji_data_producer";
    public static final String GIPHY_CATEGORY_TREND = "Trend";

    public static List<EmojiPackageInfo> getInitStickerData(Context context) {
        List<EmojiPackageInfo> result = new ArrayList<>();

        String packageName = context.getPackageName();
        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
        recentInfo.mName = "recent";
        recentInfo.mEmojiPackageType = EmojiPackageType.RECENT;
        recentInfo.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_category_recent", "drawable", packageName)).toString();
        recentInfo.mTabIconSelectedUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_category_recent_selected", "drawable", packageName)).toString();
        recentInfo.mEmojiInfoList = EmojiManager.getRecentInfo(EmojiPackageType.STICKER);
        result.add(recentInfo);

        List<EmojiPackageInfo> addInfo = EmojiConfig.getInstance().getAddedEmojiFromConfig();
        for (EmojiPackageInfo info : addInfo) {      // clear data, on retain tab info. Keep fluency during view initialisation
            info.mEmojiInfoList = new ArrayList<>();
        }
        result.addAll(addInfo);
        return result;
    }

    public static List<EmojiPackageInfo> getInitGifData(Context context) {
        List<EmojiPackageInfo> result = new ArrayList<>();

        String packageName = context.getPackageName();
        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
        recentInfo.mName = "recent";
        recentInfo.mEmojiPackageType = EmojiPackageType.RECENT;
        recentInfo.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_category_recent", "drawable", packageName)).toString();
        recentInfo.mTabIconSelectedUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_category_recent_selected", "drawable", packageName)).toString();
        recentInfo.mEmojiInfoList = EmojiManager.getRecentInfo(EmojiPackageType.GIF);
        result.add(recentInfo);

        List<String> categories = (List<String>) HSConfig.getList("Application", "Giphy", "Category");
        if (!categories.contains(GIPHY_CATEGORY_TREND)) {
            categories.add(0, GIPHY_CATEGORY_TREND);
        }
        int size = categories.size();

        for (int i = 0; i < size; i++) {
            EmojiPackageInfo category = new EmojiPackageInfo();
            category.mName = categories.get(i);
            result.add(category);
        }
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
                context.getResources().getIdentifier("emoji_category_recent", "drawable", packageName)).toString();
        recentInfo.mTabIconSelectedUrl = Uri.parse("android.resource://" + packageName + "/" +
                context.getResources().getIdentifier("emoji_category_recent_selected", "drawable", packageName)).toString();
        recentInfo.mEmojiInfoList = new ArrayList<>(loadEmojiRecentData());
        result.add(recentInfo);

        for (EmojiCategory category : categoryList) {
            EmojiPackageInfo info = new EmojiPackageInfo();
            info.mEmojiPackageType = EmojiPackageType.EMOJI;
            info.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                    context.getResources().getIdentifier(category.getIcon() + "", "drawable", packageName)).toString();
            info.mTabIconSelectedUrl = Uri.parse("android.resource://" + packageName + "/" +
                    context.getResources().getIdentifier(category.getIconSelected() + "", "drawable", packageName)).toString();
            info.mEmojiInfoList = new ArrayList<>();
            result.add(info);
        }
        return result;
    }

    public static List<EmojiPackageInfo> loadEmojiData(String emojiStyle) {
        boolean useSystemStyle = EmojiManager.isSystemEmojiStyle();
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiCategory[] categoryList = EmojiProvider.getCategories();
        for (EmojiCategory category : categoryList) {
            List<BaseEmojiInfo> emojiList = new ArrayList<>();
            for (Emoji emoji : category.getEmojis()) {
                // skip the emoji unicode which system not support
                if (useSystemStyle && (!emoji.isSupport())) {
                    continue;
                }
                EmojiInfo itemInfo = EmojiInfo.convert(emoji, emojiStyle);
                changeSkin(itemInfo);
                emojiList.add(itemInfo);
            }
            EmojiPackageInfo info = new EmojiPackageInfo();
            info.mEmojiInfoList = emojiList;
            result.add(info);
        }
        return result;
    }

    public static List<EmojiInfo> loadEmojiRecentData() {
        List<BaseEmojiInfo> recentEmojis = EmojiManager.getRecentInfo(EmojiPackageType.EMOJI);
        String emojiStyle = EmojiManager.getEmojiStyle();

        List<EmojiInfo> result = new ArrayList<>();
        for (BaseEmojiInfo item :recentEmojis) {
            EmojiInfo info = (EmojiInfo) item;
            info.mEmojiStyle = emojiStyle;
            changeSkin(info);
            result.add(info);
        }
        return result;
    }

    public static List<EmojiPackageInfo> loadStickerData() {
        return EmojiConfig.getInstance().getAddedEmojiFromConfig();
    }

    public static List<StickerInfo> loadStickerRecentData() {
        List<BaseEmojiInfo> recentInfos = EmojiManager.getRecentInfo(EmojiPackageType.STICKER);
        List<StickerInfo> results = new ArrayList<>();
        for(BaseEmojiInfo info : recentInfos){
            results.add((StickerInfo) info);
        }
        return results;
    }


    private static void changeSkin(EmojiInfo info) {
        if (info.hasVariant()) {
            // get skin record
            String record = EmojiManager.getSkinSingleRecord(info.getUnicode());
            if (record != null) {
                info.mEmoji = record;
            } else {
                int index = EmojiManager.getSkinDefault();
                if (index >= 0 && index < info.mVariants.length) {
                    info.mEmoji = info.mVariants[index].mEmoji;
                }
            }
        }
    }
}
