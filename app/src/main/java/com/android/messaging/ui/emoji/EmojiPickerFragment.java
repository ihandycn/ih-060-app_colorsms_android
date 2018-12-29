package com.android.messaging.ui.emoji;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmojiPickerFragment extends Fragment {

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
        mEmojiPackagePagerAdapter = new EmojiPackagePagerAdapter(getActivity(), mTabLayout);
        mEmojiPager = view.findViewById(R.id.emoji_pager);
        mEmojiPager.setAdapter(mEmojiPackagePagerAdapter);
        mTabLayout.setupWithViewPager(mEmojiPager);
        mEmojiPackagePagerAdapter.update(getTestData());
    }

    private List<EmojiPackageInfo> getTestData() {
        List<EmojiPackageInfo> result = new ArrayList<>();
        EmojiPackageInfo info = new EmojiPackageInfo();
        info.mEmojiPackageType = EmojiPackageType.EMOJI;
        info.mTabDrawableIconRes = R.drawable.emoji_normal_tab_icon;
        info.mEmojiInfoList = getTestList();
        result.add(info);

        for (int i = 0; i < 3; i++) {
            EmojiPackageInfo stickerInfo = new EmojiPackageInfo();
            stickerInfo.mEmojiPackageType = EmojiPackageType.STICKER;
            stickerInfo.mTabDrawableIconRes = R.drawable.ic_audio_play;
            stickerInfo.mEmojiInfoList = getStickerTestList();
            result.add(stickerInfo);
        }

        return result;
    }

    private static int[] colors = new int[]{Color.RED, Color.GRAY, Color.BLUE, Color.YELLOW, Color.GREEN, Color.CYAN};

    private @ColorInt int getColor() {
        Random random = new Random();
        int i = random.nextInt(colors.length);
        return colors[i];
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

    private List<BaseEmojiInfo> getStickerTestList() {
        List<BaseEmojiInfo> result = new ArrayList<>();
        for (int i = 0; i < 89; i++) {
            StickerImageInfo info = new StickerImageInfo();
            info.mImageUrl = "http://img.my.csdn.net/uploads/201308/31/1377949442_4562.jpg";
            result.add(info);
        }

        return result;
    }


}
