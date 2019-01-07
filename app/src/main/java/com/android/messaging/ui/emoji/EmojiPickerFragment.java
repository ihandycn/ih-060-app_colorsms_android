package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
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
import com.android.messaging.util.ContentType;
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
    private OnEmojiEditListener mOnEmojiEditListener;
    private OnStickerSendListener mOnStickerSendListener;

    public static EmojiPickerFragment newInstance() {
        return new EmojiPickerFragment();
    }

    public void setOnEmojiEditListener(OnEmojiEditListener onEmojiEditListener) {
        mOnEmojiEditListener = onEmojiEditListener;
    }

    public void setOnStickerSendListener(OnStickerSendListener onStickerSendListener) {
        mOnStickerSendListener = onStickerSendListener;
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
        mEmojiPackagePagerAdapter = new EmojiPackagePagerAdapter(getActivity(), tabLayout, new OnEmojiClickListener() {
            @Override
            public void emojiClick(BaseEmojiInfo emojiInfo) {
                if (emojiInfo instanceof StickerInfo) {
                    StickerInfo info = (StickerInfo) emojiInfo;

                    updateRecentSticker(info);

                    EmojiManager.getStickerFile(getActivity(), info.mStickerUrl, file -> {
                        String contentType = ContentType.IMAGE_PNG;
                        if (info.mEmojiType == EmojiType.STICKER_GIF) {
                            contentType = ContentType.IMAGE_GIF;
                        }
                        sendSticker(file, info.mStartRect, contentType, info.mStickerWidth, info.mStickerHeight);
                    });

                } else if (emojiInfo instanceof EmojiInfo) {
                    if (mOnEmojiEditListener != null) {
                        mOnEmojiEditListener.add(((EmojiInfo) emojiInfo).mEmoji);
                    }
                }
            }

            @Override
            public void delete() {
                if (mOnEmojiEditListener != null) {
                    mOnEmojiEditListener.delete();
                }
            }
        });

        mEmojiPager = view.findViewById(R.id.emoji_pager);
        mEmojiPager.setAdapter(mEmojiPackagePagerAdapter);
        tabLayout.setupWithViewPager(mEmojiPager);
        mEmojiPackagePagerAdapter.update(initData());
        view.findViewById(R.id.emoji_store_btn).setOnClickListener(v -> EmojiStoreActivity.start(getActivity()));
    }

    private void updateRecentSticker(StickerInfo info) {
        EmojiManager.saveRecentSticker(info.toString());
        mEmojiPackagePagerAdapter.updateRecentItem();
    }

    private void sendSticker(File file, Rect rect, String contentType, int width, int height) {
        if (mOnStickerSendListener != null) {
            final List<MessagePartData> items = new ArrayList<>(1);
            items.add(new MediaPickerMessagePartData(rect, contentType, Uri.fromFile(file), width, height));
            mOnStickerSendListener.sendSticker(items);
        }
    }

    private List<EmojiPackageInfo> initData() {
        Activity activity = getActivity();
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiPackageInfo info = new EmojiPackageInfo();
        info.mEmojiPackageType = EmojiPackageType.EMOJI;
        String packageName = activity.getPackageName();
        info.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_normal_tab_icon", "drawable", packageName)).toString();
        info.mEmojiInfoList = getEmojiList();
        result.add(info);

        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
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
                    updateRecentSticker(stickerInfo);

                    sendSticker(file, stickerInfo.mStartRect, ContentType.IMAGE_GIF, stickerInfo.mStickerWidth, stickerInfo.mStickerHeight);
                }, 500);
                break;
            default:
                break;
        }
    }

    public interface OnEmojiEditListener {

        void add(String emojiStr);

        void delete();
    }

    public interface OnEmojiClickListener {
        void emojiClick(BaseEmojiInfo emojiInfo);

        void delete();
    }

    public interface OnStickerSendListener {
        void sendSticker(Collection<MessagePartData> items);
    }

}
