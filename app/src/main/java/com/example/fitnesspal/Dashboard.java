package com.example.fitnesspal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class Dashboard extends Fragment implements SensorEventListener {

    private TextView stepCountView, distanceCountView, caloriesCountView;
    private ProgressBar progressBar;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int initialStepsCount = -1; // Initialize to -1 to detect first sensor event
    private double userWeightKg = 70; // User's weight in kilograms
    private double userHeightM = 175; // User's height in meters
    private double averageStepLength = 0.762; // Average step length in meters
    private int stepGoal = 5000; // Step goal

    private static final long STEP_UPDATE_INTERVAL_MS = 1000; // Update interval for step count and distance in milliseconds
    private static final int MOVING_AVERAGE_WINDOW_SIZE = 10; // Size of the moving average window

    private LinkedList<Integer> stepCountBuffer = new LinkedList<>(); // Buffer for storing step count history

    private DatabaseReference databaseReference;
    private String userId;
    private boolean dataLoaded = false; // To check if initial data is loaded

    private SharedPreferences sharedPreferences;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateStepCountTask = new Runnable() {
        @Override
        public void run() {
            updateStepCountAndDistance();
            handler.postDelayed(this, STEP_UPDATE_INTERVAL_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stepCountView = view.findViewById(R.id.steps);
        distanceCountView = view.findViewById(R.id.distance);
        caloriesCountView = view.findViewById(R.id.calories);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start StepGraphActivity when progress bar is clicked
                startActivity(new Intent(requireActivity(), StepsGraphActivity.class));
            }
        });



        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        sharedPreferences = requireActivity().getSharedPreferences("FitnessPal", Context.MODE_PRIVATE);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle the case when the user is not logged in
            // You can redirect the user to the login screen or handle it based on your app's logic
        }

        loadCachedData();
        loadUserData();
        loadStepData();
    }

    private void loadCachedData() {
        stepCountView.setText("Steps: " + sharedPreferences.getInt("steps", 0));
        distanceCountView.setText(String.format(Locale.getDefault(), "Distance: %.2f meters", sharedPreferences.getFloat("distance", 0)));
        caloriesCountView.setText("Calories: " + sharedPreferences.getInt("calories", 0));
        updateProgressBar(sharedPreferences.getInt("steps", 0));
    }

    private void loadUserData() {
        databaseReference.child("User").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userWeightKg = snapshot.child("weight").getValue(Double.class);
                    userHeightM = snapshot.child("height").getValue(Double.class);
                    averageStepLength = calculateAverageStepLength(userHeightM);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void loadStepData() {
        String todayDate = getTodayDate();
        databaseReference.child("StepCount").child(userId).child(todayDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    initialStepsCount = snapshot.child("steps_counted").getValue(Integer.class);
                    double distance = snapshot.child("distance").getValue(Double.class);
                    int calories = snapshot.child("calories").getValue(Integer.class);

                    stepCountView.setText("Steps: " + initialStepsCount);
                    distanceCountView.setText(String.format(Locale.getDefault(), "Distance: %.2f meters", distance));
                    caloriesCountView.setText("Calories: " + calories);
                    updateProgressBar(initialStepsCount);

                    // Cache the loaded data
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("steps", initialStepsCount);
                    editor.putFloat("distance", (float) distance);
                    editor.putInt("calories", calories);
                    editor.apply();
                }
                dataLoaded = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private double calculateAverageStepLength(double heightM) {
        // Average step length is approximately 0.413 times the height of a person
        return heightM * 0.413;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentSteps = (int) event.values[0];
            if (initialStepsCount == -1) {
                initialStepsCount = currentSteps; // Set initial step count on first sensor event
                return;
            }

            if (dataLoaded) {
                int steps = currentSteps - initialStepsCount;

                stepCountBuffer.add(steps);
                if (stepCountBuffer.size() > MOVING_AVERAGE_WINDOW_SIZE) {
                    stepCountBuffer.removeFirst(); // Keep buffer size within window
                }
                int smoothedSteps = calculateMovingAverage(stepCountBuffer);
                stepCountView.setText("Steps: " + smoothedSteps);

                // Update distance
                double distance = smoothedSteps * averageStepLength;
                distanceCountView.setText(String.format(Locale.getDefault(), "Distance: %.2f meters", distance));

                // Update calories burned
                int caloriesBurned = calculateCaloriesBurned(smoothedSteps);
                caloriesCountView.setText("Calories: " + caloriesBurned);

                // Update progress bar
                updateProgressBar(smoothedSteps);

                // Cache the updated data
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("steps", smoothedSteps);
                editor.putFloat("distance", (float) distance);
                editor.putInt("calories", caloriesBurned);
                editor.apply();

                // Update Firebase database
                updateFirebaseDatabase(smoothedSteps, distance, caloriesBurned);

                // Check if step goal is reached
                if (smoothedSteps >= stepGoal) {
                    notifyStepGoalReached();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(updateStepCountTask);
    }

    private void updateProgressBar(int currentSteps) {
        double progress = ((double) currentSteps / stepGoal) * 100;
        progressBar.setProgress((int) progress);
    }

    private int calculateMovingAverage(LinkedList<Integer> buffer) {
        int sum = 0;
        for (Integer value : buffer) {
            sum += value;
        }
        return sum / buffer.size();
    }

    private int calculateCaloriesBurned(int steps) {
        // A typical estimate for calories burned per step is around 0.04 to 0.06 calories per step.
        // Adjust this value according to your needs and the user's weight.
        return (int) (steps * 0.05); // Adjust the multiplier based on a more accurate formula if needed
    }

    private void updateFirebaseDatabase(int steps, double distance, int calories) {
        String todayDate = getTodayDate();

        Map<String, Object> data = new HashMap<>();
        data.put("steps_counted", steps);
        data.put("distance", distance);
        data.put("calories", calories);
        databaseReference.child("StepCount").child(userId).child(todayDate).setValue(data);
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void updateStepCountAndDistance() {
        int steps = stepCountBuffer.peekLast() != null ? stepCountBuffer.peekLast() : 0;
        double distance = steps * averageStepLength;
        distanceCountView.setText(String.format(Locale.getDefault(), "Distance: %.2f meters", distance));

        int caloriesBurned = calculateCaloriesBurned(steps);
        caloriesCountView.setText("Calories: " + caloriesBurned);
    }

    private void notifyStepGoalReached() {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            String channelId = "step_goal_channel";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Step Goal", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                    .setContentTitle("Step Goal Reached!")
                    .setContentText("Well done! You've reached your step goal for today.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            notificationManager.notify(1, builder.build());
        }
    }
}
