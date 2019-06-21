package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.MediaPickerMessagePartData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.download.Downloader;
import com.android.messaging.ui.emoji.utils.EmojiDataProducer;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.view.ViewPagerFixed;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmojiPickerFragment extends Fragment implements INotificationObserver {

    private static final String TAG = EmojiPickerFragment.class.getSimpleName();

    public static final String NOTIFICATION_ADD_EMOJI_FROM_STORE = "notification_add_emoji_from_store";
    public static final String NOTIFICATION_BUNDLE_PACKAGE_INFO = "notification_bundle_package_info";

    public static final String FRAGMENT_TAG = "emoji_picker";

    private EmojiPackagePagerAdapter mEmojiPackagePagerAdapter;
    private ViewPagerFixed mEmojiPager;
    private OnEmojiPickerListener mOnEmojiPickerListener;
    private boolean mIsEnableSend = true;
    private EmojiVariantPopup mEmojiVariantPopup;

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
                    updateRecentEmoji(emojiInfo);
                }
            }

            @Override
            public void emojiLongClick(View view, EmojiInfo emojiInfo) {
                mEmojiVariantPopup = new EmojiVariantPopup(EmojiPickerFragment.this.getView(), this);
                mEmojiVariantPopup.show(view, emojiInfo);
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
        Activity activity = getActivity();
        Map<EmojiPackageType, List<EmojiPackageInfo>> data = new HashMap<>();
        data.put(EmojiPackageType.EMOJI, EmojiDataProducer.getInitEmojiData(activity));
        data.put(EmojiPackageType.STICKER, EmojiDataProducer.getInitStickerData(activity));
        mEmojiPackagePagerAdapter.setData(data);

        mEmojiPager = view.findViewById(R.id.emoji_pager);
        mEmojiPager.setAdapter(mEmojiPackagePagerAdapter);
        tabLayout.setupWithViewPager(mEmojiPager);
        mEmojiPackagePagerAdapter.updateTab(initMainTab());
        mEmojiPager.setCurrentItem(1);

        View deleteView = view.findViewById(R.id.emoji_delete_btn);
        deleteView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                activity.getResources().getColor(android.R.color.white), Dimensions.pxFromDp(20), true));
        deleteView.setVisibility(View.GONE);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEmojiPickerListener.deleteEmoji();
            }
        });
        mEmojiPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position != 0) {
                    deleteView.setVisibility(View.GONE);
                } else {
                    deleteView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateRecentSticker(StickerInfo info) {
        EmojiManager.saveRecentInfo(info.toString(), EmojiPackageType.STICKER);
        mEmojiPackagePagerAdapter.updateRecentSticker();
    }

    private void updateRecentEmoji(EmojiInfo info) {
        EmojiManager.saveRecentInfo(info.toString(), EmojiPackageType.EMOJI);
        mEmojiPackagePagerAdapter.updateRecentEmoji();
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

    // add emojiPackageInfo only with iconTabUrl, without image data. Only for TabLayout showing.
    private List<EmojiPackageInfo> initMainTab() {
        Activity activity = getActivity();
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiPackageInfo info = new EmojiPackageInfo();
        info.mEmojiPackageType = EmojiPackageType.EMOJI;
        info.mName = "emoji";
        String packageName = activity.getPackageName();
        info.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_tab_normal_icon", "drawable", packageName)).toString();
        info.mTabIconSelectedUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_tab_normal_selected_icon", "drawable", packageName)).toString();

        result.add(info);

        EmojiPackageInfo stickerInfo = new EmojiPackageInfo();
        stickerInfo.mName = "sticker";
        stickerInfo.mEmojiPackageType = EmojiPackageType.STICKER;
        stickerInfo.mTabIconUrl = Uri.parse("android.resource://" + activity.getPackageName() +
                "/" + activity.getResources().getIdentifier("emoji_tab_sticker_icon", "drawable",
                activity.getPackageName())).toString();
        stickerInfo.mTabIconSelectedUrl = Uri.parse("android.resource://" + activity.getPackageName() +
                "/" + activity.getResources().getIdentifier("emoji_tab_sticker_selected_icon", "drawable",
                activity.getPackageName())).toString();
        result.add(stickerInfo);

        return result;
    }

    @Override
    public void onDestroyView() {
        if (mEmojiVariantPopup != null) {
            this.mEmojiVariantPopup.dismiss();
        }
        super.onDestroyView();
        HSLog.e(TAG, "onDestroyView()");
        BaseStickerItemRecyclerAdapter.releaseListener();
    }

    @Override
    public void onDestroy() {
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
                    mEmojiPackagePagerAdapter.insertStickItem(1, packageInfo);
                    mEmojiPager.setCurrentItem(1);
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
