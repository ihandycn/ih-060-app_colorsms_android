package com.android.messaging.notificationcleaner.activity;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.BaseActivity;
import com.android.messaging.BugleFiles;
import com.android.messaging.R;
import com.android.messaging.notificationcleaner.BuglePackageManager;
import com.android.messaging.notificationcleaner.NotificationCleanerConstants;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.android.messaging.notificationcleaner.views.AnimatedNotificationView;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.Typefaces;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationCleanerSettingActivity extends BaseActivity {
    private View mWhiteMaskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification_cleaner_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.primary_color));
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary_color));
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setTypeface(Typefaces.getCustomSemiBold());
        title.setText(getString(R.string.notification_cleaner_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mWhiteMaskView = ViewUtils.findViewById(this, R.id.white_mask_view);

        List<String> unblockedAppList = new ArrayList<>();

        Bundle bundle = HSApplication.getContext().getContentResolver().call(
                NotificationCleanerProvider.createBlockAppContentUri(HSApplication.getContext()),
                NotificationCleanerProvider.METHOD_GET_UNBLOCKED_APP_LIST, null, null);

        if (bundle != null) {
            List<String> unblockListFormCall = bundle.getStringArrayList(NotificationCleanerProvider.EXTRA_APP_PACKAGE_NAME_LIST);
            if (unblockListFormCall != null) {
                unblockedAppList.addAll(unblockListFormCall);
            }
        }

        List<BlockNotificationItemData> blockNotificationItemDataList = new ArrayList<>();

        for (ApplicationInfo applicationInfo : BuglePackageManager.getInstance().getInstalledApplications()) {
            if (TextUtils.equals(applicationInfo.packageName, getPackageName())) {
                continue;
            }

            String appTitle = BuglePackageManager.getInstance().getApplicationLabel(applicationInfo);
            if (TextUtils.isEmpty(appTitle)) {
                continue;
            }

            BlockNotificationRegularItemData blockSettingRegularItemData = new BlockNotificationRegularItemData();
            blockSettingRegularItemData.mIcon = BuglePackageManager.getInstance().getApplicationIcon(applicationInfo);
            blockSettingRegularItemData.mTitle = appTitle;
            blockSettingRegularItemData.mPackageName = applicationInfo.packageName;
            blockSettingRegularItemData.mIsBlocked = !unblockedAppList.contains(applicationInfo.packageName);
            blockNotificationItemDataList.add(blockSettingRegularItemData);
        }

        Collections.sort(blockNotificationItemDataList, (lhs, rhs) -> {

            if (!((BlockNotificationRegularItemData) lhs).mIsBlocked
                    && ((BlockNotificationRegularItemData) rhs).mIsBlocked) {
                return -1;
            }

            if (((BlockNotificationRegularItemData) lhs).mIsBlocked
                    && !((BlockNotificationRegularItemData) rhs).mIsBlocked) {
                return 1;
            }

            return ((BlockNotificationRegularItemData) lhs).mTitle
                    .compareToIgnoreCase(((BlockNotificationRegularItemData) rhs).mTitle);
        });

        BlockNotificationListAdapter notificationSettingAdapter =
                new BlockNotificationListAdapter(blockNotificationItemDataList);

        RecyclerView settingRecyclerView = findViewById(R.id.notification_setting_recyclerview);
        settingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        settingRecyclerView.setAdapter(notificationSettingAdapter);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.notification_setting_switch_button);
        View actionView = item.getActionView();
        if (null != actionView) {
            SwitchCompat switchButton = actionView.findViewById(R.id.menu_switch_compat);

            switchButton.setChecked(NotificationCleanerProvider.isNotificationOrganizerSwitchOn());
            switchButton.setOnClickListener(v -> {
                if (switchButton.isChecked()) {
                    if (null != mWhiteMaskView) {
                        mWhiteMaskView.setVisibility(View.GONE);
                    }
                    Preferences.get(BugleFiles.NOTIFICATION_PREFS)
                            .putLong(NotificationCleanerConstants.NOTIFICATION_CLEANER_USAGE_TIME, System.currentTimeMillis());
                    NotificationCleanerProvider.switchNotificationOrganizer(true);
                    switchButton.setChecked(true);
                    return;
                }

                SpannableString contentSpannableString;
                int count = Preferences.get(BugleFiles.NOTIFICATION_PREFS)
                        .getInt(NotificationCleanerConstants.NOTIFICATION_CLEANER_NOTIFICATION_BLOCKED_COUNT, 0);
                long lastNCUsedTime = Preferences.get(BugleFiles.NOTIFICATION_PREFS)
                        .getLong(NotificationCleanerConstants.NOTIFICATION_CLEANER_USAGE_TIME, -1);
                long installTime = CommonUtils.getAppInstallTimeMillis();
                long timeSinceLastUse;
                if (lastNCUsedTime > 0) {
                    timeSinceLastUse = System.currentTimeMillis() - lastNCUsedTime;
                }  else {
                    timeSinceLastUse = System.currentTimeMillis() - installTime;
                }
                String countText = String.valueOf(count);
                String dayText = String.valueOf(timeSinceLastUse / (1000 * 60 * 60 * 24) + 1);
                String content = NotificationCleanerSettingActivity.this.getString(R.string.function_off_dialog_NC_content, countText, dayText);

                contentSpannableString = new SpannableString(content);
                contentSpannableString.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(NotificationCleanerSettingActivity.this, R.color.func_off_content)),
                        content.indexOf(countText), content.indexOf(countText) + countText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                contentSpannableString.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(NotificationCleanerSettingActivity.this, R.color.func_off_content)),
                        content.indexOf(dayText), content.indexOf(dayText) + dayText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                AlertDialog functionOffDialog = new AlertDialog.Builder(NotificationCleanerSettingActivity.this).create();
                functionOffDialog.setCanceledOnTouchOutside(false);
                @SuppressLint("InflateParams")
                View functionOffView = getLayoutInflater().inflate(R.layout.function_off_dialog, null);
                functionOffDialog.setView(functionOffView);

                TextView titleTv = functionOffView.findViewById(R.id.func_off_dialog_title);
                titleTv.setText(NotificationCleanerSettingActivity.this.getString(R.string.function_off_dialog_NC_title));
                TextView contentTv = functionOffView.findViewById(R.id.func_off_dialog_content);
                contentTv.setText(contentSpannableString);

                functionOffView.findViewById(R.id.func_off_dialog_btn_yes).setBackground(
                        BackgroundDrawables.createBackgroundDrawable(
                                getResources().getColor(R.color.primary_color),
                                getResources().getColor(R.color.ripples_ripple_color),
                                0, 0, Dimensions.pxFromDp(3.3f), 0,
                                true, true
                        ));
                functionOffView.findViewById(R.id.func_off_dialog_btn_yes).setOnClickListener(view -> {
                    if (null != mWhiteMaskView) {
                        mWhiteMaskView.setVisibility(View.GONE);
                    }
                    NotificationCleanerProvider.switchNotificationOrganizer(true);
                    switchButton.setChecked(true);
                    functionOffDialog.dismiss();
                });
                functionOffView.findViewById(R.id.func_off_dialog_btn_no).setBackground(
                        BackgroundDrawables.createBackgroundDrawable(0xffF7F8F9,
                                getResources().getColor(R.color.ripples_ripple_color),
                                0, 0, 0, Dimensions.pxFromDp(3.3f),
                                true, true
                        ));
                functionOffView.findViewById(R.id.func_off_dialog_btn_no).setOnClickListener(view -> {
                    if (null != mWhiteMaskView) {
                        mWhiteMaskView.setVisibility(View.VISIBLE);
                    }
                    NotificationCleanerProvider.switchNotificationOrganizer(false);
                    switchButton.setChecked(false);
                    functionOffDialog.dismiss();
                });
                functionOffDialog.setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (null != mWhiteMaskView) {
                            mWhiteMaskView.setVisibility(View.GONE);
                        }
                        NotificationCleanerProvider.switchNotificationOrganizer(true);
                        switchButton.setChecked(true);
                        functionOffDialog.dismiss();
                        return true;
                    }
                    return false;
                });
                if (!NotificationCleanerSettingActivity.this.isFinishing()) {
                    functionOffDialog.show();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_cleaner_settings_switch_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!NotificationCleanerProvider.isNotificationOrganizerSwitchOn()) {
            HSGlobalNotificationCenter.sendNotification(NotificationBlockedActivity.NOTIFICATION_FINISH_SELF);
        }
    }


    class BlockNotificationItemViewHolder extends RecyclerView.ViewHolder {
        ImageView appIconImageView;
        TextView appNameTextView;
        SwitchCompat appBlockSwitchButton;


        BlockNotificationItemViewHolder(View itemView) {
            super(itemView);
            appIconImageView = itemView.findViewById(R.id.app_icon_image);
            appNameTextView = itemView.findViewById(R.id.app_name_text);
            appBlockSwitchButton = ViewUtils.findViewById(itemView, R.id.app_block_toggle_button);
        }
    }

    class BlockNotificationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        static final int TYPE_ITEM_HEADER = 0;
        static final int TYPE_ITEM_REGULAR = 1;

        private List<BlockNotificationItemData> blockNotificationItemDataList = new ArrayList<>();

        BlockNotificationListAdapter(List<BlockNotificationItemData> blockNotificationItemDataList) {
            this.blockNotificationItemDataList.add(new BlockNotificationHeaderItemData());
            this.blockNotificationItemDataList.addAll(blockNotificationItemDataList);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            switch (viewType) {
                case TYPE_ITEM_HEADER:
                    return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.notification_cleaner_header_view_item, parent, false)) {};

                case TYPE_ITEM_REGULAR:
                    return new BlockNotificationItemViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.notification_cleaner_block_setting_app_item, parent, false));
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_ITEM_HEADER) {
                return;
            }

            final BlockNotificationItemViewHolder itemHolder = (BlockNotificationItemViewHolder) holder;
            final BlockNotificationRegularItemData blockSettingItemData = (BlockNotificationRegularItemData)
                    blockNotificationItemDataList.get(position);

            itemHolder.appIconImageView.setImageDrawable(blockSettingItemData.mIcon);
            itemHolder.appNameTextView.setText(blockSettingItemData.mTitle);
            itemHolder.appBlockSwitchButton.setChecked(blockSettingItemData.mIsBlocked);

            itemHolder.itemView.setOnClickListener(v -> {
                blockSettingItemData.mIsBlocked = !blockSettingItemData.mIsBlocked;
                itemHolder.appBlockSwitchButton.setChecked(blockSettingItemData.mIsBlocked);

                String toastContent = blockSettingItemData.mIsBlocked ? getString(R.string.notification_cleaner_block_opened, blockSettingItemData.mTitle)
                        : getString(R.string.notification_cleaner_block_closed, blockSettingItemData.mTitle);
                Toast.makeText(v.getContext(), toastContent, Toast.LENGTH_SHORT).show();

                Bundle bundle = new Bundle();
                bundle.putString(NotificationCleanerProvider.EXTRA_APP_PACKAGE_NAME, blockSettingItemData.mPackageName);

                if (blockSettingItemData.mIsBlocked) {
                    HSApplication.getContext().getContentResolver().call(
                            NotificationCleanerProvider.createBlockAppContentUri(HSApplication.getContext()),
                            NotificationCleanerProvider.METHOD_REMOVE_APP_FROM_UNBLOCK_LIST, null, bundle);
                    AnimatedNotificationView.sendGetActiveNotificationBroadcast();
                } else {
                    HSApplication.getContext().getContentResolver().call(
                            NotificationCleanerProvider.createBlockAppContentUri(HSApplication.getContext()),
                            NotificationCleanerProvider.METHOD_ADD_APP_TO_UNBLOCK_LIST, null, bundle);
                }
            });
        }

        @Override
        public int getItemCount() {
            return blockNotificationItemDataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return blockNotificationItemDataList.get(position).getItemType();
        }
    }

    abstract class BlockNotificationItemData {
        public abstract int getItemType();
    }

    class BlockNotificationHeaderItemData extends BlockNotificationItemData {

        @Override
        public int getItemType() {
            return BlockNotificationListAdapter.TYPE_ITEM_HEADER;
        }
    }

    class BlockNotificationRegularItemData extends BlockNotificationItemData {
        Drawable mIcon;
        String mPackageName;
        String mTitle;
        boolean mIsBlocked;

        @Override
        public int getItemType() {
            return BlockNotificationListAdapter.TYPE_ITEM_REGULAR;
        }
    }
}
