package com.android.messaging.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackagePagerAdapter;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.DisplayUtils;

import java.util.List;

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
        initView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
//        Point locationOnScreen = Utils.locationOnScreen(mAnchorView);
//        Point point = new Point((locationOnScreen.x - (initView.getMeasuredWidth() / 2)) + (mAnchorView.getWidth() / 2), locationOnScreen.y - initView.getMeasuredHeight());
//        this.mPopupWindow.showAtLocation(this.mRootView, 0, point.x, point.y);
//        this.mAnchorView.getParent().requestDisallowInterceptTouchEvent(true);
//        Utils.fixPopupLocation(this.mPopupWindow, point);

        this.mPopupWindow.showAsDropDown(anchorView);
    }

    public void dismiss() {
        this.mAnchorView = null;
        if (this.mPopupWindow != null) {
            this.mPopupWindow.dismiss();
            this.mPopupWindow = null;
        }
    }

    private View initView(Context context, EmojiInfo emojiInfo, int i) {
        View inflate = View.inflate(context, R.layout.emoji_skin_popup, null);
        LinearLayout linearLayout = (LinearLayout) inflate.findViewById(R.id.container);
        EmojiInfo[] variants = emojiInfo.mVariants;
        LayoutInflater from = LayoutInflater.from(context);
        for (final EmojiInfo item : variants) {
            TextView view = (TextView) from.inflate(R.layout.emoji_item_layout, linearLayout, false);
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) view.getLayoutParams();
            int dpToPx = (int) DisplayUtils.dpToPx(context, 2.0f);
            marginLayoutParams.width = i;
            marginLayoutParams.setMargins(dpToPx, dpToPx, dpToPx, dpToPx);
            view.setText(item.mEmoji);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.emojiClick(item);
                    }
                    EmojiManager.addSkinSingleRecord(item.getUnicode(), item.mEmoji);
                    emojiInfo.mEmoji = item.mEmoji;
                    ((TextView)mAnchorView.findViewById(R.id.emoji_text)).setText(emojiInfo.mEmoji);
                }
            });
            linearLayout.addView(view);
        }
        return inflate;
    }
}
