package com.android.messaging.ui.emoji;

import java.util.ArrayList;
import java.util.List;

public class EmojiUtils {

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
