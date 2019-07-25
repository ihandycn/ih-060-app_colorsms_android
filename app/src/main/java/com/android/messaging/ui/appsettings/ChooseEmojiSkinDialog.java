package com.android.messaging.ui.appsettings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.LoadEmojiManager;
import com.android.messaging.util.BugleAnalytics;

public class ChooseEmojiSkinDialog extends BaseDialogFragment {

    private RecyclerView mRecyclerView;
    private ChooseEmojiSkinAdapter mAdapter;

    private final String BASE_EMOJI = new String(Character.toChars(0x1f590));

    public ChooseEmojiSkinDialog() {
    }

    public static ChooseEmojiSkinDialog newInstance() {
        ChooseEmojiSkinDialog dialog = new ChooseEmojiSkinDialog();
        return dialog;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View getContentView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_emoji_skin, null);
        mRecyclerView = view.findViewById(R.id.recycler_view);

        mAdapter = new ChooseEmojiSkinAdapter(SettingEmojiSkinItemView.getSkinResource(), EmojiManager.getSkinDefault(), index -> {
            EmojiManager.setSkinDefault(index);
            LoadEmojiManager.getInstance().flush();
            dismissAllowingStateLoss();
            BugleAnalytics.logEvent("Settings_EmojiSkintone_Set", "type", String.valueOf(index + 1));
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

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
