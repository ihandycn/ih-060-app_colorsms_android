package com.android.messaging.ui.ringtone;

import android.os.Parcel;
import android.os.Parcelable;

public class RingtoneInfo implements Parcelable {
    public static int TYPE_SYSTEM = 1;
    public static int TYPE_FILE = 2;
    public static int TYPE_APP = 3;

    public int type;
    public String name;
    public String uri;

    public RingtoneInfo(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeString(uri);
    }

    public RingtoneInfo(Parcel in) {
        type = in.readInt();
        name = in.readString();
        uri = in.readString();
    }

    public static final Parcelable.Creator<RingtoneInfo> CREATOR = new Parcelable.Creator<RingtoneInfo>() {

        @Override
        public RingtoneInfo createFromParcel(Parcel source) {
            return new RingtoneInfo(source);
        }

        @Override
        public RingtoneInfo[] newArray(int size) {
            return new RingtoneInfo[size];
        }
    };
}
