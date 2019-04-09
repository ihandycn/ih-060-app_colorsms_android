package com.android.messaging.ui.emoji;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.glide.GlideRequests;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.view.RecyclerViewWidthSlideListener;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.view.RoundImageView;
import com.superapps.view.MessagesTextView;

import java.util.List;

public class EmojiStoreFragment extends Fragment implements INotificationObserver {

    public static final String FRAGMENT_TAG = "emoji_fragment";

    public static final String BUNDLE_SOURCE = "bundle_source";
    public static final String NOTIFICATION_REFRESH_ITEM_STATUS = "notificaiton_refresh_item_status";
    public static final String NOTIFICATION_BUNDLE_ITEM_NAME = "notification_bundle_item_position";
    private static final int MAX_COLUMNS = 2;
    private List<EmojiPackageInfo> mStoreEmojiPackageInfoList;
    private StoreAdapter mAdapter;
    private String mSource;
    private boolean mIsFirstLoad = false;
    private RecyclerViewWidthSlideListener mRecyclerView;

    public static EmojiStoreFragment newInstance(String source) {
        EmojiStoreFragment fragment = new EmojiStoreFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mSource = bundle.getString(BUNDLE_SOURCE);
        }
    }

    public void setViewPager(ViewPager viewPager) {
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (position == 2) {
                        startGif();
                    } else {
                        stopGif();
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emoji_store, container, false);

        mStoreEmojiPackageInfoList = EmojiConfig.getInstance().getStoreEmojiFromConfig();
        mRecyclerView = view.findViewById(R.id.emoji_store_list);
        mRecyclerView.setOnSlideListener(new RecyclerViewWidthSlideListener.OnSlideListener() {
            @Override
            public void slideUp() {
                if (!TextUtils.isEmpty(mSource)) {
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_StoreList_Slideup", true, "type", mSource);
                }
            }

            @Override
            public void slideDown() {
            }
        });
        mAdapter = new StoreAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), MAX_COLUMNS));
        mRecyclerView.addItemDecoration(new StoreItemDecoration());
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REFRESH_ITEM_STATUS, this);
        mIsFirstLoad = true;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsFirstLoad) {
            mAdapter.notifyDataSetChanged();
        } else {
            mIsFirstLoad = false;
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case NOTIFICATION_REFRESH_ITEM_STATUS:
                if (hsBundle == null) {
                    return;
                }

                String name = hsBundle.getString(NOTIFICATION_BUNDLE_ITEM_NAME);
                if (TextUtils.isEmpty(name)) {
                    return;
                }

                if (mAdapter != null) {
                    mAdapter.updateItem(name);
                }
                break;
            default:
                break;
        }
    }

    private void stopGif() {
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View child = mRecyclerView.getChildAt(i);
            View layout = child.findViewById(R.id.preview_layout);
            if (layout instanceof EmojiStorePreviewLayout) {
                ((EmojiStorePreviewLayout) layout).stopGif();
            }
        }
    }

    private void startGif() {
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View child = mRecyclerView.getChildAt(i);
            View layout = child.findViewById(R.id.preview_layout);
            if (layout instanceof EmojiStorePreviewLayout) {
                ((EmojiStorePreviewLayout) layout).startGif();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    private class StoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_NORMAL = 0;
        private static final int TYPE_MAGIC_EMOJ = 1;
        private Context mContext;
        private Drawable mColorDrawable = new ColorDrawable(0xFFF7F7F7);

        StoreAdapter(Context context) {
            mContext = context;
        }

        @Override
        @NonNull
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_MAGIC_EMOJ) {
                return new StoreLottieViewHolder(LayoutInflater.from(mContext).
                        inflate(R.layout.emoji_store_item_lottie, parent, false));
            } else {
                return new StoreViewHolder(LayoutInflater.from(mContext).
                        inflate(R.layout.emoji_store_item, parent, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_MAGIC_EMOJ;
            } else {
                return TYPE_NORMAL;
            }
        }

        @Override
        public int getItemCount() {
            return mStoreEmojiPackageInfoList.size();
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            EmojiPackageInfo packageInfo = mStoreEmojiPackageInfoList.get(position);
            Resources res = getResources();
            if (holder.getItemViewType() == TYPE_NORMAL) {
                StoreViewHolder storeViewHolder = (StoreViewHolder) holder;
                if (EmojiManager.isTabSticker(packageInfo.mName)) {
                    storeViewHolder.getBtn.setOnClickListener(null);
                    storeViewHolder.getBtn.setText(res.getString(R.string.emoji_added));
                    storeViewHolder.getBtn.setTextColor(0xFFFFFFFF);
                    storeViewHolder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(15), true));
                } else {
                    storeViewHolder.getBtn.setText(res.getString(R.string.emoji_get));
                    storeViewHolder.getBtn.setTextColor(0xFF333333);
                    storeViewHolder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFF4BE3E, 0xFFDAA017, Dimensions.pxFromDp(15), false, true));
                    storeViewHolder.getBtn.setOnClickListener(v -> {
                        if (!TextUtils.isEmpty(mSource)) {
                            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_StoreList_Get", true, "type", packageInfo.mName, "source", mSource);
                        }
                        storeViewHolder.getBtn.setOnClickListener(null);
                        storeViewHolder.getBtn.setText(res.getString(R.string.emoji_added));
                        storeViewHolder.getBtn.setTextColor(0xFFFFFFFF);
                        storeViewHolder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(15), true));
                        EmojiManager.addTabSticker(packageInfo.mName);
                        HSBundle bundle = new HSBundle();
                        bundle.putObject(EmojiPickerFragment.NOTIFICATION_BUNDLE_PACKAGE_INFO, packageInfo);
                        HSGlobalNotificationCenter.sendNotification(EmojiPickerFragment.NOTIFICATION_ADD_EMOJI_FROM_STORE, bundle);
                    });
                }

                storeViewHolder.previewLayout.bindEmojiItems(packageInfo);

                storeViewHolder.itemView.setOnClickListener(v -> {
                    if (!TextUtils.isEmpty(packageInfo.mName)) {
                        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_StoreList_Click", true, "type", packageInfo.mName);
                    }
                    EmojiDetailActivity.start(mSource, getActivity(), packageInfo);
                });
                GlideRequests imageRequest = GlideApp.with(mContext);
                imageRequest.asBitmap()
                        .load(packageInfo.mBannerUrl)
                        .placeholder(mColorDrawable)
                        .error(mColorDrawable)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(storeViewHolder.image);
            } else if (holder.getItemViewType() == TYPE_MAGIC_EMOJ) {
                StoreLottieViewHolder storeViewHolder = (StoreLottieViewHolder) holder;
                storeViewHolder.getBtn.setOnClickListener(null);
                storeViewHolder.getBtn.setText(res.getString(R.string.emoji_added));
                storeViewHolder.getBtn.setTextColor(0xFFFFFFFF);
                storeViewHolder.getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(15), true));
                storeViewHolder.previewLayout.bindEmojiItemsForLottie();
                storeViewHolder.itemView.setOnClickListener(v -> {
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_StoreList_Click", true, "type", "Magic Emoji");
                    Intent intent = new Intent(getActivity(), EmojiLottieDetailActivity.class);
                    getActivity().startActivity(intent);

                });
            }


        }

        void updateItem(String name) {
            for (int i = 0; i < mStoreEmojiPackageInfoList.size(); i++) {
                EmojiPackageInfo packageInfo = mStoreEmojiPackageInfoList.get(i);
                if (packageInfo.mName.equals(name)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    private class StoreViewHolder extends RecyclerView.ViewHolder {

        RoundImageView image;
        MessagesTextView getBtn;
        EmojiStorePreviewLayout previewLayout;

        StoreViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.emoji_store_item_image);
            getBtn = itemView.findViewById(R.id.emoji_store_item_get_btn);
            previewLayout = itemView.findViewById(R.id.preview_layout);
        }
    }

    private class StoreLottieViewHolder extends RecyclerView.ViewHolder {

        LottieAnimationView image;
        MessagesTextView getBtn;
        EmojiStorePreviewLayout previewLayout;

        StoreLottieViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.emoji_store_item_lottie);
            getBtn = itemView.findViewById(R.id.emoji_store_item_get_btn);
            previewLayout = itemView.findViewById(R.id.preview_layout);
        }
    }

    private static class StoreItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view);
            int column = position % MAX_COLUMNS;

            int dp3_5 = Dimensions.pxFromDp(3.5f);
            int dp10 = Dimensions.pxFromDp(10);
            int dp9 = Dimensions.pxFromDp(9);

            if (column == 0) {
                outRect.left = dp10;
                outRect.right = dp3_5;
            } else if (column == (MAX_COLUMNS - 1)) {
                outRect.left = dp3_5;
                outRect.right = dp10;
            } else {
                outRect.left = dp3_5;
                outRect.right = dp3_5;
            }

            if (position < MAX_COLUMNS) {
                outRect.top = Dimensions.pxFromDp(14);
                outRect.bottom = dp9;
            } else {
                outRect.bottom = dp9;
            }
        }
    }
}
