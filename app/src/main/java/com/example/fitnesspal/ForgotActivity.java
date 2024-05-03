package com.example.fitnesspal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button getLinkButton,goBackButton;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot);
        emailEditText = findViewById(R.id.email);
        getLinkButton = findViewById(R.id.backbutton);
        goBackButton = findViewById(R.id.loginbtn);
        firebaseAuth = FirebaseAuth.getInstance();
        getLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                goBackButton.setVisibility(View.INVISIBLE);
                goBackButton.setEnabled(false);
                if(isValidEmail(email)) {
                    firebaseAuth.sendPasswordResetEmail(email);
                    getLinkButton.setVisibility(View.INVISIBLE); // or View.GONE if you want to remove the button from layout
                    getLinkButton.setEnabled(false);
                    goBackButton.setVisibility(View.VISIBLE);
                    goBackButton.setEnabled(true);
                    Toast.makeText(ForgotActivity.this, "Reset Link Successfully Sent. Go back to Login.", Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(ForgotActivity.this, "Enter valid email.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotActivity.this,Login_Activity.class));

            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private boolean isValidEmail(String email) {
        // Use Patterns.EMAIL_ADDRESS to validate email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}

