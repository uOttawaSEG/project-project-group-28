package csi2105.group28.otams;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.*;

public class StudentBookingActivity extends AppCompatActivity {
    // for the search FEATURE - UI components
    private TextView studentWelcomeHeader;
    private EditText courseSearch;
    private ListView availableList, requestedList, bookedList;
    private ArrayAdapter<String> availableAdapter, requestedAdapter, bookedAdapter;

    // for the search FEATURE  - Data lists
    private ArrayList<String> availableSessions = new ArrayList<>();
    private ArrayList<String> requestedSessions = new ArrayList<>();
    private ArrayList<String> bookedSessions = new ArrayList<>();

    // for the search FEATURE - the full data lists (for filtering)
    private ArrayList<SessionData> allAvailableSessions = new ArrayList<>();
    private ArrayList<SessionData> allRequestedSessions = new ArrayList<>();
    private ArrayList<SessionData> allBookedSessions = new ArrayList<>();

    // for the search FEATURE - Firebase references
    private DatabaseReference availabilityRef;

    // ADDED FOR SEARCH/FILTER FEATURE - Student info
    private Student student;
    private String studentEmail;
    private String studentUsername;

    // for the search FEATURE -  to store session data
    private static class SessionData {
        String tutorUsername;
        String tutorName;
        String sessionKey;
        String date;
        String start;
        String end;
        String course;
        boolean booked;
        boolean autoApprove;
        Map<String, Object> studentInfo;

        SessionData(String tutorUsername, String tutorName, String sessionKey,
                   String date, String start, String end, String course,
                   boolean booked, boolean autoApprove, Map<String, Object> studentInfo) {
            this.tutorUsername = tutorUsername;
            this.tutorName = tutorName;
            this.sessionKey = sessionKey;
            this.date = date;
            this.start = start;
            this.end = end;
            this.course = course;
            this.booked = booked;
            this.autoApprove = autoApprove;
            this.studentInfo = studentInfo;
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_booking);

        // for the search FEATURE - get student info from intent
        student = (Student) getIntent().getSerializableExtra("info");
        if (student != null) {
            studentEmail = student.getEmail();
            studentUsername = removePeriods(student.getUsername());
        } else {
            Toast.makeText(this, "Student object is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // for the search FEATURE - initialize Firebase reference
        availabilityRef = FirebaseDatabase.getInstance().getReference("otams/Availability");

        // for the search FEATURE - initialize the UI components
        studentWelcomeHeader = findViewById(R.id.studentWelcomeHeader);
        courseSearch = findViewById(R.id.courseSearch);
        availableList = findViewById(R.id.availableList);
        requestedList = findViewById(R.id.requestedList);
        bookedList = findViewById(R.id.bookedList);

        // set personalized welcome message
        studentWelcomeHeader.setText("Welcome, " + student.getFirstName() + "!");

        // for the search FEATURE - initialize adapters
        availableAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableSessions);
        requestedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requestedSessions);
        bookedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookedSessions);

        availableList.setAdapter(availableAdapter);
        requestedList.setAdapter(requestedAdapter);
        bookedList.setAdapter(bookedAdapter);

        // for the search FEATURE - lad all sessions
        loadAllSessions();

