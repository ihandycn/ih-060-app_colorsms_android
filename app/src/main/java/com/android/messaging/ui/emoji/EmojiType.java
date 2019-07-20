package com.android.messaging.ui.emoji;

public enum EmojiType {
    EMOJI(1),
    STICKER_IMAGE(2),
    STICKER_MAGIC(3),
    STICKER_GIF(4),
    EMOJI_EMPTY(5),
    GIPHY_GIF(6);

    public int value;

    EmojiType(int v) {
        value = v;
    }

    public static EmojiType valueOfInt(int value) {
        switch (value) {
            case 1:
                return EMOJI;
            case 2:
                return STICKER_IMAGE;
            case 3:
                return STICKER_MAGIC;
            case 4:
                return STICKER_GIF;
            case 5:
                return EMOJI_EMPTY;
            case 6:
                return GIPHY_GIF;
            default:
                throw new IllegalStateException(value + "no this type!!!");
        }
    }
}
