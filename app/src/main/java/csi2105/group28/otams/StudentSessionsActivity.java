package csi2105.group28.otams;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/*
 * 
 * Activity for students to view their booked tutoring sessions
 * Allows rating of completed sessions
 */
public class StudentSessionsActivity extends AppCompatActivity {

    private ListView upcomingSessionsList, pastSessionsList;
    private ArrayAdapter<String> upcomingAdapter, pastAdapter;
    private ArrayList<String> upcomingSessions = new ArrayList<>();
    private ArrayList<String> pastSessions = new ArrayList<>();
    private ArrayList<SessionInfo> upcomingSessionsInfo = new ArrayList<>();
    private ArrayList<SessionInfo> pastSessionsInfo = new ArrayList<>();

    private TextView studentNameHeader;
    private Button backButton;

    private Student student;
    private String studentUsername;
    private String studentEmail;

    // Inner class to store session information
    private static class SessionInfo {
        String tutorUsername;
        String tutorName;
        String sessionKey;
        String date;
        String start;
        String end;
        String course;
        boolean rated;

        SessionInfo(String tutorUsername, String tutorName, String sessionKey,
                   String date, String start, String end, String course, boolean rated) {
            this.tutorUsername = tutorUsername;
            this.tutorName = tutorName;
            this.sessionKey = sessionKey;
            this.date = date;
            this.start = start;
            this.end = end;
            this.course = course;
            this.rated = rated;
            this.booked = booked;
        }
    }
    private boolean canCancel24h(String date, String startTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date sessionDate = sdf.parse(date + " " + startTime);
            Date now = new Date();

            long diff = sessionDate.getTime() - now.getTime();
            long hours = diff / (1000 * 60 * 60);

            return hours >= 24;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sessions);

        // Get student info from intent
        student = (Student) getIntent().getSerializableExtra("info");
        if (student != null) {
            studentUsername = removePeriods(student.getUsername());
            studentEmail = student.getEmail();
        } else {
            Toast.makeText(this, "Student object is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        studentNameHeader = findViewById(R.id.studentNameHeader);
        upcomingSessionsList = findViewById(R.id.upcomingSessionsList);
        pastSessionsList = findViewById(R.id.pastSessionsList);
        backButton = findViewById(R.id.backButton);

        studentNameHeader.setText("Sessions for " + student.getFirstName() + " " + student.getLastName());

        // Initialize adapters
        upcomingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, upcomingSessions);
        pastAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pastSessions);

