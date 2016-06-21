package com.example.trafficprediction;


import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by sohailyarkhan on 10/03/16.
 */
public class TimePickerFragment extends DialogFragment {

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        minute = minute * 10;
        Toast.makeText(mActivity, "Selected minute: " + minute, Toast.LENGTH_LONG).show();
    }

    private Activity mActivity;
    private TimePickerDialog.OnTimeSetListener mListener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        // This error will remind you to implement an OnTimeSetListener
        //   in your Activity if you forget
        try {
            mListener = (TimePickerDialog.OnTimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTimeSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int current_min = c.get(Calendar.MINUTE);
        int minute = c.get(Calendar.MINUTE) / 15;
        minute = (minute > 3) ? 0 : minute;

        Log.i("Time", " " + minute);

        // Create a new instance of TimePickerDialog and return it
        final TimePickerDialog tpd = new TimePickerDialog(mActivity, android.R.style.Theme_Holo_Light_Dialog, mListener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        tpd.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                int tpLayoutId = getResources().getIdentifier("timePickerLayout", "id", "android");

                ViewGroup tpLayout = (ViewGroup) tpd.findViewById(tpLayoutId);
                ViewGroup layout = (ViewGroup) tpLayout.getChildAt(0);

                // Customize minute NumberPicker
                NumberPicker minutePicker = (NumberPicker) layout.getChildAt(2);
                String str_arr[] = {"00", "15", "30", "45"};

                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(3);
                minutePicker.setDisplayedValues(str_arr);
            }
        });

        return tpd;
    }
}