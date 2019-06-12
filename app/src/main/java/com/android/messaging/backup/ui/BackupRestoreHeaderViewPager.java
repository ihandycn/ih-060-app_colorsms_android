package com.android.messaging.backup.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.R;
import com.android.messaging.ui.CustomViewPager;

public class BackupRestoreHeaderViewPager extends CustomViewPager {

    public BackupRestoreHeaderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.backup_restore_header_view_pager;
    }
}
