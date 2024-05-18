package com.example.fitnesspal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Activity extends Fragment {

    private static final String TAG = "WorkoutFragment";

    private AutoCompleteTextView exerciseDropdown;
    private EditText repsEditText;
    private EditText setsEditText;
    private EditText weightEditText;
    private Button addButton;
    private TextView summaryTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Map<String, Exercise> exerciseData;
    private String userId;

    // Exercise data should be populated with actual values (calories burned per rep)
    private void initializeExerciseData() {
        exerciseData = new HashMap<>();
        exerciseData.put("Running", new Exercise(10, "Cardio"));
        exerciseData.put("Push-ups", new Exercise(0.5, "Strength"));
        exerciseData.put("Squats", new Exercise(0.7, "Strength"));
        exerciseData.put("Pull-ups", new Exercise(1, "Strength"));
        exerciseData.put("Bench Press", new Exercise(1.5, "Strength"));
        exerciseData.put("Deadlift", new Exercise(2, "Strength"));
        exerciseData.put("Bicep Curls", new Exercise(0.3, "Strength"));
        exerciseData.put("Tricep Dips", new Exercise(0.6, "Strength"));
        exerciseData.put("Lunges", new Exercise(0.8, "Strength"));
        exerciseData.put("Plank", new Exercise(1, "Core"));
        exerciseData.put("Crunches", new Exercise(0.4, "Core"));
        exerciseData.put("Leg Press", new Exercise(1.2, "Strength"));
        exerciseData.put("Shoulder Press", new Exercise(1.3, "Strength"));
        exerciseData.put("Lat Pulldown", new Exercise(1.1, "Strength"));
        exerciseData.put("Bent-over Row", new Exercise(1.4, "Strength"));
        exerciseData.put("Dumbbell Flyes", new Exercise(0.9, "Strength"));
        exerciseData.put("Kettlebell Swings", new Exercise(1.6, "Strength"));
        exerciseData.put("Mountain Climbers", new Exercise(0.7, "Cardio"));
        exerciseData.put("Burpees", new Exercise(1.2, "Cardio"));
        exerciseData.put("Jumping Jacks", new Exercise(0.5, "Cardio"));
        exerciseData.put("High Knees", new Exercise(0.8, "Cardio"));
        exerciseData.put("Box Jumps", new Exercise(1.5, "Cardio"));
        exerciseData.put("Treadmill Running", new Exercise(10, "Cardio"));
        exerciseData.put("Cycling", new Exercise(8, "Cardio"));
        exerciseData.put("Rowing", new Exercise(7, "Cardio"));
        exerciseData.put("Swimming", new Exercise(6, "Cardio"));
        exerciseData.put("Jump Rope", new Exercise(1, "Cardio"));
        exerciseData.put("Hiking", new Exercise(5, "Cardio"));
        exerciseData.put("Skiing", new Exercise(4, "Cardio"));
        exerciseData.put("Snowboarding", new Exercise(4, "Cardio"));
        exerciseData.put("Rollerblading", new Exercise(5, "Cardio"));
        exerciseData.put("Kayaking", new Exercise(3, "Cardio"));
        exerciseData.put("Paddleboarding", new Exercise(2, "Cardio"));
        exerciseData.put("Surfing", new Exercise(3, "Cardio"));
        exerciseData.put("Rock Climbing", new Exercise(5, "Cardio"));
        exerciseData.put("Skateboarding", new Exercise(2, "Cardio"));
        exerciseData.put("Dancing", new Exercise(4, "Cardio"));
        exerciseData.put("Zumba", new Exercise(4, "Cardio"));
        exerciseData.put("Pilates", new Exercise(2, "Strength"));
        exerciseData.put("Yoga", new Exercise(1, "Strength"));
        exerciseData.put("Tai Chi", new Exercise(0.5, "Strength"));
        exerciseData.put("Stretching", new Exercise(0.3, "Strength"));
        exerciseData.put("Basketball", new Exercise(6, "Cardio"));
        exerciseData.put("Soccer", new Exercise(7, "Cardio"));
        exerciseData.put("Tennis", new Exercise(5, "Cardio"));
        exerciseData.put("Baseball", new Exercise(4, "Cardio"));
        exerciseData.put("Golf", new Exercise(2, "Cardio"));
        exerciseData.put("Volleyball", new Exercise(3, "Cardio"));
        exerciseData.put("Rugby", new Exercise(8, "Cardio"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        try {
            exerciseDropdown = view.findViewById(R.id.exerciseDropdown);
            repsEditText = view.findViewById(R.id.repsEditText);
            setsEditText = view.findViewById(R.id.setsEditText);
            weightEditText = view.findViewById(R.id.weightEditText);
            addButton = view.findViewById(R.id.addButton);
            summaryTextView = view.findViewById(R.id.summaryTextView);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            userId = mAuth.getCurrentUser().getUid();

            initializeExerciseData();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, new ArrayList<>(exerciseData.keySet()));
            exerciseDropdown.setAdapter(adapter);

            addButton.setOnClickListener(v -> addWorkout());

            // Fetch and display existing workout data
            fetchAndDisplayExistingData();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing view: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing view", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void addWorkout() {
        try {
            String selectedExercise = exerciseDropdown.getText().toString();
            String repsStr = repsEditText.getText().toString();
            String setsStr = setsEditText.getText().toString();
            String weightStr = weightEditText.getText().toString();

            if (selectedExercise.isEmpty() || repsStr.isEmpty() || setsStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int reps = Integer.parseInt(repsStr);
            int sets = Integer.parseInt(setsStr);
            double weight = Double.parseDouble(weightStr);

            if (!exerciseData.containsKey(selectedExercise)) {
                Toast.makeText(getContext(), "Exercise not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Exercise exercise = exerciseData.get(selectedExercise);
            int caloriesBurned = calculateCaloriesBurned(exercise.getCaloriesBurnedPerRep(), reps, sets);

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            DatabaseReference workoutRef = mDatabase.child("workout").child(userId).child(date).child(selectedExercise);
            workoutRef.child("calories_burned").setValue(caloriesBurned);
            workoutRef.child("type").setValue(exercise.getType());
            workoutRef.child("reps").setValue(reps);
            workoutRef.child("sets").setValue(sets);
            workoutRef.child("weight").setValue(weight);

            updateSummaryTextView();
        } catch (Exception e) {
            Log.e(TAG, "Error adding workout: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error adding workout", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateCaloriesBurned(double caloriesPerRep, int reps, int sets) {
        // Calculate total calories burned based on reps, sets, and calories per rep
        return (int) (caloriesPerRep * reps * sets);
    }

    private void fetchAndDisplayExistingData() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Query query = mDatabase.child("workout").child(userId).child(date);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder summary = new StringBuilder("Today's workout:\n");
                for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                    String exerciseName = exerciseSnapshot.getKey();
                    int caloriesBurned = exerciseSnapshot.child("calories_burned").getValue(Integer.class);
                    String type = exerciseSnapshot.child("type").getValue(String.class);
                    int reps = exerciseSnapshot.child("reps").getValue(Integer.class);
                    int sets = exerciseSnapshot.child("sets").getValue(Integer.class);
                    double weight = exerciseSnapshot.child("weight").getValue(Double.class);

                    summary.append(exerciseName)
                            .append(": ")
                            .append(reps)
                            .append(" reps, ")
                            .append(sets)
                            .append(" sets, ")
                            .append(weight)
                            .append(" kg, ")
                            .append(caloriesBurned)
                            .append(" calories burned\n");
                }
                summaryTextView.setText(summary.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch workout data", databaseError.toException());
            }
        });
    }

    private void updateSummaryTextView() {
        // Update summary after adding new workout
        fetchAndDisplayExistingData();
    }

    // Exercise class
    public static class Exercise {
        private double caloriesBurnedPerRep;
        private String type;

        public Exercise() {
            // Default constructor required for calls to DataSnapshot.getValue(Exercise.class)
        }

        public Exercise(double caloriesBurnedPerRep, String type) {
            this.caloriesBurnedPerRep = caloriesBurnedPerRep;
            this.type = type;
        }

        public double getCaloriesBurnedPerRep() {
            return caloriesBurnedPerRep;
        }

        public String getType() {
            return type;
        }
    }
}
