package csi2105.group28.otams;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdminRequestsActivity extends AppCompatActivity {

    private LinearLayout requestsContainer;
    private DatabaseReference adminRef;
    private String currentNode = "Requests";
    DatabaseReference otamsroot;


    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        requestsContainer = findViewById(R.id.requestsContainer);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        db = new Database();
        adminRef = FirebaseDatabase.getInstance()
                .getReference("otams/Administrator/admin@mail@com");
        loadRequests();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadRequests();
                } else if (tab.getPosition() == 1) {
                    loadRejects();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        otamsroot = FirebaseDatabase.getInstance().getReference("otams");
    }


    protected void onStart() {
        super.onStart();
        db.listenToReq();
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createUserView(User user, boolean canAccept, boolean canReject){

        LinearLayout returnLayout = new LinearLayout(this);
        returnLayout.setOrientation(LinearLayout.VERTICAL);
        returnLayout.setPadding(24, 24, 24, 24);
        returnLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        // Made a new layout for buttons so I could shift the buttons to the bottom using returnLayout.addView(buttonRow);
        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, 8, 0, 0);
        buttonRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        btnParams.setMargins(8, 0, 8, 0);


        // add an accept button if needed
        if (canAccept) {
            Button acceptButton = new Button(this);

            // set some formatting
            acceptButton.setText("Accept");
            acceptButton.setLayoutParams(btnParams);
            // make the button accept the request when clicked
            acceptButton.setOnClickListener(v -> approveRequest(user));

            buttonRow.addView(acceptButton);
        }

        // add a reject button if needed
        if (canReject) {
            Button rejectButton = new Button(this);

            // set some formatting
            rejectButton.setText("Reject");
            rejectButton.setLayoutParams(btnParams);
            // make the button accept the request when clicked
            rejectButton.setOnClickListener(v -> rejectRequest(user));

            buttonRow.addView(rejectButton);
        }


        TextView userType = new TextView(this);
        userType.setText(user.getUserType());
        returnLayout.addView(userType);

        TextView userData = new TextView(this);
        String userDataString = "";
        if (Objects.equals(user.getUserType(), "Tutor")) {
            Tutor tutorUser = (Tutor) user;
            userDataString = String.format("%s  %s\n%s  %s\n%s\n%s", tutorUser.getFirstName(), tutorUser.getLastName(), tutorUser.getEmail(), tutorUser.getPhoneNum(), tutorUser.getHighestDegree(), tutorUser.getCoursesOffered().toString());
        } else if (Objects.equals(user.getUserType(), "Student")) {
            Student studentUser = (Student) user;
            userDataString = String.format("%s  %s\n%s  %s\n%s", studentUser.getFirstName(), studentUser.getLastName(), studentUser.getEmail(), studentUser.getPhoneNum(), studentUser.getProgramOfStudy());
        }



        userData.setText(userDataString);
        userData.setTextSize(15);
        userData.setPadding(0, 0, 0, 16);
        returnLayout.addView(userData);


        returnLayout.addView(buttonRow);

        return returnLayout;


    }

    private void loadRequests() {
        requestsContainer.removeAllViews();
        for(User user : db.getRequests()){
            requestsContainer.addView(createUserView(user, true, true));
        }
    }

    private void loadRejects() {
        requestsContainer.removeAllViews();
        for(User user : db.getRejected()){
            requestsContainer.addView(createUserView(user, true, false));
        }
    }



    private void showApproveRejectDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Approve or Reject")
                .setMessage("Do you want to approve or reject " + user.getEmail() + "?")
                .setPositiveButton("Approve", (dialog, which) -> approveRequest(user))
                .setNegativeButton("Reject", (dialog, which) -> rejectRequest(user))
                .show();
    }

    private void approveRequest(User user) {
        String username = user.getUsername();
        String userT = user.getUserType();
        String status = user.getStatus();       //getting important values of the user
        String origin="";
        user.setStatus("approved");    // user approved

        DatabaseReference tref;
        tref = otamsroot.child("Users").child(userT).child(username.substring(0,1)).child(username.substring(1,2));// makes a path based on the first two letter
        tref.child(username).child("password").setValue(user.getPassword()); // sets pasword of user
        tref.child(username).child("info").setValue(user);//if new user, make user with key username, password/class stored in key password/info
        // finding where the user is currently stored
        if(status.equals("rejected")){
            origin="Rejected";
        }else if(status.equals("pending")){
            origin="Requests";
        }

        //removing user from requests/rejected lists
        otamsroot.child("Administrator").child("admin@mail@com").child(origin).child(userT).child(username).removeValue();
        Toast.makeText(this, user.getEmail() + " approved", Toast.LENGTH_SHORT).show();
        loadRequests();
    }

    private void rejectRequest(User user) {
        String username = user.getUsername();
        String userT = user.getUserType();
        user.setStatus("rejected");    // user rejected
        //adding value to rejected list
        otamsroot.child("Administrator").child("admin@mail@com").child("Rejected").child(userT).child(username).setValue(user);
        //removing user from requests/rejected lists
        otamsroot.child("Administrator").child("admin@mail@com").child("Requests").child(userT).child(username).removeValue();

        Toast.makeText(this, user.getEmail() + " rejected", Toast.LENGTH_SHORT).show();
        loadRequests();
    }
}
