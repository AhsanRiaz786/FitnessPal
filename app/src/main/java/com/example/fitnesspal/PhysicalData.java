package com.example.fitnesspal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;

import java.util.Objects;

public class PhysicalData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_physical_data);
        Button nextButton = findViewById(R.id.nextbutton);
        EditText heightField = findViewById(R.id.height);
        EditText weightField = findViewById(R.id.weight);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            int age = intent.getIntExtra("age", 0); // default value is 0
            String gender = intent.getStringExtra("gender");

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String height = heightField.getText().toString();
                    String weight = weightField.getText().toString();
                    if ((!weight.isEmpty()) && (!height.isEmpty())) {
                        int userHeight = Integer.parseInt(height);
                        int userWeight = Integer.parseInt(weight);

                        // Create a User instance
                        User user = new User(name, age, gender, userHeight, userWeight);
                        BodyMeasurements bodyMeasurements = new BodyMeasurements(userHeight, userWeight);

                        // Push user data to Firebase Realtime Database
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                        DatabaseReference bodyMeasurementsDb = FirebaseDatabase.getInstance().getReference("BodyMeasurements");
                        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        bodyMeasurementsDb.child(userId).setValue(bodyMeasurements);
                        usersRef.child(userId).setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Data successfully saved
                                        // You can perform any additional actions here
                                        Toast.makeText(PhysicalData.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(PhysicalData.this,MainActivity.class));

                                        // Navigate to the next activity or perform any other action
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to save data
                                        Toast.makeText(PhysicalData.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(PhysicalData.this, "Enter all of the data.", Toast.LENGTH_SHORT).show();
                    }


                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}