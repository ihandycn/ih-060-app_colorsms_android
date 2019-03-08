package com.android.messaging.smsshow;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.messaging.R;

import java.util.ArrayList;

public class MessageBoxListItemAdapter extends RecyclerView.Adapter<MessageBoxListItemAdapter.ViewHolder> {
    interface OnItemClickListener {
        void onItemClick();
    }

    private ArrayList<String> msgList;
    private OnItemClickListener onItemClickListener;

    MessageBoxListItemAdapter(ArrayList<String> data) {
        this.msgList = data;
    }

    void addNewIncomingMessage(String message) {
        msgList.add(message);
        notifyItemInserted(msgList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_box_message_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.msgBodyTextView.setText(msgList.get(position));
//        holder.msgDateTextView.setText(msgList.get(position).getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return msgList == null ? 0 : msgList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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


