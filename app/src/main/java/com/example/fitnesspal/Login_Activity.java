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

import java.util.Objects;

public class Login_Activity extends AppCompatActivity {


    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        EditText email_field = findViewById(R.id.email);
        EditText password_field = findViewById(R.id.password);
        Button login = findViewById(R.id.loginbtn);
        TextView forgot_password = findViewById(R.id.forgot);


        TextView btn = findViewById(R.id.registertextview);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login_Activity.this,Register_Activity.class));
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login_Activity.this,ForgotActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_field.getText().toString();
                String password = password_field.getText().toString();
                if(email.isEmpty())
                {
                    Toast.makeText(Login_Activity.this, "Enter password." , Toast.LENGTH_SHORT).show();

                }
                else if (!(isValidEmail(email)))
                {
                    Toast.makeText(Login_Activity.this, "Enter a valid email" , Toast.LENGTH_SHORT).show();

                }
                else if(password.isEmpty())
                {
                    Toast.makeText(Login_Activity.this, "Enter password." , Toast.LENGTH_SHORT).show();

                }
                else
                {
                    loginUser(email,password);
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Proceed to next screen or perform additional tasks
                            Toast.makeText(Login_Activity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            // Login failed
                            Toast.makeText(Login_Activity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        // Use Patterns.EMAIL_ADDRESS to validate email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}