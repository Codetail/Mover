package io.codetail.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import codetail.utils.ThemeUtils;
import io.codetail.watchme.R;

public class CategoryMenuAdapter extends ArrayAdapter<String>{

    LayoutInflater mFactory;

    int mResource;
    int mSelectedPosition;
    int mAccentColor;


    public CategoryMenuAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mResource = resource;
        mSelectedPosition = 0;
        mFactory = LayoutInflater.from(context);

        mAccentColor = ThemeUtils.getThemeColor(context, R.attr.colorAccent);
    }

    public void setSelected(int selectedPosition) {
        mSelectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title = (TextView) convertView;

        if(title == null){
            title = (TextView) mFactory.inflate(mResource, parent, false);
        }

        title.setText(getItem(position));
        if(position == mSelectedPosition){
            title.setTextColor(mAccentColor);
        }

        return title;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == mSelectedPosition ? 1 : 0;
    }
}
