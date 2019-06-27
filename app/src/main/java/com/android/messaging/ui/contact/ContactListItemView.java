/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.contact;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ex.chips.RecipientEntry;
import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.data.ContactListItemData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.google.common.annotations.VisibleForTesting;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

/**
 * The view for a single entry in a contact list.
 */
public class ContactListItemView extends LinearLayout implements OnClickListener {
    public interface HostInterface {
        void onContactListItemClicked(ContactListItemData item, ContactListItemView view);

        boolean isContactSelected(ContactListItemData item);
    }

    @VisibleForTesting
    final ContactListItemData mData;
    private TextView mContactNameTextView;
    private TextView mContactDetailsTextView;
    private TextView mContactDetailTypeTextView;
    private ContactIconView mContactIconView;
    private ImageView mContactCheckmarkView;
    private HostInterface mHostInterface;

    public ContactListItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mData = DataModel.get().createContactListItemData();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContactNameTextView = (TextView) findViewById(R.id.contact_name);
        mContactDetailsTextView = (TextView) findViewById(R.id.contact_details);
        mContactDetailTypeTextView = (TextView) findViewById(R.id.contact_detail_type);
        mContactIconView = (ContactIconView) findViewById(R.id.contact_icon);
        findViewById(R.id.contact_bg).setBackground(
                BackgroundDrawables.createBackgroundDrawable(0xffd1d6dc, Dimensions.pxFromDp(20), false));
        mContactCheckmarkView = (ImageView) findViewById(R.id.contact_checkmark);
        mContactCheckmarkView.setBackground(BackgroundDrawables.
                createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(28), false));

    }

    /**
     * Fills in the data associated with this view by binding to a contact cursor provided by
     * ContactUtil.
     *
     * @param cursor        the contact cursor.
     * @param hostInterface host interface to this view.
     */
    public void bind(final Cursor cursor, final HostInterface hostInterface) {
        mData.bind(cursor);
        mHostInterface = hostInterface;
        setOnClickListener(this);
        updateViewAppearance();
    }

    /**
     * Binds a RecipientEntry. This is used by the chips text view's dropdown layout.
     *
     * @param recipientEntry    the source RecipientEntry provided by ContactDropdownLayouter, which
     *                          was in turn directly from one of the existing chips, or from filtered results
     *                          generated by ContactRecipientAdapter.
     * @param styledName        display name where the portion that matches the search text is bold.
     * @param styledDestination number where the portion that matches the search text is bold.
     * @param hostInterface     host interface to this view.
     * @param isSingleRecipient whether this item is shown as the only line item in the single
     *                          recipient drop down from the chips view. If this is the case, we always show the
     *                          contact avatar even if it's not a first-level entry.
     * @param isWorkContact     whether the contact is in managed profile.
     */
    public void bind(final RecipientEntry recipientEntry, final CharSequence styledName,
                     final CharSequence styledDestination, final HostInterface hostInterface,
                     final boolean isSingleRecipient, final boolean isWorkContact) {
        mData.bind(recipientEntry, styledName, styledDestination, isSingleRecipient, isWorkContact);
        mHostInterface = hostInterface;
        updateViewAppearance();
    }

    private void updateViewAppearance() {
        mContactNameTextView.setText(mData.getDisplayName());
        mContactDetailsTextView.setText(mData.getDestination());
        mContactDetailTypeTextView.setText(Phone.getTypeLabel(getResources(),
                mData.getDestinationType(), mData.getDestinationLabel()));
        final RecipientEntry recipientEntry = mData.getRecipientEntry();
        final String destinationString = String.valueOf(mData.getDestination());
        if (mData.getIsSimpleContactItem()) {
            // This is a special number-with-avatar type of contact (for unknown contact chips
            // and for direct "send to destination" item). In this case, make sure we only show
            // the display name (phone number) and the avatar and hide everything else.
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    ParticipantData.getFromRecipientEntry(recipientEntry));
            mContactIconView.setImageResourceUri(avatarUri, mData.getContactId(),
                    mData.getLookupKey(), destinationString, false);
            mContactIconView.setVisibility(VISIBLE);
            mContactCheckmarkView.setVisibility(GONE);
            mContactDetailTypeTextView.setVisibility(GONE);
            mContactDetailsTextView.setVisibility(GONE);
            mContactNameTextView.setVisibility(VISIBLE);
        } else if (mData.getIsFirstLevel()) {
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    ParticipantData.getFromRecipientEntry(recipientEntry));
            mContactIconView.setImageResourceUri(avatarUri, mData.getContactId(),
                    mData.getLookupKey(), destinationString, false);
            mContactIconView.setVisibility(VISIBLE);
            mContactNameTextView.setVisibility(VISIBLE);
            final boolean isSelected = mHostInterface.isContactSelected(mData);
            setSelected(isSelected);
            mContactCheckmarkView.setVisibility(isSelected ? VISIBLE : GONE);
            mContactDetailsTextView.setVisibility(VISIBLE);
            mContactDetailTypeTextView.setVisibility(VISIBLE);
        } else {
            mContactIconView.setImageResourceUri(null);
            mContactIconView.setVisibility(INVISIBLE);
            mContactNameTextView.setVisibility(GONE);
            final boolean isSelected = mHostInterface.isContactSelected(mData);
            setSelected(isSelected);
            mContactCheckmarkView.setVisibility(isSelected ? VISIBLE : GONE);
            mContactDetailsTextView.setVisibility(VISIBLE);
            mContactDetailTypeTextView.setVisibility(VISIBLE);
        }
    }

    /**
     * {@inheritDoc} from OnClickListener
     */
    @Override
    public void onClick(final View v) {
        Assert.isTrue(v == this);
        Assert.isTrue(mHostInterface != null);
        mHostInterface.onContactListItemClicked(mData, this);
    }

    public void setImageClickHandlerDisabled(final boolean isHandlerDisabled) {
        mContactIconView.setImageClickHandlerDisabled(isHandlerDisabled);
    }
}
