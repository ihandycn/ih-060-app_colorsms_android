package com.android.messaging.ui.smsshow;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.SmsShowListData;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.smsshow.SmsShowUtils;
import com.bumptech.glide.integration.webp.decoder.WebpDrawable;
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Threads;

public class SmsShowActivity extends HSAppCompatActivity implements Runnable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_show_activity);
        String url = SmsShowListData.getInstance().findItemById(SmsShowUtils.getSmsShowAppliedId()).getSmsShowUrl();
        ImageView smsShowImage = findViewById(R.id.sms_show_image);

        Transformation<Bitmap> centerInside = new CenterInside();
        GlideApp.with(this)
                .load(url)
                .optionalTransform(centerInside)
                .optionalTransform(WebpDrawable.class, new WebpDrawableTransformation(centerInside))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        smsShowImage.setImageDrawable(resource);
                        if (resource instanceof WebpDrawable) {
                            ((WebpDrawable) resource).setLoopCount(1);
                            ((WebpDrawable) resource).start();
                        }

                        Threads.postOnMainThreadDelayed(SmsShowActivity.this, 4000L);
                    }
                });
    }

    @Override
    public void run() {
        finish();
    }
}
