package com.android.messaging.ui.emoji;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.android.messaging.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class EmojiStoreActivity extends HSAppCompatActivity {

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

        ImageView backBtn = findViewById(R.id.emoji_store_back_btn);
        backBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0x33000000, Dimensions.pxFromDp(18), false, true));
        backBtn.setOnClickListener(v -> this.finish());
    }
}
