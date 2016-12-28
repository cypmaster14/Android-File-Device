package com.example.ciprian.project_afd;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ciprian on 24/12/2016.
 */

public class ItemAdapter extends ArrayAdapter<Item> {

    Context context;
    int layoutResourceId;
    List<Item> data = null;

    public ItemAdapter(Context context, int layoutResourceId, List<Item> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
            holder.TextView02 = (TextView) row.findViewById(R.id.TextView02);
            holder.TextViewDate = (TextView) row.findViewById(R.id.TextViewDate);


            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        Item item = data.get(position);
        holder.txtTitle.setText(item.title);
        holder.imgIcon.setImageResource(item.icon);
        holder.TextView02.setText(item.data);
        holder.TextViewDate.setText(item.path);

        return row;
    }

    static class ItemHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView TextView02;
        TextView TextViewDate;
    }
}
