package com.android.messaging.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.DetachableBinding;
import com.android.messaging.datamodel.data.PersonItemData;
import com.android.messaging.datamodel.data.PersonItemData.PersonItemDataListener;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class AttachmentVCardItemView extends LinearLayout implements PersonItemDataListener,
        OnLayoutChangeListener {
    public interface PersonItemViewListener {
        void onPersonClicked(PersonItemData data);

        boolean onPersonLongClicked(PersonItemData data);
    }

    protected final DetachableBinding<PersonItemData> mBinding;
    private TextView mNameTextView;
    private TextView mDetailsTextView;
    private ContactIconView mContactIconView;
    private View mDetailsContainer;
    private PersonItemViewListener mListener;
    private boolean mAvatarOnly;

    public AttachmentVCardItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mBinding = BindingBase.createDetachableBinding(this);
        LayoutInflater.from(getContext()).inflate(R.layout.attachment_vcard_item_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNameTextView = findViewById(R.id.name);
        mDetailsTextView = findViewById(R.id.details);
        mContactIconView = findViewById(R.id.contact_icon);
        mNameTextView.addOnLayoutChangeListener(this);
        mContactIconView.setImageResource(R.drawable.vcard_custom_contact);
        mDetailsContainer = findViewById(R.id.details_container);
        mNameTextView.setTextColor(0xffffffff);
        mDetailsTextView.setTextColor(0xb3ffffff);
        setBackground(BackgroundDrawables.createBackgroundDrawable(
                PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(10), true));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBinding.isBound()) {
            mBinding.detach();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBinding.reAttachIfPossible();
    }

    /**
     * Binds to a person item data which will provide us info to be displayed.
     *
     * @param personData the PersonItemData to be bound to.
     */
    public void bind(final PersonItemData personData) {
        if (mBinding.isBound()) {
            if (mBinding.getData().equals(personData)) {
                // Don't rebind if we are requesting the same data.
                return;
            }
            mBinding.unbind();
        }

        if (personData != null) {
            mBinding.bind(personData);
            mBinding.getData().setListener(this);
        }
        updateViewAppearance();
    }

    /**
     * @return Display name, possibly comma-ellipsized.
     */
    private String getDisplayName() {
        final int width = mNameTextView.getMeasuredWidth();
        final String displayName = mBinding.getData().getDisplayName();
        if (width == 0 || TextUtils.isEmpty(displayName) || !displayName.contains(",")) {
            return displayName;
        }
        final String plusOneString = getContext().getString(R.string.plus_one);
        final String plusNString = getContext().getString(R.string.plus_n);
        return BidiFormatter.getInstance().unicodeWrap(
                UiUtils.commaEllipsize(
                        displayName,
                        mNameTextView.getPaint(),
                        width,
                        plusOneString,
                        plusNString).toString(),
                TextDirectionHeuristicsCompat.LTR);
    }

    @Override
    public void onLayoutChange(final View v, final int left, final int top, final int right,
                               final int bottom, final int oldLeft, final int oldTop, final int oldRight,
                               final int oldBottom) {
        if (mBinding.isBound() && v == mNameTextView) {
            setNameTextView();
        }
    }

    /**
     * When set to true, we display only the avatar of the person and hide everything else.
     */
    public void setAvatarOnly(final boolean avatarOnly) {
        mAvatarOnly = avatarOnly;
        mDetailsContainer.setVisibility(avatarOnly ? GONE : VISIBLE);
    }

    public void setListener(final PersonItemViewListener listener) {
        mListener = listener;
        if (mListener == null) {
            return;
        }
        setOnClickListener(v -> {
            if (mListener != null && mBinding.isBound()) {
                mListener.onPersonClicked(mBinding.getData());
            }
        });
        final OnLongClickListener onLongClickListener = v -> {
            if (mListener != null && mBinding.isBound()) {
                return mListener.onPersonLongClicked(mBinding.getData());
            }
            return false;
        };
        setOnLongClickListener(onLongClickListener);
        mContactIconView.setOnLongClickListener(onLongClickListener);
    }

    protected void updateViewAppearance() {
        if (mBinding.isBound()) {
            setNameTextView();

            final String details = mBinding.getData().getDetails();
            if (TextUtils.isEmpty(details)) {
                mDetailsTextView.setVisibility(GONE);
            } else {
                mDetailsTextView.setVisibility(VISIBLE);
                mDetailsTextView.setText(details);
            }
        } else {
            mNameTextView.setText("");
        }
    }

    private void setNameTextView() {
        final String displayName = getDisplayName();
        if (TextUtils.isEmpty(displayName)) {
            mNameTextView.setVisibility(GONE);
        } else {
            mNameTextView.setVisibility(VISIBLE);
            mNameTextView.setText(displayName);
        }
    }

    @Override
    public void onPersonDataUpdated(final PersonItemData data) {
        mBinding.ensureBound(data);
        updateViewAppearance();
    }

    @Override
    public void onPersonDataFailed(final PersonItemData data, final Exception exception) {
        mBinding.ensureBound(data);
        updateViewAppearance();
    }

    public Intent getClickIntent() {
        return mBinding.getData().getClickIntent();
    }
}
