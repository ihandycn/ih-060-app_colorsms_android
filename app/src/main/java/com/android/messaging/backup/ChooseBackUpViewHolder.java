package com.android.messaging.backup;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static com.ihs.app.framework.HSApplication.getContext;

public class ChooseBackUpViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private static final String TAG = "ChooseBackUpViewHolder";
    public ChooseBackUpViewHolder(final Context context) {
        mContext = context;
    }
    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.choose_backup_page,
                container /* root */,
                false /* attachToRoot */);
        AppCompatCheckBox localCheckBox = view.findViewById(R.id.backup_local);
        AppCompatCheckBox cloudCheckBox = view.findViewById(R.id.backup_cloud);
        localCheckBox.setChecked(true);
        MessagesTextView backupButton = view.findViewById(R.id.backup_confirm_button);
        backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));
        localCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && !cloudCheckBox.isChecked()){
                    backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                            Dimensions.pxFromDp(3.3f), false));
                } else {
                    backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                            Dimensions.pxFromDp(3.3f), true));
                }
            }
        });
        cloudCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && !localCheckBox.isChecked()){
                    backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                            Dimensions.pxFromDp(3.3f), false));
                } else {
                    backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                            Dimensions.pxFromDp(3.3f), true));
                }
            }
        });
        backupButton.setOnClickListener(v -> {
            if (localCheckBox.isChecked() && cloudCheckBox.isChecked()){
            //todo 本地备份后再上传
            } else if (localCheckBox.isChecked()){
                LocalBackUpProcessDialog localBackUpProcessDialog = new LocalBackUpProcessDialog();
                UiUtils.showDialogFragment((Activity) mContext, localBackUpProcessDialog);
            } else if (cloudCheckBox.isChecked()){
                CloudBackUpProcessDialog cloudBackUpProcessDialog = new CloudBackUpProcessDialog();
                UiUtils.showDialogFragment((Activity) mContext, cloudBackUpProcessDialog);
            }
        });
        return view;
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.backup_tab);
    }

    @Override
    public void onPageSelected() {

    }
}
