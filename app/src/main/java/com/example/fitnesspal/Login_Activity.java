package com.example.fitnesspal;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login_Activity extends AppCompatActivity {


    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static final String SHARED_PREF = "sharedPrefs";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        EditText email_field = findViewById(R.id.email);
        EditText password_field = findViewById(R.id.password);
        Button login = findViewById(R.id.loginbtn);
        TextView forgot_password = findViewById(R.id.forgot);
        loginCheck();


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

    private void loginCheck() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
        String check = sharedPreferences.getString("name","");
        if(check.equals("true"))
        {
            startActivity(new Intent(Login_Activity.this,MainActivity.class));
            finish();
        }
    }


    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name","true");
                            editor.apply();
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                            assert currentUser != null;
                            String userId = currentUser.getUid();
                            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // User data exists, redirect to main page
                                        startActivity(new Intent(Login_Activity.this, MainActivity.class));
                                        Toast.makeText(Login_Activity.this, "Login successful", Toast.LENGTH_SHORT).show();

                                        finish();
                                    } else {
                                        // User data doesn't exist, collect biodata and physical information
                                        // Navigate to the Biodata activity
                                        startActivity(new Intent(Login_Activity.this,Biodata.class));
                                        Toast.makeText(Login_Activity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle database error
                                    Toast.makeText(Login_Activity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }); // Add this closing brace
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