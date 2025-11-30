package csi2105.group28.otams;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * Activity for tutors to view their availability slots in a weekly calendar format
 */
public class TutorWeeklyScheduleActivity extends AppCompatActivity {

    // UI components
    private TextView weekRangeText;
    private Button previousWeekButton, nextWeekButton, backButton;
    private LinearLayout daysContainer;

    // Data
    private DatabaseReference availabilityRef;
    private String tutorUsername;
    private Calendar currentWeekStart;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM d", Locale.US);
    private SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.US);

    // Store slots by date
    private Map<String, List<SlotInfo>> slotsByDate = new HashMap<>();

    // Slot info class
    private static class SlotInfo {
        String time;
        String course;
        String status; // "available", "pending", "booked"
        String studentName;

        SlotInfo(String time, String course, String status, String studentName) {
            this.time = time;
            this.course = course;
            this.status = status;
            this.studentName = studentName;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_weekly_schedule);

        // Get tutor info
        Tutor tutor = (Tutor) getIntent().getSerializableExtra("info");
        if (tutor != null) {
            tutorUsername = removePeriods(tutor.getUsername());
        } else {
            Toast.makeText(this, "Tutor object is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase reference
        availabilityRef = FirebaseDatabase.getInstance()
            .getReference("otams/Availability")
            .child(tutorUsername);

        // Initialize views
        weekRangeText = findViewById(R.id.weekRangeText);
        previousWeekButton = findViewById(R.id.previousWeekButton);
        nextWeekButton = findViewById(R.id.nextWeekButton);
        backButton = findViewById(R.id.backToAvailabilityButton);
        daysContainer = findViewById(R.id.daysContainer);

        // Initialize current week to today
        currentWeekStart = Calendar.getInstance();
        // Get the current day of week
        int currentDay = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        // Calculate days to subtract to get to Monday (Monday = 2, Sunday = 1)
        int daysFromMonday = (currentDay == Calendar.SUNDAY) ? 6 : currentDay - Calendar.MONDAY;
        currentWeekStart.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);

        // Set up button listeners
        previousWeekButton.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            loadWeeklySchedule();
        });

        nextWeekButton.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            loadWeeklySchedule();
        });

        backButton.setOnClickListener(v -> finish());

        // Load initial schedule
        loadWeeklySchedule();
    }

    /**
     * Removes periods from username for Firebase compatibility
     */
    private String removePeriods(String username) {
        if (username == null) return "";
        return username.replace(".", "");
    }

    /**
     * Loads the tutor's slots for the current week
     */
    private void loadWeeklySchedule() {
        // Update week range display
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);
        String weekRange = displayDateFormat.format(currentWeekStart.getTime()) +
            " - " + displayDateFormat.format(weekEnd.getTime()) + ", " +
            currentWeekStart.get(Calendar.YEAR);
        weekRangeText.setText(weekRange);

        // Clear previous data
        slotsByDate.clear();
        daysContainer.removeAllViews();

        // Load slots from Firebase
        availabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Process all slots
                for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                    String date = slotSnapshot.child("date").getValue(String.class);
                    String start = slotSnapshot.child("start").getValue(String.class);
                    String end = slotSnapshot.child("end").getValue(String.class);
                    String course = slotSnapshot.child("course").getValue(String.class);
                    Boolean booked = slotSnapshot.child("Booked").getValue(Boolean.class);
                    Boolean autoApprove = slotSnapshot.child("autoApprove").getValue(Boolean.class);

                    Map<String, Object> studentInfo = (Map<String, Object>)
                        slotSnapshot.child("studentInfo").getValue();

                    // Determine status
                    String status;
                    String studentName = "";
                    if (Boolean.TRUE.equals(booked)) {
                        status = "booked";
                        if (studentInfo != null) {
                            studentName = (String) studentInfo.get("name");
                        }
                    } else if (studentInfo != null && !Boolean.TRUE.equals(autoApprove)) {
                        String email = (String) studentInfo.get("email");
                        if (email != null && !email.isEmpty()) {
                            status = "pending";
                            studentName = (String) studentInfo.get("name");
                        } else {
                            status = "available";
                        }
                    } else {
                        status = "available";
                    }

                    // Check if date is in current week
                    if (date != null && isDateInCurrentWeek(date)) {
                        String timeRange = start + " - " + end;
                        SlotInfo slot = new SlotInfo(timeRange, course, status, studentName);

                        // Normalize date format (add leading zeros if needed)
                        String normalizedDate = normalizeDateFormat(date);

                        if (!slotsByDate.containsKey(normalizedDate)) {
                            slotsByDate.put(normalizedDate, new ArrayList<>());
                        }
                        slotsByDate.get(normalizedDate).add(slot);
                    }
                }

                // Display the weekly schedule
                displayWeeklySchedule();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TutorWeeklyScheduleActivity.this,
                    "Failed to load schedule: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Normalizes date format to yyyy-MM-dd (adds leading zeros if needed)
     */
    private String normalizeDateFormat(String dateStr) {
        try {
            // Parse the date string (handles both "2025-12-1" and "2025-12-01")
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                // Format with leading zeros
                return String.format(Locale.US, "%d-%02d-%02d", year, month, day);
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Checks if a date string is within the current week
     */
    private boolean isDateInCurrentWeek(String dateStr) {
        try {
            // Normalize the date format first
            String normalizedDate = normalizeDateFormat(dateStr);

            Calendar slotDate = Calendar.getInstance();
            slotDate.setTime(dateFormat.parse(normalizedDate));
            slotDate.set(Calendar.HOUR_OF_DAY, 0);
            slotDate.set(Calendar.MINUTE, 0);
            slotDate.set(Calendar.SECOND, 0);
            slotDate.set(Calendar.MILLISECOND, 0);

            Calendar weekEnd = (Calendar) currentWeekStart.clone();
            weekEnd.add(Calendar.DAY_OF_YEAR, 7);

            long slotTime = slotDate.getTimeInMillis();
            long weekStartTime = currentWeekStart.getTimeInMillis();
            long weekEndTime = weekEnd.getTimeInMillis();

            return slotTime >= weekStartTime && slotTime < weekEndTime;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Displays the weekly schedule organized by day
     */
    private void displayWeeklySchedule() {
        Calendar day = (Calendar) currentWeekStart.clone();

        // Iterate through each day of the week
        for (int i = 0; i < 7; i++) {
            String dateStr = dateFormat.format(day.getTime());
            String dayName = dayNameFormat.format(day.getTime());
            String displayDate = displayDateFormat.format(day.getTime());

            // Create day section
            LinearLayout daySection = createDaySection(dayName, displayDate, dateStr);
            daysContainer.addView(daySection);

            day.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * Creates a section for one day showing all slots
     */
    private LinearLayout createDaySection(String dayName, String displayDate, String dateStr) {
        LinearLayout daySection = new LinearLayout(this);
        daySection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        daySection.setLayoutParams(params);
        daySection.setBackgroundColor(Color.parseColor("#F5F5F5"));
        daySection.setPadding(12, 12, 12, 12);

        // Day header
        TextView dayHeader = new TextView(this);
        dayHeader.setText(dayName + ", " + displayDate);
        dayHeader.setTextSize(18);
        dayHeader.setTextColor(Color.parseColor("#000000"));
        dayHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        dayHeader.setPadding(0, 0, 0, 8);
        daySection.addView(dayHeader);

        // Get slots for this day
        List<SlotInfo> slots = slotsByDate.get(dateStr);

        if (slots == null || slots.isEmpty()) {
            // No slots for this day
            TextView noSlots = new TextView(this);
            noSlots.setText("No slots scheduled");
            noSlots.setTextSize(14);
            noSlots.setTextColor(Color.parseColor("#666666"));
            noSlots.setGravity(Gravity.CENTER);
            noSlots.setPadding(0, 16, 0, 16);
            daySection.addView(noSlots);
        } else {
            // Sort slots by time
            Collections.sort(slots, (s1, s2) -> s1.time.compareTo(s2.time));

            // Display each slot
            for (SlotInfo slot : slots) {
                LinearLayout slotView = createSlotView(slot);
                daySection.addView(slotView);
            }
        }

        return daySection;
    }

    /**
     * Creates a view for a single slot with color coding
     */
    private LinearLayout createSlotView(SlotInfo slot) {
        LinearLayout slotLayout = new LinearLayout(this);
        slotLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 4, 0, 4);
        slotLayout.setLayoutParams(params);
        slotLayout.setPadding(12, 8, 12, 8);

        // Set background color based on status
        switch (slot.status) {
            case "available":
                slotLayout.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
                break;
            case "pending":
                slotLayout.setBackgroundColor(Color.parseColor("#FFF9C4")); // Light yellow
                break;
            case "booked":
                slotLayout.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue
                break;
        }

        // Status indicator and time
        TextView timeText = new TextView(this);
        String statusIcon = slot.status.equals("available") ? "🟢" :
                           slot.status.equals("pending") ? "🟡" : "🔵";
        timeText.setText(statusIcon + " " + slot.time + " | " + slot.course);
        timeText.setTextSize(14);
        timeText.setTextColor(Color.parseColor("#000000"));
        timeText.setTypeface(null, android.graphics.Typeface.BOLD);
        slotLayout.addView(timeText);

        // Student info if booked or pending
        if (!slot.status.equals("available") && slot.studentName != null &&
            !slot.studentName.isEmpty()) {
            TextView studentText = new TextView(this);
            studentText.setText("Student: " + slot.studentName);
            studentText.setTextSize(12);
            studentText.setTextColor(Color.parseColor("#666666"));
            studentText.setPadding(0, 4, 0, 0);
            slotLayout.addView(studentText);
        }

        return slotLayout;
    }
}
