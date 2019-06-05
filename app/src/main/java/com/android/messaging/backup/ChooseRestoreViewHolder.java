package com.android.messaging.backup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

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
        MessagesTextView restoreButton = view.findViewById(R.id.restore_confirm_button);
        restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));
        restoreButton.setOnClickListener(v -> {
            RestoreProcessDialog restoreProcessDialog = new RestoreProcessDialog();
            UiUtils.showDialogFragment((Activity) mContext, restoreProcessDialog);
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
