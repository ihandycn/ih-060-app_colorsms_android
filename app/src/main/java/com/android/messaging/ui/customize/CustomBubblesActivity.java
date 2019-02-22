package com.android.messaging.ui.customize;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.messaging.R;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomViewPager;

public class CustomBubblesActivity extends AppCompatActivity  {

    private CustomHeaderViewPager mCustomHeaderViewPager;
    private BubbleStyleViewHolder mBubbleStyleViewHolder;
    private BubbleColorViewHolder mBubbleColorViewHolder;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_bubbles_activity);
        initActionBar();
        mCustomHeaderViewPager = findViewById(R.id.customize_pager);

        mBubbleStyleViewHolder = new BubbleStyleViewHolder(this);
        mBubbleColorViewHolder = new BubbleColorViewHolder(this);

        final CustomPagerViewHolder[] viewHolders = {
                mBubbleStyleViewHolder,
                mBubbleColorViewHolder};

        mCustomHeaderViewPager = findViewById(R.id.customize_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomHeaderViewPager.setCurrentItem(0);
    }

    public void openCustomizeColor(int type) {

    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
