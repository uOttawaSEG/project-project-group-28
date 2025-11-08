package csi2105.group28.otams;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.*;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorAvailabilityActivity extends AppCompatActivity
{
    private EditText dateInput;
    private EditText StartTimeInput;
    private EditText EndTimeInput;

    private Button AddButton;

    private Spinner courseSpinner;

    private TextView highestDegree;

    private ArrayAdapter<String> TutorSlotAdapter;
    private ArrayAdapter<String> courseAdapter;

    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayList<String> TutorSlotDisplay = new ArrayList<>();
    private ArrayList<String> slotKeys = new ArrayList<>();

    private ListView SlotList;


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


        dateInput = findViewById(R.id.dateInput);
        StartTimeInput = findViewById(R.id.StartTimeInput);
        EndTimeInput = findViewById(R.id.EndTimeInput);
        AddButton = findViewById(R.id.AddButton);
        SlotList = findViewById(R.id.SlotList);
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



        TutorSlotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TutorSlotDisplay);
        SlotList.setAdapter(TutorSlotAdapter);
        SlotLoader();

        AddButton.setOnClickListener(v -> AddSlot());

        SlotList.setOnItemLongClickListener((adapterView, view, pos, l) ->
        {
            deleteSlot(slotKeys.get(pos));
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
        TutorAvailabilityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TutorSlotDisplay.clear();
                slotKeys.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String start = dataSnapshot.child("start").getValue(String.class);
                    String end = dataSnapshot.child("end").getValue(String.class);
                    boolean Booked = dataSnapshot.child("Booked").getValue(boolean.class);
                    String slot = "Tutor: " + TutorFirstname + " " + TutorLastname + " |  Course: "+ ChosenCourse + " | Date: " + date + " | Start time: " + start + " | End time: " + end + " | Status: " + (Booked ? "Booked" : "Available");
                    TutorSlotDisplay.add(slot);
                    slotKeys.add(key);
                }
                TutorSlotAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(TutorAvailabilityActivity.this, "Error Loading slots", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void AddSlot()
    {
        String date = dateInput.getText().toString();
        String start = StartTimeInput.getText().toString();
        String end = EndTimeInput.getText().toString();

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

        String SlotID = UUID.randomUUID().toString();
        Map<String, Object> slotData = new HashMap<>();
        slotData.put("date", date);
        slotData.put("start", start);
        slotData.put("end", end);
        slotData.put("Booked", false);

        TutorAvailabilityRef.child(SlotID).setValue(slotData).addOnSuccessListener(aVoid ->
        {
            Toast.makeText(TutorAvailabilityActivity.this, "Slot Added", Toast.LENGTH_SHORT).show();
            dateInput.setText("");
            StartTimeInput.setText("");
            EndTimeInput.setText("");
        }).addOnFailureListener(e -> Toast.makeText(TutorAvailabilityActivity.this, "Issue Adding Slot Mehtod: AddSlot Error ", Toast.LENGTH_SHORT).show());

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



}