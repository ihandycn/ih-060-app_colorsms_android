package com.android.messaging.privatebox.ui.addtolist;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsSelectAdapter extends RecyclerView.Adapter<ContactsSelectAdapter.ViewHolder> {

    public interface OnButtonClickListener {
        void onItemViewClicked(String contactName, String contactNumber, String avatarUriStr);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mainTitle, subTitle;
        ImageView checkBoxView, avatarImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mainTitle = itemView.findViewById(R.id.main_title);
            subTitle = itemView.findViewById(R.id.sub_title);
            checkBoxView = itemView.findViewById(R.id.call_assistant_sub_category_checked);
            avatarImageView = itemView.findViewById(R.id.missied_calls_sub_category_icon);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

            subTitle.setLayoutParams(params);
        }
    }

    private List<CallAssistantUtils.ContactInfo> recyclerDataList = new ArrayList<>();
    private OnButtonClickListener listener;
    private boolean isWhitelistMode;

    public ContactsSelectAdapter(boolean isWhitelistMode) {
        this.isWhitelistMode = isWhitelistMode;
    }

    public void updateData(List<CallAssistantUtils.ContactInfo> data) {
        this.recyclerDataList.clear();
        this.recyclerDataList.addAll(data);
        notifyDataSetChanged();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public List<CallAssistantUtils.ContactInfo> getRecyclerDataList() {
        return new ArrayList<>(recyclerDataList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_assistant_sub_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CallAssistantUtils.ContactInfo contactInfo = recyclerDataList.get(position);

        if (TextUtils.isEmpty(contactInfo.name)) {
            holder.mainTitle.setText(contactInfo.number);
            holder.subTitle.setVisibility(View.GONE);
        } else {
            holder.mainTitle.setText(contactInfo.name);
            holder.subTitle.setText(contactInfo.number);
            holder.subTitle.setVisibility(View.VISIBLE);
        }

        if (isWhitelistMode) {
            holder.checkBoxView.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {

                if (listener != null) {
                    listener.onItemViewClicked(contactInfo.name, contactInfo.number, contactInfo.avatarUriStr);
                }
            });
        } else {
            if (contactInfo.customInfo.equals(Boolean.TRUE)) {
                holder.checkBoxView.setImageResource(R.drawable.ic_all_checked);
            } else {
                holder.checkBoxView.setImageResource(R.drawable.ic_all_unchecked);
            }

            holder.itemView.setOnClickListener(view -> {
                if (contactInfo.customInfo.equals(Boolean.TRUE)) {
                    contactInfo.customInfo = Boolean.FALSE;
                    holder.checkBoxView.setImageResource(R.drawable.ic_all_unchecked);
                } else {
                    contactInfo.customInfo = Boolean.TRUE;
                    holder.checkBoxView.setImageResource(R.drawable.ic_all_checked);
                }
            });
        }

        CallAssistantUtils.displayRoundCornerAvatar(holder.avatarImageView, contactInfo.avatarUriStr);
    }

    @Override
    public int getItemCount() {
        return recyclerDataList == null ? 0 : recyclerDataList.size();
    }
}