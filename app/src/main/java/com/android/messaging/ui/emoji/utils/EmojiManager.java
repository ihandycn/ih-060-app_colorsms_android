package com.android.messaging.ui.emoji.utils;

import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.StickerInfo;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class EmojiManager {

    private static final String PREF_TAB_STICKER = "pref_tab_sticker";
    private static final String PREF_FILE_NAME = "emoji";
    private static final String PREF_RECENT_STICKER = "pref_recent_sticker";

    static List<String> getTabSticker() {
        return Preferences.get(PREF_FILE_NAME).getStringList(PREF_TAB_STICKER);
    }

    static void addTabSticker(List<String> nameList) {
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_TAB_STICKER, nameList);
    }

    public static void addTabSticker(String name) {
        List<String> data = getTabSticker();
        if (data.contains(name)) {
            throw new IllegalStateException("The sticker of " + name + " already added to emoji picker!!!");
        }
        data.add(0, name);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_TAB_STICKER, data);
    }

    @SuppressWarnings("unused")
    public static void removeTabSticker(String name) {
        List<String> data = getTabSticker();
        if (!data.contains(name)) {
            throw new IllegalStateException("The sticker of " + name + " already removed from emoji picker!!!");
        }
        data.remove(name);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_TAB_STICKER, data);
    }

    private static List<String> getRecentStickerStr() {
        return Preferences.get(PREF_FILE_NAME).getStringList(PREF_RECENT_STICKER);
    }

    public static List<BaseEmojiInfo> getRecentStickerInfo() {
        List<String> recentList = getRecentStickerStr();
        List<BaseEmojiInfo> result = new ArrayList<>(recentList.size());
        for (int i = 0; i < recentList.size(); i++) {
            String stickerMsg = recentList.get(i);
            StickerInfo info = StickerInfo.unflatten(stickerMsg);
            result.add(info);
        }
        return result;
    }

    public static void saveRecentSticker(String stickerMsg) {
        List<String> list = getRecentStickerStr();
        list.remove(stickerMsg);
        list.add(0, stickerMsg);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_STICKER, list);
    }

    public static boolean isTabSticker(String name) {
        return getTabSticker().contains(name);
    }

    public static List<List<BaseEmojiInfo>> subList(List<BaseEmojiInfo> data, int pageCount) {
        int count = (int) Math.ceil(data.size() / (float) pageCount);
        List<List<BaseEmojiInfo>> result = new ArrayList<>(count);
        if (count == 1) {
            result.add(data);
        } else if (count > 1) {
            for (int i = 0; i < count; i++) {
                int fromIndex = i * pageCount;
                int toIndex = fromIndex + pageCount;
                if (toIndex > data.size()) {
                    toIndex = data.size();
                }
                List<BaseEmojiInfo> itemList = data.subList(fromIndex, toIndex);
                result.add(itemList);
            }
        } else {
            throw new IllegalStateException("count must bigger than 0!!!!");
        }
        return result;
    }
}
