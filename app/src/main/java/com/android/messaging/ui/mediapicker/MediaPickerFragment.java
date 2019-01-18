package com.android.messaging.ui.mediapicker;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;

public class MediaPickerFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "media_picker_fragment";

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
        view.findViewById(R.id.media_camera).setOnClickListener(this);
        view.findViewById(R.id.media_photo).setOnClickListener(this);
        view.findViewById(R.id.media_voice).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.media_camera:
                if (mOnMediaItemClickListener != null) {
                    mOnMediaItemClickListener.showCamera();
                }
                break;
            case R.id.media_photo:
                if (mOnMediaItemClickListener != null) {
                    mOnMediaItemClickListener.showPhoto();
                }
                break;
            case R.id.media_voice:
                break;
            default:
                break;
        }
    }

    private OnMediaItemClickListener mOnMediaItemClickListener;

    public void setOnMediaItemClickListener(OnMediaItemClickListener onMediaItemClickListener) {
        this.mOnMediaItemClickListener = onMediaItemClickListener;
    }

    public interface OnMediaItemClickListener {
        void showCamera();

        void showPhoto();
    }

}
