package com.android.messaging.ui.appsettings;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Threads;

public class ChooseEmojiSkinDialog extends BaseDialogFragment {

    private RecyclerView mRecyclerView;
    private ChooseEmojiSkinAdapter mAdapter;

    private int mChoose = 0;
    private final String BASE_EMOJI = new String(Character.toChars(0x270B));

    private String[] mEmojiSkins = new String[]{
            BASE_EMOJI,
            BASE_EMOJI + EmojiManager.EMOJI_SKINS[1],
            BASE_EMOJI + EmojiManager.EMOJI_SKINS[2],
            BASE_EMOJI + EmojiManager.EMOJI_SKINS[3],
            BASE_EMOJI + EmojiManager.EMOJI_SKINS[4],
            BASE_EMOJI + EmojiManager.EMOJI_SKINS[5]
    };

    private ChooseEmojiSkinAdapter.SkinChooseListener mListener;

    public ChooseEmojiSkinDialog(int choose, ChooseEmojiSkinAdapter.SkinChooseListener listener) {
        this.mChoose = choose;
        this.mListener = listener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getContentView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_emoji_skin, null);
        mRecyclerView = view.findViewById(R.id.recycler_view);

        mAdapter = new ChooseEmojiSkinAdapter(mEmojiSkins, mChoose, new ChooseEmojiSkinAdapter.SkinChooseListener() {
            @Override
            public void onSkinChooseListener(int index) {
                mListener.onSkinChooseListener(index);
                Threads.postOnMainThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ChooseEmojiSkinDialog.this.dismissAllowingStateLoss();
                    }
                }, 340);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(layoutManager);
        removeDialogContentHorizontalMargin();
        removeDialogContentVerticalMargin();
        return view;
    }

    @Override
    protected CharSequence getTitle() {
        return null;
    }

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return null;
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return null;
    }
}
