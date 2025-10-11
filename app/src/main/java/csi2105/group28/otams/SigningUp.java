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

public class SigningUp extends AppCompatActivity {

    DatabaseReference rootref;
    Spinner utype;
    EditText emailGet, passwordGet;
    TextView majorError;
    // Layouts for conditional visibility
    LinearLayout degreeLayoutSU, coursesLayoutSU;



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
     * creates a new user if everything meets requirements
     * @param view which is the view on th screen
     */
    public void onClickSignUpSU(View view){
        String email = emailGet.getText().toString().toLowerCase();   //email case does not matter, so always lowercase
        String password = passwordGet.getText().toString();
        String usertype = utype.getSelectedItem().toString();
        User newU = null;    //make a null user
        //will create user if and only if all the prerequisites are met--see user class
        try {
            newU = new User(usertype, email, password);
        }catch(IllegalArgumentException e){
            if(e.getMessage().equals("passwordl")){
                passwordGet.setError("Password needs to be 7 characters long");       // short password
            }else if(e.getMessage().equals("passwordc")){
                passwordGet.setError("Password needs to have at least a number and a letter"); // no letter and num
            }else if(e.getMessage().equals("email")){
                emailGet.setError("Email is invalid");//any email error(base)
            }
        }
        // only try to send to firebase if everything is ok
        if (newU!=null) {
            newUserToFirebase(newU, usertype);
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
                    tref.child(username).child("password").setValue(newUser.getPassword());
                    tref.child(username).child("info").setValue(newUser);//if new user, make user with key username, password/class stored in key password/info
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
}