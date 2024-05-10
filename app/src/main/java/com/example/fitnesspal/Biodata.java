package com.example.fitnesspal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.Period;

public class Biodata extends AppCompatActivity {

    int age;
    String name;
    String gender; // Declare gender variable at the class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biodata);
        EditText name_field = findViewById(R.id.name);
        DatePicker datePicker = findViewById(R.id.datePicker);
        Button button = findViewById(R.id.nextbutton);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        // Remove the second declaration of gender variable here

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Get the selected RadioButton by its ID
                RadioButton radioButton = findViewById(checkedId);

                // Get the text of the selected RadioButton
                gender = radioButton.getText().toString(); // Assign value to the class-level gender variable
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String day, month, year;
                // Initialize name here
                name = name_field.getText().toString(); // Move this line here
                if ((!name.isEmpty()) && (gender != null && !gender.isEmpty())) {
                    day = String.valueOf(datePicker.getDayOfMonth());
                    month = String.valueOf(datePicker.getMonth() + 1);
                    year = String.valueOf(datePicker.getYear());
                    age = calculateAge(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));

                    // Pass data to PhysicalData activity
                    Intent intent = new Intent(Biodata.this, PhysicalData.class);
                    intent.putExtra("name", name);
                    intent.putExtra("age", age);
                    intent.putExtra("gender", gender);
                    startActivity(intent);
                } else {
                    Toast.makeText(Biodata.this, "Enter all of the data.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public static int calculateAge(int birthYear, int birthMonth, int birthDay) {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Create a LocalDate object for the birth date
        LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);

        // Calculate the period between the birth date and the current date
        Period period = Period.between(birthDate, currentDate);

        // Return the age in years
        return period.getYears();
    }
}
