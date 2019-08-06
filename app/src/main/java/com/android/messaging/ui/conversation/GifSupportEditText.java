package com.android.messaging.ui.conversation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.android.messaging.ui.emoji.EmojiEditText;

public class GifSupportEditText extends EmojiEditText {

    public interface onGetRichContentFromImeListener {
        void onGetContent(InputContentInfoCompat contentInfoCompat);
    }

    private onGetRichContentFromImeListener mOnGetRichContentFromImeListener;

    public GifSupportEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnGetRichContentFromImeListener(onGetRichContentFromImeListener listener) {
        mOnGetRichContentFromImeListener = listener;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String[]{"image/gif"});

        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        // read and display inputContentInfo asynchronously
                        if (BuildCompat.isAtLeastNMR1() && (flags &
                                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();
                            } catch (Exception e) {
                                return false; // return false if failed
                            }

                            if (mOnGetRichContentFromImeListener != null) {
                                mOnGetRichContentFromImeListener.onGetContent(inputContentInfo);
                            }
                        }



                        // read and display inputContentInfo asynchronously.
                        // call inputContentInfo.releasePermission() as needed.

                        return true;  // return true if succeeded
                    }

                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }
}
