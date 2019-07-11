package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.DisplayUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class EmojiVariantPopup {
    private final EmojiPackagePagerAdapter.OnEmojiClickListener mListener;
    private PopupWindow mPopupWindow;
    private View mAnchorView;
    private final View mRootView;

    public EmojiVariantPopup(View rootView, EmojiPackagePagerAdapter.OnEmojiClickListener onEmojiInfoClickListener) {
        this.mRootView = rootView;
        this.mListener = onEmojiInfoClickListener;
    }

    public void show(View anchorView, EmojiInfo emojiInfo) {
        dismiss();
        this.mAnchorView = anchorView;
        View initView = initView(anchorView.getContext(), emojiInfo, anchorView.getWidth());
        this.mPopupWindow = new PopupWindow(initView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mPopupWindow.setFocusable(true);
        this.mPopupWindow.setOutsideTouchable(true);
        this.mPopupWindow.setInputMethodMode(2);
        this.mPopupWindow.setBackgroundDrawable(new BitmapDrawable(anchorView.getContext().getResources(), (Bitmap) null));
        initView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        Point locationOnScreen = locationOnScreen(mAnchorView);
        Point point = new Point((locationOnScreen.x - (initView.getMeasuredWidth() / 2)) + (mAnchorView.getWidth() / 2), locationOnScreen.y - initView.getMeasuredHeight());
        this.mPopupWindow.showAtLocation(this.mRootView, 0, point.x, point.y);
        this.mAnchorView.getParent().requestDisallowInterceptTouchEvent(true);
        fixPopupLocation(this.mPopupWindow, point);
    }

    private Point locationOnScreen(View view) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        return new Point(iArr[0], iArr[1]);
    }

    private void fixPopupLocation(final PopupWindow popupWindow, final Point point) {
        popupWindow.getContentView().post(new Runnable() {
            public void run() {
                Point locationOnScreen = locationOnScreen(popupWindow.getContentView());
                if (locationOnScreen.x != point.x || locationOnScreen.y != point.y) {
                    int i;
                    int i2;
                    int i3 = locationOnScreen.x - point.x;
                    int i4 = locationOnScreen.y - point.y;
                    if (locationOnScreen.x > point.x) {
                        i = point.x - i3;
                    } else {
                        i = point.x + i3;
                    }
                    if (locationOnScreen.y > point.y) {
                        i2 = point.y - i4;
                    } else {
                        i2 = point.y + i4;
                    }
                    popupWindow.update(i, i2, -1, -1);
                }
            }
        });
    }

    public void dismiss() {
        this.mAnchorView = null;
        if (this.mPopupWindow != null) {
            this.mPopupWindow.dismiss();
            this.mPopupWindow = null;
        }
    }

    private View initView(Context context, EmojiInfo emojiInfo, int width) {
        View inflate = View.inflate(context, R.layout.emoji_skin_popup, null);
        LinearLayout linearLayout = (LinearLayout) inflate.findViewById(R.id.container);
        EmojiInfo[] variants = emojiInfo.mVariants;
        LayoutInflater from = LayoutInflater.from(context);
        for (final EmojiInfo item : variants) {
            View container = from.inflate(R.layout.emoji_item_layout, linearLayout, false);
            ImageView view = container.findViewById(R.id.emoji_view);
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) view.getLayoutParams();
            int dpToPx = (int) DisplayUtils.dpToPx(context, 2.0f);
            marginLayoutParams.width = width;
            marginLayoutParams.setMargins(dpToPx, dpToPx, dpToPx, dpToPx);
            view.setImageDrawable(new EmojiItemRecyclerAdapter.EmojiDrawable(item.mEmoji));
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (mListener != null) {
                        item.isRecent = emojiInfo.isRecent;
                        if(emojiInfo.isRecent) {
                            mListener.emojiClick(item, false);
                        }else{
                            mListener.emojiClick(item, true);
                        }
                    }
                    EmojiManager.addSkinSingleRecord(item.getUnicode(), item.mEmoji);
                    emojiInfo.mEmoji = item.mEmoji;
                    ((ImageView)mAnchorView).setImageDrawable(new EmojiItemRecyclerAdapter.EmojiDrawable(emojiInfo.mEmoji));
                    mPopupWindow.dismiss();
                }
            });
            view.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    context.getResources().getColor(android.R.color.white), Dimensions.pxFromDp(16), true));

            // cancel the layout_margin_top of the item xml
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(container.getLayoutParams());
            lp.topMargin = 0;
            lp.bottomMargin = 0;
            container.setLayoutParams(lp);
            linearLayout.addView(container);
        }
        return inflate;
    }
}
