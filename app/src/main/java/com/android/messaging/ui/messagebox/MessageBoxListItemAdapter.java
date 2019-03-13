package com.android.messaging.ui.messagebox;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.Dates;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.util.ArrayList;

import static com.android.messaging.ui.messagebox.MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX;

public class MessageBoxListItemAdapter extends RecyclerView.Adapter<MessageBoxListItemAdapter.ViewHolder> {

    private ArrayList<String> mMessageList;
    private ArrayList<Long> mTimestampList;
    private String mConversationId;

    MessageBoxListItemAdapter(ArrayList<String> data, String conversationId) {
        this.mMessageList = data;
        this.mConversationId = conversationId;
        mTimestampList = new ArrayList<>(4);
        mTimestampList.add(System.currentTimeMillis());
    }

    void addNewIncomingMessage(String message) {
        mMessageList.add(message);
        mTimestampList.add(System.currentTimeMillis());
        notifyItemInserted(mMessageList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_box_message_item, parent, false);

        ViewHolder holder = new ViewHolder(v);
        holder.msgBodyTextView.setOnClickListener(v1 -> {
            UIIntents.get().launchConversationActivity(v1.getContext(), mConversationId, null);
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH_MESSAGE_BOX);
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.msgBodyTextView.setText(mMessageList.get(position));
        holder.msgDateTextView.setText(Dates.getConversationTimeString(mTimestampList.get(position)).toString());
    }

    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView msgBodyTextView;
        TextView msgDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            msgBodyTextView = itemView.findViewById(R.id.message_content);
            msgDateTextView = itemView.findViewById(R.id.message_date);
        }
    }

}


