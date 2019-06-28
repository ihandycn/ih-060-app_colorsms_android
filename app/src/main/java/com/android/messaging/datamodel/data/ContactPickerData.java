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

package com.android.messaging.datamodel.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;

import com.android.messaging.datamodel.BoundCursorLoader;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.binding.BindableData;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.sms.MmsConfig;
import com.android.messaging.util.Assert;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.LogUtil;

/**
 * Class to access phone contacts.
 * The caller is responsible for ensuring that the app has READ_CONTACTS permission (see
 * {@link ContactUtil#hasReadContactsPermission()}) before instantiating this class.
 */
public class ContactPickerData extends BindableData implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public interface ContactPickerDataListener {
        void onAllContactsCursorUpdated(Cursor data);

        void onContactCustomColorLoaded(ContactPickerData data);
    }

    private static final String BINDING_ID = "bindingId";
    private final Context mContext;
    private LoaderManager mLoaderManager;
    private ContactPickerDataListener mListener;

    public ContactPickerData(final Context context, final ContactPickerDataListener listener) {
        mListener = listener;
        mContext = context;
    }

    private static final int ALL_CONTACTS_LOADER = 1;
    private static final int PARTICIPANT_LOADER = 3;

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String bindingId = args.getString(BINDING_ID);
        // Check if data still bound to the requesting ui element
        if (isBound(bindingId)) {
            switch (id) {
                case ALL_CONTACTS_LOADER:
                    return ContactUtil.getPhones(mContext)
                            .createBoundCursorLoader(bindingId);
                case PARTICIPANT_LOADER:
                    return new BoundCursorLoader(bindingId, mContext,
                            MessagingContentProvider.PARTICIPANTS_URI,
                            ParticipantData.ParticipantsQuery.PROJECTION, null, null, null);
                default:
                    Assert.fail("Unknown loader id for contact picker!");
                    break;
            }
        } else {
            LogUtil.w(LogUtil.BUGLE_TAG, "Loader created after unbinding the contacts list");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        final BoundCursorLoader cursorLoader = (BoundCursorLoader) loader;
        if (mListener == null) {
            return;
        }

        if (isBound(cursorLoader.getBindingId())) {
            switch (loader.getId()) {
                case ALL_CONTACTS_LOADER:
                    if (data != null) {
                        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(data));
                    }
                    mListener.onAllContactsCursorUpdated(data);
                    break;
                case PARTICIPANT_LOADER:
                    mListener.onContactCustomColorLoaded(this);
                    break;
                default:
                    Assert.fail("Unknown loader id for contact picker!");
                    break;
            }
        } else {
            LogUtil.w(LogUtil.BUGLE_TAG, "Loader finished after unbinding the contacts list");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        final BoundCursorLoader cursorLoader = (BoundCursorLoader) loader;
        if (isBound(cursorLoader.getBindingId())) {
            switch (loader.getId()) {
                case ALL_CONTACTS_LOADER:
                    if (mListener != null) {
                        mListener.onAllContactsCursorUpdated(null);
                    }
                    break;
                case PARTICIPANT_LOADER:
                    if (mListener != null) {
                        mListener.onContactCustomColorLoaded(this);
                    }
                    break;
                default:
                    Assert.fail("Unknown loader id for contact picker!");
                    break;
            }
        } else {
            LogUtil.w(LogUtil.BUGLE_TAG, "Loader reset after unbinding the contacts list");
        }
    }

    public void init(final LoaderManager loaderManager,
                     final BindingBase<ContactPickerData> binding) {
        final Bundle args = new Bundle();
        args.putString(BINDING_ID, binding.getBindingId());
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(ALL_CONTACTS_LOADER, args, this);
        mLoaderManager.initLoader(PARTICIPANT_LOADER, args, this);
    }

    @Override
    protected void unregisterListeners() {
        mListener = null;


        // This could be null if we bind but the caller doesn't init the BindableData
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(ALL_CONTACTS_LOADER);
            mLoaderManager.destroyLoader(PARTICIPANT_LOADER);
            mLoaderManager = null;
        }
    }

    public static boolean isTooManyParticipants(final int participantCount) {
        // When creating a conversation, the conversation will be created using the system's
        // default SIM, so use the default MmsConfig's recipient limit.
        return (participantCount > MmsConfig.get(ParticipantData.DEFAULT_SELF_SUB_ID)
                .getRecipientLimit());
    }

    public static boolean getCanAddMoreParticipants(final int participantCount) {
        // When creating a conversation, the conversation will be created using the system's
        // default SIM, so use the default MmsConfig's recipient limit.
        return (participantCount < MmsConfig.get(ParticipantData.DEFAULT_SELF_SUB_ID)
                .getRecipientLimit());
    }
}
