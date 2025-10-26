package csi2105.group28.otams;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminRequestsActivity extends AppCompatActivity {

    private LinearLayout requestsContainer;
    private DatabaseReference adminRef;
    private String currentNode = "Requests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        requestsContainer = findViewById(R.id.requestsContainer);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        adminRef = FirebaseDatabase.getInstance()
                .getReference("otams/Administrator/admin@mail@com");

        loadRequests(currentNode);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentNode = "Requests"; break;
                    case 1: currentNode = "Approved"; break;
                    case 2: currentNode = "Rejected"; break;
                }
                loadRequests(currentNode);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadRequests(String nodeName) {
        requestsContainer.removeAllViews();

        adminRef.child(nodeName).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String email = child.getKey();

                    TextView tv = new TextView(AdminRequestsActivity.this);
                    tv.setText(email);
                    tv.setTextSize(18);
                    tv.setPadding(16,16,16,16);

                    if(nodeName.equals("Requests")) {
                        tv.setOnClickListener(v -> showApproveRejectDialog(email));
                    }

                    requestsContainer.addView(tv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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
