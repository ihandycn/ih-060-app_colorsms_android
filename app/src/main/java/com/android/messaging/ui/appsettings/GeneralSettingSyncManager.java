package com.android.messaging.ui.appsettings;

import android.support.annotation.ColorInt;

import com.android.messaging.R;
import com.android.messaging.ui.messagebox.MessageBoxSettings;
import com.android.messaging.util.BuglePrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.HashMap;


public class GeneralSettingSyncManager {

    private static final String KEY_OUTGOING_MESSAGE_SOUNDS = "outgoingSounds";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_MESSAGE_BOX = "messageBox";
    private static final String KEY_VIBRATE = "vibrate";

    public static DatabaseReference getRootRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getUserRef() {
        return FirebaseDatabase.getInstance().getReference("users");
    }

    public static DatabaseReference getAuthUserRef() {
        return FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public static void uploadOutgoingMessageSoundsSwitchToServer(boolean enabled) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        getAuthUserRef().child(KEY_OUTGOING_MESSAGE_SOUNDS).setValue(enabled);
    }

    public static void uploadNotificationSwitchToServer(boolean enabled) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        getAuthUserRef().child(KEY_NOTIFICATION).setValue(enabled);
    }

    public static void uploadMessageBoxSwitchToServer(boolean enabled) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        getAuthUserRef().child(KEY_MESSAGE_BOX).setValue(enabled);
    }

    public static void uploadVibrateSwitchToServer(boolean enabled) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        getAuthUserRef().child(KEY_VIBRATE).setValue(enabled);
    }


    public static void overrideLocalData(Runnable callBack) {
        DatabaseReference authUserRef = getAuthUserRef();
        authUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    return;
                }
                if (dataSnapshot.hasChildren()) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    if (hashMap == null) {
                        return;
                    }

                    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
                    if (hashMap.get(KEY_OUTGOING_MESSAGE_SOUNDS) != null) {
                        boolean enable = (boolean) hashMap.get(KEY_OUTGOING_MESSAGE_SOUNDS);
                        final String prefKey = HSApplication.getContext().getString(R.string.send_sound_pref_key);
                        prefs.putBoolean(prefKey, enable);
                    }

                    if (hashMap.get(KEY_NOTIFICATION) != null) {
                        boolean enable = (boolean) hashMap.get(KEY_NOTIFICATION);
                        final String prefKey = HSApplication.getContext().getString(R.string.notifications_enabled_pref_key);
                        prefs.putBoolean(prefKey, enable);
                    }

                    if (hashMap.get(KEY_MESSAGE_BOX) != null) {
                        boolean enable = (boolean) hashMap.get(KEY_MESSAGE_BOX);
                        MessageBoxSettings.setSMSAssistantModuleEnabled(enable);
                    }

                    if (hashMap.get(KEY_VIBRATE) != null) {
                        boolean enable = (boolean) hashMap.get(KEY_VIBRATE);
                        final String prefKey = HSApplication.getContext().getString(R.string.notification_vibration_pref_key);
                        prefs.putBoolean(prefKey, enable);
                    }
                }
                if (callBack != null) {
                    callBack.run();
                }

                authUserRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}

