/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.photoviewer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.rastermill.FrameSequenceDrawable;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;

import com.android.ex.photo.PhotoViewCallbacks;
import com.android.ex.photo.fragments.PhotoViewFragment;
import com.android.ex.photo.loaders.PhotoBitmapLoaderInterface.BitmapResult;
import com.android.messaging.download.Downloader;
import com.android.messaging.ui.emoji.utils.EmojiManager;

public class BuglePhotoViewFragment extends PhotoViewFragment {

    /**
     * Public no-arg constructor for allowing the framework to handle orientation changes
     */
    public BuglePhotoViewFragment() {
        // Do nothing.
    }

    public static PhotoViewFragment newInstance(Intent intent, int position,
                                                boolean onlyShowSpinner) {
        final PhotoViewFragment f = new BuglePhotoViewFragment();
        initializeArguments(intent, position, onlyShowSpinner, f);
        return f;
    }

    @Override
    public void onLoadFinished(Loader<BitmapResult> loader, BitmapResult result) {
        super.onLoadFinished(loader, result);
        // Need to check for the first time when we load the photos
        if (PhotoViewCallbacks.BITMAP_LOADER_PHOTO == loader.getId()
                && result.status == BitmapResult.STATUS_SUCCESS
                && mCallback.isFragmentActive(this)) {
            startGif();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TextUtils.isEmpty(mResolvedPhotoUri)) {
            String uri = EmojiManager.getStickerMagicUriByPartUri(mResolvedPhotoUri);
            if (!TextUtils.isEmpty(uri)) {
                mPhotoUriStr = mResolvedPhotoUri;
                mResolvedPhotoUri = uri;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!TextUtils.isEmpty(mResolvedPhotoUri)) {
            mSoundFilePath = getSoundFilePath(mResolvedPhotoUri);
            mLottieFilePath = getLottieFilePath(mResolvedPhotoUri);
        }

        if (isLottieModel()) {
            mStickerLottieMagicView.setVisibility(View.VISIBLE);
            mPhotoView.setVisibility(View.GONE);
            mPhotoPreviewAndProgress.setVisibility(View.GONE);
        } else {
            mStickerLottieMagicView.setVisibility(View.GONE);
            mPhotoView.setVisibility(View.VISIBLE);
            mPhotoPreviewAndProgress.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        onViewVisible();
    }

    @Override
    public void onPause() {
        super.onPause();
        onViewInVisible();
    }

    @Override
    public void resetViews() {
        super.resetViews();
        onViewInVisible();
    }

    private void stopGif() {
        if (!TextUtils.isEmpty(mSoundFilePath)) {
            pauseSoundPlayer();
        }
        final Drawable drawable = getDrawable();
        if (drawable instanceof FrameSequenceDrawable) {
            ((FrameSequenceDrawable) drawable).stop();
        }
    }

    private void startGif() {
        if (mCallback.getCurrentPagePosition() != mPosition) {
            return;
        }
        if (!TextUtils.isEmpty(mSoundFilePath)) {
            startSoundPlayer();
        }
        final Drawable drawable = getDrawable();
        if (drawable instanceof FrameSequenceDrawable) {
            ((FrameSequenceDrawable) drawable).start();
        }
    }

    @Override
    protected void onViewVisible() {
        super.onViewVisible();
        if (isLottieModel()) {
            startPlayLottie();
        } else {
            startGif();
        }
    }

    @Override
    protected void onViewInVisible() {
        super.onViewInVisible();
        pauseLottie();
        stopGif();
    }

    private String getLottieFilePath(String uriStr) {
        return Downloader.getInstance().getDownloadFilePath(EmojiManager.getLottieUrlByGifUriStr(uriStr));
    }

    private String getSoundFilePath(String uriStr) {
        return Downloader.getInstance().getDownloadFilePath(EmojiManager.getSoundUrlByGifUriStr(uriStr));
    }
}
