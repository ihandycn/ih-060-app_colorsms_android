package com.android.messaging.ui.emoji.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.download.Downloader;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.appsettings.SystemEmojiStylePreview;
import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.android.messaging.ui.emoji.EmojiPackageType;
import com.android.messaging.ui.emoji.GiphyInfo;
import com.android.messaging.ui.emoji.StickerInfo;
import com.android.messaging.ui.emoji.utils.emoispan.EmojiCache;
import com.android.messaging.ui.emoji.utils.emoispan.EmojiSpannableWorker;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmojiManager {

    private static final String TAG = EmojiManager.class.getSimpleName();

    public static final String PREF_FILE_NAME = "emoji";

    private static final String PREF_TAB_STICKER = "pref_tab_sticker";
    private static final String PREF_RECENT_STICKER = "pref_recent_sticker";
    private static final String PREF_RECENT_EMOJI = "pref_recent_emoji";
    private static final String PREF_RECENT_GIF = "pref_recent_gif";
    private static final String PREF_NEW_TAB_STICKER = "pref_new_tab_sticker";
    private static final String PREF_IS_SHOW_EMOJI_GUIDE = "pref_is_show_emoji_guide";
    private static final String PREF_STICKER_MAGIC_LOTTIE_URL_PREFIX = "pref_sticker_magic_lottie_url_";
    private static final String PREF_STICKER_MAGIC_SOUND_URL_PREFIX = "pref_sticker_magic_sound_url_";
    private static final String PREF_STICKER_MAGIC_FILE_URI = "pref_sticker_magic_file_uri";
    private static final String PREF_DEFAULT_MAIN_POSITION = "pref_default_main_position";

    private static final String PREF_FIRST_VARIANT_CLICK = "pref_first_variant_click";
    private static final String PREF_FIRST_EMOJI_PAGE_CLICK = "pref_frist_emoji_page_click";
    private static final String PREF_EMOJI_STYLE_GUIDE_SHOW_TIMES = "pref_emoji_style_guide_show_times";

    private static final String PREF_SKIN_FILE_NAME = "pref_skin_record";
    private static final String PREF_SKIN_SET_DEFAULT = "pref_skin_set_default";

    private static final String PREF_EMOJI_STYLE = "pref_emoji_style";
    public static final String EMOJI_STYLE_SYSTEM = "System";

    public final static String NOTIFICATION_EMOJI_STYLE_CHANGE = "notification_emoji_style_change";

    static List<String> getTabSticker() {
        return Preferences.get(PREF_FILE_NAME).getStringList(PREF_TAB_STICKER);
    }

    static void addTabSticker(List<String> nameList) {
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_TAB_STICKER, nameList);
    }

    public static void addTabSticker(String name) {
        Preferences.get(PREF_FILE_NAME).addStringToList(PREF_NEW_TAB_STICKER, name);
        List<String> data = getTabSticker();
        if (data.contains(name)) {
            return;
        }
        data.add(0, name);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_TAB_STICKER, data);
        LoadEmojiManager.getInstance().flush();
    }

    @SuppressWarnings("unused")
    public static void removeTabSticker(String name) {
        removeNewTabSticker(name);
        List<String> data = getTabSticker();
        if (!data.contains(name)) {
            return;
        }
        data.remove(name);
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_TAB_STICKER, data);
    }

    public static void removeNewTabSticker(String name) {
        Preferences.get(PREF_FILE_NAME).removeStringFromList(PREF_NEW_TAB_STICKER, name);
    }

    public static boolean isNewTabSticker(String name) {
        List<String> data = Preferences.get(PREF_FILE_NAME).getStringList(PREF_NEW_TAB_STICKER);
        return data.contains(name);
    }

    public static boolean isShowEmojiGuide() {
        return Preferences.get(PREF_FILE_NAME).getBoolean(PREF_IS_SHOW_EMOJI_GUIDE, true);
    }

    public static void recordAlreadyShowEmojiGuide() {
        Preferences.get(PREF_FILE_NAME).putBoolean(PREF_IS_SHOW_EMOJI_GUIDE, false);
    }

    public static void upgradeEmojiRecentForEmojiStylePattern() {
        List<String> result = Preferences.get(PREF_FILE_NAME).getStringList(PREF_RECENT_EMOJI);
        List<EmojiPackageInfo> packageInfos = EmojiDataProducer.loadEmojiData(EMOJI_STYLE_SYSTEM);
        List<String> convertedStr = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            String[] split = result.get(i).split("\\|");
            if (split != null && split.length > 0 && !TextUtils.isEmpty(split[0])) {
                String unicode = split[0];
                for (EmojiPackageInfo packageInfo : packageInfos) {
                    for (BaseEmojiInfo baseEmojiInfo : packageInfo.mEmojiInfoList) {
                        if (baseEmojiInfo instanceof EmojiInfo && ((EmojiInfo) baseEmojiInfo).mUnicode.equals(unicode)) {
                            convertedStr.add(baseEmojiInfo.toString());
                            continue;
                        }
                    }
                }
            }
        }
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_EMOJI, convertedStr);
    }

    public static void upgradeEmojiSkin() {
        final String[] EMOJI_SKINS = new String[]{
                "",
                new String(Character.toChars(0x1F3FB)),
                new String(Character.toChars(0x1F3FC)),
                new String(Character.toChars(0x1F3FD)),
                new String(Character.toChars(0x1F3FE)),
                new String(Character.toChars(0x1F3FF)),
        };
        Map<String, ?> map = Preferences.get(PREF_SKIN_FILE_NAME).getAll();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String value;
            try {
                value = (String) entry.getValue();
            } catch (Exception e) {
                Preferences.get(PREF_SKIN_FILE_NAME).remove(entry.getKey());
                continue;
            }
            Preferences.get(PREF_SKIN_FILE_NAME).remove(entry.getKey());
            boolean hasFound = false;
            for (int i = 1; i < EMOJI_SKINS.length; i++) {
                if(value.contains(EMOJI_SKINS[i])){
                    Preferences.get(PREF_SKIN_FILE_NAME).putInt(entry.getKey(), i);
                    hasFound = true;
                    break;
                }
            }
            if(!hasFound){
                Preferences.get(PREF_SKIN_FILE_NAME).putInt(entry.getKey(), 0);
            }
        }
    }

    private static List<String> getRecentStr(EmojiPackageType emojiType) {
        String key;
        List<String> result;
        switch (emojiType) {
            case STICKER:
                result = Preferences.get(PREF_FILE_NAME).getStringList(PREF_RECENT_STICKER);
                key = "RecentStickerCount";
                break;
            case EMOJI:
                result = Preferences.get(PREF_FILE_NAME).getStringList(PREF_RECENT_EMOJI);
                key = "RecentEmojiCount";
                break;
            case GIF:
                result = Preferences.get(PREF_FILE_NAME).getStringList(PREF_RECENT_GIF);
                key = "RecentGifCount";
                break;
            default:
                throw new IllegalStateException("emojiType illegal");
        }
        int maxRecentCount = EmojiConfig.getInstance().optInteger(0, key);
        if (maxRecentCount > 0 && result.size() > maxRecentCount) {
            List<String> removeList = new ArrayList<>(result.size() - maxRecentCount);
            for (int i = result.size() - 1; i >= maxRecentCount; i--) {
                removeList.add(result.get(i));
            }
            result.removeAll(removeList);
        }
        return result;
    }

    public static List<BaseEmojiInfo> getRecentInfo(EmojiPackageType emojiType) {
        List<String> recentList = getRecentStr(emojiType);
        List<BaseEmojiInfo> result = new ArrayList<>(recentList.size());
        String emojiStyle = getEmojiStyle();
        for (int i = 0; i < recentList.size(); i++) {
            String msg = recentList.get(i);
            BaseEmojiInfo info;
            switch (emojiType) {
                case STICKER:
                    info = StickerInfo.unflatten(msg);
                    break;
                case EMOJI:
                    try {
                        info = EmojiInfo.unflatten(msg, emojiStyle);
                    } catch (Exception e) {
                        Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_EMOJI, new ArrayList<>());
                        return new ArrayList<>();
                    }
                    break;
                case GIF:
                    info = GiphyInfo.unflatten(msg);
                    break;
                default:
                    throw new IllegalStateException("emojiType illegal");
            }
            info.isRecent = true;
            result.add(info);
        }
        return result;
    }

    public static void saveRecentInfo(String msg, EmojiPackageType emojiType) {
        List<String> list = getRecentStr(emojiType);
        list.remove(msg);
        list.add(0, msg);
        switch (emojiType) {
            case STICKER:
                Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_STICKER, list);
                break;
            case EMOJI:
                Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_EMOJI, list);
                break;
            case GIF:
                Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_GIF, list);
                break;
            default:
                throw new IllegalStateException("emojiType illegal");
        }
    }

    public static void deleteEmojiRecent() {
        Preferences.get(PREF_FILE_NAME).putStringList(PREF_RECENT_EMOJI, new ArrayList<>());
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

    @SuppressLint("CheckResult")
    public static void getStickerFile(Context context,
                                      final String picUrl, OnGetStickerFileListener stickerFileListener) {
        if (!TextUtils.isEmpty(picUrl)) {
            GlideApp.with(context)
                    .asFile()
                    .load(picUrl)
                    .downloadOnly(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(@NonNull File file, @Nullable Transition<? super File> transition) {
                            if (stickerFileListener != null) {
                                stickerFileListener.onSuccess(file);
                            }
                        }
                    });
        }
    }

    @SuppressLint("CheckResult")
    public static void getGifFile(Context context,
                                  final String picUrl, OnGetStickerFileListener stickerFileListener) {
        if (!TextUtils.isEmpty(picUrl)) {
            GlideApp.with(context)
                    .asFile()
                    .load(picUrl)
                    .downloadOnly(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(@NonNull File file, @Nullable Transition<? super File> transition) {
                            if (stickerFileListener != null) {
                                stickerFileListener.onSuccess(file);
                            }
                        }
                    });
        }

    }


    public static void makeGifRelateToLottie(String gifUrl, String lottieUrl, String soundUrl) {
        File gifFile = Downloader.getInstance().getDownloadFile(gifUrl);
        if (!gifFile.exists()) {
            HSLog.d(TAG, "gif file is not exist!!!");
            return;
        }

        File lottieFile = Downloader.getInstance().getDownloadFile(lottieUrl);
        if (!lottieFile.exists()) {
            HSLog.d(TAG, "lottie file is not exist!!!");
            return;
        }

        SharedPreferences.Editor editor = Preferences.get(PREF_FILE_NAME).edit();
        String uriStr = Uri.fromFile(gifFile).toString();
        editor.putString(PREF_STICKER_MAGIC_LOTTIE_URL_PREFIX + uriStr, lottieUrl);
        editor.putString(PREF_STICKER_MAGIC_SOUND_URL_PREFIX + uriStr, soundUrl);
        editor.apply();
    }

    public static void makeGifRelateToSound(String gifUrl, String soundUrl) {
        File gifFile = Downloader.getInstance().getDownloadFile(gifUrl);
        if (!gifFile.exists()) {
            HSLog.d(TAG, "gif file is not exist!!!");
            return;
        }
        Preferences.get(PREF_FILE_NAME).putString(PREF_STICKER_MAGIC_SOUND_URL_PREFIX + Uri.fromFile(gifFile).toString(), soundUrl);
    }

    public static String getLottieUrlByGifUriStr(String uriStr) {
        return Preferences.get(PREF_FILE_NAME).getString(PREF_STICKER_MAGIC_LOTTIE_URL_PREFIX + uriStr, "");
    }

    public static String getSoundUrlByGifUriStr(String uriStr) {
        return Preferences.get(PREF_FILE_NAME).getString(PREF_STICKER_MAGIC_SOUND_URL_PREFIX + uriStr, "");
    }

    public static boolean isStickerMagicUri(String uriStr) {
        return Preferences.get(PREF_FILE_NAME).getStringList(PREF_STICKER_MAGIC_FILE_URI).contains(uriStr);
    }

    public static void makePartUriRelateToStickerMagicUri(String partUriStr, String
            stickerMagicUriStr) {
        Preferences.get(PREF_FILE_NAME).putString(partUriStr, stickerMagicUriStr);
    }

    public static String getStickerMagicUriByPartUri(String partUriStr) {
        return Preferences.get(PREF_FILE_NAME).getString(partUriStr, "");
    }

    public static void addStickerMagicFileUri(String uriStr) {
        if (isStickerMagicUri(uriStr)) {
            return;
        }
        Preferences.get(PREF_FILE_NAME).addStringToList(PREF_STICKER_MAGIC_FILE_URI, uriStr);
    }

    public interface OnGetStickerFileListener {
        void onSuccess(@NonNull File file);
    }

    public static int getSkinSingleRecord(String unicode) {
        return Preferences.get(PREF_SKIN_FILE_NAME).getInt(unicode, -1);
    }

    public static void addSkinSingleRecord(String unicode, int index) {
        Preferences.get(PREF_SKIN_FILE_NAME).putInt(unicode, index);
    }

    public static int getSkinDefault() {
        return Preferences.get(PREF_SKIN_FILE_NAME).getInt(PREF_SKIN_SET_DEFAULT, 0);
    }

    public static void setSkinDefault(int index) {
        Preferences.get(PREF_SKIN_FILE_NAME).putInt(PREF_SKIN_SET_DEFAULT, index);
    }

    public static int getDefaultMainPosition() {
        return Preferences.get(PREF_FILE_NAME).getInt(PREF_DEFAULT_MAIN_POSITION, 1);
    }

    public static void setDefaultMainPosition(int position) {
        Preferences.get(PREF_FILE_NAME).putInt(PREF_DEFAULT_MAIN_POSITION, position);
    }

    public static String getEmojiStyle() {
        return Preferences.get(PREF_FILE_NAME).getString(PREF_EMOJI_STYLE, EMOJI_STYLE_SYSTEM);
    }

    public static List<Map<String, String>> getAllEmojiStyles() {
        return (List<Map<String, String>>) HSConfig.getList("Application", "EmojiStyle");
    }

    public static boolean getConfigEmojiStyleGuide(){
        return HSConfig.getString("Application", "EmojiStyleGuide").equals("YES");
    }

    public static boolean isSystemEmojiStyle() {
        return getEmojiStyle().equals(EMOJI_STYLE_SYSTEM);
    }

    public static void setEmojiStyle(String style) {
        if (!style.equals(getEmojiStyle())) {
            BugleAnalytics.logEvent("Settings_EmojiStyle_Change", true,"type", style);
            Preferences.get(PREF_FILE_NAME).putString(PREF_EMOJI_STYLE, style);
            EmojiCache.getInstance().flush();
            LoadEmojiManager.getInstance().flush();
            EmojiSpannableWorker.loadAndInstall();
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_EMOJI_STYLE_CHANGE);
        }
    }

    public static long getEmojiStyleFileSize(String name) {
        return Preferences.get(PREF_FILE_NAME).getLong(name + "_size", 0);
    }

    public static void setEmojiStyleFileSize(String name, long size) {
        Preferences.get(PREF_FILE_NAME).putLong(name + "_size", size);
    }

    public static void setEmojiStyleDownloaded(String name) {
        Preferences.get(PREF_FILE_NAME).putBoolean(name, true);
    }

    public static boolean isEmojiStyleDownloaded(String name) {
        return Preferences.get(PREF_FILE_NAME).getBoolean(name, false);
    }

    public static boolean isFirstEmojiVariantClick() {
        boolean result = Preferences.get(PREF_FILE_NAME).getBoolean(PREF_FIRST_VARIANT_CLICK, true);
        Preferences.get(PREF_FILE_NAME).putBoolean(PREF_FIRST_VARIANT_CLICK, false);
        return result;
    }

    public static boolean getEnableEmojiStyleGuide() {
        boolean result = Preferences.get(PREF_FILE_NAME).getBoolean(PREF_FIRST_EMOJI_PAGE_CLICK, true);
        return result;
    }

    public static void disableEmojiStyleGuide(){
        Preferences.get(PREF_FILE_NAME).putBoolean(PREF_FIRST_EMOJI_PAGE_CLICK, false);
    }

    public static int getEmojiStyleGuideShowTimes(){
        return Preferences.get(PREF_FILE_NAME).incrementAndGetInt(PREF_EMOJI_STYLE_GUIDE_SHOW_TIMES);
    }

    public static Drawable getEmojiStyleResource(String name){
        Resources resources = HSApplication.getContext().getResources();
        switch (name) {
            case "Android Blob":
                return resources.getDrawable(R.drawable.emoji_style_blob);
            case "Android Pie":
                return resources.getDrawable(R.drawable.emoji_style_pie);
            case "Twitter":
                return resources.getDrawable(R.drawable.emoji_style_twitter);
            default:
                return new SystemEmojiStylePreview();
        }
    }
}
