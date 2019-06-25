package com.android.messaging.ui.appsettings;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;

import java.util.List;

public class SimSelectDialog extends BaseDialogFragment {

    public interface OnSimSelectListener {
        void onSimSelect(SubscriptionListData.SubscriptionListEntry entry);
    }

    private View mContentView;
    private OnSimSelectListener listener;

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Override
    protected CharSequence getTitle() {
        return null;
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return null;
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return null;
    }

    @Override
    protected View getContentView() {
        return createBodyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private View createBodyView() {
        mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_select_sim, null);
        ImageView ivTip1 = mContentView.findViewById(R.id.iv_tip_1);
        ivTip1.getDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
        ImageView ivTip2 = mContentView.findViewById(R.id.iv_tip_2);
        ivTip2.getDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);

        for (int i = 0; i < 2 && i < data.size(); i++) {
            final SubscriptionListData.SubscriptionListEntry entry = data.get(i);
            if (entry.slotId == 1) {
                ((TextView) mContentView.findViewById(R.id.carrier_1)).setText(entry.displayName);

                final String displayDestination = TextUtils.isEmpty(entry.displayDestination) ?
                        getContext().getResources().getString(R.string.sim_settings_unknown_number) :
                        entry.displayDestination;
                ((TextView) mContentView.findViewById(R.id.phone_number_1)).setText(displayDestination);
                mContentView.findViewById(R.id.container_sim_1).setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSimSelect(entry);
                    }
                    dismissAllowingStateLoss();
                });
                mContentView.findViewById(R.id.container_sim_1).setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
            } else {
                ((TextView) mContentView.findViewById(R.id.carrier_2)).setText(entry.displayName);
                final String displayDestination = TextUtils.isEmpty(entry.displayDestination) ?
                        getContext().getResources().getString(R.string.sim_settings_unknown_number) :
                        entry.displayDestination;
                ((TextView) mContentView.findViewById(R.id.phone_number_2)).setText(displayDestination);
                mContentView.findViewById(R.id.container_sim_2).setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSimSelect(entry);
                    }
                    dismissAllowingStateLoss();
                });
                mContentView.findViewById(R.id.container_sim_2).setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
            }
        }

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.iv_sim_selected);
        Drawable selectedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(selectedDrawable, PrimaryColors.getPrimaryColor());
        if (currentSlotId == 1) {
            ImageView ivCheckSim1 = mContentView.findViewById(R.id.iv_check_sim_1);
            ivCheckSim1.setImageDrawable(selectedDrawable);
            ImageView ivCheckSim2 = mContentView.findViewById(R.id.iv_check_sim_2);
            ivCheckSim2.setImageResource(R.drawable.iv_sim_unselected);
        } else {
            ImageView ivCheckSim1 = mContentView.findViewById(R.id.iv_check_sim_1);
            ivCheckSim1.setImageResource(R.drawable.iv_sim_unselected);
            ImageView ivCheckSim2 = mContentView.findViewById(R.id.iv_check_sim_2);
            ivCheckSim2.setImageDrawable(selectedDrawable);
        }
        return mContentView;
    }

    private List<SubscriptionListData.SubscriptionListEntry> data;
    private int currentSlotId;

    public void bindData(List<SubscriptionListData.SubscriptionListEntry> data, int currentSlotId, OnSimSelectListener listener) {
        this.data = data;
        this.currentSlotId = currentSlotId;
        this.listener = listener;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
