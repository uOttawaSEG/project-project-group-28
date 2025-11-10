package csi2105.group28.otams;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.*;

public class TutorAvailabilityActivity extends AppCompatActivity {

    private Button dateInput, startTimeInput, endTimeInput, addButton;
    private TextView dateInputText, startTimeText, endTimeText, highestDegree;
    private Spinner courseSpinner;
    private Switch autoApproveSwitch;
    private ListView futureList, pendingList, pastList;
    private ArrayAdapter<String> courseAdapter, futureAdapter, pendingAdapter, pastAdapter;
    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayList<String> futureSlots = new ArrayList<>();
    private ArrayList<String> pendingSlots = new ArrayList<>();
    private ArrayList<String> pastSlots = new ArrayList<>();
    private ArrayList<String> slotKeys = new ArrayList<>();
    private DatabaseReference availabilityRef, tutorInfoRef, sessionRequestsRef;

    private String tutorUsername, tutorFirstName, tutorLastName, chosenCourse;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_availability);

        Tutor tutor = (Tutor) getIntent().getSerializableExtra("info");
        if (tutor != null) {
            tutorUsername = removePeriods(tutor.getUsername());
        } else {
            Toast.makeText(this, "Tutor object is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        availabilityRef = FirebaseDatabase.getInstance().getReference("otams/Availability").child(tutorUsername);
        tutorInfoRef = FirebaseDatabase.getInstance().getReference("otams/Users/Tutor")
                .child(tutorUsername.substring(0,1))
                .child(tutorUsername.substring(1,2))
                .child(tutorUsername)
                .child("info");

        dateInput = findViewById(R.id.pickDate);
        dateInputText = findViewById(R.id.pickDateText);
        startTimeInput = findViewById(R.id.StartTimeInput);
        startTimeText = findViewById(R.id.StartTimeText);
        endTimeInput = findViewById(R.id.EndTimeInput);
        endTimeText = findViewById(R.id.EndTimeText);
        addButton = findViewById(R.id.AddButton);
        courseSpinner = findViewById(R.id.courseSpinner);
        autoApproveSwitch = findViewById(R.id.autoApproveSwitch);
        highestDegree = findViewById(R.id.highestDegree);

        futureList = findViewById(R.id.futureList);
        pendingList = findViewById(R.id.pendingList);
        pastList = findViewById(R.id.pastList);

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);

        futureAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, futureSlots);
        pendingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pendingSlots);
        pastAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pastSlots);

        futureList.setAdapter(futureAdapter);
        pendingList.setAdapter(pendingAdapter);
        pastList.setAdapter(pastAdapter);

        loadTutorInfo();

        // date picker
        dateInput.setOnClickListener(v -> {
            DatePicker picker = new DatePicker();
            picker.show(getSupportFragmentManager(), "datePicker");
        });

        // start time picker
        startTimeInput.setOnClickListener(v -> {
            TimePicker picker = new TimePicker();
            picker.isStartTimePicker = true;
            picker.show(getSupportFragmentManager(), "startTimePicker");
        });

        // end time picker
        endTimeInput.setOnClickListener(v -> {
            TimePicker picker = new TimePicker();
            picker.isEndTimePicker = true;
            picker.show(getSupportFragmentManager(), "endTimePicker");
        });

        addButton.setOnClickListener(v -> addSlot());

        // load slots
        loadSlots();

        // be able to click on the upcoming sessions
        futureList.setOnItemClickListener((parent, view, position, id) -> {
            String key = slotKeys.get(position);
            showUpcomingSessionDialog(key);
        });

        // click to delete available slot
        futureList.setOnItemLongClickListener((parent, view, position, id) -> {
            String key = slotKeys.get(position);
            deleteSlot(key);
            return true;
        });

        // ability to tap on pending requests
        pendingList.setOnItemClickListener((parent, view, position, id) -> {
            String key = slotKeys.get(position);
            showPendingSessionDialog(key);
        });
    }

    private String removePeriods(String username) {
        if (username == null) return "";
        return username.replace(".", "");
    }

    private void loadTutorInfo() {
        tutorInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorFirstName = snapshot.child("firstName").getValue(String.class);
                tutorLastName = snapshot.child("lastName").getValue(String.class);

                String degree = snapshot.child("highestDegree").getValue(String.class);
                highestDegree.setText(degree != null ? degree : "No Degree");

                courseList.clear();
                if (snapshot.child("coursesOffered").exists()) {
                    for (DataSnapshot courseSnap : snapshot.child("coursesOffered").getChildren()) {
                        String course = courseSnap.getValue(String.class);
                        if (course != null) courseList.add(course);
                    }
                }
                if (!courseList.isEmpty()) chosenCourse = courseList.get(0);
                if (courseList.isEmpty()) courseList.add("No courses offered");

                courseAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addSlot() {
        String date = dateInputText.getText().toString();
        String start = startTimeText.getText().toString();
        String end = endTimeText.getText().toString();
        chosenCourse = (String) courseSpinner.getSelectedItem();
        boolean autoApprove = autoApproveSwitch.isChecked();

        if (date.isEmpty() || start.isEmpty() || end.isEmpty() || chosenCourse == null) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTime(start) || !isValidTime(end) || !endAfterStart(start, end) || isPastDate(date, start)) {
            Toast.makeText(this, "Invalid time or past date", Toast.LENGTH_SHORT).show();
            return;
        }

        // check if time overlap
        availabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String existingDate = snap.child("date").getValue(String.class);
                    String existingStart = snap.child("start").getValue(String.class);
                    String existingEnd = snap.child("end").getValue(String.class);
                    if (existingDate != null && existingStart != null && existingEnd != null &&
                            existingDate.equals(date) && timeOverlap(start, end, existingStart, existingEnd)) {
                        Toast.makeText(TutorAvailabilityActivity.this, "Slot overlaps existing slot", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                String key = availabilityRef.push().getKey();
                Map<String, Object> slot = new HashMap<>();
                slot.put("date", date);
                slot.put("start", start);
                slot.put("end", end);
                slot.put("course", chosenCourse);
                slot.put("Booked", false);
                slot.put("autoApprove", autoApprove);

                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("name", "");
                studentInfo.put("email", "");
                studentInfo.put("phone", "");
                studentInfo.put("studies", "");
                slot.put("studentInfo", studentInfo);

                if (key != null) availabilityRef.child(key).setValue(slot);
                dateInputText.setText(""); startTimeText.setText(""); endTimeText.setText("");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSlots() {
        availabilityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                futureSlots.clear(); pendingSlots.clear(); pastSlots.clear(); slotKeys.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String key = snap.getKey();
                    String date = snap.child("date").getValue(String.class);
                    String start = snap.child("start").getValue(String.class);
                    String end = snap.child("end").getValue(String.class);
                    boolean booked = Boolean.TRUE.equals(snap.child("Booked").getValue(Boolean.class));
                    boolean autoApprove = Boolean.TRUE.equals(snap.child("autoApprove").getValue(Boolean.class));
                    Map<String, Object> studentInfo = (Map<String, Object>) snap.child("studentInfo").getValue();

                    String studentName = studentInfo != null ? (String) studentInfo.get("name") : "";
                    String studentEmail = studentInfo != null ? (String) studentInfo.get("email") : "";
                    String studentPhone = studentInfo != null ? (String) studentInfo.get("phone") : "";
                    String studentStudies = studentInfo != null ? (String) studentInfo.get("studies") : "";

                    String slotText = "Course: " + (snap.child("course").getValue(String.class) != null ? snap.child("course").getValue(String.class) : "N/A")
                            + " | Date: " + date + " | Start: " + start + " | End: " + end
                            + " | Status: " + (booked ? "Booked" : "Available");

                    if (booked) {
                        futureSlots.add(slotText);
                    } else if (!booked && !autoApprove) {
                        pendingSlots.add(slotText);
                    } else if (isPastDate(date, end)){
                        pastSlots.add(slotText);
                    }

                    slotKeys.add(key);
                }
                futureAdapter.notifyDataSetChanged();
                pendingAdapter.notifyDataSetChanged();
                pastAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showPendingSessionDialog(String key) {
        availabilityRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Map<String, Object> studentInfo = (Map<String, Object>) snap.child("studentInfo").getValue();
                String studentName = studentInfo != null ? (String) studentInfo.get("name") : "N/A";
                String studentEmail = studentInfo != null ? (String) studentInfo.get("email") : "N/A";
                String studentPhone = studentInfo != null ? (String) studentInfo.get("phone") : "N/A";
                String studentStudies = studentInfo != null ? (String) studentInfo.get("studies") : "N/A";

                AlertDialog.Builder builder = new AlertDialog.Builder(TutorAvailabilityActivity.this);
                builder.setTitle("Pending Request")
                        .setMessage("Student: " + studentName + "\nEmail: " + studentEmail + "\nPhone: " + studentPhone + "\nStudies: " + studentStudies)
                        .setPositiveButton("Approve", (dialog, which) -> {
                            availabilityRef.child(key).child("Booked").setValue(true);
                            Toast.makeText(TutorAvailabilityActivity.this, "Approved", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Reject", (dialog, which) -> {
                            availabilityRef.child(key).removeValue();
                            Toast.makeText(TutorAvailabilityActivity.this, "Rejected", Toast.LENGTH_SHORT).show();
                        }).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showUpcomingSessionDialog(String key) {
        availabilityRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Map<String, Object> studentInfo = (Map<String, Object>) snap.child("studentInfo").getValue();
                String studentName = studentInfo != null ? (String) studentInfo.get("name") : "N/A";
                String studentEmail = studentInfo != null ? (String) studentInfo.get("email") : "N/A";
                String studentPhone = studentInfo != null ? (String) studentInfo.get("phone") : "N/A";
                String studentStudies = studentInfo != null ? (String) studentInfo.get("studies") : "N/A";

                AlertDialog.Builder builder = new AlertDialog.Builder(TutorAvailabilityActivity.this);
                builder.setTitle("Upcoming Session")
                        .setMessage("Student: " + studentName + "\nEmail: " + studentEmail + "\nPhone: " + studentPhone + "\nStudies: " + studentStudies)
                        .setPositiveButton("Cancel Session", (dialog, which) -> {
                            availabilityRef.child(key).child("Booked").setValue(false);
                            availabilityRef.child(key).child("studentInfo").setValue(new HashMap<>());
                            Toast.makeText(TutorAvailabilityActivity.this, "Session Cancelled", Toast.LENGTH_SHORT).show();
                        }).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void deleteSlot(String key) {
        availabilityRef.child(key).removeValue();
        Toast.makeText(this, "Slot deleted", Toast.LENGTH_SHORT).show();
    }

    // Time helpers
    private boolean isValidTime(String time) {
        if (!time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) return false;
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[1]);
        return minutes % 30 == 0;
    }

    private boolean endAfterStart(String start, String end) {
        String[] s = start.split(":"); String[] e = end.split(":");
        int startH = Integer.parseInt(s[0]), startM = Integer.parseInt(s[1]);
        int endH = Integer.parseInt(e[0]), endM = Integer.parseInt(e[1]);
        return endH > startH || (endH == startH && endM > startM);
    }

    private boolean isPastDate(String date, String time) {
        try {
            String[] d = date.split("-");
            int y = Integer.parseInt(d[0]), m = Integer.parseInt(d[1]) - 1, day = Integer.parseInt(d[2]);
            String[] t = time.split(":"); int h = Integer.parseInt(t[0]), min = Integer.parseInt(t[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(y, m, day, h, min, 0);
            return cal.getTimeInMillis() < System.currentTimeMillis();
        } catch (Exception e) { return false; }
    }

    private boolean timeOverlap(String s1, String e1, String s2, String e2) {
        int start1 = Integer.parseInt(s1.replace(":", ""));
        int end1 = Integer.parseInt(e1.replace(":", ""));
        int start2 = Integer.parseInt(s2.replace(":", ""));
        int end2 = Integer.parseInt(e2.replace(":", ""));
        return start1 < end2 && end1 > start2;
    }

    // methods that are called by DatePicker and TimePicker fragments
    public void setDate(int y, int m, int d) { dateInputText.setText(y + "-" + (m + 1) + "-" + d); }
    public void setStartTime(int h, int m) { startTimeText.setText(String.format("%02d:%02d", h, m)); }
    public void setEndTime(int h, int m) { endTimeText.setText(String.format("%02d:%02d", h, m)); }
}
