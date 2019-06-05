package com.android.messaging.backup;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ChooseBackUpViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private static final String TAG = "ChooseBackUpViewHolder";
    private AppCompatRadioButton mBackupLocalRadioButton;
    private AppCompatRadioButton mBackupCloudRadioButton;
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
        MessagesTextView backupButton = view.findViewById(R.id.backup_confirm_button);
        backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));
        mBackupLocalRadioButton = view.findViewById(R.id.backup_local);
        mBackupCloudRadioButton = view.findViewById(R.id.backup_cloud);
        mBackupLocalRadioButton.setChecked(true);
        backupButton.setOnClickListener(v -> {
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
