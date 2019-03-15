package com.android.messaging.ui.emoji;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class EmojiStoreActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, EmojiStoreActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emoji_store_layout);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(
                R.id.emoji_store_layout,
                EmojiStoreFragment.newInstance("char_tab"),
                EmojiStoreFragment.FRAGMENT_TAG).commit();
        View backBtn = findViewById(R.id.emoji_store_back_btn);
        backBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(17.5f), true));
        backBtn.setOnClickListener(v -> this.finish());
        View titleView = findViewById(R.id.title_rl);

        try {
            titleView.setBackground(new ColorDrawable(PrimaryColors.getPrimaryColor()));
            UiUtils.setStatusBarColor(this, PrimaryColors.getPrimaryColorDark());
        } catch (IllegalArgumentException e) {
            titleView.setBackground(new ColorDrawable(getResources().getColor(R.color.action_bar_background_color)));
            UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));
        }

    }
}
