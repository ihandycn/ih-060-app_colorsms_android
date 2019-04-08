package com.android.messaging.ui.messagebox;


import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.messaging.R;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Dates;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.util.ArrayList;

import static com.android.messaging.ui.messagebox.MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX;

public class MessageBoxMessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_SMS = 0;
    private static final int ITEM_MMS = 1;

    private ArrayList<MessageBoxItemData> mDataList = new ArrayList<>();
    private String mConversationId;

    @ColorInt
    private int mIncomingTextColor;

    MessageBoxMessageListAdapter(MessageBoxItemData data) {
        mDataList.add(data);
        mConversationId =  data.getConversationId();
        mIncomingTextColor = ConversationColors.get().getMessageTextColor(true, mConversationId);
    }

    void addNewIncomingMessage(MessageBoxItemData data) {
        mDataList.add(data);
        notifyItemInserted(mDataList.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_SMS:
                View smsViewItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_box_sms_item, parent, false);
                SmsViewHolder holder = new SmsViewHolder(smsViewItem);
                holder.mContentText.setOnClickListener(v1 -> {
                    UIIntents.get().launchConversationActivityWithParentStack(v1.getContext(), mConversationId, null);
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH_MESSAGE_BOX);
                });
                holder.mContentText.setTextColor(mIncomingTextColor);
                holder.mContentText.setBackground(ConversationDrawables.get().getBubbleDrawable(false, true,
                        true, false, mConversationId));
                return holder;
            case ITEM_MMS:
                View mmsViewItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_box_mms_item, parent, false);

                MmsViewHolder mmsViewHolder = new MmsViewHolder(mmsViewItem);
                mmsViewHolder.mContentView.setOnClickListener(v -> {
                    UIIntents.get().launchConversationActivityWithParentStack(parent.getContext(), mConversationId, null);
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH_MESSAGE_BOX);
                    MessageBoxAnalytics.logEvent("SMS_PopUp_MMS_Click");
                });
                return mmsViewHolder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_SMS:
                SmsViewHolder smsViewHolder = (SmsViewHolder) holder;
                smsViewHolder.mContentText.setText(mDataList.get(position).getContent());
                smsViewHolder.mDateText.setText(Dates.getConversationTimeString(mDataList.get(position).getReceivedTimestamp()));
                break;
            case ITEM_MMS:
                MmsViewHolder mmsViewHolder = (MmsViewHolder) holder;
                mmsViewHolder.mDateText.setText(Dates.getConversationTimeString(mDataList.get(position).getReceivedTimestamp()));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TextUtils.isEmpty(mDataList.get(position).getContent()) ? ITEM_MMS : ITEM_SMS;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView mContentText;
        TextView mDateText;

        SmsViewHolder(View itemView) {
            super(itemView);
            mContentText = itemView.findViewById(R.id.message_content);
            mDateText = itemView.findViewById(R.id.message_date);
        }
    }

    public static class MmsViewHolder extends RecyclerView.ViewHolder {
        TextView mDateText;
        ViewGroup mContentView;

        MmsViewHolder(View itemView) {
            super(itemView);
            mDateText = itemView.findViewById(R.id.message_date);
            mContentView = itemView.findViewById(R.id.mms_container);
        }
    }

}


