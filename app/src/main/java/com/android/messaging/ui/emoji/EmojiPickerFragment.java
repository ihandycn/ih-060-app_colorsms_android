package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.MediaPickerMessagePartData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.download.Downloader;
import com.android.messaging.ui.emoji.EmojiPagerFragment.OnEmojiClickListener;
import com.android.messaging.ui.emoji.utils.EmojiDataProducer;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.LoadEmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmojiPickerFragment extends Fragment implements INotificationObserver, LoadEmojiManager.EmojiDataCallback {

    private static final String TAG = EmojiPickerFragment.class.getSimpleName();

    public static final String NOTIFICATION_ADD_EMOJI_FROM_STORE = "notification_add_emoji_from_store";
    public static final String NOTIFICATION_BUNDLE_PACKAGE_INFO = "notification_bundle_package_info";

    public static final String FRAGMENT_TAG = "emoji_picker";

    private static final int EMOJI_PAGE_INDEX = 0;
    private static final int STICKER_PAGE_INDEX = 1;
    private static final int GIF_PAGE_INDEX = 2;

    private MainTabViewLayout mTabLayout;
    private OnEmojiPickerListener mOnEmojiPickerListener;
    private boolean mIsEnableSend = true;
    private EmojiVariantPopup mEmojiVariantPopup;

    private boolean mIsDataPrepared = false;
    private boolean mIsAnimationFinished = false;
    private boolean mIsViewCreated = false;
    private boolean mIsDataLoaded = false;

    private boolean mCanDelete = false;

    private boolean mIsOnlyEmoji = false;

    private List<EmojiPackageInfo> mEmojiData = new ArrayList<>();
    private List<EmojiPackageInfo> mStickerData = new ArrayList<>();

    private int mCurrentPos = -1;
    private EmojiPagerFragment mEmojiFragment = new EmojiPagerFragment();
    private EmojiPagerFragment mStickFragment = new EmojiPagerFragment();
    private EmojiPagerFragment mGiphyFragment = new EmojiPagerFragment();
    private EmojiPagerFragment[] mFragments = new EmojiPagerFragment[]{mEmojiFragment, mStickFragment, mGiphyFragment};
    private ViewGroup mContainer;


    private OnEmojiClickListener mOnEmojiClickListener = new OnEmojiClickListener() {
        @Override
        public void emojiClick(EmojiInfo emojiInfo, boolean saveRecent) {
            if (mOnEmojiPickerListener != null) {
                mOnEmojiPickerListener.addEmoji(emojiInfo.mEmoji);
                updateRecentEmoji(emojiInfo, saveRecent);
            }
        }

        @Override
        public void emojiLongClick(View view, EmojiInfo emojiInfo) {
            mEmojiVariantPopup = new EmojiVariantPopup(EmojiPickerFragment.this.getView(), this);
            mEmojiVariantPopup.show(view, emojiInfo);
            BugleAnalytics.logEvent("SMSEmoji_Emoji_SkintoneLongPress");
        }

        @Override
        public void stickerClickExcludeMagic(@NonNull StickerInfo info) {
            EmojiManager.getStickerFile(getActivity(), info.mStickerUrl, file -> {
                sendSticker(info, file);
            });
        }

        @Override
        public void gifClick(GiphyInfo gifInfo) {
            EmojiManager.getGifFile(getActivity(), gifInfo.mFixedWidthGifUrl, file -> {
                sendGif(gifInfo, file);
            });
        }

        @Override
        public void deleteEmoji() {
            if (mOnEmojiPickerListener != null) {
                mOnEmojiPickerListener.deleteEmoji();
            }
        }
    };

    public static EmojiPickerFragment newInstance() {
        return new EmojiPickerFragment();
    }

    public EmojiPickerFragment() {
    }

    public void setOnEmojiPickerListener(OnEmojiPickerListener onEmojiPickerListener) {
        mOnEmojiPickerListener = onEmojiPickerListener;
    }

    public void setOnlyEmojiPage(boolean onlyEmoji) {
        mIsOnlyEmoji = onlyEmoji;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_ADD_EMOJI_FROM_STORE, this);
        HSGlobalNotificationCenter.addObserver(StickerMagicDetailActivity.NOTIFICATION_SEND_MAGIC_STICKER, this);
        LoadEmojiManager.getInstance().getEmojiData(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emoji_layout, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIsViewCreated = true;
        if (mIsDataPrepared && mIsAnimationFinished) {
            loadData();
        }
    }

    private void initView(View view) {
        mTabLayout = view.findViewById(R.id.emoji_tab_layout);
        Activity activity = getActivity();
        mEmojiFragment.initData(activity, EmojiPackageType.EMOJI, EmojiDataProducer.getInitEmojiData(activity), mOnEmojiClickListener);
        if (!mIsOnlyEmoji) {
            mStickFragment.initData(activity, EmojiPackageType.STICKER, EmojiDataProducer.getInitStickerData(activity), mOnEmojiClickListener);
            mGiphyFragment.initData(activity, EmojiPackageType.GIF, EmojiDataProducer.getInitGifData(activity), mOnEmojiClickListener);
        }

        mContainer = view.findViewById(R.id.fragment_container);

        View deleteView = view.findViewById(R.id.emoji_delete_btn);
        deleteView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                activity.getResources().getColor(android.R.color.white), Dimensions.pxFromDp(19), true));
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEmojiPickerListener.deleteEmoji();
            }
        });
        deleteView.setVisibility(View.GONE);
        makeDeleteContinuous(deleteView);

        List<EmojiPackageInfo> data = initMainTab();
        mTabLayout.setInfoList(data);
        mTabLayout.setListener(new MainTabViewLayout.OnTabSelectListener() {
            @Override
            public void onTabSelected(int position) {
                showOrHideDeleteView(deleteView, position);
                if (position == EMOJI_PAGE_INDEX) {
                    BugleAnalytics.logEvent("SMSEmoji_Emojitab_Click");
                } else if (position == STICKER_PAGE_INDEX) {
                    BugleAnalytics.logEvent("SMSEmoji_Stickertab_Click");
                } else if (position == GIF_PAGE_INDEX) {
                    BugleAnalytics.logEvent("SMSEmoji_Giftab_Click");
                }


                if(mCurrentPos == position){
                    return ;
                }
                if(!mIsOnlyEmoji && position == EMOJI_PAGE_INDEX && EmojiManager.isFirstEmojiPageClick()){
                    Toast.makeText(activity, activity.getResources().getString(R.string.emoji_page_first_click), Toast.LENGTH_SHORT).show();
                }
                FragmentManager fm = getChildFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment to = mFragments[position];
                if(mCurrentPos == -1){
                    ft.replace(R.id.fragment_container, to).commit();
                    mCurrentPos = position;
                    return ;
                }
                if(!to.isAdded()){
                    ft.add(R.id.fragment_container, to);
                }
                ft.hide(mFragments[mCurrentPos]);

                ft.show(to);
                ft.commit();
                mCurrentPos = position;
            }

            @Override
            public void onTabUnselected(int position) {

            }
        });

        if (mIsOnlyEmoji) {
            mTabLayout.select(EMOJI_PAGE_INDEX);
            deleteView.setVisibility(View.VISIBLE);
        } else {
            int position = EmojiManager.getDefaultMainPosition();
            showOrHideDeleteView(deleteView, position);
            mTabLayout.select(position);
        }
    }

    public void onAnimationFinished() {
        mIsAnimationFinished = true;
        if (mIsDataPrepared && mIsViewCreated) {
            loadData();
        } else {
            HSLog.e("emoji_picker", "onAnimationFinished: mIsDataPrepared: " + mIsDataPrepared + " mIsViewCreated: " + mIsViewCreated);
        }
    }

    @Override
    public void onDataPrepared(List<EmojiPackageInfo> emojiList, List<EmojiPackageInfo> stickerList) {
        mIsDataPrepared = true;
        mEmojiData.addAll(emojiList);
        mStickerData.addAll(stickerList);
        if (mIsAnimationFinished && mIsViewCreated) {
            loadData();
        }
    }

    private void showOrHideDeleteView(View deleteView, int position) {
        if (position != 0) {
            deleteView.setVisibility(View.GONE);
        } else {
            deleteView.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        if (mIsDataLoaded) {
            return;
        }
        mEmojiFragment.loadData(mEmojiData);
        mStickFragment.loadData(mStickerData);

        mIsDataLoaded = true;
    }

    private void updateRecentSticker(StickerInfo info) {
        EmojiManager.saveRecentInfo(info.toString(), EmojiPackageType.STICKER);
        mStickFragment.updateRecent();
    }

    private void updateRecentEmoji(EmojiInfo info, boolean saveRecent) {
        HSLog.d("ui_test", "" + saveRecent);
        if (saveRecent) {
            EmojiManager.saveRecentInfo(info.toString(), EmojiPackageType.EMOJI);
            mEmojiFragment.updateRecent();
        }
    }

    private void updateRecentGif(GiphyInfo info) {
        EmojiManager.saveRecentInfo(info.toString(), EmojiPackageType.GIF);
        mGiphyFragment.updateRecent();
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
            mOnEmojiPickerListener.prepareSendMedia(items);

            updateRecentSticker(info);
        }
    }

    private void sendGif(GiphyInfo info, File file) {
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

            String contentType = ContentType.IMAGE_GIF;
            final List<MessagePartData> items = new ArrayList<>(1);
            MediaPickerMessagePartData data = new MediaPickerMessagePartData(info.mStartRect, contentType, uri, info.mGifWidth, info.mGifHeight);
            data.setEmojiType(EmojiType.GIPHY_GIF);
            data.setName(info.mFixedWidthGifUrl);
            items.add(data);
            mOnEmojiPickerListener.prepareSendMedia(items);

            updateRecentGif(info);
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

        if (!mIsOnlyEmoji) {
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

            EmojiPackageInfo gifInfo = new EmojiPackageInfo();
            gifInfo.mName = "gif";
            gifInfo.mEmojiPackageType = EmojiPackageType.GIF;
            gifInfo.mTabIconUrl = Uri.parse("android.resource://" + activity.getPackageName() +
                    "/" + activity.getResources().getIdentifier("ic_tab_gif", "drawable",
                    activity.getPackageName())).toString();
            gifInfo.mTabIconSelectedUrl = Uri.parse("android.resource://" + activity.getPackageName() +
                    "/" + activity.getResources().getIdentifier("ic_tab_gif_selected", "drawable",
                    activity.getPackageName())).toString();

            result.add(gifInfo);
        }

        return result;
    }

    private class DeleteRunnable implements Runnable {
        @Override
        public void run() {
            if (mCanDelete) {
                mOnEmojiPickerListener.deleteEmoji();
                Threads.postOnMainThreadDelayed(new DeleteRunnable(), 70);
            }
        }
    }

    private void makeDeleteContinuous(View deleteView) {
        deleteView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCanDelete = true;
                Threads.postOnMainThread(new DeleteRunnable());
                return false;
            }
        });

        deleteView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mCanDelete = false;
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (mEmojiVariantPopup != null) {
            this.mEmojiVariantPopup.dismiss();
        }
        super.onDestroyView();
        HSLog.e(TAG, "onDestroyView()");
        EmojiManager.setDefaultMainPosition(mCurrentPos);
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
                mStickFragment.insertStickItem(2, packageInfo);
                mTabLayout.select(STICKER_PAGE_INDEX);
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

        void prepareSendMedia(Collection<MessagePartData> items);

        boolean isContainMessagePartData(Uri uri);
    }
}
