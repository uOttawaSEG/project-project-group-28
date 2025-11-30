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

        // ADDED: redirect approved students to StudentBookingActivity
        if (thisuser != null && "approved".equalsIgnoreCase(thisuser.getStatus())
                && "Student".equalsIgnoreCase(thisuser.getUserType())) {
            // if student is approved - redirect to booking activity
            Intent bookingIntent = new Intent(SignedIn.this, StudentBookingActivity.class);
            bookingIntent.putExtra("info", thisuser);
            startActivity(bookingIntent);
            finish(); // close
            return;
        }

        // set up displays welcome or status-specific message
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
                welcomeMSG = "Sorry " + thisuser.getFirstName() + ", your account request has been rejected.\n" +
                        "Contact support at: 555-555-5555 for more information.";
                break;

            default:
                welcomeMSG = "Welcome " + thisuser.getFirstName() + "! Status unknown.";
                break;
        }

        welcome.setText(welcomeMSG);


    }

    public void onClickBack(View view){
        finish();
    }
}