        // for the search FEATURE - set up course search filter
        courseSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSessionsByCourse(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // for the search FEATURE - handle  the clicking on available sessions to book
        availableList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < allAvailableSessions.size()) {
                SessionData session = allAvailableSessions.get(position);
                showBookingDialog(session);
            }
        });

        // for the search FEATURE - handle clicking on requested sessions to cancel
        requestedList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < allRequestedSessions.size()) {
                SessionData session = allRequestedSessions.get(position);
                showCancelRequestDialog(session);
            }
        });

        // for the search FEATURE - handle clicking on booked sessions to view details
        bookedList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < allBookedSessions.size()) {
                SessionData session = allBookedSessions.get(position);
                showSessionDetailsDialog(session);
            }
        });
    }

    /**
     * for the search FEATURE - removes periods from username for Firebase compatibility
     * @param username
     * @return
     */
    private String removePeriods(String username) {
        if (username == null) return "";
        return username.replace(".", "");
    }

    /**
     * for the search FEATURE -
     * Loads all sessions from all tutors and categorizes them
     */
    private void loadAllSessions() {
        availabilityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear all lists
                allAvailableSessions.clear();
                allRequestedSessions.clear();
                allBookedSessions.clear();

                // iterate through all tutors
                for (DataSnapshot tutorSnapshot : snapshot.getChildren()) {
                    String tutorUsername = tutorSnapshot.getKey();

                    // iterate through all sessions for this tutor
                    for (DataSnapshot sessionSnapshot : tutorSnapshot.getChildren()) {
                        processSession(tutorUsername, sessionSnapshot);
                    }
                }

                // update display
                filterSessionsByCourse(courseSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentBookingActivity.this,
                    "Failed to load sessions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * for the search FEATURE -
     * ability to process a single session and categorizes it
     */
    private void processSession(String tutorUsername, DataSnapshot sessionSnapshot) {
        String sessionKey = sessionSnapshot.getKey();
        String date = sessionSnapshot.child("date").getValue(String.class);
        String start = sessionSnapshot.child("start").getValue(String.class);
        String end = sessionSnapshot.child("end").getValue(String.class);
        String course = sessionSnapshot.child("course").getValue(String.class);
        Boolean booked = sessionSnapshot.child("Booked").getValue(Boolean.class);
        Boolean autoApprove = sessionSnapshot.child("autoApprove").getValue(Boolean.class);
        Map<String, Object> studentInfo = (Map<String, Object>) sessionSnapshot.child("studentInfo").getValue();

        // get tutor name and categorize session
        getTutorName(tutorUsername, tutorName -> {
            SessionData sessionData = new SessionData(
                tutorUsername,
                tutorName,
                sessionKey,
                date,
                start,
                end,
                course,
                Boolean.TRUE.equals(booked),
                Boolean.TRUE.equals(autoApprove),
                studentInfo
            );

            categorizeSession(sessionData);
        });
    }

    /**
     * for the search FEATURE -
     * Categorizes a session into Available, Requested, or Booked
     */
    private void categorizeSession(SessionData session) {
        String sessionStudentEmail = "";
        if (session.studentInfo != null) {
            sessionStudentEmail = (String) session.studentInfo.get("email");
        }

        // this checks if this session belongs to current student
        boolean isMySession = studentEmail != null && studentEmail.equals(sessionStudentEmail);

        if (session.booked && isMySession) {
            // if BOOKED: Session is now confirmed and belongs to this student
            allBookedSessions.add(session);
        } else if (!session.booked && isMySession && !session.autoApprove) {
            // if REQUESTED: student requested but tutor hasn't approved yet
            allRequestedSessions.add(session);
        } else if (!session.booked && (sessionStudentEmail == null || sessionStudentEmail.isEmpty())) {
            // if AVAILABLE: Session is not booked and has no student info
            allAvailableSessions.add(session);
        }
    }

    /**
     * for the search FEATURE -
     * Retrieves tutor name from Firebase
     */
    private void getTutorName(String tutorUsername, TutorNameCallback callback) {
        DatabaseReference tutorRef = FirebaseDatabase.getInstance()
            .getReference("otams/Users/Tutor")
            .child(tutorUsername.substring(0, 1))
            .child(tutorUsername.substring(1, 2))
            .child(tutorUsername)
            .child("info");

        tutorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String fullName = (firstName != null ? firstName : "Unknown") + " " +
                                (lastName != null ? lastName : "");
                callback.onTutorNameReceived(fullName.trim());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onTutorNameReceived("Unknown Tutor");
            }
        });
    }

    // for the search FEATURE - Callback interface
    private interface TutorNameCallback {
        void onTutorNameReceived(String name);
    }

    /**
     * for the search FEATURE -
     * Filters sessions by course code
     */
    private void filterSessionsByCourse(String searchText) {
        String filter = searchText.trim().toLowerCase();

        // Clear display lists
        availableSessions.clear();
        requestedSessions.clear();
        bookedSessions.clear();

        // Filter available sessions
        for (SessionData session : allAvailableSessions) {
            if (filter.isEmpty() || (session.course != null && session.course.toLowerCase().contains(filter))) {
                availableSessions.add(formatSessionDisplay(session));
            }
        }

        // Filter requested sessions
        for (SessionData session : allRequestedSessions) {
            if (filter.isEmpty() || (session.course != null && session.course.toLowerCase().contains(filter))) {
                requestedSessions.add(formatSessionDisplay(session) + " [PENDING APPROVAL]");
            }
        }

        // Filter booked sessions
        for (SessionData session : allBookedSessions) {
            if (filter.isEmpty() || (session.course != null && session.course.toLowerCase().contains(filter))) {
                bookedSessions.add(formatSessionDisplay(session) + " [CONFIRMED]");
            }
        }

        // Update adapters
        availableAdapter.notifyDataSetChanged();
        requestedAdapter.notifyDataSetChanged();
        bookedAdapter.notifyDataSetChanged();
    }

    /**
     * for the search FEATURE -
     * Formats session data for display
     */
    private String formatSessionDisplay(SessionData session) {
        return "Tutor: " + session.tutorName + "\n" +
               "Course: " + session.course + "\n" +
               "Date: " + session.date + " | " + session.start + " - " + session.end;
    }

    /**
     * for the search FEATURE -
     * Shows dialog to book an available session
     */
    private void showBookingDialog(SessionData session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Book Session")
            .setMessage("Do you want to book this session?\n\n" +
                "Tutor: " + session.tutorName + "\n" +
                "Course: " + session.course + "\n" +
                "Date: " + session.date + "\n" +
                "Time: " + session.start + " - " + session.end)
            .setPositiveButton("Book", (dialog, which) -> bookSession(session))
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * for the search FEATURE -
     * Books a session for the student
     */
    private void bookSession(SessionData session) {
        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
            .getReference("otams/Availability")
            .child(session.tutorUsername)
            .child(session.sessionKey);

        // Create student info map
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("name", student.getFirstName() + " " + student.getLastName());
        studentInfo.put("email", studentEmail);
        studentInfo.put("phone", student.getPhoneNum());
        studentInfo.put("studies", student.getProgramOfStudy());

        // Update session with student info
        sessionRef.child("studentInfo").setValue(studentInfo);

        // If autoApprove is true, mark as booked immediately (on tutor side)
        if (session.autoApprove) {
            sessionRef.child("Booked").setValue(true);
            Toast.makeText(this, "Session booked successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Booking request sent! Waiting for tutor approval.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * for the search FEATURE -
     * Shows dialog to cancel a requested session
     */
    private void showCancelRequestDialog(SessionData session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Request")
            .setMessage("Do you want to cancel this booking request?\n\n" +
                "Tutor: " + session.tutorName + "\n" +
                "Course: " + session.course + "\n" +
                "Date: " + session.date + "\n" +
                "Time: " + session.start + " - " + session.end)
            .setPositiveButton("Cancel Request", (dialog, which) -> cancelRequest(session))
            .setNegativeButton("Keep Request", null)
            .show();
    }

    /**
     * for the search FEATURE -
     * Cancels a booking request
     */
    private void cancelRequest(SessionData session) {
        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
            .getReference("otams/Availability")
            .child(session.tutorUsername)
            .child(session.sessionKey);

        // Clear student info
        Map<String, Object> emptyStudentInfo = new HashMap<>();
        emptyStudentInfo.put("name", "");
        emptyStudentInfo.put("email", "");
        emptyStudentInfo.put("phone", "");
        emptyStudentInfo.put("studies", "");

        sessionRef.child("studentInfo").setValue(emptyStudentInfo);
        Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show();
    }

    /**
     * for the search FEATURE -
     * Shows details for a booked session
     */
    private void showSessionDetailsDialog(SessionData session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Session Details")
            .setMessage("Tutor: " + session.tutorName + "\n" +
                "Course: " + session.course + "\n" +
                "Date: " + session.date + "\n" +
                "Time: " + session.start + " - " + session.end + "\n\n" +
                "This session is confirmed!")
            .setPositiveButton("OK", null)
            .show();
    }
}
