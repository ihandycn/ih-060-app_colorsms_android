package com.android.messaging.ui.customize.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class ThemePreviewPagerAdapter extends PagerAdapter {

    private final int mCount;

    private final List<View> mItemList = new ArrayList<>();

    ThemePreviewPagerAdapter(Context context, ThemeInfo theme) {
        mCount = theme.mPreviewList.size();

        for (int i = 0; i < mCount; i++) {
            @SuppressLint("InflateParams")
            View item = LayoutInflater.from(context).inflate(R.layout.choose_theme_pager_item, null,false);

            ImageView imageView = item.findViewById(R.id.theme_preview_image);
            item.findViewById(R.id.current_theme_tag).setVisibility(View.GONE);
            if (theme.mIsLocalTheme) {
                imageView.setImageDrawable(ThemeUtils.getLocalThemeDrawableFromPath(
                        theme.mThemeKey + "/" + theme.mPreviewList.get(i)));
            } else {
                Drawable placeholder = context.getResources().getDrawable(R.drawable.theme_detail_placeholder);
                placeholder.setBounds(0,0, Dimensions.pxFromDp(30), Dimensions.pxFromDp(30));
                GlideApp.with(context)
                        .asBitmap()
                        .load(ThemeDownloadManager.getBaseRemoteUrl()
                                + theme.mThemeKey + "/" + theme.mPreviewList.get(i))
                        .placeholder(placeholder)
                        .error(R.drawable.theme_detail_glide_failed)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
            }
            mItemList.add(item);
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mItemList.get(position));
        return mItemList.get(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mItemList.get(position));
    }
}
