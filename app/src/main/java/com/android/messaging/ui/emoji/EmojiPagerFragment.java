package com.android.messaging.ui.emoji;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.view.ViewPagerFixed;

import java.util.List;

public class EmojiPagerFragment extends Fragment {

    private EmojiPackageType mType;
    private AbstractEmojiItemPagerAdapter mAdapter;
    private OnEmojiClickListener mOnEmojiClickListener;
    private final String TAG = EmojiPagerFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getActivity();
        HSLog.i(TAG, "EmojiPagerFragment: onCrateView: ");
        View view = LayoutInflater.from(context).inflate(R.layout.emoji_page_item_layout, container, false);
        ViewPagerFixed itemPager = view.findViewById(R.id.emoji_item_pager);
        TabLayout itemTabLayout = view.findViewById(R.id.emoji_item_tab_layout);
        if (mAdapter == null) {
            throw new RuntimeException("adapter is null");
        }
        mAdapter.setTabLayout(itemTabLayout);
        itemPager.setAdapter(mAdapter);
        if (mAdapter instanceof EmojiItemPagerAdapter) {
            itemPager.setOffscreenPageLimit(10);
        }

        itemPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (mType) {
                    case STICKER:
                        BugleAnalytics.logEvent("SMSEmoji_StickerType_Switch");
                        break;
                    case EMOJI:
                        BugleAnalytics.logEvent("SMSEmoji_EmojiType_Switch");
                        break;
                    case GIF:
                        BugleAnalytics.logEvent("SMSEmoji_GifType_Switch");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        itemTabLayout.setSelectedTabIndicatorColor(PrimaryColors.getPrimaryColor());
        itemTabLayout.setupWithViewPager(itemPager);
        mAdapter.updateTabView();

        if (mAdapter instanceof StickerItemPagerAdapter) {
            View addBtn = view.findViewById(R.id.emoji_add_btn);
            addBtn.setVisibility(View.VISIBLE);
            addBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    context.getResources().getColor(R.color.white), 0, true));
            addBtn.setOnClickListener(v -> {
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", true, true, "type", "chat_tab");
                EmojiStoreActivity.start(container.getContext());
            });

            int width = (int) (Dimensions.getPhoneWidth(context) / 9 + 0.5f);
            ViewGroup.LayoutParams params = addBtn.getLayoutParams();
            params.width = width;
            addBtn.setLayoutParams(params);
        }
        HSLog.i(TAG, "EmojiPagerFragment: onCrateView: OnFinish");
        return view;
    }

    @Override
    public void onDestroy() {
        HSLog.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public void loadData(List<EmojiPackageInfo> data) {
        if (mAdapter != null) {
            mAdapter.loadData(data);
        }
    }

    public void initData(Context context, EmojiPackageType type, List<EmojiPackageInfo> data, OnEmojiClickListener listener) {
        this.mOnEmojiClickListener = listener;
        this.mType = type;
        switch (type) {
            case STICKER:
                mAdapter = new StickerItemPagerAdapter(data, context, mOnEmojiClickListener);
                break;
            case EMOJI:
                mAdapter = new EmojiItemPagerAdapter(context, data, mOnEmojiClickListener);
                break;
            case GIF:
                mAdapter = new GiphyItemPagerAdapter(context, data, mOnEmojiClickListener);
                break;
        }
    }

    public void insertStickItem(int position, EmojiPackageInfo packageInfo) {
        if (mType == EmojiPackageType.STICKER && mAdapter != null) {
            ((StickerItemPagerAdapter) mAdapter).insertItem(position, packageInfo);
        }
    }

    public void updateRecent() {
        if (mAdapter != null) {
            mAdapter.updateRecentItem();
        }
    }

    public interface OnEmojiClickListener {

        void emojiClick(EmojiInfo emojiInfo, boolean saveRecent);

        void emojiLongClick(View view, EmojiInfo emojiInfo);

        void stickerClickExcludeMagic(StickerInfo stickerInfo);

        void gifClick(GiphyInfo gifInfo);

        void deleteEmoji();
    }
}
