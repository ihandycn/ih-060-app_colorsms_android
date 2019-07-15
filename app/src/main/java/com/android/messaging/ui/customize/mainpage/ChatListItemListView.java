package com.android.messaging.ui.customize.mainpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.util.AvatarUriUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatListItemListView extends LinearLayout {
    private List<View> mChildrenView = new ArrayList<>();
    private LayoutInflater mInflater;

    public ChatListItemListView(Context context) {
        this(context, null);
    }

    public ChatListItemListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatListItemListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflater = LayoutInflater.from(getContext());
        loadList();
    }

    @SuppressLint("SetTextI18n")
    private void loadList() {
        addItemView("Luke Lloyd", "When and where shall we...", "09:16");
        addItemView("Robert Wilson", "How are you?", "04:37");
        addItemView("Raymond Howard", "You are so sweet!", "11:14 am");
        addItemView("Dennis Joseph", "Happy birthday!", "Mon");
        addItemView("10692861304017", "Miss you.", "Thu");
        addItemView("Belle Gonzalez", "See you there", "Feb 25");
        addItemView("Milo Masson", "I’m driving, call you later.", "Feb 24");

        addItemView("Luke Lloyd", "When and where shall we...", "09:16");
        addItemView("Robert Wilson", "How are you?", "04:37");
        addItemView("Raymond Howard", "You are so sweet!", "11:14 am");
        addItemView("Dennis Joseph", "Happy birthday!", "Mon");
        addItemView("10692861304017", "Miss you.", "Thu");
    }

    private void addItemView(String name, String snippet, String time) {
        View view =
                mInflater.inflate(R.layout.conversation_list_customize_item_view, this, false);
        ((ImageView) view.findViewById(R.id.conversation_icon_bg)).setImageDrawable(AvatarBgDrawables.getAvatarBg(false));
        String s = "[A-Z]+.*";
        Uri avatarUri = AvatarUriUtil.createAvatarUri(null, Pattern.matches(s, name) ? name : null, null, null);
        ContactIconView icon = view.findViewById(R.id.conversation_icon);
        icon.setImageResourceUri(avatarUri);
        icon.setImageClickHandlerDisabled(true);
        ((TextView) view.findViewById(R.id.conversation_name)).setText(name);
        ((TextView) view.findViewById(R.id.conversation_snippet)).setText(snippet);
        ((TextView) view.findViewById(R.id.conversation_timestamp)).setText(time);
        addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mChildrenView.add(view);
    }

    public void changeFontColor(int titleColor, int snippetColor, int timeColor) {
        for (View v : mChildrenView) {
            ((TextView) v.findViewById(R.id.conversation_name)).setTextColor(titleColor);
            ((TextView) v.findViewById(R.id.conversation_snippet)).setTextColor(snippetColor);
            ((TextView) v.findViewById(R.id.conversation_timestamp)).setTextColor(timeColor);
        }
    }
}
