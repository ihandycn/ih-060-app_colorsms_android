package com.android.messaging.ui.emoji.utils;

import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class EmojiManager {

    private static final String PREF_ADDED_STICKER = "pref_added_sticker";
    private static final String PREF_FILE_NAME = "emoji";

    public static List<String> getAddedSticker() {
        return Preferences.get(PREF_FILE_NAME).getStringList(PREF_ADDED_STICKER);
    }

    public static void addSticker(List<String> nameList) {
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_ADDED_STICKER, nameList);
    }

    public static void addSticker(String name) {
        List<String> data = getAddedSticker();
        if (data.contains(name)) {
            throw new IllegalStateException("The sticker of " + name + " already added to emoji picker!!!");
        }
        data.add(name);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_ADDED_STICKER, data);
    }

    public static void removeSticker(String name) {
        List<String> data = getAddedSticker();
        if (!data.contains(name)) {
            throw new IllegalStateException("The sticker of " + name + " already removed from emoji picker!!!");
        }
        data.remove(name);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_ADDED_STICKER, data);
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
