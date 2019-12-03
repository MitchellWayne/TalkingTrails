package com.example.untitledcs118proj;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ProfItemAdapter extends BaseAdapter {

    private ArrayList<ProfItem> singleRow;
    private LayoutInflater thisInflater;

    public ProfItemAdapter(Context context, ArrayList<ProfItem> aRow) {
        this.singleRow = aRow;
        thisInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return singleRow.size();
    }

    @Override
    public Object getItem(int position) {
        return singleRow.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = thisInflater.inflate(R.layout.profilelist_item, parent, false);
        }

        ImageView theImage = (ImageView) convertView.findViewById(R.id.imgView_image);
        TextView theText = (TextView) convertView.findViewById(R.id.imgView_textdata);

        ProfItem currRow = (ProfItem) getItem(position);

//        theImage.setImageBitmap(currRow.getImg());
        theImage.setImageResource(R.drawable.add_sign);
        theText.setText("Caption: " + currRow.getCaption() + "\nViews: " + currRow.getViewCount());


        return convertView;
    }
}