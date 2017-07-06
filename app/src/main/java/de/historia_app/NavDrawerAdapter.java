package de.historia_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem> {

    private LayoutInflater inflater;

    public NavDrawerAdapter(Context context, ArrayList<NavDrawerItem> data) {
        super(context, 0, data);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final int neededLayoutId = getLayoutType(position);

        if(convertView == null) {
            convertView = inflater.inflate(neededLayoutId, parent, false);
        } else {
            if (convertView.getId() != neededLayoutId)  {
                convertView = inflater.inflate(neededLayoutId, parent, false);
            }
        }

        final NavDrawerItem item = getItem(position);
        if(item != null) {
            ((TextView) convertView).setText(item.getTitle());
            if(!item.isDisplayed()) {
                convertView.setVisibility(View.GONE);
                convertView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
            }
        }
        return convertView;
    }

    public int getLayoutType(int position) {
        NavDrawerItem item = getItem(position);

        if(item == null || !item.isSubItem()) {
            return R.layout.drawer_list_item;
        }
        return R.layout.drawer_list_subitem;
    }
}
