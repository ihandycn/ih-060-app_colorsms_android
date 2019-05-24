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
import com.android.messaging.util.LogUtil;

public class ContactListAdapter extends CustomCursorAdapter {

    private static final int ITEM_VIEW_TYPE_ALPHABET_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_CONTACT = 1;
    private static final int ITEM_VIEW_TYPE_SELECT_GROUP_HEADER = 2;
    private boolean isSelectSingleMode = true;

    private final ContactListItemView.HostInterface mClivHostInterface;
    private final boolean mNeedAlphabetHeader;
    private ContactSectionIndexer mSectionIndexer;

    public interface SelectGroupMessageHost {
        void onSelectGroupMessage();
    }

    private SelectGroupMessageHost mSelectGroupMessageHost;

    private final String TAG = "contact_list_adapter";

    ContactListAdapter(final ContactListItemView.HostInterface clivHostInterface,
                       final boolean needAlphabetHeader) {
        mClivHostInterface = clivHostInterface;
        mNeedAlphabetHeader = needAlphabetHeader;
    }

    public void setSelectGroupMessageHost(SelectGroupMessageHost host) {
        this.mSelectGroupMessageHost = host;
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
            case ITEM_VIEW_TYPE_SELECT_GROUP_HEADER:
                view = View.inflate(parent.getContext(), R.layout.contact_list_select_group_header, null);
                break;
        }
        return view;
    }

    @Override
    protected void onBindView(int position, int itemViewType, View view) {
        position = getRealPosition(position, itemViewType);
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
                LogUtil.d(TAG, cursorPosition + "   " + position);
                if (mCursor.moveToPosition(cursorPosition)) {
                    ((ContactListItemView) view).bind(mCursor, mClivHostInterface);
                }
                break;
            case ITEM_VIEW_TYPE_SELECT_GROUP_HEADER:
                if (!isSelectSingleMode) {
                    break;
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isSelectSingleMode = false;
                        notifyDataSetChanged();
                        mSelectGroupMessageHost.onSelectGroupMessage();
                    }
                });
                break;
        }
    }

    private int getRealPosition(int position, int itemViewType) {
        if (itemViewType == ITEM_VIEW_TYPE_SELECT_GROUP_HEADER)
            return position;
        if (isSelectSingleMode)
            return position - 1;
        else
            return position;
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
            return 0;// when mCursor is null, must return 0, even if has a header. Don't know why.
        }
        int headerBase = 0;
        if (isSelectSingleMode) {
            headerBase = 1;
        }
        if (mNeedAlphabetHeader) {
            Log.d("List.type", "mCursor.getCount(): " + mCursor.getCount() + " mSectionIndexer.getSectionCount(): " + mSectionIndexer.getSectionCount());
            return mCursor.getCount() + mSectionIndexer.getSectionCount() + headerBase; //add select_group_header
        } else {
            return mCursor.getCount() + headerBase + headerBase;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        int headerBase = 0;
        if (isSelectSingleMode)
            headerBase = 1;
        if (position == 0 && isSelectSingleMode)
            return ITEM_VIEW_TYPE_SELECT_GROUP_HEADER;

        if (mNeedAlphabetHeader) {
            String alphabetHeader = mSectionIndexer.getSectionForStartingPosition(position - headerBase); //position - 1, skip 0, 0 is the position of select_group_header
            return alphabetHeader == null ? ITEM_VIEW_TYPE_CONTACT : ITEM_VIEW_TYPE_ALPHABET_HEADER;
        } else {
            return ITEM_VIEW_TYPE_CONTACT;
        }
    }
}
