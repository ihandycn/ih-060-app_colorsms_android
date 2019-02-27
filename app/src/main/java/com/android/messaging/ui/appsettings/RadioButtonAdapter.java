package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.font.MessageFontManager;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Fonts;

import org.qcode.fontchange.impl.QueryBuilder;

import java.util.List;
import java.util.Map;

public class RadioButtonAdapter extends BaseAdapter {

    private List<Map<String, Object>> mData;
    private LayoutInflater mInflater;
    private int mSelected;
    private Context mContext;
    private Handler mHandler = null;
    private boolean mRequested = false;

    public RadioButtonAdapter(Context context, List<Map<String, Object>> list, int select) {
        mData = list;
        mSelected = select;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String fontFamily = (String) mData.get(position).get("item");
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.new_dialog_select_item, null);
            holder.radioBtn = convertView.findViewById(R.id.dialog_select_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Typeface font = Fonts.getTypeface(mContext.getResources().getString(R.string.light_dialog_choice_text_font));
        TypedValue out = new TypedValue();
        mContext.getResources().getValue(R.dimen.light_dialog_choice_text, out, true);
        float alpha = out.getFloat();
        holder.radioBtn.setTypeface(font);
        holder.radioBtn.setAlpha(alpha);
        holder.radioBtn.setText(fontFamily);
        holder.radioBtn.setClickable(false);
        /*holder.radioBtn.setButtonTintList(new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled}, //disabled
                        new int[]{android.R.attr.state_enabled} //enabled
                },
                new int[]{
                        Color.BLACK, //disabled
                        Color.BLUE //enabled
                }
        ));*/
        if (mSelected == position) {
            holder.radioBtn.setChecked(true);
        } else {
            holder.radioBtn.setChecked(false);
        }
        if (!mRequested && !fontFamily.equals("Default")) {
            if (fontFamily.equals("System")) {
                fontFamily = "Roboto";
            }
            MessageFontManager.loadTypeface(fontFamily,400, new FontsContractCompat.FontRequestCallback(){
                @Override
                public void onTypefaceRetrieved(Typeface typeface) {
                    holder.radioBtn.setTypeface(typeface);
                }

                @Override
                public void onTypefaceRequestFailed(int reason) {

                }
            });
        }
        return convertView;
    }

    class ViewHolder {
        RadioButton radioBtn;
    }


}
