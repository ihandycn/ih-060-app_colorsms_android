package com.android.messaging.ui.emoji;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.messaging.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;

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
                EmojiFragment.newInstance(),
                EmojiFragment.FRAGMENT_TAG).commit();

        findViewById(R.id.emoji_store_back_btn).setOnClickListener(v -> this.finish());
    }
}
