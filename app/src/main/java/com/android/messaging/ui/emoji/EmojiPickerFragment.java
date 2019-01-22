package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.MediaPickerMessagePartData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.download.Downloader;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Threads;
import com.superapps.view.ViewPagerFixed;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmojiPickerFragment extends Fragment implements INotificationObserver {

    public static final String NOTIFICATION_ADD_EMOJI_FROM_STORE = "notification_add_emoji_from_store";
    public static final String NOTIFICATION_BUNDLE_PACKAGE_INFO = "notification_bundle_package_info";

    public static final String FRAGMENT_TAG = "emoji_picker";

    private EmojiPackagePagerAdapter mEmojiPackagePagerAdapter;
    private ViewPagerFixed mEmojiPager;
    private OnEmojiPickerListener mOnEmojiPickerListener;
    private boolean mIsEnableSend = true;

    public static EmojiPickerFragment newInstance() {
        return new EmojiPickerFragment();
    }

    public void setOnEmojiPickerListener(OnEmojiPickerListener onEmojiPickerListener) {
        mOnEmojiPickerListener = onEmojiPickerListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_ADD_EMOJI_FROM_STORE, this);
        HSGlobalNotificationCenter.addObserver(StickerMagicDetailActivity.NOTIFICATION_SEND_MAGIC_STICKER, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emoji_layout, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        TabLayout tabLayout = view.findViewById(R.id.emoji_tab_layout);
        mEmojiPackagePagerAdapter = new EmojiPackagePagerAdapter(getActivity(), tabLayout, new EmojiPackagePagerAdapter.OnEmojiClickListener() {
            @Override
            public void emojiClick(EmojiInfo emojiInfo) {
                if (mOnEmojiPickerListener != null) {
                    mOnEmojiPickerListener.addEmoji(emojiInfo.mEmoji);
                }
            }

            @Override
            public void stickerClickExcludeMagic(@NonNull StickerInfo info) {
                EmojiManager.getStickerFile(getActivity(), info.mStickerUrl, file -> {
                    sendSticker(info, file);
                });
            }

            @Override
            public void deleteEmoji() {
                if (mOnEmojiPickerListener != null) {
                    mOnEmojiPickerListener.deleteEmoji();
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                EmojiPackageInfo packageInfo = getPackageInfo(tab);
                if (packageInfo != null) {
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Tab_Click", true, "type", packageInfo.mName);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                EmojiPackageInfo packageInfo = getPackageInfo(tab);
                if (packageInfo != null && EmojiManager.isNewTabSticker(packageInfo.mName)) {
                    EmojiManager.removeNewTabSticker(packageInfo.mName);
                    assert tab.getCustomView() != null;
                    tab.getCustomView().findViewById(R.id.tab_new_view).setVisibility(View.GONE);
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            private EmojiPackageInfo getPackageInfo(TabLayout.Tab tab) {
                Object object = tab.getTag();
                if (object instanceof EmojiPackageInfo) {
                    return (EmojiPackageInfo) object;
                }
                return null;
            }
        });

        mEmojiPager = view.findViewById(R.id.emoji_pager);
        mEmojiPager.setAdapter(mEmojiPackagePagerAdapter);
        tabLayout.setupWithViewPager(mEmojiPager);
        mEmojiPackagePagerAdapter.update(initData());
        view.findViewById(R.id.emoji_store_btn).setOnClickListener(v -> {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", true, "type", "chat_tab");
            EmojiStoreActivity.start(getActivity());
        });
    }

    private void updateRecentSticker(StickerInfo info) {
        EmojiManager.saveRecentSticker(info.toString());
        mEmojiPackagePagerAdapter.updateRecentItem();
    }

    private void sendSticker(StickerInfo info, File file) {
        if (mOnEmojiPickerListener != null) {
            Uri uri = Uri.fromFile(file);
            if (mOnEmojiPickerListener.isContainMessagePartData(uri)) {
                return;
            }

            if (!mIsEnableSend) {
                return;
            }
            mIsEnableSend = false;
            Threads.postOnMainThreadDelayed(() -> mIsEnableSend = true, UiUtils.MEDIAPICKER_TRANSITION_DURATION);

            String contentType = ContentType.IMAGE_PNG;
            if (info.mEmojiType == EmojiType.STICKER_GIF || info.mEmojiType == EmojiType.STICKER_MAGIC) {
                contentType = ContentType.IMAGE_GIF;
            }
            final List<MessagePartData> items = new ArrayList<>(1);
            MediaPickerMessagePartData data = new MediaPickerMessagePartData(info.mStartRect, contentType, uri, info.mStickerWidth, info.mStickerHeight);
            data.setName(info.mPackageName + "-" + StickerInfo.getNumFromUrl(info.mStickerUrl));
            data.setEmojiType(info.mEmojiType);
            items.add(data);
            mOnEmojiPickerListener.prepareSendSticker(items);

            updateRecentSticker(info);
        }
    }


    private List<EmojiPackageInfo> initData() {
        Activity activity = getActivity();
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiPackageInfo info = new EmojiPackageInfo();
        info.mEmojiPackageType = EmojiPackageType.EMOJI;
        info.mName = "emoji";
        String packageName = activity.getPackageName();
        info.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_normal_tab_icon", "drawable", packageName)).toString();
        info.mEmojiInfoList = getEmojiList();
        result.add(info);

        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
        recentInfo.mName = "recent";
        recentInfo.mEmojiPackageType = EmojiPackageType.RECENT;
        recentInfo.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_recent_tab_icon", "drawable", packageName)).toString();
        result.add(recentInfo);

        result.addAll(EmojiConfig.getInstance().getAddedEmojiFromConfig());
        return result;
    }

    private List<BaseEmojiInfo> getEmojiList() {
        List<BaseEmojiInfo> result = new ArrayList<>();
        String[] arrays = getResources().getStringArray(R.array.emoji_faces);
        for (String array : arrays) {
            EmojiInfo info = new EmojiInfo();
            info.mEmoji = new String((Character.toChars(Integer.parseInt(array, 16))));
            result.add(info);
        }
        return result;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case NOTIFICATION_ADD_EMOJI_FROM_STORE:
                if (hsBundle == null) {
                    return;
                }
                Object object = hsBundle.getObject(NOTIFICATION_BUNDLE_PACKAGE_INFO);
                if (!(object instanceof EmojiPackageInfo)) {
                    return;
                }
                EmojiPackageInfo packageInfo = (EmojiPackageInfo) object;
                if (mEmojiPackagePagerAdapter != null) {
                    mEmojiPackagePagerAdapter.insertThirdItem(packageInfo);
                    mEmojiPager.setCurrentItem(2);
                }
                break;
            case StickerMagicDetailActivity.NOTIFICATION_SEND_MAGIC_STICKER:
                Threads.postOnMainThreadDelayed(() -> {
                    Object o = hsBundle.getObject(StickerMagicDetailActivity.BUNDLE_SEND_MAGIC_STICKER_DATA);
                    if (!(o instanceof StickerInfo)) {
                        return;
                    }
                    StickerInfo stickerInfo = (StickerInfo) o;
                    File file = Downloader.getInstance().getDownloadFile(stickerInfo.mMagicUrl);
                    if (!file.exists()) {
                        return;
                    }
                    sendSticker(stickerInfo, file);
                }, 500);
                break;
            default:
                break;
        }
    }

    public interface OnEmojiPickerListener {
        void addEmoji(String emojiStr);

        void deleteEmoji();

        void prepareSendSticker(Collection<MessagePartData> items);

        boolean isContainMessagePartData(Uri uri);
    }
}
