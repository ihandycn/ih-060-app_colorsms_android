package com.android.messaging.ui.invitefriends;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;
import com.android.messaging.util.BugleAnalytics;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.ui.invitefriends.InviteFriendsActivity.REQUEST_CODE_ADD_FRIENDS;

public class InviteFriendsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface OnItemCountChangeListener {
        void onChange();
    }
    private OnItemCountChangeListener mOnItemCountChangeListener;
    private Activity mActivity;

    private List<CallAssistantUtils.ContactInfo> mContactInfos = new ArrayList<>();

    public InviteFriendsListAdapter(Activity activity) {
        mActivity = activity;
    }

    public void setOnItemCountChangeListener(OnItemCountChangeListener onItemCountChangeListener) {
        mOnItemCountChangeListener = onItemCountChangeListener;
    }

    public List<CallAssistantUtils.ContactInfo> getContactInfos() {
        return mContactInfos;
    }

    public void initData(List<CallAssistantUtils.ContactInfo> contactInfos) {
        mContactInfos = contactInfos;
        notifyDataSetChanged();
        if (mOnItemCountChangeListener != null) {
            mOnItemCountChangeListener.onChange();
        }
    }

    public void addContact(List<CallAssistantUtils.ContactInfo> contactInfos) {
        if (contactInfos == null) {
            return;
        }
        int preCount = getItemCount();

        for (CallAssistantUtils.ContactInfo contactInfo : contactInfos) {
            if (!mContactInfos.contains(contactInfo)) {
                mContactInfos.add(contactInfo);
            }
        }

        int currentCount = getItemCount();
        if (currentCount - preCount > 0) {
            notifyItemRangeInserted(preCount, currentCount - preCount);
        }

        if (mOnItemCountChangeListener != null) {
            mOnItemCountChangeListener.onChange();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mActivity).inflate(R.layout.invite_friends_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.mDeleteBtn.setOnClickListener(v1 -> {
            int target = viewHolder.getAdapterPosition();
            mContactInfos.remove(target - 1);
            notifyItemRemoved(target);
            BugleAnalytics.logEvent("Invite_SendPage_Delete_Click");
            if (mOnItemCountChangeListener != null) {
                mOnItemCountChangeListener.onChange();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            if (position == 0) {
                ((ViewHolder) holder).mContactIcon.setImageResource(R.drawable.ic_add_black);
                ((ViewHolder) holder).mContactName.setText(R.string.invite_friends_add_friends);
                ((ViewHolder) holder).mContactIcon.setBackgroundResource(R.drawable.light_gray_circle_btn_bg_drwable);
                ((ViewHolder) holder).mContactIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(mActivity, SelectFriendsToInviteActivity.class);
                    mActivity.startActivityForResult(intent, REQUEST_CODE_ADD_FRIENDS);
                    BugleAnalytics.logEvent("Invite_SendPage_Add_Click");

                });
                ((ViewHolder) holder).mDeleteBtn.setVisibility(View.GONE);
            } else {
                position -= 1;
                GlideApp.with(mActivity).load(mContactInfos.get(position).avatarUriStr).placeholder(R.drawable.default_contact_avatar).circleCrop().into(((ViewHolder) holder).mContactIcon);
                ((ViewHolder) holder).mContactName.setText(mContactInfos.get(position).name);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mContactInfos.size() + 1;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView mContactIcon;
        TextView mContactName;
        ImageView mDeleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            mContactIcon = itemView.findViewById(R.id.contact_icon);
            mContactName = itemView.findViewById(R.id.contact_name);
            mDeleteBtn = itemView.findViewById(R.id.delete_friends);
        }
    }
}