        upcomingSessionsList.setAdapter(upcomingAdapter);
        upcomingSessionsList.setOnItemClickListener((parent, view, position, id) -> {

            SessionInfo info = upcomingSessionsInfo.get(position);

            // 1. Only approved (booked == true)
            if (!info.booked) {
                Toast.makeText(this, "Only approved sessions can be canceled.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Must be > 24 hours before start time
            if (!canCancel24h(info.date, info.start)) {
                Toast.makeText(this, "Cannot cancel less than 24 hours before the session.", Toast.LENGTH_LONG).show();
                return;
            }

            // 3. Confirm cancel
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Session")
                    .setMessage("Do you really want to cancel this session?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // Update Firebase: set Booked = false
                        FirebaseDatabase.getInstance()
                                .getReference("otams")
                                .child("Availability")
                                .child(info.tutorUsername)
                                .child(info.sessionKey)
                                .child("Booked")
                                .setValue(false);

                        // Remove studentInfo field
                        FirebaseDatabase.getInstance()
                                .getReference("otams")
                                .child("Availability")
                                .child(info.tutorUsername)
                                .child(info.sessionKey)
                                .child("studentInfo")
                                .removeValue();

                        Toast.makeText(this, "Session canceled successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        pastSessionsList.setAdapter(pastAdapter);

        // Load sessions
        loadStudentSessions();

        // Set up past sessions click listener for rating
        pastSessionsList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < pastSessionsInfo.size()) {
                SessionInfo session = pastSessionsInfo.get(position);
                if (session.rated) {
                    Toast.makeText(this, "You have already rated this session", Toast.LENGTH_SHORT).show();
                } else {
                    showRatingDialog(session);
                }
            }
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * 
     * Removes periods from username for Firebase key compatibility
     */
    private String removePeriods(String username) {
        if (username == null) return "";
        return username.replace(".", "");
    }

    /**
     *
     * Loads all tutoring sessions for this student from Firebase
     */
    private void loadStudentSessions() {
        DatabaseReference availabilityRef = FirebaseDatabase.getInstance()
            .getReference("otams/Availability");

        availabilityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upcomingSessions.clear();
                pastSessions.clear();
                upcomingSessionsInfo.clear();
                pastSessionsInfo.clear();

                // Iterate through all tutors
                for (DataSnapshot tutorSnapshot : snapshot.getChildren()) {
                    String tutorUsername = tutorSnapshot.getKey();

                    // Iterate through all sessions for this tutor
                    for (DataSnapshot sessionSnapshot : tutorSnapshot.getChildren()) {
                        String sessionKey = sessionSnapshot.getKey();
                        Boolean booked = sessionSnapshot.child("Booked").getValue(Boolean.class);

                        // Only process booked sessions
                        if (Boolean.TRUE.equals(booked)) {
                            Map<String, Object> studentInfo = (Map<String, Object>)
                                sessionSnapshot.child("studentInfo").getValue();

                            if (studentInfo != null) {
                                String sessionStudentEmail = (String) studentInfo.get("email");

                                // Check if this session belongs to the current student
                                if (studentEmail != null && studentEmail.equals(sessionStudentEmail)) {
                                    String date = sessionSnapshot.child("date").getValue(String.class);
                                    String start = sessionSnapshot.child("start").getValue(String.class);
                                    String end = sessionSnapshot.child("end").getValue(String.class);
                                    String course = sessionSnapshot.child("course").getValue(String.class);
                                    Boolean rated = sessionSnapshot.child("rated").getValue(Boolean.class);

                                    // Get tutor name
                                    getTutorName(tutorUsername, tutorName -> {
                                        SessionInfo sessionInfo = new SessionInfo(
                                            tutorUsername,
                                            tutorName,
                                            sessionKey,
                                            date,
                                            start,
                                            end,
                                            course,
                                            Boolean.TRUE.equals(booked),
                                            Boolean.TRUE.equals(rated)
                                        );


                                        // Determine if session is past or upcoming (based on start time)
                                        if (isSessionInPast(date, start)) {
                                            pastSessionsInfo.add(sessionInfo);
                                            String ratingStatus = sessionInfo.rated ? " [RATED ★]" : " [Tap to rate]";
                                            pastSessions.add(formatSessionDisplay(sessionInfo) + ratingStatus);
                                            pastAdapter.notifyDataSetChanged();
                                        } else {
                                            upcomingSessionsInfo.add(sessionInfo);
                                            upcomingSessions.add(formatSessionDisplay(sessionInfo));
                                            upcomingAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentSessionsActivity.this,
                    "Failed to load sessions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *
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

    // Callback interface for async tutor name retrieval
    private interface TutorNameCallback {
        void onTutorNameReceived(String name);
    }

    /**
     *
     * Formats session info for display in ListView
     */
    private String formatSessionDisplay(SessionInfo session) {
        return "Tutor: " + session.tutorName + "\n" +
               "Course: " + session.course + "\n" +
               "Date: " + session.date + " | " + session.start + " - " + session.end;
    }

    /**
     * Checks if a session is in the past based on date and start time
     */
    private boolean isSessionInPast(String date, String startTime) {
        try {
            String[] dateParts = date.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(dateParts[2]);

            String[] timeParts = startTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar sessionCal = Calendar.getInstance();
            sessionCal.set(year, month, day, hour, minute, 0);

            return sessionCal.getTimeInMillis() < System.currentTimeMillis();
        } catch (Exception e) {
            return false; // If parsing fails, assume not past
        }
    }

    /**
     *
     * Shows the rating dialog for a completed session
     */
    private void showRatingDialog(SessionInfo session) {
        RateTutorDialog dialog = new RateTutorDialog(
            this,
            studentUsername,
            student.getFirstName() + " " + student.getLastName(),
            session.tutorUsername,
            session.tutorName,
            session.sessionKey,
            session.date,
            session.course
        );

        dialog.setOnRatingSubmittedListener(rating -> {
            // Refresh sessions list to show updated rating status
            loadStudentSessions();
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
