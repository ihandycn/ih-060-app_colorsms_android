package com.android.messaging.ui.mediapicker.sendcontact;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ContactListItemData;
import com.android.messaging.ui.CustomCursorAdapter;
import com.android.messaging.ui.contact.ContactListItemView;
import com.android.messaging.ui.contact.ContactSectionIndexer;
import com.android.messaging.ui.customize.PrimaryColors;

public class ContactSelectListAdapter extends CustomCursorAdapter {

    private static final int ITEM_VIEW_TYPE_ALPHABET_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_CONTACT = 1;

    private final ContactListItemView.HostInterface mClivHostInterface;
    private ContactSectionIndexer mSectionIndexer;

    ContactSelectListAdapter(final ContactListItemView.HostInterface clivHostInterface) {
        mClivHostInterface = clivHostInterface;
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
                view = View.inflate(parent.getContext(), R.layout.select_send_contact_item_view, null);
                break;
        }
        return view;
    }

    @Override
    protected void onBindView(int position, int itemViewType, View view) {
        position = getRealPosition(position);
        String alphabetHeader;
        switch (itemViewType) {
            case ITEM_VIEW_TYPE_ALPHABET_HEADER:
                alphabetHeader = mSectionIndexer.getSectionForStartingPosition(position);
                ((TextView) view).setText(alphabetHeader);
                break;
            case ITEM_VIEW_TYPE_CONTACT:
                int cursorPosition = mSectionIndexer.mapListPositionToCursorPosition(position);
                if (mCursor.moveToPosition(cursorPosition)) {
                    ContactSelectListItemView itemView = ((ContactSelectListItemView) view);
                    itemView.bind(mCursor, mClivHostInterface);
                    ContactListItemData data = itemView.getData();
                    itemView.setSelectState(mClivHostInterface.isContactSelected(data));
                }
                break;
        }
    }

    private int getRealPosition(int position) {
        return position;
    }

    @Override
    protected void onSwapCursor(Cursor cursor) {
        mSectionIndexer = new ContactSectionIndexer(cursor);
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;// when mCursor is null, must return 0, even if has a header. Don't know why.
        }
        return mCursor.getCount() + mSectionIndexer.getSectionCount(); //add select_group_header
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
        String alphabetHeader = mSectionIndexer.getSectionForStartingPosition(position - headerBase); //position - 1, skip 0, 0 is the position of select_group_header
        return alphabetHeader == null ? ITEM_VIEW_TYPE_CONTACT : ITEM_VIEW_TYPE_ALPHABET_HEADER;
    }
}
