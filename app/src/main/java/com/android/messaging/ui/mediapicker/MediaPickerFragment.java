package com.android.messaging.ui.mediapicker;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class MediaPickerFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "media_picker_fragment";

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
        ImageView mediaVoice= view.findViewById(R.id.media_voice);
        mediaVoice.setOnClickListener(this);
        mediaVoice.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                PrimaryColors.getPrimaryColorDark(),
                Dimensions.pxFromDp(35), false, true));

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

            @Override public int getConversationSelfSubId() {
                if (mSubscriptionDataProvider != null) {
                    return mSubscriptionDataProvider.getConversationSelfSubId();
                }
                return 0;
            }
        });

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
            default:
                break;
        }
    }

    public interface OnMediaItemListener {
        void showCamera();

        void showPhoto();

        void onAudioRecorded(MessagePartData item);
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
}
