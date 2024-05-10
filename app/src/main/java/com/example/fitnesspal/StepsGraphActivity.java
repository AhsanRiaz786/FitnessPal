package com.example.fitnesspal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StepsGraphActivity extends AppCompatActivity {

    private static final String TAG = "StepsGraphActivity";
    private BarChart barChartStepCount, barChartDistance, barChartCalories;

    private DatabaseReference stepCountRef;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_graph);

        barChartStepCount = findViewById(R.id.barChartSteps);
        barChartDistance = findViewById(R.id.barChartDistance);
        barChartCalories = findViewById(R.id.barChartCalories);

        // Remove description label text
        barChartStepCount.getDescription().setEnabled(false);
        barChartDistance.getDescription().setEnabled(false);
        barChartCalories.getDescription().setEnabled(false);

        // Ensure y-axis labels are only on the left side
        YAxis leftAxisStep = barChartStepCount.getAxisLeft();
        leftAxisStep.setDrawLabels(true);
        YAxis rightAxisStep = barChartStepCount.getAxisRight();
        rightAxisStep.setDrawLabels(false);

        YAxis leftAxisDistance = barChartDistance.getAxisLeft();
        leftAxisDistance.setDrawLabels(true);
        YAxis rightAxisDistance = barChartDistance.getAxisRight();
        rightAxisDistance.setDrawLabels(false);

        YAxis leftAxisCalories = barChartCalories.getAxisLeft();
        leftAxisCalories.setDrawLabels(true);
        YAxis rightAxisCalories = barChartCalories.getAxisRight();
        rightAxisCalories.setDrawLabels(false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            stepCountRef = FirebaseDatabase.getInstance().getReference("StepCount").child(userId);
            fetchAndDisplayData();
        } else {
            Log.e(TAG, "User not authenticated");
        }
    }

    private void fetchAndDisplayData() {
        // Get the date range for the last 7 days
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        Date startDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Query the database for step count data within the date range
        stepCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BarEntry> caloriesEntries = new ArrayList<>();
                ArrayList<BarEntry> distanceEntries = new ArrayList<>();
                ArrayList<BarEntry> stepsEntries = new ArrayList<>();
                String[] days = new String[7];
                Calendar cal = Calendar.getInstance();

                for (int i = 0; i < 7; i++) {
                    cal.setTime(endDate);
                    cal.add(Calendar.DAY_OF_YEAR, -i);
                    String dateKey = dateFormat.format(cal.getTime());
                    Log.d(TAG, "Checking date: " + dateKey);

                    DataSnapshot daySnapshot = dataSnapshot.child(dateKey);
                    int caloriesBurned = 0;
                    int stepsCounted = 0;
                    float distance = 0;
                    if (daySnapshot.exists()) {
                        Integer calories = daySnapshot.child("calories").getValue(Integer.class);
                        Integer steps = daySnapshot.child("steps_counted").getValue(Integer.class);
                        Float dist = daySnapshot.child("distance").getValue(Float.class);
                        if (calories != null && steps != null && dist != null) {
                            caloriesBurned = calories;
                            stepsCounted = steps;
                            distance = dist;
                        }
                    }
                    Log.d(TAG, "Date: " + dateKey + " Calories: " + caloriesBurned + " Steps: " + stepsCounted + " Distance: " + distance);
                    caloriesEntries.add(new BarEntry(6 - i, caloriesBurned));
                    distanceEntries.add(new BarEntry(6 - i, distance));
                    stepsEntries.add(new BarEntry(6 - i, stepsCounted));
                    days[6 - i] = android.text.format.DateFormat.format("EEE", cal).toString();
                }

                if (stepsEntries.isEmpty() && distanceEntries.isEmpty() && caloriesEntries.isEmpty()) {
                    Log.e(TAG, "Entries list is empty. No data to display.");
                } else {
                    setupCaloriesBarChart(caloriesEntries, days);
                    setupStepBarChart(stepsEntries, days);
                    setupDistanceBarChart(distanceEntries, days);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void setupStepBarChart(ArrayList<BarEntry> entries, String[] days) {
        BarDataSet dataSet = new BarDataSet(entries, "Steps");
        dataSet.setColor(getResources().getColor(R.color.DistancebarColor));
        BarData barData = new BarData(dataSet);
        barChartStepCount.setData(barData);

        XAxis xAxis = barChartStepCount.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChartStepCount.setFitBars(true);
        barChartStepCount.animateY(1000);
        barChartStepCount.invalidate();
    }

    private void setupDistanceBarChart(ArrayList<BarEntry> entries, String[] days) {
        BarDataSet dataSet = new BarDataSet(entries, "Distance");
        dataSet.setColor(getResources().getColor(R.color.StepsbarColor));
        BarData barData = new BarData(dataSet);
        barChartDistance.setData(barData);

        XAxis xAxis = barChartDistance.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChartDistance.setFitBars(true);
        barChartDistance.animateY(1000);
        barChartDistance.invalidate();
    }

    private void setupCaloriesBarChart(ArrayList<BarEntry> entries, String[] days) {
        BarDataSet dataSet = new BarDataSet(entries, "Calories");
        dataSet.setColor(getResources().getColor(R.color.CaloriesbarColor));
        BarData barData = new BarData(dataSet);
        barChartCalories.setData(barData);

        XAxis xAxis = barChartCalories.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChartCalories.setFitBars(true);
        barChartCalories.animateY(1000);
        barChartCalories.invalidate();
    }
}
