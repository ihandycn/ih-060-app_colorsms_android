package com.android.messaging.ui.emoji;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.appsettings.EmojiStyleSetActivity;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class EmojiStyleGuideView extends FrameLayout {

    public static final String EXTRA_INTENT_FROM_GUIDE = "extra_intent_from_guide";

    public EmojiStyleGuideView(Context context) {
        super(context);
        initView(context);
    }

    public EmojiStyleGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public EmojiStyleGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.emoji_style_set_guide, this, false);
        addView(view);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        ImageView cancelBtn = view.findViewById(R.id.cancel_btn);
        cancelBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(15.4f), true));
        MessagesTextView okBtn = view.findViewById(R.id.ok_btn);

        int times = EmojiManager.getEmojiStyleGuideShowTimes();
        BugleAnalytics.logEvent("Detailspage_EmojiStyleGuide_Show", true, "showtime", times + "");

        okBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff148dea, 0xffffffff, Dimensions.pxFromDp(17f), false, true));
        okBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EmojiStyleSetActivity.class);
                intent.putExtra(EXTRA_INTENT_FROM_GUIDE, true);
                context.startActivity(intent);

                EmojiManager.disableEmojiStyleGuide();
                BugleAnalytics.logEvent("Settings_EmojiStyle_Click", true, "showtime", times + "");

                ((ViewGroup) EmojiStyleGuideView.this.getParent()).removeView(EmojiStyleGuideView.this);
            }
        });

        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) EmojiStyleGuideView.this.getParent()).removeView(EmojiStyleGuideView.this);
            }
        });

        if (UiUtils.getKeyboardHeight() > Dimensions.pxFromDp(280)) {
            view.setScaleX(1.1f);
            view.setScaleY(1.1f);
            MarginLayoutParams mlp = (MarginLayoutParams) view.getLayoutParams();
            mlp.topMargin += Dimensions.pxFromDp(3) + Dimensions.pxFromDp(34.7f) / 2;
            view.requestLayout();
        } else {
            MarginLayoutParams mlp = (MarginLayoutParams) view.getLayoutParams();
            mlp.topMargin += Dimensions.pxFromDp(2);
            view.requestLayout();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return true;  // consume touch event, don't transform to next view
    }
}
