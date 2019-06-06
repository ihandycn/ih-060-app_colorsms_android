package com.android.messaging.privatebox.ui.addtolist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.util.PhoneUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

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

    public static class CallLogInfo {
        public String name = "";
        public String number = "";
        public long callTimeMillis;
        public String avatarUriStr = "";

        private volatile int hashcode;

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof CallLogInfo)) {
                return false;
            }

            CallLogInfo callLogObj = (CallLogInfo) obj;
            return TextUtils.equals(name, callLogObj.name)
                    && TextUtils.equals(number, callLogObj.number)
                    && callTimeMillis == callLogObj.callTimeMillis;
        }

        @Override
        public int hashCode() {
            int result = hashcode;
            if (result == 0) {
                result = 17;
                result = 31 * result + name.hashCode();
                result = 31 * result + number.hashCode();
                result = 31 * result + (int) (callTimeMillis ^ (callTimeMillis >>> 32));
                hashcode = result;
            }
            return result;
        }

        @Override
        public String toString() {
            return name + callTimeMillis + number;
        }

        //getBlockedlist读写存取的时候用
        public static String writeIntoStorageFormat(String blockedNumber) {
            return System.currentTimeMillis() + "/" + blockedNumber;
        }

        public static CallLogInfo readFromStorageFormat(String string) {
            if (TextUtils.isEmpty(string)) {
                return new CallLogInfo();
            }

            int separatePosition = string.indexOf("/");
            String time = string.substring(0, separatePosition);
            String contactNumber = string.substring(separatePosition + 1);

            CallLogInfo callLogInfo = new CallLogInfo();
            callLogInfo.number = contactNumber;
            callLogInfo.callTimeMillis = Long.parseLong(time);
            callLogInfo.name = "";
            callLogInfo.avatarUriStr = "";
            return callLogInfo;
        }
    }

    public static final String PRIVATE_NUMBER = "PRIVATE_NUMBER";

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

    public static boolean isContactListNull() {

        Cursor cursor = null;
        int count = 0;

        try {
            cursor = HSApplication.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                    }, null, null, null);

            if (cursor == null) {
                return true;
            }

            count = cursor.getCount();
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count == 0;
    }

    //此接口应该只用于单个读取的情况，如果需要大量频繁调用此接口，建议使用getAllContactsFromPhoneBook()函数读取全部通话记录之后再自行匹配联系人名字
    public static String getContactNameFromPhoneBook(String phoneNum) {

        if (TextUtils.isEmpty(phoneNum)) {
            return "";
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));

        Cursor cursor = null;
        String contactName = "";

        try {
            cursor = HSApplication.getContext().getContentResolver().query(
                    uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return contactName;
            }

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return contactName;
    }

    public static void displayRoundCornerAvatar(final ImageView imageView, final String avatarUri) {
        imageView.setImageDrawable(VectorDrawableCompat.create(
                HSApplication.getContext().getResources(), R.drawable.default_contact_avatar, null));

        if (TextUtils.isEmpty(avatarUri)) {
            return;
        }

        // TODO: 04/12/2017 display avatar

//        Glide.with(HSApplication.getContext()).load(avatarUri).asBitmap().into(new SimpleTarget<Bitmap>() {
//            @Override
//            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                imageView.setImageBitmap(DisplayUtils.getRoundedCornerBitmap(resource));
//            }
//        });
    }

    //此接口仅用于单个取数据时使用，批量取数据时用getAllContactsFromPhoneBook()接口
    public static String getAvatarThumbnailUri(String phoneNum) {

        String avatarUri = "";

        if (TextUtils.isEmpty(phoneNum)) {
            return avatarUri;
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
        Cursor cursor = null;

        try {
            cursor = HSApplication.getContext().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI}, null, null, null);

            if (cursor == null) {
                return avatarUri;
            }

            if (cursor.moveToFirst()) {
                avatarUri = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return avatarUri;
    }

    public static void makeCall(String callNumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + callNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Navigations.startActivitySafely(HSApplication.getContext(), intent);
    }

    public static String getSMSContent(String phoneNum) {

        if (TextUtils.isEmpty(phoneNum)) {
            return "";
        }

        Cursor cursor = null;
        String messageContent = "";
        String[] projection = new String[]{"body"};
        String where = " address = '" + phoneNum + "' AND date >  " + (System.currentTimeMillis() - 10 * 60 * 1000);

        try {
            cursor = HSApplication.getContext().getContentResolver().query(Uri.parse("content://sms/"), projection, where, null, "date desc");

            if (cursor == null) {
                return messageContent;
            }

            if (cursor.moveToFirst()) {
                messageContent = cursor.getString(cursor.getColumnIndex("body"));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return messageContent;
    }

    public static boolean isNum(String str) {
        str = str.replaceAll(" ", "");
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }
}
