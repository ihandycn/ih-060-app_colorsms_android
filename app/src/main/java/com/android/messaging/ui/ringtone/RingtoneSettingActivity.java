package com.android.messaging.ui.ringtone;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.appsettings.RingtoneEntranceAutopilotUtils;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.UriUtil;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Navigations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class RingtoneSettingActivity extends BaseActivity implements RingtoneSettingAdapter.OnAppRingtoneSelected {

    public static final int REQUEST_CODE_START_SYSTEM_RINGTONE_PICKER = 1;
    public static final int REQUEST_CODE_START_FILE_RINGTONE_PICKER = 2;

    public static final String EXTRA_CUR_RINGTONE_INFO = "extra_cur_ringtone_info";
    public static final String EXTRA_FROM_PAGE = "extra_from_page";
    public static final String FROM_SETTING = "settings";
    public static final String FROM_DETAILS_PAGE = "detailspage";

    private ViewGroup mSystemItemView;
    private ViewGroup mFileItemView;
    private RecyclerView mRecyclerView;

    private RingtoneSettingAdapter mAdapter;
    private RingtoneInfo mCurRingtoneInfo;
    private RingtoneInfo mSetInfo = null;
    private boolean mIsFromNavigation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.ringtone));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mCurRingtoneInfo = getCurRingtoneUri();

        TextView ringtoneTitle = findViewById(R.id.ringtone_title);
        ringtoneTitle.setTextColor(PrimaryColors.getPrimaryColor());

        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RingtoneSettingAdapter(getApplicationRingtones(), mCurRingtoneInfo);
        mAdapter.setHost(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSystemItemView = findViewById(R.id.system_item);
        mSystemItemView.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        mSystemItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSystemItemClick();
                BugleAnalytics.logEvent("Ringtone_Page_System_Click");
            }
        });
        mFileItemView = findViewById(R.id.file_item);
        mFileItemView.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
        mFileItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFileItemClick();
                BugleAnalytics.logEvent("Ringtone_Page_Music_Click");
            }
        });

        RingtoneEntranceAutopilotUtils.logRingtonePageShow();
    }

    private void onSystemItemClick() {
        Intent ringtonePickerIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                mCurRingtoneInfo.uri.equals(RingtoneInfoManager.SILENT_URI) ? null : Uri.parse(mCurRingtoneInfo.uri));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
        Navigations.startActivityForResultSafely(this, ringtonePickerIntent, REQUEST_CODE_START_SYSTEM_RINGTONE_PICKER);
        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
    }

    private void onFileItemClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Navigations.startActivityForResultSafely(this, intent, REQUEST_CODE_START_FILE_RINGTONE_PICKER);
    }

    private List<RingtoneInfo> getApplicationRingtones() {
        return RingtoneInfoManager.getAppRingtoneInfoFromConfig();
    }

    private RingtoneInfo getCurRingtoneUri() {
        Intent intent = getIntent();
        RingtoneInfo info = intent.getParcelableExtra(EXTRA_CUR_RINGTONE_INFO);
        String from = intent.getStringExtra(RingtoneSettingActivity.EXTRA_FROM_PAGE);
        if (from == null) {
            // if intent's data "from" is null, maybe the page is start from navigation view
            mIsFromNavigation = true;
            info = RingtoneInfoManager.getCurSound();
            return info;
        }
        BugleAnalytics.logEvent("Ringtone_Page_Show", true, "from", from);

        return info;
    }

    private void putRingtoneIntoIntent(RingtoneInfo info) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CUR_RINGTONE_INFO, info);
        setResult(RESULT_OK, intent);
        mSetInfo = info;
        if (info.type != RingtoneInfo.TYPE_APP) {
            mAdapter.clearChoose();
        }

        switch (info.type) {
            case RingtoneInfo.TYPE_APP:
                BugleAnalytics.logEvent("Ringtone_Page_Set", true, "type", info.name);
                break;
            case RingtoneInfo.TYPE_SYSTEM:
                BugleAnalytics.logEvent("Ringtone_Page_Set", true, "type", "system");
                break;
            case RingtoneInfo.TYPE_FILE:
                BugleAnalytics.logEvent("Ringtone_Page_Set", true, "type", "music");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_SYSTEM_RINGTONE_PICKER) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (data == null) {
                putRingtoneIntoIntent(RingtoneInfoManager.getSystemRingtoneInfo(RingtoneInfoManager.SILENT_URI));
                return;
            }
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (UriUtil.isFileUri(uri)) {
                handleUriFromFile(uri);
                return ;
            }
            String uriStr = uri == null ? RingtoneInfoManager.SILENT_URI : uri.toString();
            putRingtoneIntoIntent(RingtoneInfoManager.getSystemRingtoneInfo(uriStr));
        } else if (requestCode == REQUEST_CODE_START_FILE_RINGTONE_PICKER) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            handleUriFromFile(uri);
        }
    }

    private void handleUriFromFile(Uri uri) {
        File file = new File(getFilesDir(), "file_ringtone");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            InputStream fis = getContentResolver().openInputStream(uri);
            if (fis == null) {
                throw new Exception();
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[2 * 1024];
            while (fis.read(buffer) != -1) {
                fos.write(buffer);
            }
            fos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String fileName = getFileFromContentUri(uri, this);
        if (Build.VERSION.SDK_INT < 24) {
            uri = Uri.fromFile(file);
        } else {
            uri = FileProvider.getUriForFile(this, getResources().getString(R.string.file_provider), file);
        }
        RingtoneInfo info = new RingtoneInfo();
        info.uri = uri.toString();
        info.name = fileName;
        info.type = RingtoneInfo.TYPE_FILE;
        // add grant to system process to read our file provider
        getApplicationContext().grantUriPermission("com.android.systemui",
                Uri.parse(info.uri), Intent.FLAG_GRANT_READ_URI_PERMISSION);

        putRingtoneIntoIntent(info);
    }

    public static String getFileFromContentUri(Uri contentUri, Context context) {
        if (contentUri == null) {
            return null;
        }
        String fileName = null;
        String[] filePathColumn = {MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, null, null,
                null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            try {
                fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    @Override
    public void onAppRingtoneSelected(RingtoneInfo info) {
        putRingtoneIntoIntent(info);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (mSetInfo != null) {
            if (mIsFromNavigation) {
                RingtoneInfoManager.setCurSound(mSetInfo);
            }
            RingtoneEntranceAutopilotUtils.logRingtonePageSet();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mAdapter.onActivityChange();
        super.onPause();
    }
}
