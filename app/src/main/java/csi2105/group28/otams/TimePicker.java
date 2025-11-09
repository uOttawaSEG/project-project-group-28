package csi2105.group28.otams;


import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimePicker extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public boolean isStartTimePicker;
    public boolean isEndTimePicker;
    TutorAvailabilityActivity parent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker.
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        parent = (TutorAvailabilityActivity) getActivity();

        // Create a new instance of TimePickerDialog and return it.
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        if(isStartTimePicker) {
            parent.setStartTime(hourOfDay, minute);
        }
        if(isEndTimePicker) {
            parent.setEndTime(hourOfDay, minute);
        }
    }
}