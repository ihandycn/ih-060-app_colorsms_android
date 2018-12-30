package com.android.messaging.ui.emoji;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.libwebp.WebpUtils;
import com.bumptech.glide.Glide;

public class EmojiFragment extends Fragment {
    public static final String FRAGMENT_TAG = "emoji";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emoji_fragment, container, false);

        ImageView imageView = view.findViewById(R.id.image_view);
        Glide.with(getActivity())
                .load(WebpUtils.getWebpPath("boost_anim.webp"))
                .into(imageView);
        return view;
    }
}
