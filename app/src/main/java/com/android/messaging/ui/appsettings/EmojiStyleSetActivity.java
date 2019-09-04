package com.android.messaging.ui.appsettings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.emoji.EmojiStyleGuideView;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.EmojiStyleDownloadManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Compats;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmojiStyleSetActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private ChooseEmojiStyleAdapter mAdapter;

    private String newStyle = null;

    private boolean mFromGuide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_style_set);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.emoji_style_setting));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            mFromGuide = getIntent().getBooleanExtra(EmojiStyleGuideView.EXTRA_INTENT_FROM_GUIDE, false);
        }
        String from = mFromGuide ? "guide" : "settings";
        BugleAnalytics.logEvent("Settings_EmojiStyle_Show", true, "from", from);

        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new ChooseEmojiStyleAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ((DefaultItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        initEmojiStyles();
    }

    public void initEmojiStyles() {
        List<Map<String, String>> list = EmojiManager.getAllEmojiStyles();
        List<ChooseEmojiStyleAdapter.EmojiStyleItem> dataList = new ArrayList<>();
        dataList.add(new ChooseEmojiStyleAdapter.EmojiStyleItem());
        for (Map<String, String> map : list) {
            ChooseEmojiStyleAdapter.EmojiStyleItem item = new ChooseEmojiStyleAdapter.EmojiStyleItem(
                    map.get("name"),
                    map.get("DownloadSize"),
                    map.get("PreViewPicUrl"),
                    map.get("DownloadUrl")
            );
            item.setDownloaded(EmojiManager.isEmojiStyleDownloaded(item.name));
            dataList.add(item);
        }
        fixDataList(dataList);
        String curStyle = EmojiManager.getEmojiStyle();
        for (int i = 0; i < dataList.size(); i++) {
            ChooseEmojiStyleAdapter.EmojiStyleItem item = dataList.get(i);
            if (item.name.equals(curStyle)) {
                mAdapter.setDefaultSelectPos(i);
                break;
            }
        }

        mAdapter.setDataList(dataList);
        for (int i = 0; i < dataList.size(); i++) {
            ChooseEmojiStyleAdapter.EmojiStyleItem item = dataList.get(i);
            EmojiStyleDownloadManager.getInstance().rebindDownloadTask(item.name,
                    new SettingDownloadCallback(item, new ChooseEmojiStyleAdapter.UiDownloadCallback(mAdapter, item, i), this));
        }
        mAdapter.setItemSelectListener(new ChooseEmojiStyleAdapter.EmojiStyleItemSelectListener() {
            @Override
            public void onItemSelected(ChooseEmojiStyleAdapter.EmojiStyleItem item, EmojiStyleDownloadManager.DownloadCallback callback) {
                if (!item.isDownloaded && !item.isSystem) {
                    String from = mFromGuide ? "guide" : "settings";
                    BugleAnalytics.logEvent("Settings_EmojiStyle_Download", true, "type", item.name, "from", from);
                    EmojiStyleDownloadManager.getInstance().downloadEmojiStyle(item.downloadUrl, item.name,
                            new SettingDownloadCallback(item, callback, EmojiStyleSetActivity.this));
                } else {
                    setNewEmojiStyle(item);
                }
            }

            @Override
            public void onItemUnSelected(ChooseEmojiStyleAdapter.EmojiStyleItem item) {
                EmojiStyleDownloadManager.getInstance().cancelDownload(item.name);
            }
        });
    }

    private void fixDataList(List<ChooseEmojiStyleAdapter.EmojiStyleItem> dataList) {

        if (Compats.IS_SAMSUNG_DEVICE) {
            // samsung has a special emoji style
            return;
        }
        String name = null;
        if (Build.VERSION.SDK_INT <= 25) {
            name = "Android Blob";
        } else {
            name = "Android Pie";
        }
        ChooseEmojiStyleAdapter.EmojiStyleItem removeItem = null;
        for (ChooseEmojiStyleAdapter.EmojiStyleItem item : dataList) {
            if (item.name.equals(name)) {
                removeItem = item;
                break;
            }
        }
        dataList.remove(removeItem);
    }

    private void setNewEmojiStyle(ChooseEmojiStyleAdapter.EmojiStyleItem item) {
        newStyle = item.name;
        Intent intent = new Intent();
        intent.putExtra("name", item.name);
        intent.putExtra("url", item.sampleImageUrl);
        setResult(RESULT_OK, intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (newStyle != null) {
            EmojiManager.setEmojiStyle(newStyle);
        }
        super.onDestroy();
    }

    private static class SettingDownloadCallback implements EmojiStyleDownloadManager.DownloadCallback {
        private EmojiStyleDownloadManager.DownloadCallback uiCallback;
        private ChooseEmojiStyleAdapter.EmojiStyleItem item;
        private WeakReference<EmojiStyleSetActivity> outer;

        public SettingDownloadCallback(ChooseEmojiStyleAdapter.EmojiStyleItem item, EmojiStyleDownloadManager.DownloadCallback downloadCallback, EmojiStyleSetActivity outer) {
            this.item = item;
            this.uiCallback = downloadCallback;
            this.outer = new WeakReference<>(outer);
        }

        @Override
        public void onFail(EmojiStyleDownloadManager.EmojiStyleDownloadTask task, String msg) {
            uiCallback.onFail(task, msg);
        }

        @Override
        public void onSuccess(EmojiStyleDownloadManager.EmojiStyleDownloadTask task) {
            uiCallback.onSuccess(task);
            Toast.makeText(HSApplication.getContext(),
                    item.name + " " + HSApplication.getContext().getResources().getString(R.string.emoji_style_download_success),
                    Toast.LENGTH_SHORT)
                    .show();
            Map<String, String> log = new HashMap<>();
            log.put("type", item.name);
            BugleAnalytics.logEvent("Settings_EmojiStyle_Download_Success", true, log);
            if (this.outer.get() != null && !this.outer.get().isDestroyed()) {
                outer.get().setNewEmojiStyle(item);
            }
        }

        @Override
        public void onUpdate(long downloadSize, long totalSize) {
            uiCallback.onUpdate(downloadSize, totalSize);
        }

        @Override
        public void onCancel() {
            uiCallback.onCancel();
        }
    }
}

