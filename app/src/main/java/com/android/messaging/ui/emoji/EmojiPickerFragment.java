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
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;

public class EmojiPickerFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "emoji_picker";

    private EmojiPackagePagerAdapter mEmojiPackagePagerAdapter;
    private ViewPagerFixed mEmojiPager;
    private TabLayout mTabLayout;

    public static EmojiPickerFragment newInstance() {
        return new EmojiPickerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emoji_layout, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTabLayout = view.findViewById(R.id.emoji_tab_layout);
        mEmojiPackagePagerAdapter = new EmojiPackagePagerAdapter(getActivity(), mTabLayout, this);
        mEmojiPager = view.findViewById(R.id.emoji_pager);
        mEmojiPager.setAdapter(mEmojiPackagePagerAdapter);
        mTabLayout.setupWithViewPager(mEmojiPager);
        mEmojiPackagePagerAdapter.update(initData());
    }

    private List<EmojiPackageInfo> initData() {
        Activity activity = getActivity();
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiPackageInfo info = new EmojiPackageInfo();
        info.mEmojiPackageType = EmojiPackageType.EMOJI;
        String packageName = activity.getPackageName();
        info.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_normal_tab_icon", "drawable", packageName)).toString();
        info.mEmojiInfoList = getTestList();
        result.add(info);

        EmojiPackageInfo recentInfo = new EmojiPackageInfo();
        recentInfo.mEmojiPackageType = EmojiPackageType.RECENT;
        recentInfo.mTabIconUrl = Uri.parse("android.resource://" + packageName + "/" +
                activity.getResources().getIdentifier("emoji_recent_tab_icon", "drawable", packageName)).toString();
        result.add(recentInfo);

        result.addAll(EmojiConfig.getInstance().getAddedEmojiFromConfig());
        return result;
    }

    private List<BaseEmojiInfo> getTestList() {
        List<BaseEmojiInfo> result = new ArrayList<>();
        for (int i = 0; i < 89; i++) {
            EmojiInfo info = new EmojiInfo();
            info.mEmojiValue = 0x1F602;
            result.add(info);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (!(tag instanceof StickerInfo)) {
            return;
        }

        StickerInfo info = (StickerInfo) tag;
        EmojiManager.saveRecentSticker(info.toString());
        mEmojiPackagePagerAdapter.updateRecentItem();
    }
}
