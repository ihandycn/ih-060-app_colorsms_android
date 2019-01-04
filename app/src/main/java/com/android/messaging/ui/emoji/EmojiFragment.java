package com.android.messaging.ui.emoji;


import android.app.Fragment;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class EmojiFragment extends Fragment {
    public static final String FRAGMENT_TAG = "emoji";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emoji_fragment, container, false);

        ImageView imageView = view.findViewById(R.id.image_view);
        GlideApp.with(getActivity())
                .load("https://res.cloudinary.com/demo/image/upload/fl_awebp/bored_animation.webp")
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                        if (resource instanceof Animatable) {
                            ((Animatable) resource).start();
                        }
                    }
                });

        return view;
    }
}
