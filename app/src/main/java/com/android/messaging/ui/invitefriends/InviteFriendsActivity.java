package com.android.messaging.ui.invitefriends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import com.android.messaging.R;
import com.android.messaging.ui.view.MessagesTextView;

public class InviteFriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        MessagesTextView autoLinkMessagesTextView = findViewById(R.id.invite_friends_message_auto_link);
        stripUnderlines(autoLinkMessagesTextView);
    }

    private class URLSpanNoUnderline extends URLSpan {
        private URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    private void stripUnderlines(MessagesTextView textView) {
        if(null!=textView&&textView.getText() instanceof Spannable){
            Spannable s = (Spannable)textView.getText();
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span: spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                s.setSpan(span, start, end, 0);
            }

            textView.setAutoLinkMask(0);
            textView.setText(s);
        }
    }
}
