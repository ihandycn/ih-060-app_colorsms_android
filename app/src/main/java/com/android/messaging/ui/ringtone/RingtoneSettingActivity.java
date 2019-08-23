package com.android.messaging.ui.ringtone;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
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
import com.superapps.util.Navigations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class RingtoneSettingActivity extends BaseActivity implements RingtoneSettingAdapter.OnAppRingtoneSelected {

    public static final int REQUEST_CODE_START_SYSTEM_RINGTONE_PICKER = 1;
    public static final int REQUEST_CODE_START_FILE_RINGTONE_PICKER = 2;

    public static final String EXTRA_CUR_RINGTONE_INFO = "extra_cur_ringtone_info";

    private ViewGroup mSystemItemView;
    private ViewGroup mFileItemView;
    private RecyclerView mRecyclerView;

    private RingtoneSettingAdapter mAdapter;
    private RingtoneInfo mCurRingtoneInfo;
    private boolean mIsFromNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singstone_setting);

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
        mSystemItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSystemItemClick();
                BugleAnalytics.logEvent("Ringtone_Page_System_Click");
            }
        });
        mFileItemView = findViewById(R.id.file_item);
        mFileItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFileItemClick();
                BugleAnalytics.logEvent("Ringtone_Page_Music_Click");
            }
        });

        RingtoneEntranceAutopilotUtils.logRingtonePageShow();
        BugleAnalytics.logEvent("Ringtone_Page_Show");
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
        Navigations.startActivityForResultSafely(this, intent, REQUEST_CODE_START_FILE_RINGTONE_PICKER);
    }

    private List<RingtoneInfo> getApplicationRingtones() {
        return RingtoneInfoManager.getAppRingtoneInfoFromConfig();
    }

    private RingtoneInfo getCurRingtoneUri() {
        Intent intent = getIntent();
        RingtoneInfo info = intent.getParcelableExtra(EXTRA_CUR_RINGTONE_INFO);
        if (info == null) {
            // if intent's data is null, maybe the page is start from navigation view
            mIsFromNavigation = true;
            info = RingtoneInfoManager.getCurSound();
        }
        return info;
    }

    private void putRingtoneIntoIntent(RingtoneInfo info) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CUR_RINGTONE_INFO, info);
        setResult(RESULT_OK, intent);
        if (mIsFromNavigation) {
            RingtoneInfoManager.setCurSound(info);
        }
        RingtoneEntranceAutopilotUtils.logRingtonePageSet();
        BugleAnalytics.logEvent("Ringtone_Page_Set", true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_SYSTEM_RINGTONE_PICKER) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (data == null) {
                return;
            }
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String uriStr = uri == null ? RingtoneInfoManager.SILENT_URI : uri.toString();
            putRingtoneIntoIntent(RingtoneInfoManager.getSystemRingtoneInfo(uriStr));
            finish();
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
            uri = FileProvider.getUriForFile(this, getResources().getString(R.string.file_provider), file);
            RingtoneInfo info = new RingtoneInfo();
            info.uri = uri.toString();
            info.name = fileName;
            info.type = RingtoneInfo.TYPE_FILE;
            putRingtoneIntoIntent(info);

            finish();
        }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    @Override
    public void onAppRingtoneSelected(RingtoneInfo info) {
        RingtoneEntranceAutopilotUtils.logAppRingtoneSet();
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
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mAdapter.onActivityChange();
        super.onPause();
    }
}
