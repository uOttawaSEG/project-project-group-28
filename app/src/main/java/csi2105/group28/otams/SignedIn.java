package csi2105.group28.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignedIn extends AppCompatActivity {
    User thisuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signed_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        thisuser = (User) getIntent().getSerializableExtra("info");

        //Says welcome or status-specific message
        TextView welcome = findViewById(R.id.Welcome);
        String status = thisuser.getStatus();  // get user status
        String welcomeMSG;

        switch (status.toLowerCase()) {
            case "approved":
                welcomeMSG = "Welcome " + thisuser.getFirstName() + " " + thisuser.getLastName() +
                        "! You are signed in as " + thisuser.getUserType();
                break;

            case "pending":
                welcomeMSG = "Hello " + thisuser.getFirstName() + ", your account approval is still pending. Please wait for administrator approval.";
                break;

            case "rejected":
                welcomeMSG = "Sorry " + thisuser.getFirstName() + ", your account has been rejected.\n" +
                        "Contact support at: 555-555-5555 for more information.";
                break;

            default:
                welcomeMSG = "Welcome " + thisuser.getFirstName() + "! Status unknown.";
                break;
        }

        welcome.setText(welcomeMSG);

        // Optional debug text to see if everything is stored in firebase user class
        /*
        TextView data = findViewById(R.id.Data);
        String dataMSG = "User Type:\t" + thisuser.getUserType() + "\nEmail:\t" + thisuser.getUsername() +
                "\nUser Password:\t" + thisuser.getPassword() + "\nFirebase Username:\t" + thisuser.getUsername();
        data.setText(dataMSG);
        */
    }

    public void onClickBack(View view){
        finish();
    }
}
