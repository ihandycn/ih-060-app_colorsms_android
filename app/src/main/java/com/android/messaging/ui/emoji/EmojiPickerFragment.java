package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;

public class EmojiPickerFragment extends Fragment implements INotificationObserver {

    public static final String NOTIFICATION_ADD_EMOJI_FROM_STORE = "notification_add_emoji_from_store";
    public static final String NOTIFICATION_BUNDLE_PACKAGE_INFO = "notification_bundle_package_info";

    public static final String FRAGMENT_TAG = "emoji_picker";

    private EmojiPackagePagerAdapter mEmojiPackagePagerAdapter;
    private ViewPagerFixed mEmojiPager;
    private OnEmojiEditListener mOnEmojiEditListener;

    public static EmojiPickerFragment newInstance() {
        return new EmojiPickerFragment();
    }

    public void setOnEmojiEditListener(OnEmojiEditListener onEmojiEditListener) {
        mOnEmojiEditListener = onEmojiEditListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_ADD_EMOJI_FROM_STORE, this);
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
                    EmojiManager.saveRecentSticker(info.toString());
                    mEmojiPackagePagerAdapter.updateRecentItem();
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

}
