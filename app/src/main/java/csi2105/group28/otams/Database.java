package csi2105.group28.otams;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Database {
    DatabaseReference otamsroot, rootref;
    ArrayList<String> requestsS, rejectedS, requestsT, rejectedT;

    public Database() {
        //initializing list for request and rejected users
        requestsS = new ArrayList<>();
        rejectedS = new ArrayList<>();
        requestsT = new ArrayList<>();
        rejectedT = new ArrayList<>();

        //set the reference of the firebase to the users(where students and tutor info are stored)
        otamsroot = FirebaseDatabase.getInstance().getReference("otams");
        rootref = FirebaseDatabase.getInstance().getReference("otams/Users");

        otamsroot.child("Administrator").child("admin@mail@com").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsS.clear();
                rejectedS.clear();
                requestsT.clear();
                rejectedT.clear();
                for (DataSnapshot children : snapshot.getChildren()) {

                    if (children.getKey().equals("Requests")) { // looking through requests
                        if (children.hasChildren()) {
                            for (DataSnapshot req : children.getChildren()) {
                                if (req.getKey().equals("Tutor")) {                   //finding tutors,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqTutor : req.getChildren()) {
                                            String obj = reqTutor.getKey();
                                            requestsT.add(obj);
                                        }
                                    }
                                } else if (req.getKey().equals("Student")) {//finding Students,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqStudent : req.getChildren()) {
                                            String obj = reqStudent.getKey();
                                            requestsS.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (children.getKey().equals("Rejected")) {// looking through rejected requests
                        if (children.hasChildren()) {
                            for (DataSnapshot rej : children.getChildren()) {
                                if (rej.getKey().equals("Tutor")) {//finding tutors,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejTutor : rej.getChildren()) {
                                            String obj = rejTutor.getKey();
                                            rejectedT.add(obj);
                                        }
                                    }
                                } else if (rej.getKey().equals("Student")) {//finding Students,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejStudent : rej.getChildren()) {
                                            String obj = rejStudent.getKey();
                                            rejectedS.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    /*
     * Checks if the user is has yet to be approved or was rejected by the admin
     * @param usermail(String) is the email of the user
     * @param requestmsg(TextView) is the view to show the user a message
     * @return found(Boolean) that is true if the user yet to be approved or was rejected
     */
    public boolean isPendingOrRejected(String usermail, String usertype){
        String username = usermail.replace(".", "@");
        boolean found = false;
        if(usertype.equals("Student")) {
            for (String request : requestsS) {
                if (request.equals(username)) {
                    found = true;
                }
            }
            for (String rejects : rejectedS) {
                if (rejects.equals(username)) {
                    found = true;
                }
            }
        }else if (usertype.equals("Tutor")){
            for (String request : requestsT) {
                if (request.equals(username)) {
                    found = true;
                }
            }
            for (String rejects : rejectedT) {
                if (rejects.equals(username)) {
                    found = true;
                }
            }
        }
        return found;
    }

    // creates a new storage for the user in firebase
    private void newUser(User newUser, String userType) throws IllegalArgumentException{

        String username = newUser.getUsername();
        DatabaseReference tref;
        tref = rootref.child(userType).child(username.substring(0,1)).child(username.substring(1,2));// makes a path based on the first two letter
        tref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //everything in the snapshot is done in sync, everything outside is async. calling values gotten here outside will have
            //a nullpointer exeption
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    for (DataSnapshot children : snapshot.getChildren()) {
                        if (children.getKey().compareTo(username) == 0) {
                            throw new IllegalArgumentException("Username already exists. Use another email."); //checks if username exist so no overwrite occurs
                        }
                    }

                    //tref.child(username).child("password").setValue(newUser.getPassword());
                    otamsroot.child("Administrator").child("admin@mail@com").child("Requests").child(userType).child(username).setValue(newUser)
                            .addOnSuccessListener(aVoid -> {
                                // ✅ Show pending approval message
                                android.widget.Toast.makeText(SigningUp.this,
                                        "Your account approval is now pending. Please check back later.",
                                        android.widget.Toast.LENGTH_LONG).show();
                                // Optionally redirect to login screen
                                android.content.Intent intent = new android.content.Intent(SigningUp.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                android.widget.Toast.makeText(SigningUp.this,
                                        "Failed to submit your request: " + e.getMessage(),
                                        android.widget.Toast.LENGTH_SHORT).show();
                            });

                }catch (IllegalArgumentException e) {
                    String error = "Username/email is already taken for " + userType;
                    majorError.setText(error);
                    emailGet.setError(error);                                       // shows error if username already taken
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
