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

import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.CustomCursorAdapter;
import com.android.messaging.ui.customize.PrimaryColors;

public class ContactListAdapter extends CustomCursorAdapter {

    private static final int ITEM_VIEW_TYPE_ALPHABET_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_CONTACT = 1;

    private final ContactListItemView.HostInterface mClivHostInterface;
    private final boolean mNeedAlphabetHeader;
    private ContactSectionIndexer mSectionIndexer;

    ContactListAdapter(final ContactListItemView.HostInterface clivHostInterface,
                       final boolean needAlphabetHeader) {
        mClivHostInterface = clivHostInterface;
        mNeedAlphabetHeader = needAlphabetHeader;
    }

    @Override
    protected View onCreateView(int position, int itemViewType, ViewGroup parent) {
        View view = null;
        switch (itemViewType) {
            case ITEM_VIEW_TYPE_ALPHABET_HEADER:
                view = View.inflate(parent.getContext(), R.layout.contact_list_section_view, null);
                ((TextView) view).setTextColor(PrimaryColors.getPrimaryColor());
                break;
            case ITEM_VIEW_TYPE_CONTACT:
                view = View.inflate(parent.getContext(), R.layout.contact_list_item_view, null);
                break;
        }
        return view;
    }

    @Override
    protected void onBindView(int position, int itemViewType, View view) {
        String alphabetHeader;
        switch (itemViewType) {
            case ITEM_VIEW_TYPE_ALPHABET_HEADER:
                alphabetHeader = mSectionIndexer.getSectionForStartingPosition(position);
                ((TextView) view).setText(alphabetHeader);
                break;
            case ITEM_VIEW_TYPE_CONTACT:
                int cursorPosition = position;
                if (mNeedAlphabetHeader) {
                    cursorPosition = mSectionIndexer.mapListPositionToCursorPosition(position);
                }
                if (mCursor.moveToPosition(cursorPosition)) {
                    ((ContactListItemView) view).bind(mCursor, mClivHostInterface);
                }
                break;
        }
    }

    @Override
    protected void onSwapCursor(Cursor cursor) {
        if (mNeedAlphabetHeader) {
            mSectionIndexer = new ContactSectionIndexer(cursor);
        }
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }

        if (mNeedAlphabetHeader) {
            Log.d("List.type", "mCursor.getCount(): " + mCursor.getCount() + " mSectionIndexer.getSectionCount(): " + mSectionIndexer.getSectionCount());
            return mCursor.getCount() + mSectionIndexer.getSectionCount();
        } else {
            return mCursor.getCount();
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mNeedAlphabetHeader) {
            String alphabetHeader = mSectionIndexer.getSectionForStartingPosition(position);
            return alphabetHeader == null ? ITEM_VIEW_TYPE_CONTACT : ITEM_VIEW_TYPE_ALPHABET_HEADER;
        } else {
            return ITEM_VIEW_TYPE_CONTACT;
        }
    }
}
