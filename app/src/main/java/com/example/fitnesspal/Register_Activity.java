package com.example.fitnesspal;

import android.util.Patterns;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register_Activity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
// ...
// Initialize Firebase Auth


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_register);
        TextView btn = findViewById(R.id.logintextview);
        Button register = findViewById(R.id.loginbtn);
        EditText email_field = findViewById(R.id.email);
        EditText password_field = findViewById(R.id.password);
        EditText confirmPassword_field = findViewById(R.id.confirmpassword);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register_Activity.this, Login_Activity.class));
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_field.getText().toString();
                String password = password_field.getText().toString();
                String confirmPassword = confirmPassword_field.getText().toString();
                if (!password.equals(confirmPassword)) {
                    // Passwords don't match, show Toast message
                    Toast.makeText(Register_Activity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                }  else if (password.length() < 8 || password.length() > 15) {
                    Toast.makeText(Register_Activity.this, "Password should be between 8 and 15 characters.", Toast.LENGTH_SHORT).show();

                } else if (!isValidEmail(email)) {
                    Toast.makeText(Register_Activity.this, "Email should be valid.", Toast.LENGTH_SHORT).show();
                } else {
                    // Passwords match, proceed with registration
                    registerUser(email, password);
                    Toast.makeText(Register_Activity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void registerUser(String email, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            sendVerificationEmail(user);

                            // Proceed to next screen or perform additional tasks
                        } else {
                            Toast.makeText(Register_Activity.this, "Registration failed: ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register_Activity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register_Activity.this, "Failed to send verification email: ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        // Use Patterns.EMAIL_ADDRESS to validate email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}


