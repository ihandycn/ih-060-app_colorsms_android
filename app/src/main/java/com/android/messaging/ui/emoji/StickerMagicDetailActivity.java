package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.download.Downloader;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;

import java.io.File;

public class StickerMagicDetailActivity extends HSAppCompatActivity implements View.OnClickListener {

    private static final String TAG = StickerMagicDetailActivity.class.getSimpleName();
    static final String INTENT_KEY_EMOJI_INFO = "emoji_info";

    public final static String NOTIFICATION_SEND_MAGIC_STICKER = "notification_send_magic_sticker";
    public final static String BUNDLE_SEND_MAGIC_STICKER_DATA = "bundle_send_magic_sticker_data";
    private static final String FROM_WHERE = "from_where";
    public static final String FROM_EMOJ_STORE = "from_emoj_store";

    public static void start(Context context, StickerInfo stickerInfo) {
        Intent starter = new Intent(context, StickerMagicDetailActivity.class);
        starter.putExtra(INTENT_KEY_EMOJI_INFO, stickerInfo);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.startActivity(starter);
        }
    }

    public static void start(Context context, StickerInfo stickerInfo, String from) {
        Intent starter = new Intent(context, StickerMagicDetailActivity.class);
        starter.putExtra(INTENT_KEY_EMOJI_INFO, stickerInfo);
        starter.putExtra(FROM_WHERE, from);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.startActivity(starter);
        }


    }

    private StickerInfo mStickerInfo;
    private StickerMagicView mStickerMagicView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_magic_layout);
        String from = "";
        if (getIntent() != null) {
            from = getIntent().getStringExtra(FROM_WHERE);
            mStickerInfo = getIntent().getParcelableExtra(INTENT_KEY_EMOJI_INFO);
        }
        if (TextUtils.equals(FROM_EMOJ_STORE, from)) {
            BugleAnalytics.logEvent("SMSEmoji_Store_Magic_View", true, "type", StickerInfo.getNumFromUrl(mStickerInfo.mMagicUrl));
            findViewById(R.id.send_btn).setVisibility(View.GONE);

        } else {
            findViewById(R.id.send_btn).setOnClickListener(this);
        }
        findViewById(R.id.emoji_show_close).setOnClickListener(this);
        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_View", true, "type", StickerInfo.getNumFromUrl(mStickerInfo.mMagicUrl));
        File file = Downloader.getInstance().getDownloadFile(mStickerInfo.mMagicUrl);
        if (!file.exists()) {
            finish();
        } else {
            Uri gifUri = Uri.fromFile(file);
            EmojiManager.addStickerMagicFileUri(gifUri.toString());
            mStickerMagicView = new StickerMagicView();
            mStickerMagicView.setupView(findViewById(R.id.magic_container_view), gifUri, mStickerInfo.mSoundUrl, (width, height) -> {
                mStickerInfo.mStickerWidth = width;
                mStickerInfo.mStickerHeight = height;
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emoji_show_close:
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Cancel", true);
                finish();
                break;
            case R.id.send_btn:
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Select_Click", true, "type", StickerInfo.getNumFromUrl(mStickerInfo.mMagicUrl));
                HSBundle bundle = new HSBundle();
                bundle.putObject(BUNDLE_SEND_MAGIC_STICKER_DATA, mStickerInfo);
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SEND_MAGIC_STICKER, bundle);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Cancel", true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStickerMagicView != null && mStickerMagicView.getSoundPlayer() != null) {
            mStickerMagicView.getSoundPlayer().pause();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStickerMagicView != null) {
            mStickerMagicView.release();
        }
    }
}
