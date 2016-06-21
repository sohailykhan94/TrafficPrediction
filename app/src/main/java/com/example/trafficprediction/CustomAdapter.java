package com.example.trafficprediction;

/**
 * Created by sohailyarkhan on 25/03/16.
 */
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.support.v4.widget.SlidingPaneLayout;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter implements Filterable {

    private ArrayList<Location> mOriginalValues; // Original Values
    private ArrayList<Location> mDisplayedValues;    // Values to be displayed
    LayoutInflater inflater;
    private Context context;
    private Activity mainActivity;
    Resources resources;

    public CustomAdapter(Activity mainActivity,Context context, ArrayList<Location> mLocationArray) {
        this.mOriginalValues = mLocationArray;
        this.mDisplayedValues = mLocationArray;
        this.context = context;
        this.mainActivity = mainActivity;
        resources = mainActivity.getResources();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        LinearLayout llContainer;
        TextView des,src, timeHoler;
        ImageView circle;
        //SlidingUpPanelLayout mLayout;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.test_layout, null);
            holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
            holder.src = (TextView) convertView.findViewById(R.id.src);
            holder.des = (TextView) convertView.findViewById(R.id.des);
            holder.timeHoler = (TextView) convertView.findViewById(R.id.timeHolder);
            holder.circle = (ImageView) convertView.findViewById(R.id.circle);
            //holder.mLayout = (SlidingUpPanelLayout) convertView.findViewById(R.id.sliding_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.src.setText(mDisplayedValues.get(position).src);
        holder.des.setText(mDisplayedValues.get(position).des);
        holder.timeHoler.setText(mDisplayedValues.get(position).time);

        if (mDisplayedValues.get(position).isDelay == 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.circle.setImageDrawable(resources.getDrawable(R.drawable.circle_green, mainActivity.getTheme()));
            } else {
                holder.circle.setImageDrawable(resources.getDrawable(R.drawable.circle_green));
            }
        }else if(mDisplayedValues.get(position).isDelay == 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.circle.setImageDrawable(resources.getDrawable(R.drawable.circle_yellow, mainActivity.getTheme()));
            } else {
                holder.circle.setImageDrawable(resources.getDrawable(R.drawable.circle_yellow));
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.circle.setImageDrawable(resources.getDrawable(R.drawable.circle_red, mainActivity.getTheme()));
            } else {
                holder.circle.setImageDrawable(resources.getDrawable(R.drawable.circle_red));
            }
        }

        final OnClickListener makeListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //main.makeInfo(pos);
                ((MapsActivity)mainActivity).makeInfo(position, mDisplayedValues.get(position).srcLatLng, mDisplayedValues.get(position).desLatLng,  mDisplayedValues.get(position).src,  mDisplayedValues.get(position).des);
            }
        };
        holder.llContainer.setOnClickListener(makeListener);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDisplayedValues = (ArrayList<Location>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Location> FilteredArrList = new ArrayList<Location>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Location>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).src;
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(new Location(mOriginalValues.get(i).src,mOriginalValues.get(i).des,mOriginalValues.get(i).time, mOriginalValues.get(i).isDelay, mOriginalValues.get(i).srcLatLng, mOriginalValues.get(i).desLatLng));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}