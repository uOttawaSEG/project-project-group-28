package csi2105.group28.otams;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
 * 
 * Dialog for students to rate tutors after completed sessions
 * on a 5-star rating and optional comment (max 150 words)
 */
public class RateTutorDialog extends Dialog {

    private String studentUsername;
    private String studentName;
    private String tutorUsername;
    private String tutorName;
    private String sessionKey;
    private String sessionDate;
    private String course;

    private RatingBar ratingBar;
    private EditText commentInput;
    private TextView currentRatingText;
    private TextView wordCountText;
    private TextView tutorInfoText;
    private TextView sessionInfoText;
    private Button submitButton;
    private Button cancelButton;

    private OnRatingSubmittedListener listener;

    // Interface for callback when the students rating is submitted
    public interface OnRatingSubmittedListener {
        void onRatingSubmitted(TutorRating rating);
    }

    /**
     * Constructor for the rating dialog
     * @param context application context
     * @param studentUsername username of the student
     * @param studentName name of the student
     * @param tutorUsername username of the tutor being rated
     * @param tutorName name of the tutor
     * @param sessionKey unique key for the session
     * @param sessionDate date of the session
     * @param course course that was tutored
     */
    public RateTutorDialog(@NonNull Context context, String studentUsername, String studentName,
                           String tutorUsername, String tutorName, String sessionKey,
                           String sessionDate, String course) {
        super(context);
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.tutorUsername = tutorUsername;
        this.tutorName = tutorName;
        this.sessionKey = sessionKey;
        this.sessionDate = sessionDate;
        this.course = course;
    }

    public void setOnRatingSubmittedListener(OnRatingSubmittedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rate_tutor);

        // this initializes views
        ratingBar = findViewById(R.id.tutorRatingBar);
        commentInput = findViewById(R.id.ratingCommentInput);
        currentRatingText = findViewById(R.id.currentRatingText);
        wordCountText = findViewById(R.id.wordCountText);
        tutorInfoText = findViewById(R.id.tutorInfoText);
        sessionInfoText = findViewById(R.id.sessionInfoText);
        submitButton = findViewById(R.id.submitRatingButton);
        cancelButton = findViewById(R.id.cancelRatingButton);

        // set tutor and session info
        tutorInfoText.setText("Tutor: " + tutorName);
        sessionInfoText.setText("Course: " + course + " | Date: " + sessionDate);

        // rating bar listener
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (rating == 0) {
                currentRatingText.setText("No rating selected");
                submitButton.setEnabled(false);
            } else {
                currentRatingText.setText(String.format("%.0f stars", rating));
                submitButton.setEnabled(true);
            }
        });

        // comment input listener for word count
        commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // submit button listener
        submitButton.setOnClickListener(v -> submitRating());

        // cancel button listener
        cancelButton.setOnClickListener(v -> dismiss());
    }

    /**
     *
     * Updates word count display and add 150-word limit
     */
    private void updateWordCount(String text) {
        String trimmed = text.trim();
        int wordCount = 0;

        if (!trimmed.isEmpty()) {
            String[] words = trimmed.split("\\s+");
            wordCount = words.length;

            // enforce 150-word limit
            if (wordCount > 150) {
                // truncate to 150 words
                StringBuilder truncated = new StringBuilder();
                for (int i = 0; i < 150; i++) {
                    truncated.append(words[i]);
                    if (i < 149) truncated.append(" ");
                }
                commentInput.setText(truncated.toString());
                commentInput.setSelection(truncated.length());
                wordCount = 150;
            }
        }

        wordCountText.setText(wordCount + " / 150 words");

        // change color if approaching/at limit (little bonus)
        if (wordCount >= 150) {
            wordCountText.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else if (wordCount >= 130) {
            wordCountText.setTextColor(getContext().getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            wordCountText.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
        }
    }

    /**
     * 
     * Submits the rating to Firebase database
     */
    private void submitRating() {
        float rating = ratingBar.getRating();

        if (rating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = commentInput.getText().toString().trim();

        // create rating object
        TutorRating tutorRating = new TutorRating(
            studentUsername,
            studentName,
            tutorUsername,
            tutorName,
            (int) rating,
            comment,
            sessionKey,
            sessionDate,
            course
        );

        // save to Firebase
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance()
            .getReference("otams/Ratings/Tutors")
            .child(tutorUsername);

        String ratingKey = ratingsRef.push().getKey();
        if (ratingKey != null) {
            ratingsRef.child(ratingKey).setValue(tutorRating)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Rating submitted successfully!", Toast.LENGTH_SHORT).show();

                    // mark session as rated in Firebase
                    markSessionAsRated();

                    // notify listener
                    if (listener != null) {
                        listener.onRatingSubmitted(tutorRating);
                    }

                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit rating: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    /**
     *
     * Marks the session as rated to prevent duplicate ratings
     */
    private void markSessionAsRated() {
        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
            .getReference("otams/Availability")
            .child(tutorUsername)
            .child(sessionKey);

        sessionRef.child("rated").setValue(true);
    }
}
