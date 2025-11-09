package csi2105.group28.otams;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorAvailabilityActivity extends AppCompatActivity
{
    private Button dateInput;
    private Button StartTimeInput;
    private Button EndTimeInput;
    private TextView dateInputText;
    private TextView StartTimeText;
    private TextView EndTimeText;

    private Button AddButton;

    private Spinner courseSpinner;

    private TextView highestDegree;

    private ArrayAdapter<String> TutorSlotAdapter;
    private ArrayAdapter<String> courseAdapter;
    private ArrayAdapter<String> futureslotAdapter;
    private ArrayAdapter<String> pastslotAdapter;


    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayList<String> TutorSlotDisplay = new ArrayList<>();
    private ArrayList<String> slotKeys = new ArrayList<>();
    private ArrayList<String> upcomingslots = new ArrayList<>();
    private ArrayList<String> pastslots = new ArrayList<>();




    private ListView SlotList;
    private ListView FutureSlotList;
    private ListView PastSlotList;



    private DatabaseReference TutorAvailabilityRef;
    private DatabaseReference TutorInfoRef;

    private String Tutorusername;
    private String TutorFirstname;
    private String TutorLastname;
    private String ChosenCourse;



    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_availability);

        Tutor tutor = (Tutor) getIntent().getSerializableExtra("info");

        if (tutor != null)
        {
            Tutorusername = removeperiods(tutor.getUsername());
        }
        else
        {
            Toast.makeText(this, "Tutor object is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        TutorAvailabilityRef = FirebaseDatabase.getInstance().getReference("otams").child("Availability").child(Tutorusername);
        TutorInfoRef = FirebaseDatabase.getInstance().getReference("otams").child("Users").child("Tutor").child(Tutorusername.substring(0, 1)).child(Tutorusername.substring(1, 2)).child(Tutorusername).child("info");


        dateInput = findViewById(R.id.pickDate);

        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker mDatePickerDialogFragment;
                mDatePickerDialogFragment = new DatePicker();
                mDatePickerDialogFragment.show(getSupportFragmentManager(), "Appointement Date");
            }
        });
        dateInputText = findViewById(R.id.pickDateText);

        StartTimeInput = findViewById(R.id.StartTimeInput);

        StartTimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker mTimePickerDialogFragment;
                mTimePickerDialogFragment = new TimePicker();
                mTimePickerDialogFragment.isStartTimePicker = true;
                mTimePickerDialogFragment.show(getSupportFragmentManager(), "Start Time");
            }
        });
        StartTimeText = findViewById(R.id.StartTimeText);

        EndTimeInput = findViewById(R.id.EndTimeInput);

        EndTimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker mTimePickerDialogFragment;
                mTimePickerDialogFragment = new TimePicker();
                mTimePickerDialogFragment.isEndTimePicker = true;
                mTimePickerDialogFragment.show(getSupportFragmentManager(), "End Time");
            }
        });

        EndTimeText = findViewById(R.id.EndTimeText);

        AddButton = findViewById(R.id.AddButton);

        FutureSlotList = findViewById(R.id.futureList);
        PastSlotList = findViewById(R.id.pastList);

        highestDegree = findViewById(R.id.highestDegree);
        courseSpinner = findViewById(R.id.courseSpinner);

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);


        loadTutorInfo(TutorInfoRef);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ChosenCourse = courseList.get(position);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                ChosenCourse = "";
            }
        });



        futureslotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, upcomingslots);
        pastslotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pastslots);


        FutureSlotList.setAdapter(futureslotAdapter);
        PastSlotList.setAdapter(pastslotAdapter);

        SlotLoader();

        AddButton.setOnClickListener(v -> AddSlot());

        FutureSlotList.setOnItemLongClickListener((adapterView, view, pos, l) ->
        {
            if (pos < slotKeys.size())
            {
                deleteSlot(slotKeys.get(pos));
            }
            return true;
        });

    }
    private String removeperiods(String username) // May be redundant
    {
        if (username == null)
        {
            return "";
        }
        return username.replace(".", "");
    }
    private void SlotLoader() {
        TutorAvailabilityRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upcomingslots.clear();
                pastslots.clear();
                slotKeys.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String start = dataSnapshot.child("start").getValue(String.class);
                    String end = dataSnapshot.child("end").getValue(String.class);
                    String course = dataSnapshot.child("course").getValue(String.class); // read stored course
                    boolean Booked = Boolean.TRUE.equals(dataSnapshot.child("Booked").getValue(Boolean.class));
                    if (course == null) course = "Unknown Course";
                    if (date == null) date = "Unknown Date";
                    if (start == null) start = "Unknown Start Time";
                    if (end == null) end = " Unknown End Time";
                    {
                        String slot = "Tutor: " + TutorFirstname + " " + TutorLastname + " |  Course: " + course + " | Date: " + date + " | Start time: " + start + " | End time: " + end + " | Status: " + (Booked ? "Booked" : "Available");
                        TutorSlotDisplay.add(slot);

                        if (pastSessionCheck(date, end))
                        {
                            pastslots.add(slot);
                        }
                        else
                        {
                            upcomingslots.add(slot);

                        }
                        slotKeys.add(key);
                    }
                }
                futureslotAdapter.notifyDataSetChanged();
                pastslotAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(TutorAvailabilityActivity.this, "Error Loading slots", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean pastSessionCheck(String date, String endTime)
    {
        try
        {
           String dateParts[] = date.split("-");
           int year = Integer.parseInt(dateParts[0]);
           int month = Integer.parseInt(dateParts[1]);
           int day = Integer.parseInt(dateParts[2]);

           String timeParts[] = endTime.split(":");
           int hour = Integer.parseInt(timeParts[0]);
           int minute = Integer.parseInt(timeParts[1]);

           java.util.Calendar calendar = java.util.Calendar.getInstance();
           calendar.set(year, month - 1, day, hour, minute, 0);
           calendar.set(java.util.Calendar.MILLISECOND, 0);
           return calendar.getTimeInMillis() < System.currentTimeMillis();

        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void AddSlot()
    {
        String date = dateInputText.getText().toString();
        String start = StartTimeText.getText().toString();
        String end = EndTimeText.getText().toString();

        if (date.isEmpty() || start.isEmpty() || end.isEmpty())
        {
            Toast.makeText(TutorAvailabilityActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidTime(start) || !isValidTime(end))
        {
            Toast.makeText(TutorAvailabilityActivity.this, "Times must be in 30 minute increments (e.g. 10:00, 10:30, 11:00)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!EndtimeafterStarttime(start, end))
         {
             Toast.makeText(TutorAvailabilityActivity.this, "End time must be after start time", Toast.LENGTH_SHORT).show();
             return;
         }
        if (IsPastDate (date, start))
        {
            Toast.makeText(TutorAvailabilityActivity.this, "Date cannot be in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        TutorAvailabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String currentDate = dataSnapshot.child("date").getValue(String.class);
                    String currentstart = dataSnapshot.child("start").getValue(String.class);
                    String currentEnd = dataSnapshot.child("end").getValue(String.class);

                    if (currentDate == null || currentstart == null || currentEnd == null)
                        continue;

                    if (currentDate.equals(date)) {
                        if (timeOverlap(start, end, currentstart, currentEnd)) {
                            Toast.makeText(TutorAvailabilityActivity.this, "Time slot already exists or overlaps with another slot", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }


                String SlotID = UUID.randomUUID().toString();
                Map<String, Object> slotData = new HashMap<>();
                slotData.put("date", date);
                slotData.put("start", start);
                slotData.put("end", end);
                slotData.put("Booked", false);
                slotData.put("course", ChosenCourse);

                TutorAvailabilityRef.child(SlotID).setValue(slotData).addOnSuccessListener(aVoid ->
                {
                    Toast.makeText(TutorAvailabilityActivity.this, "Slot Added", Toast.LENGTH_SHORT).show();
                    dateInputText.setText("");
                    StartTimeText.setText("");
                    EndTimeText.setText("");
                }).addOnFailureListener(e -> Toast.makeText(TutorAvailabilityActivity.this, "Issue Adding Slot Method: AddSlot Error ", Toast.LENGTH_SHORT).show());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TutorAvailabilityActivity.this, "Error checking slots", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean IsPastDate(String date, String start)
    {
        try
        {

            String dateParts[] = date.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);

            String timeParts[] = start.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month - 1, day, hour, minute, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis() < System.currentTimeMillis();

        }
        catch (Exception e)
        {
            return false;
        }
    }


    private boolean timeOverlap(String start, String end, String currentstart, String currentEnd)
            {
                try
                    {
                        int existingStart = Integer.parseInt(currentstart.replace(":", ""));
                        int existingEnd = Integer.parseInt(currentEnd.replace(":", ""));
                        int newStart = Integer.parseInt(start.replace(":", ""));
                        int newEnd = Integer.parseInt(end.replace(":", ""));
                        return newStart < existingEnd && newEnd > existingStart;

                    }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            private boolean EndtimeafterStarttime(String start, String end)
            {
                try {


                    String[] startParts = start.split(":");
                    String[] endParts = end.split(":");
                    int startHour = Integer.parseInt(startParts[0]);
                    int startMinute = Integer.parseInt(startParts[1]);
                    int endHour = Integer.parseInt(endParts[0]);
                    int endMinute = Integer.parseInt(endParts[1]);
                    return startHour < endHour || (startHour == endHour && startMinute < endMinute);
                }
                catch (Exception e)
                {
                    return false;
                }


            }

    private boolean isValidTime(String time)
    {
        if (!time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$" ))
        {
        return false;
    }
        try
        {
            {
                String[] Timeparts = time.split(":");
                int minutes =  Integer.parseInt(Timeparts[1]);
                return minutes % 30 == 0;
            }
        } catch (Exception e) {
            throw new NumberFormatException();
        }
    }

    private void loadTutorInfo(DatabaseReference tutorInfoReference)
    {
        tutorInfoReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                TutorFirstname = snapshot.child("firstName").getValue(String.class);
                TutorLastname = snapshot.child("lastName").getValue(String.class);

                String degree = snapshot.child("highestDegree").getValue(String.class);
                highestDegree.setText(degree != null ? degree : "No Degree");


                courseList.clear();
                if (snapshot.child("coursesOffered").exists())
                {
                    for (DataSnapshot courseSnapshot : snapshot.child("coursesOffered").getChildren())
                    {
                        String course = courseSnapshot.getValue(String.class);
                        if (course != null) courseList.add(course);
                    }
                }

                if (!courseList.isEmpty()) {
                    ChosenCourse = courseList.get(0);
                }
                if (courseList.isEmpty()) {
                    courseList.add("No courses offered");
                }
                courseAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(TutorAvailabilityActivity.this, "Error Loading Tutor Info : Issue from: loadTutorInfo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSlot(String key)
    {
        TutorAvailabilityRef.child(key).removeValue().addOnSuccessListener(aVoid ->
        {
            Toast.makeText(TutorAvailabilityActivity.this, "Slot Deleted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(TutorAvailabilityActivity.this, "Issue Deleting Slot Mehtod: deleteSlot Error ", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    public void setDate(int year, int month, int dayOfMonth) {
        dateInputText.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
    }

    @SuppressLint("DefaultLocale")
    public void setStartTime(int hourOfDay, int minute) {
        StartTimeText.setText(String.format("%02d:%02d", hourOfDay, minute));
    }

    @SuppressLint("DefaultLocale")
    public void setEndTime(int hourOfDay, int minute) {
        EndTimeText.setText(String.format("%02d:%02d", hourOfDay, minute));
    }
}






