package com.android.messaging.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.appsettings.BaseItemView;
import com.android.messaging.ui.appsettings.SystemEmojiStylePreview;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Dimensions;

import java.util.List;
import java.util.Map;

public class SettingEmojiStyleItemView extends BaseItemView {

    public SettingEmojiStyleItemView(Context context) {
        super(context);
    }

    public SettingEmojiStyleItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingEmojiStyleItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ImageView imageView;

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        super.initView(context, attrs);

        ViewGroup container = findViewById(R.id.widget_frame);
        container.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        container.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        imageView = new ImageView(context);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Dimensions.pxFromDp(44.7f), Dimensions.pxFromDp(44.7f));
        imageView.setLayoutParams(lp);
        container.addView(imageView);

        String curStyleName = EmojiManager.getEmojiStyle();
        this.mSummaryView.setVisibility(VISIBLE);
        this.mSummaryView.setText(curStyleName);
        if (EmojiManager.isSystemEmojiStyle()) {
            imageView.setImageDrawable(new SystemEmojiStylePreview());
        } else {
            List<Map<String, String>> emojiStyles = EmojiManager.getAllEmojiStyles();
            for (Map<String, String> item : emojiStyles) {
                String styleName = item.get("name");
                if (styleName != null && styleName.equals(curStyleName)) {
                    imageView.setImageDrawable(EmojiManager.getEmojiStyleResource(styleName));
                    break;
                }
            }
        }
    }

    public void update(String name){
        this.mSummaryView.setText(name);
        if(name.equals(EmojiManager.EMOJI_STYLE_SYSTEM)){
            imageView.setImageDrawable(new SystemEmojiStylePreview());
        }else {
            imageView.setImageDrawable(EmojiManager.getEmojiStyleResource(name));
        }
    }
}
