package com.android.messaging.ui.emoji.utils.emoji;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public final class Emoji implements Serializable {
    private static final long serialVersionUID = 3;
    private Emoji base;
    private final String resource;
    private final String unicode;
    private final List<Emoji> variants;

    public Emoji(int[] codes, String str) {
        this(codes, str, new Emoji[0]);
    }

    public Emoji(int i, String str) {
        this(i, str, new Emoji[0]);
    }

    public Emoji(int i, String str, Emoji... emojiArr) {
        this(new int[]{i}, str, emojiArr);
    }

    public Emoji(int[] codes, String str, Emoji... emojiArr) {
        int i = 0;
        StringBuilder builder = new StringBuilder();
        for (int item : codes) {
            builder.append(new String(Character.toChars(item)));
        }
        // add 0xFE0F, avoid emoji show as text-style(black-white)
        builder.append(new String(Character.toChars(0xFE0F)));
        this.unicode = builder.toString();
        this.resource = str;
        this.variants = Arrays.asList(emojiArr);
        while (i < emojiArr.length) {
            emojiArr[i].base = this;
            i++;
        }
    }

    public String getUnicode() {
        return this.unicode;
    }

    public List<Emoji> getVariants() {
        return this.variants;
    }

    public Emoji getBase() {
        Emoji emoji = this;
        while (emoji.base != null) {
            emoji = emoji.base;
        }
        return emoji;
    }

    public int getLength() {
        return this.unicode.length();
    }

    public boolean hasVariants() {
        return !this.variants.isEmpty();
    }

    public boolean equals(Object obj) {
        boolean result = true;
        if (this == obj) {
            return true;
        }
        if (obj != null) {
            if (getClass() == obj.getClass()) {
                Emoji emoji = (Emoji) obj;
                if (!this.resource.equals(emoji.resource) || !this.unicode.equals(emoji.unicode) || !this.variants.equals(emoji.variants)) {
                    result = false;
                }
                return result;
            }
        }
        return false;
    }

    public int hashCode() {
        return (this.unicode.hashCode() * 31) + this.variants.hashCode();
    }
}
