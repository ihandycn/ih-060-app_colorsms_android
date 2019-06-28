package com.android.messaging.datamodel.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.android.ex.chips.RecipientEntry;
import com.android.messaging.util.Assert;
import com.android.messaging.util.ContactRecipientEntryUtils;
import com.android.messaging.util.ContactUtil;

public class PrivateContactItemData {
    // Keeps the contact data in the form of RecipientEntry that RecipientEditTextView can
    // directly use.
    private RecipientEntry mRecipientEntry;

    private CharSequence mStyledName;
    private CharSequence mStyledDestination;

    // If this contact is the first in the list for its first letter, then this will be the
    // first letter, otherwise this is null.
    private String mAlphabetHeader;

    // Is the contact the only item in the list (happens when the user clicks on an
    // existing chip for which we show full contact detail for the selected contact).
    private boolean mSingleRecipient;

    // Is the contact in managed profile.
    private boolean mIsWorkContact;

    /**
     * Bind to a contact cursor in the contact list.
     */
    public void bind(final Cursor cursor) {
        final long dataId = cursor.getLong(ContactUtil.INDEX_CONTACT_ID);
        final long contactId = cursor.getLong(ContactUtil.INDEX_CONTACT_ID);
        final String lookupKey = cursor.getString(ContactUtil.INDEX_LOOKUP_KEY);
        final String displayName = cursor.getString(ContactUtil.INDEX_DISPLAY_NAME);
        final String photoThumbnailUri = cursor.getString(ContactUtil.INDEX_PHOTO_URI);
        final String destination = cursor.getString(ContactUtil.INDEX_PHONE_EMAIL);
        final int destinationType = cursor.getInt(ContactUtil.INDEX_PHONE_EMAIL_TYPE);
        final String destinationLabel = cursor.getString(ContactUtil.INDEX_PHONE_EMAIL_LABEL);
        mStyledName = displayName;
        mStyledDestination = null;
        mSingleRecipient = false;

        // Check whether this contact is first level (i.e. whether it's the first entry of this
        // contact in the contact list).
        boolean isFirstLevel = true;
        if (!cursor.isFirst() && cursor.moveToPrevious()) {
            final long contactIdPrevious = cursor.getLong(ContactUtil.INDEX_CONTACT_ID);
            if (contactId == contactIdPrevious) {
                isFirstLevel = false;
            }
            cursor.moveToNext();
        }

        mRecipientEntry = ContactUtil.createRecipientEntry(displayName,
                ContactsContract.DisplayNameSources.STRUCTURED_NAME, destination, destinationType, destinationLabel,
                contactId, lookupKey, dataId, photoThumbnailUri, isFirstLevel);

        mIsWorkContact = ContactUtil.isEnterpriseContactId(contactId);
    }

    /**
     * Bind to a RecipientEntry produced by the chips text view in the search drop down, plus
     * optional styled name & destination for showing bold search match.
     */
    public void bind(final RecipientEntry entry, final CharSequence styledName,
                     final CharSequence styledDestination, final boolean singleRecipient,
                     final boolean isWorkContact) {
        Assert.isTrue(entry.isValid());
        mRecipientEntry = entry;
        mStyledName = styledName;
        mStyledDestination = styledDestination;
        mAlphabetHeader = null;
        mSingleRecipient = singleRecipient;
        mIsWorkContact = isWorkContact;
    }

    public CharSequence getDisplayName() {
        return mStyledName == null ? "" : mStyledName;
    }

    public Uri getPhotoThumbnailUri() {
        return mRecipientEntry.getPhotoThumbnailUri() == null ? null :
                mRecipientEntry.getPhotoThumbnailUri();
    }

    public CharSequence getDestination() {
        final CharSequence destination = mStyledDestination != null ?
                mStyledDestination : ContactRecipientEntryUtils.formatDestination(mRecipientEntry);
        return destination == null ? "" : destination;
    }

    public void setName(String name) {
        this.mStyledName = name;
    }

    public void setDestination(String destination) {
        this.mStyledDestination = destination;
    }


    public int getDestinationType() {
        return mRecipientEntry.getDestinationType();
    }

    public String getDestinationLabel() {
        return mRecipientEntry.getDestinationLabel();
    }

    public long getContactId() {
        return mRecipientEntry.getContactId();
    }

    public String getLookupKey() {
        return mRecipientEntry.getLookupKey();
    }

    /**
     * Returns if this item is "first-level," i.e. whether it's the first entry of the contact
     * that it represents in the list. For example, if John Smith has 3 different phone numbers,
     * then the first number is considered first-level, while the other two are considered
     * second-level.
     */
    public boolean getIsFirstLevel() {
        // Treat the item as first level if it's a top-level recipient entry, or if it's the only
        // item in the list.
        return mRecipientEntry.isFirstLevel() || mSingleRecipient;
    }

    /**
     * Returns if this item is simple, i.e. it has only avatar and a display name with phone number
     * embedded so we can hide everything else.
     */
    public boolean getIsSimpleContactItem() {
        return ContactRecipientEntryUtils.isAvatarAndNumberOnlyContact(mRecipientEntry) ||
                ContactRecipientEntryUtils.isSendToDestinationContact(mRecipientEntry);
    }

    public String getAlphabetHeader() {
        return mAlphabetHeader;
    }

    /**
     * Returns a RecipientEntry instance readily usable by the RecipientEditTextView.
     */
    public RecipientEntry getRecipientEntry() {
        return mRecipientEntry;
    }

    /**
     * @return whether the contact is in managed profile.
     */
    public boolean getIsWorkContact() {
        return mIsWorkContact;
    }
}
