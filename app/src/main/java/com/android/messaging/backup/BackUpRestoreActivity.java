package com.android.messaging.backup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.util.UiUtils;


public class BackUpRestoreActivity extends BaseActivity{

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.backup_restore_activity);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");

        TextView title = mToolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_backup_restore_toolbar_title));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initPager(this);

        UiUtils.setTitleBarBackground(mToolbar, this);
    }

    private void initPager(Context context){
        ChooseBackUpViewHolder mBackUpViewHolder = new ChooseBackUpViewHolder(context);
        ChooseRestoreViewHolder mRestoreViewHolder = new ChooseRestoreViewHolder(context);
        final CustomPagerViewHolder[] viewHolders = {
                mBackUpViewHolder,
                mRestoreViewHolder};
        CustomHeaderViewPager mCustomHeaderViewPager = findViewById(R.id.backup_header_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomHeaderViewPager.setCurrentItem(0);
        mCustomHeaderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
