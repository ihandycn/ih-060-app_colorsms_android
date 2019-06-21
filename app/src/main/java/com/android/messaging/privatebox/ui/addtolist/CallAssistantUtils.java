package com.android.messaging.privatebox.ui.addtolist;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.android.messaging.util.PhoneUtils;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

public class CallAssistantUtils {

    public static class ContactInfo {
        public String name;
        public String number;
        public String avatarUriStr;

        //这里供UI显示使用，实际作用相当于Boolean值：isItemSelected
        public Object customInfo = Boolean.FALSE;
        private volatile int hashcode;

        public ContactInfo(String name, String number, String avatarUriStr) {
            this.name = name;
            this.number = number;
            this.avatarUriStr = avatarUriStr;
        }

        public ContactInfo() {
            name = "";
            number = "";
            avatarUriStr = "";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof ContactInfo)) {
                return false;
            }

            ContactInfo contactInfoObj = (ContactInfo) obj;
            return TextUtils.equals(name, contactInfoObj.name)
                    && TextUtils.equals(number, contactInfoObj.number);
        }

        @Override
        public int hashCode() {
            int result = hashcode;
            if (result == 0) {
                result = 17;
                result = 31 * result + name.hashCode();
                result = 31 * result + number.hashCode();
                hashcode = result;
            }
            return result;
        }

        @Override
        public String toString() {
            return name + number;
        }
    }

    public static List<ContactInfo> getAllContactsFromPhoneBook() {

        List<ContactInfo> list = new ArrayList<>();

        Cursor cursor = null;
        try {

            String selectionClause = ContactsContract.RawContacts.ACCOUNT_NAME + " != ?";
            String[] selectionArgs = {"WhatsApp"};
            cursor = HSApplication.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                    }, selectionClause, selectionArgs, null);

            if (cursor == null) {
                return list;
            }

            while (cursor.moveToNext()) {

                ContactInfo contactInfo = new ContactInfo();
                contactInfo.name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactInfo.number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactInfo.avatarUriStr = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                if (contactInfo.number == null) {
                    contactInfo.number = "";
                }

                contactInfo.number = PhoneUtils.getDefault().formatForDisplay(contactInfo.number);

                if (TextUtils.isEmpty(contactInfo.name)) {
                    contactInfo.name = contactInfo.number;
                }

                list.add(contactInfo);
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }
}
