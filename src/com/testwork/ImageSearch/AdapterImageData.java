package com.testwork.ImageSearch;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AdapterImageData extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List data;
    private Activity activity;

    public ImageLoader imageLoader;

    static class ViewHolder {
        TextView textTitle;
        ImageView srcImage;
    }

    public AdapterImageData(Activity context, List data) {
        activity = context;
        this.data = data;
        layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        if (data == null) {
            return 0;
        }

        return data.size();
    }

    @Override
    public Object getItem(int position) {
        if (data == null) {
            return null;
        }

        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (data == null) {
            return 0;
        }

        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View curentView = view;
        ViewHolder holder = null;

        if (curentView == null) {
            curentView = layoutInflater.inflate(R.layout.item_list, parent, false);
            holder = new ViewHolder();

            holder.textTitle = (TextView) curentView.findViewById(R.id.titleImageId);
            holder.srcImage = (ImageView) curentView.findViewById(R.id.imageId);

            curentView.setTag(holder);

        } else {
            holder = (ViewHolder) curentView.getTag();
        }

        ImageData imageData = (ImageData) getItem(position);

        holder.textTitle.setText(Html.fromHtml(imageData.getTitle()));
        holder.srcImage.setTag(imageData.getThumbUrl());
        imageLoader.DisplayImage(imageData.getThumbUrl(), activity, holder.srcImage);

        return curentView;
    }
}
