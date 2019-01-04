package com.android.messaging.datamodel.data;

public class SmsShowListItemData {

    private int mId;

    private String mMainPagePreviewUrl;
    private String mSmsShowUrl;

    public SmsShowListItemData(int id, String mainPagePreviewUrl, String smsShowUrl) {
        this.mId = id;
        this.mMainPagePreviewUrl = mainPagePreviewUrl;
        this.mSmsShowUrl = smsShowUrl;
    }

    public String getMainPagePreviewUrl() {
        return mMainPagePreviewUrl;
    }

    public void setMainPagePreviewUrl(String mainPagePreviewUrl) {
        this.mMainPagePreviewUrl = mainPagePreviewUrl;
    }

    public String getSmsShowUrl() {
        return mSmsShowUrl;
    }

    public void setSmsShowUrl(String smsShowUrl) {
        this.mSmsShowUrl = smsShowUrl;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    @Override
    public int hashCode() {
        return mId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SmsShowListItemData)) {
            return false;
        }

        return mId == ((SmsShowListItemData) o).getId();
    }

}
