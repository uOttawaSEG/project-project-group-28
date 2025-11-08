package csi2105.group28.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    DatabaseReference otamsroot;

    Database db;
    ArrayList<String> childrenlist;


    private static final String TAG = "MainActivity";

    Spinner utype;

    EditText uname, pass;

    TextView requestmsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup dynamic user-defined colors
        // https://developer.android.com/develop/ui/views/theming/dynamic-colors
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //set the reference of the firebase to the otams where everything is stored
        otamsroot = FirebaseDatabase.getInstance().getReference("otams");
        childrenlist = new ArrayList<>();
        db= new Database();
        //initilalizes spinner/selector and sets its values
        utype = findViewById(R.id.utype);
        String [] spinnerIt = new String[]{"Administrator","Student","Tutor"};
        ArrayAdapter<String> utypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerIt);
        utypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);;
        utype.setAdapter(utypeAdapter);
        utype.setSelection(0);   //sets initial value to Administrator
        //initialize the username and password editText
        uname = findViewById(R.id.unameG);
        pass = findViewById(R.id.passG);

        requestmsg= findViewById(R.id.requestMessage);


    }

    /*
     * Starts a firebase value event listener
     * initiates admins and use/tutor separation if there is nothing in the firebase
     *
     */
    protected void onStart() {
        super.onStart();

        otamsroot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childrenlist.clear();
                for (DataSnapshot children : snapshot.getChildren()) {
                    childrenlist.add(children.getKey());             // checks the of the initial database
                }
                if (childrenlist.isEmpty()) {
                    User admin = new User("Administrator", "admin", "me","admin@mail.com", "Admin001", "100020653");
                    otamsroot.child("Administrator").child("admin@mail@com").child("info").setValue(admin);
                    otamsroot.child("Administrator").child("admin@mail@com").child("password").setValue("Admin001");
                    otamsroot.child("Users").child("Student").push().setValue("");
                    otamsroot.child("Users").child("Tutor").push().setValue("");          //initial values of firebase
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
       db.listenToReqNames();

    }

    /*
     * @param view is the current view
     */
    public void onClickGoSignUp(View view){
        Intent intentsu = new Intent(MainActivity.this, SigningUp.class);  //intent to cll sign uo
        startActivity(intentsu);   //calls sign up
    }

    public void onClickSignIn(View view){

        String username = uname.getText().toString().toLowerCase();
        String password =  pass.getText().toString();
        String usertype = utype.getSelectedItem().toString();               //  get all the values
        boolean valid = false, first=false;
        int found;
        requestmsg.setText("");

        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) == '@' && !first) {
                first = true;
                if (i == 0) {
                    valid = false;
                    first = false;
                }
            } else if (username.charAt(i) == '@' && first) {
                valid = false;
                first = false;
            }
            if (first & username.charAt(i) == '.') {
                valid = true;
            }
        }
        if (username.length() < 6) {
            valid = false;
        }                              //checks if username is an email
        if (valid) {
            found = db.isPendingOrRejected(username,usertype);
            String userUname = username.replace(".", "@");
            if (found==0) {
                getFirebase(usertype, userUname, password);
            }else if (found == 1){
                String msg = username+" is currently pending";
                uname.setError(msg);
                msg = "ACCESS ERROR!! "+username+" has not been approved by the Administrator yet";
                requestmsg.setText(msg);
            }else if (found == 2){
                String msg = username+" has been rejected";
                uname.setError(msg);
                msg = "ACCESS ERROR!! "+username+" has not been rejected by the Administrator";
                requestmsg.setText(msg);
            }
        }else{
            uname.setError("Invalid Email");              // if user does not put an email.
        }

    }
    /*
     * Checks if the user is has yet to be approved or was rejected by the admin
     * @param usermail(String) is the email of the user
     * @param requestmsg(TextView) is the view to show the user a message
     * @return found(Boolean) that is true if the user yet to be approved or was rejected
     */

    /* closes the main activity/program
     * @param view is the current view
     */
    public void onClickExit(View view){
        finish();
    }

    /* updates info in firebase
     * @param change user is the changed user class
     * @param Updatepass is true id the password is changed
     */

    private void getFirebase( String userType, String username, String password) {
        uname.setError(null);
        pass.setError(null);
        DatabaseReference tref;
        if (userType.equals("Administrator")) {
            tref = otamsroot.child("Administrator").child(username);
        } else {
            tref = otamsroot.child("Users").child(userType).child(username.substring(0, 1)).child(username.substring(1, 2)).child(username);
        }                    //gets reference to the place where user data is stored
        tref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String storedpassword = snapshot.child("password").getValue(String.class);    //gets password
                System.out.println(tref);
                if(storedpassword==null){
                    uname.setError("User does not exist");                   // returns null when acessing a non-data
                }else if(storedpassword.equals(password)) {
                    User userdata;
                    if (userType.equals("Tutor")){
                        userdata = snapshot.child("info").getValue(Tutor.class);             //if passwword is right
                    }else if(userType.equals("Student")){
                        userdata = snapshot.child("info").getValue(Student.class);             //if passwword is right
                    }else{
                        userdata = snapshot.child("info").getValue(User.class);
                    }

                    // send admins straight to requests screen
                    if (userType.equals("Administrator")) {
                        Intent intent = new Intent(MainActivity.this, AdminRequestsActivity.class);
                        intent.putExtra("info", userdata);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, SignedIn.class);
                        intent.putExtra("info", userdata);               //go to signed in
                        startActivity(intent);
                    }

                }else{
                    pass.setError("Wrong password");                 // if password wrong
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
