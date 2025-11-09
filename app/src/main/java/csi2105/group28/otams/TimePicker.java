package csi2105.group28.otams;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePicker extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public boolean isStartTimePicker = false;
    public boolean isEndTimePicker = false;

    TutorAvailabilityActivity parent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        parent = (TutorAvailabilityActivity) getActivity();

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        if (isStartTimePicker) {
            parent.setStartTime(hourOfDay, minute);
        } else if (isEndTimePicker) {
            parent.setEndTime(hourOfDay, minute);
        }
    }
}
