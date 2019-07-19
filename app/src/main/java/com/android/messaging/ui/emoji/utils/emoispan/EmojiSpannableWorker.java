package com.android.messaging.ui.emoji.utils.emoispan;

import android.text.Spannable;
import android.text.TextUtils;

import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.android.messaging.ui.emoji.utils.EmojiDataProducer;
import com.android.messaging.ui.emoji.utils.EmojiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hugo.weaving.DebugLog;

// work for EmojiEditText and EmojiTextView, swap emoji unicode to spannable drawable
public final class EmojiSpannableWorker {
    private static final EmojiSpannableWorker INSTANCE = new EmojiSpannableWorker();
    private static final Comparator<String> STRING_LENGTH_COMPARATOR = new Comparator<String>() {
        public int compare(String str, String str2) {
            int length = str.length();
            int length2 = str2.length();
            if (length < length2) {
                return 1;
            }
            return length == length2 ? 0 : -1;
        }
    };
    private List<EmojiPackageInfo> categories;
    private final Map<String, EmojiInfo> emojiMap = new LinkedHashMap<>(3000);
    private Pattern emojiPattern;
    private Pattern emojiRepetitivePattern;

    private EmojiSpannableWorker() {
    }

    public static EmojiSpannableWorker getInstance() {
        return INSTANCE;
    }

    @DebugLog
    public static void install() {
        int size;
        INSTANCE.categories = EmojiDataProducer.loadEmojiData(EmojiManager.getEmojiStyle());
        INSTANCE.emojiMap.clear();
        ArrayList<String> arrayList = new ArrayList<>(3000);
        for (EmojiPackageInfo emojiPackage : INSTANCE.categories) {
            for (BaseEmojiInfo item : emojiPackage.mEmojiInfoList) {
                EmojiInfo emoji = (EmojiInfo)item;
                String text = emoji.mEmoji;
                EmojiInfo[] variants = emoji.mVariants;
                INSTANCE.emojiMap.put(text, emoji);
                arrayList.add(text);
                for (int i = 0; i < variants.length; i++) {
                    EmojiInfo variant = variants[i];
                    String variantText = variant.mEmoji;
                    INSTANCE.emojiMap.put(variantText, variant);
                    arrayList.add(variantText);
                }
            }
        }
        if (arrayList.isEmpty()) {
            throw new IllegalArgumentException("Your EmojiProvider must at least have one category with at least one emoji.");
        }
        Collections.sort(arrayList, STRING_LENGTH_COMPARATOR);
        StringBuilder stringBuilder = new StringBuilder(12000);
        size = arrayList.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(Pattern.quote(arrayList.get(i)));
            stringBuilder.append('|');
        }
        stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        INSTANCE.emojiPattern = Pattern.compile(stringBuilder.toString());

        INSTANCE.emojiRepetitivePattern = Pattern.compile("(" + stringBuilder + ")+");
    }

    public static void replaceWithImages(Spannable spannable, float emojiSize) {
        EmojiSpannableWorker instance = getInstance();
        int i = 0;
        EmojiSpan[] emojiSpanArr = spannable.getSpans(0, spannable.length(), EmojiSpan.class);
        ArrayList<Integer> arrayList = new ArrayList<>(emojiSpanArr.length);
        for (Object spanStart : emojiSpanArr) {
            arrayList.add(spannable.getSpanStart(spanStart));
        }
        List<EmojiRange> findAllEmojis = instance.findAllEmojis(spannable);
        while (i < findAllEmojis.size()) {
            EmojiRange emojiRange = findAllEmojis.get(i);
            if (!arrayList.contains(emojiRange.start)) {
                spannable.setSpan(new EmojiSpan(emojiRange.emoji.getDrawable(), emojiSize), emojiRange.start, emojiRange.end, 33);
            }
            i++;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public List<EmojiPackageInfo> getCategories() {
        verifyInstalled();
        return this.categories;
    }

    /* Access modifiers changed, original: 0000 */
    public Pattern getEmojiRepetitivePattern() {
        return this.emojiRepetitivePattern;
    }

    /* Access modifiers changed, original: 0000 */
    public List<EmojiRange> findAllEmojis(CharSequence charSequence) {
        verifyInstalled();
        ArrayList<EmojiRange> arrayList = new ArrayList<>();
        if (!TextUtils.isEmpty(charSequence)) {
            Matcher matcher = this.emojiPattern.matcher(charSequence);
            while (matcher.find()) {
                EmojiInfo findEmoji = findEmoji(charSequence.subSequence(matcher.start(), matcher.end()));
                if (findEmoji != null) {
                    arrayList.add(new EmojiRange(matcher.start(), matcher.end(), findEmoji));
                }
            }
        }
        return arrayList;
    }

    /* Access modifiers changed, original: 0000 */
    public EmojiInfo findEmoji(CharSequence charSequence) {
        verifyInstalled();
        return this.emojiMap.get(charSequence.toString());
    }

    /* Access modifiers changed, original: 0000 */
    public void verifyInstalled() {
        if (this.categories == null) {
            throw new IllegalStateException("Please install an EmojiProvider through the EmojiSpannableWorker.install() method first.");
        }
    }
}
