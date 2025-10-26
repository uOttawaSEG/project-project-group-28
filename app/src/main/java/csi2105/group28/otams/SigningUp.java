package csi2105.group28.otams;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class SigningUp extends AppCompatActivity {

    DatabaseReference otamsroot, rootref;
    Spinner utype;
    EditText emailGet, passwordGet, firstnamedGet, lastnameGet, phonenumGet, programOfStudyGet, highestDegreeGet, coursesOfferedGet;
    TextView majorError;
    // Layouts for conditional visibility
    LinearLayout degreeLayoutSU, coursesLayoutSU;
    boolean hasrequests, hasrejected;
    ArrayList<String> requests, rejected;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signing_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //initializing list for request and rejected users
        requests = new ArrayList<>();
        rejected = new ArrayList<>();
// Tutor only layout visibility
        // 1. Initialize layouts
        degreeLayoutSU = findViewById(R.id.degreeLayoutSU);
        coursesLayoutSU = findViewById(R.id.coursesLayoutSU);

        // 2. Setup spinner
        utype = findViewById(R.id.typeSU);


        utype.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = parent.getItemAtPosition(position).toString();

                if (selectedUser.equals("Tutor")) {
                    degreeLayoutSU.setVisibility(View.VISIBLE);
                    coursesLayoutSU.setVisibility(View.VISIBLE);
                } else {
                    degreeLayoutSU.setVisibility(View.GONE);
                    coursesLayoutSU.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });




        //set the reference of the firebase to the users(where students and tutor info are stored)
        otamsroot = FirebaseDatabase.getInstance().getReference("otams");
        rootref = FirebaseDatabase.getInstance().getReference("otams/Users");
        //initilalizes spinner/selector and sets its values
        utype = findViewById(R.id.typeSU);
        String [] spinnerIt = new String[]{"Student","Tutor"};
        ArrayAdapter<String> utypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerIt);
        utypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  //bigger dropdown selector
        utype.setAdapter(utypeAdapter);
        utype.setSelection(0);  //set initial value to student

        //initializes all TextEdits and global textviews
        emailGet = findViewById(R.id.emailGSU);
        passwordGet = findViewById(R.id.passGSU);
        firstnamedGet = findViewById(R.id.firstNameGSU);
        lastnameGet = findViewById(R.id.lastNameGSU);
        phonenumGet = findViewById(R.id.phoneGSU);
        programOfStudyGet = findViewById(R.id.programGSU);
        highestDegreeGet = findViewById(R.id.degreeGSU);
        coursesOfferedGet = findViewById(R.id.coursesGSU);
        majorError= findViewById(R.id.errorMsgSU);

        //Set a listener that puts a message when password is being typed
        passwordGet.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                majorError.setText("Password has to have 7 characters, and at least a number and a letter");
            }
        });
    }

    /*
     * Starts a firebase value event listener
     * initiates admins and use/tutor separation if there is nothing in the firebase
     *
     */
    protected void onStart() {
        super.onStart();
        otamsroot.child("Administrator").child("admin@mail@com").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requests.clear();
                rejected.clear();
                for (DataSnapshot children : snapshot.getChildren()) {

                    if (children.getKey().equals("Requests")) {// looking through requests
                        if (children.hasChildren()) {
                            for (DataSnapshot req : children.getChildren()) {
                                if (req.getKey().equals("Tutor")){//finding tutors,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqTutor : req.getChildren()) {
                                            String obj= reqTutor.getKey();
                                            requests.add(obj);
                                        }
                                    }
                                }else if(req.getKey().equals("Student")){//finding Students,if they exists and getting their username
                                    if (req.hasChildren()) {
                                        for (DataSnapshot reqStudent : req.getChildren()) {
                                            String obj= reqStudent.getKey();
                                            requests.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }else if (children.getKey().equals("Rejected")) {// looking through rejected requests
                        if (children.hasChildren()) {
                            for (DataSnapshot rej : children.getChildren()) {
                                if (rej.getKey().equals("Tutor")){//finding tutors,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejTutor : rej.getChildren()) {
                                            String obj= rejTutor.getKey();
                                            rejected.add(obj);
                                        }
                                    }
                                }else if (rej.getKey().equals("Student")){//finding Students,if they exists and getting their username
                                    if (rej.hasChildren()) {
                                        for (DataSnapshot rejStudent : rej.getChildren()) {
                                            String obj= rejStudent.getKey();
                                            rejected.add(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    /*
     * creates a new user if everything meets requirements
     * @param view which is the view on th screen
     */
    public void onClickSignUpSU(View view){
        boolean found;

        String email = emailGet.getText().toString().toLowerCase();   //email case does not matter, so always lowercase
        String password = passwordGet.getText().toString();
        String firstname = firstnamedGet.getText().toString();
        String lastname = lastnameGet.getText().toString();
        String prog_deg = programOfStudyGet.getText().toString();
        String phonenum = phonenumGet.getText().toString();
        String highestDegree = highestDegreeGet.getText().toString();
        String cOffered = coursesOfferedGet.getText().toString();
        String usertype = utype.getSelectedItem().toString();
        User newU = null;    //make a null user
        //will create user if and only if all the prerequisites are met--see user class
        try {
            if(usertype.equals("Tutor")) {
                newU = new Tutor( firstname, lastname, email, password, phonenum, highestDegree, new ArrayList<String>(Arrays.asList(cOffered.split(","))));
            }else{
                newU = new Student( firstname, lastname, email, password, phonenum, prog_deg);
            }
        }catch(IllegalArgumentException e){
            if(e.getMessage().equals("passwordl")){
                passwordGet.setError("Password needs to be 7 characters long");       // short password
            }else if(e.getMessage().equals("passwordc")){
                passwordGet.setError("Password needs to have at least a number and a letter"); // no letter and num
            }else if(e.getMessage().equals("email")){
                emailGet.setError("Email is invalid");//any email error(base)
            } else if(e.getMessage().equals("FirstName")){
                firstnamedGet.setError("First name can only contain letters and be 50 characters long");//any name error(base)
            }else if(e.getMessage().equals("LastName")){
                lastnameGet.setError("Last name can only contain letters and 50 characters long");//any name error(base)
            }else if(e.getMessage().equals("PhoneNum")){
                phonenumGet.setError("Phone number can only contain digits, '', + and -");//any phone num error(base)
            }
        }
        // only try to send to firebase if everything is ok
        if (newU!=null) {
            found= adminlist(newU.getEmail());
            if(!found) {
                newUserToFirebase(newU, usertype);
            }
        }
    }

    /*
     * back button tht goes to main screen
     * @param view which is the view on th screen
     */
    public void onClickBackSU(View view){
        finish();
    }

    // creates a new storage for the user in firebase
    private void newUserToFirebase(User newUser, String userT){

        String username = newUser.getUsername();
        DatabaseReference tref;
        tref = rootref.child(userT).child(username.substring(0,1)).child(username.substring(1,2));// makes a path based on the first two letter
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
                    otamsroot.child("Administrator").child("admin@mail@com").child("Requests").child(userT).child(username).setValue(newUser);
                    //if new user, make user with key username, in the requsest list of the admin
                    finish();                                                      //ends after sending data to firebase
                }catch (IllegalArgumentException e) {
                    String error = "Username/email is already taken for " + userT;
                    majorError.setText(error);
                    emailGet.setError(error);                                       // shows error if username already taken
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /*
     * Checks if the user is has yet to be approved or was rejected by the admin
     * @param usermail(String) is the email of the user
     * @param requestmsg(TextView) is the view to show the user a message
     * @return found(Boolean) that is true if the user yet to be approved or was rejected
     */
    public boolean adminlist(String usermail){
        String username = usermail.replace(".", "@");
        boolean found=false;

        for(String request: requests){
            if(request.equals(username)){
                found=true;
                String msg = "Username/email is already taken for " + usermail;
                emailGet.setError(msg);
            }
        }
        for(String rejects: rejected){
            if(rejected.equals(username)){
                found=true;
                String msg = "Username/email is already taken for " + usermail;
                emailGet.setError(msg);
            }
        }
        return found;
    }
}