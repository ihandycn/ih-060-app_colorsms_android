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
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationSelectAdapter extends RecyclerView.Adapter<ConversationSelectAdapter.ViewHolder> {

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

    private List<ConversationListItemData> recyclerDataList = new ArrayList<>();
    private HashMap<ConversationListItemData, Boolean> selectedMap = new HashMap<>();

    public void updateData(List<ConversationListItemData> data) {
        this.recyclerDataList.clear();
        this.recyclerDataList.addAll(data);
        selectedMap.clear();
        notifyDataSetChanged();
    }

    public HashMap<ConversationListItemData, Boolean> getSelectedMap() {
        return selectedMap;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_assistant_sub_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ConversationListItemData contactInfo = recyclerDataList.get(position);

        if (TextUtils.isEmpty(contactInfo.getName())) {
            holder.mainTitle.setText(contactInfo.getSnippetText());
            holder.subTitle.setVisibility(View.GONE);
        } else {
            holder.mainTitle.setText(contactInfo.getName());
            holder.subTitle.setText(contactInfo.getSnippetText());
            holder.subTitle.setVisibility(View.VISIBLE);
        }

        ImageView checkbox = holder.checkBoxView;
        if (null != selectedMap.get(contactInfo) &&
                selectedMap.get(contactInfo).equals(Boolean.TRUE)) {
            checkbox.setImageResource(R.drawable.conversation_check);
            checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(20), false));
        } else {
            checkbox.setImageDrawable(null);
            checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(0, 0, 4,
                    0xffbdc2c9, Dimensions.pxFromDp(20), false, false));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != selectedMap.get(contactInfo)
                        && selectedMap.get(contactInfo).equals(Boolean.TRUE)) {
                    selectedMap.put(contactInfo, Boolean.FALSE);
                    checkbox.setImageDrawable(null);
                    checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(0, 0, 4,
                            0xffbdc2c9, Dimensions.pxFromDp(20), false, false));
                } else {
                    selectedMap.put(contactInfo, Boolean.TRUE);
                    checkbox.setImageResource(R.drawable.conversation_check);
                    checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                            PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(20), false));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclerDataList == null ? 0 : recyclerDataList.size();
    }
}