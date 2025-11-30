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
public class StudentBookingActivity extends AppCompatActivity{
    private TextView courseSearch;
    private ListView availibleList, requestedList, bookedList;
    private DatabaseReference availabilityRef, studentRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_availability);

        availabilityRef = FirebaseDatabase.getInstance().getReference("otams/Availability");
        availabilityRef = FirebaseDatabase.getInstance().getReference("otams/Users/Student");


        courseSearch = findViewById(R.id.courseSearch);
        availibleList = findViewById(R.id.availableList);
        requestedList = findViewById(R.id.requestedList);
        bookedList = findViewById(R.id.bookedList);
    }

    private void loadStudent() {
        //TODO
    }


}
