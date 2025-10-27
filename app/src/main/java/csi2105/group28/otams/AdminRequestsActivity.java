package csi2105.group28.otams;

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


    ArrayList<User> requestsS, rejectedS, requestsT, rejectedT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        requestsContainer = findViewById(R.id.requestsContainer);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        requestsS = new ArrayList<User>();
        rejectedS = new ArrayList<User>();
        requestsT = new ArrayList<User>();
        rejectedT = new ArrayList<User>();

        adminRef = FirebaseDatabase.getInstance()
                .getReference("otams/Administrator/admin@mail@com");
        loadRequests(currentNode);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentNode = "Requests"; break;
                    case 1: currentNode = "Rejected"; break;
                }
                loadRequests(currentNode);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        otamsroot = FirebaseDatabase.getInstance().getReference("otams");
    }


    protected void onStart() {
        super.onStart();
        otamsroot.child("Administrator").child("admin@mail@com").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsS.clear();
                rejectedS.clear();
                requestsT.clear();
                rejectedT.clear();
                for (DataSnapshot children : snapshot.getChildren()) {

                    if (children.getKey().equals("Requests")) {
                        if (children.hasChildren()) {
                            for (DataSnapshot req : children.getChildren()) {
                                if (req.getKey().equals("Tutor")){
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqTutor : req.getChildren()) {
                                            Tutor obj= reqTutor.getValue(Tutor.class);
                                            requestsT.add(obj);
                                        }
                                    }
                                }else if (req.getKey().equals("Student")){
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqStudent : req.getChildren()) {
                                            Student obj= reqStudent.getValue(Student.class);
                                            requestsS.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }else if (children.getKey().equals("Rejected")) {
                        if (children.hasChildren()) {
                            for (DataSnapshot rej : children.getChildren()) {
                                if (rej.getKey().equals("Tutor")){
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejTutor : rej.getChildren()) {
                                            Tutor obj= rejTutor.getValue(Tutor.class);
                                            rejectedT.add(obj);
                                        }
                                    }
                                }else if (rej.getKey().equals("Student")){
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejStudent : rej.getChildren()) {
                                            Student obj= rejStudent.getValue(Student.class);
                                            rejectedS.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                requestsS.addAll(requestsT);
                rejectedS.addAll(rejectedT);               // if we are using 1 listm concatenate all into S list
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private ConstraintLayout createUserView(User user, boolean canAccept, boolean canReject){
        ConstraintLayout returnLayout;
        returnLayout = new ConstraintLayout(this);

        // add an accept button if needed
        if (canAccept) {
            Button acceptButton = new Button(this);

            // set some formatting
            acceptButton.setText("Accept");
            acceptButton.setRight(0);
            // make the button accept the request when clicked
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    approveRequest(user.getEmail());
                }
            });

            returnLayout.addView(acceptButton);
        }

        // add a reject button if needed
        if (canReject) {
            Button rejectButton = new Button(this);

            // set some formatting
            rejectButton.setText("Reject");
            rejectButton.setRight(0);
            // make the button accept the request when clicked
            rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectRequest(user.getEmail());
                }
            });

            returnLayout.addView(rejectButton);
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
        returnLayout.addView(userData);

        return returnLayout;

    }

    private void loadRequests(String nodeName) {
        requestsContainer.removeAllViews();

        for(User user : requestsT){
            requestsContainer.addView(createUserView(user, true, true));
        }
        for(User user : requestsS){
            requestsContainer.addView(createUserView(user, true, true));
        }
    }

    private void showApproveRejectDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Approve or Reject")
                .setMessage("Do you want to approve or reject " + email + "?")
                .setPositiveButton("Approve", (dialog, which) -> approveRequest(email))
                .setNegativeButton("Reject", (dialog, which) -> rejectRequest(email))
                .show();
    }

    private void approveRequest(String email) {
        adminRef.child("Requests").child(email).removeValue();
        adminRef.child("Approved").child(email).setValue(true);
        Toast.makeText(this, email + " approved", Toast.LENGTH_SHORT).show();
        loadRequests(currentNode);
    }

    private void rejectRequest(String email) {
        adminRef.child("Requests").child(email).removeValue();
        adminRef.child("Rejected").child(email).setValue(true);
        Toast.makeText(this, email + " rejected", Toast.LENGTH_SHORT).show();
        loadRequests(currentNode);
    }
}
