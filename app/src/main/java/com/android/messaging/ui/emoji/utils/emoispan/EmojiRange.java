package com.android.messaging.ui.emoji.utils.emoispan;


import com.android.messaging.ui.emoji.EmojiInfo;

public final class EmojiRange {
    public final EmojiInfo emoji;
    public final int end;
    public final int start;

    EmojiRange(int i, int i2, EmojiInfo emoji) {
        this.start = i;
        this.end = i2;
        this.emoji = emoji;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EmojiRange emojiRange = (EmojiRange) obj;
        if (!(this.start == emojiRange.start && this.end == emojiRange.end && this.emoji.equals(emojiRange.emoji))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((this.start * 31) + this.end) * 31) + this.emoji.hashCode();
    }
}
