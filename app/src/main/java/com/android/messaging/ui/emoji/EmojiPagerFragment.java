package com.android.messaging.ui.emoji;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.view.ViewPagerFixed;

import java.util.List;

public class EmojiPagerFragment extends Fragment implements INotificationObserver {

    private EmojiPackageType mType;
    private AbstractEmojiItemPagerAdapter mAdapter;
    private OnEmojiClickListener mOnEmojiClickListener;
    private final String TAG = EmojiPagerFragment.class.getSimpleName();
    private boolean mIsViewInited = false;
    private boolean mEmojiStyleGuideEnable = false;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(EmojiManager.NOTIFICATION_EMOJI_STYLE_CHANGE, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        HSLog.i(TAG, "EmojiPagerFragment: onCrateView: ");
        View view = inflater.inflate(R.layout.emoji_page_item_layout, container, false);
        mView = view;
        if (mAdapter == null) {
            HSLog.e(TAG, "adapter is null");
            return view;
        }
        initView(view);
        return view;
    }

    private void initView(View view) {
        mIsViewInited = true;
        Context context = getActivity();
        ViewPagerFixed itemPager = view.findViewById(R.id.emoji_item_pager);
        TabLayout itemTabLayout = view.findViewById(R.id.emoji_item_tab_layout);
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
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", true, "type", "chat_tab");
                BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", "type", "chat_tab");
                EmojiStoreActivity.start(context);
            });

            int width = (int) (Dimensions.getPhoneWidth(context) / 9 + 0.5f);
            ViewGroup.LayoutParams params = addBtn.getLayoutParams();
            params.width = width;
            addBtn.setLayoutParams(params);
        }
        HSLog.i(TAG, "EmojiPagerFragment: onCrateView: OnFinish");

        if (mEmojiStyleGuideEnable) {
            Threads.postOnMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    showEmojiStyleGuide();
                }
            }, 400);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
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
        if (!mIsViewInited && mView != null) {
            initView(mView);
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

    public void enableEmojiStyleGuide(boolean enable) {
        mEmojiStyleGuideEnable = enable;
    }

    private void showEmojiStyleGuide() {
        ViewGroup root = (ViewGroup) mView;
        View view = new EmojiStyleGuideView(getActivity());
        root.addView(view);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(view.getLayoutParams());
        lp.width = ActionBar.LayoutParams.MATCH_PARENT;
        lp.height = ActionBar.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(lp);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1f);
        animator.setDuration(1000);
        animator.setInterpolator(PathInterpolatorCompat.create(0f, 0f, 0.58f, 1f));
        animator.start();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (mType != EmojiPackageType.EMOJI) {
            return;
        }
        ((EmojiItemPagerAdapter) mAdapter).onEmojiStyleChange();
    }


    public interface OnEmojiClickListener {

        void emojiClick(EmojiInfo emojiInfo, boolean saveRecent);

        void emojiLongClick(View view, EmojiInfo emojiInfo);

        void stickerClickExcludeMagic(StickerInfo stickerInfo);

        void gifClick(GiphyInfo gifInfo);

        void deleteEmoji();
    }
}
