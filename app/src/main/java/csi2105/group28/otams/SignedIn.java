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
        //Says welcome
        TextView welcome = findViewById(R.id.Welcome);
        String welcomeMSG = "Welcome ! You are signed in as "+thisuser.getUserType();
        welcome.setText(welcomeMSG);

        // Test text to see if everything is stored in firebase user class
        TextView data = findViewById(R.id.Data);
        String dataMSG = "User Type:\t" + thisuser.getUserType() + "\nEmail:\t" + thisuser.getUsername() +
                "\nUser Password:\t" + thisuser.getPassword() + "\nFirebase Username:\t" + thisuser.getUsername();
        data.setText(dataMSG);
    }

    public void onClickBack(View view){
        finish();
    }
}
