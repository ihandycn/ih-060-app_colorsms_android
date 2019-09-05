package com.android.messaging.ui.mediapicker;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.mediapicker.sendcontact.ContactFileCreator;
import com.android.messaging.ui.mediapicker.sendcontact.MediaContactPickerActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MediaPickerFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "media_picker_fragment";
    public static final int CHOOSE_CONTACT_REQUEST_CODE = 332;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 2;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 3;
    private AudioRecordView mAudioRecordView;
    private View mEnabledView;
    private View mMissingPermissionView;
    private View mMediaLayout;

    private DraftMessageData.DraftMessageSubscriptionDataProvider mSubscriptionDataProvider;

    private OnMediaItemListener mOnMediaItemListener;

    public void setOnMediaItemClickListener(OnMediaItemListener onMediaItemListener) {
        this.mOnMediaItemListener = onMediaItemListener;
    }

    public void setSubscriptionDataProvider(final DraftMessageData.DraftMessageSubscriptionDataProvider provider) {
        mSubscriptionDataProvider = provider;
    }

    public static MediaPickerFragment newInstance() {
        return new MediaPickerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_picker_layout, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mMediaLayout = view.findViewById(R.id.media_buttons);
        view.findViewById(R.id.media_camera).setOnClickListener(this);
        view.findViewById(R.id.media_photo).setOnClickListener(this);
        view.findViewById(R.id.media_schedule).setOnClickListener(this);
        view.findViewById(R.id.media_contact).setOnClickListener(this);
        ImageView mediaVoice = view.findViewById(R.id.media_voice);
        mediaVoice.setOnClickListener(this);
        mediaVoice.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color),
                getResources().getColor(R.color.primary_color_dark),
                Dimensions.pxFromDp(35), false, false));

        mAudioRecordView = view.findViewById(R.id.audio_record_layout);
        mEnabledView = view.findViewById(R.id.mediapicker_enabled);
        mMissingPermissionView = view.findViewById(R.id.missing_permission_view);
        mAudioRecordView.setHostInterface(new AudioRecordView.HostInterface() {
            @Override
            public void onAudioRecorded(MessagePartData item) {
                if (mOnMediaItemListener != null) {
                    mOnMediaItemListener.onAudioRecorded(item);
                }
            }

            @Override
            public int getConversationSelfSubId() {
                if (mSubscriptionDataProvider != null) {
                    return mSubscriptionDataProvider.getConversationSelfSubId();
                }
                return 0;
            }
        });

        if (!HSConfig.optBoolean(false, "Application", "ScheduleMessage")) {
            view.findViewById(R.id.media_schedule_container).setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) view.findViewById(R.id.media_select_placeholder).getLayoutParams()).weight = 2;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.media_camera:
                if (!CameraManager.hasCameraPermission()) {
                    requestCameraPermission();
                    return;
                }
                openCamera();
                break;
            case R.id.media_photo:
                if (!OsUtil.hasStoragePermission()) {
                    requestStoragePermission();
                    return;
                }
                openGallery();
                break;
            case R.id.media_voice:
                openAudio();
                break;
            case R.id.media_schedule:
                if (mOnMediaItemListener != null) {
                    mOnMediaItemListener.onScheduledIconClick();
                }
                break;
            case R.id.media_contact:
                Intent intent = new Intent(getContext(), MediaContactPickerActivity.class);
                startActivityForResult(intent, CHOOSE_CONTACT_REQUEST_CODE);
                BugleAnalytics.logEvent("SMS_DetailsPage_Plus_Contact", true);
                break;
            default:
                break;
        }
    }

    public interface OnMediaItemListener {
        void showCamera();

        void showPhoto();

        void onAudioRecorded(MessagePartData item);

        void onScheduledIconClick();

        void onVCardContactAdded(List<Uri> vCardList);

        void onTextContactAdded(String contactString);
    }

    private void openCamera() {
        if (mOnMediaItemListener != null) {
            mOnMediaItemListener.showCamera();
        }
    }

    private void openGallery() {
        if (mOnMediaItemListener != null) {
            mOnMediaItemListener.showPhoto();
        }
    }

    private void openAudio() {
        BugleAnalytics.logEvent("SMS_DetailsPage_Plus_Voice", true);
        mMediaLayout.setVisibility(View.GONE);
        mAudioRecordView.setVisibility(View.VISIBLE);
        if (!OsUtil.hasRecordAudioPermission()) {
            requestRecordAudioPermission();
        }
    }

    private void requestCameraPermission() {
        if (OsUtil.isAtLeastM()) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestStoragePermission() {
        if (OsUtil.isAtLeastM()) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestRecordAudioPermission() {
        if (OsUtil.isAtLeastM()) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        }
    }

    public void hideAudioView() {
        if (mAudioRecordView != null) {
            mAudioRecordView.onPause();
            mMediaLayout.setVisibility(View.VISIBLE);
            mAudioRecordView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length == 0 || grantResults.length == 0) {
            return;
        }
        if (CAMERA_PERMISSION_REQUEST_CODE == requestCode) {
            final boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionGranted) {
                openCamera();
            }
        } else if (GALLERY_PERMISSION_REQUEST_CODE == requestCode) {
            final boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionGranted) {
                openGallery();
            }
        } else if (RECORD_AUDIO_PERMISSION_REQUEST_CODE == requestCode) {
            final boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (mEnabledView != null) {
                mEnabledView.setVisibility(permissionGranted ? View.VISIBLE : View.GONE);
            }

            if (mMissingPermissionView != null) {
                mMissingPermissionView.setVisibility(permissionGranted ? View.GONE : View.VISIBLE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == CHOOSE_CONTACT_REQUEST_CODE) {
            HashMap<String, String> contacts =
                    (HashMap<String, String>) data.getSerializableExtra("contacts");
            if (MediaContactPickerActivity.CONTACT_SEND_TYPE_TEXT.equals(data.getStringExtra("type"))) {
                if (mOnMediaItemListener != null) {
                    StringBuilder sb = new StringBuilder();
                    Set<String> set = contacts.keySet();
                    for (String key : set) {
                        if (sb.length() > 0) {
                            sb.append(",\r\n");
                        }
                        sb.append(contacts.get(key)).append(" (").append(key).append(")");
                    }
                    mOnMediaItemListener.onTextContactAdded(sb.toString());
                }
            } else if (MediaContactPickerActivity.CONTACT_SEND_TYPE_VCARD.equals(data.getStringExtra("type"))) {
                if (mOnMediaItemListener != null) {
                    Set<String> set = contacts.keySet();
                    List<Uri> vCardList = new ArrayList<>();
                    for (String key : set) {
                        Uri uri = ContactFileCreator.create(contacts.get(key), null, key);
                        vCardList.add(uri);
                    }
                    mOnMediaItemListener.onVCardContactAdded(vCardList);
                }
            }
        }
    }
}
