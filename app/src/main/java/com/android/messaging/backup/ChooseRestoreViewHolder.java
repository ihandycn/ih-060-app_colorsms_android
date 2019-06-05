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

public class ChooseRestoreViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;

    public ChooseRestoreViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.choose_restore_page,
                container /* root */,
                false /* attachToRoot */);
        AppCompatCheckBox fromLocalCheckBox = view.findViewById(R.id.from_local);
        AppCompatCheckBox fromCloudCheckBox = view.findViewById(R.id.from_cloud);
        MessagesTextView restoreButton = view.findViewById(R.id.restore_confirm_button);
        restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                Dimensions.pxFromDp(3.3f), false));
        fromLocalCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                        Dimensions.pxFromDp(3.3f), true));
                if (isChecked && fromCloudCheckBox.isChecked()) {
                    fromCloudCheckBox.setChecked(false);
                } else if (!isChecked && !fromCloudCheckBox.isChecked()) {
                    fromLocalCheckBox.setChecked(true);
                }
            }
        });

        fromCloudCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                        Dimensions.pxFromDp(3.3f), true));
                if (isChecked && fromLocalCheckBox.isChecked()) {
                    fromLocalCheckBox.setChecked(false);
                } else if (!isChecked && !fromLocalCheckBox.isChecked()) {
                    fromCloudCheckBox.setChecked(true);
                }
            }
        });
        restoreButton.setOnClickListener(v -> {
            if(fromLocalCheckBox.isChecked() || fromCloudCheckBox.isChecked()) {
                RestoreProcessDialog restoreProcessDialog = new RestoreProcessDialog();
                UiUtils.showDialogFragment((Activity) mContext, restoreProcessDialog);
            }
        });
        return view;
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.restore_tab);
    }

    @Override
    public void onPageSelected() {

    }
}
