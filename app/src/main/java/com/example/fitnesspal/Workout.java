package com.example.fitnesspal;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Locale;

public class Workout extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "WorkoutFragment";

    private ImageView userPhoto;
    private TextView name, gender, age, heightDisplay, weightDisplay, stepGoalDisplay, bmiDisplay, bodyFatPercentageDisplay, muscleMassDisplay;
    private EditText editHeight, editWeight, editStepGoal, editBodyFatPercentage, editMuscleMass;
    private Button editHeightButton, editWeightButton, editStepGoalButton, editBodyFatButton, editMuscleMassButton, saveButton, changePhotoButton, logoutButton;

    private DatabaseReference userRef;
    private DatabaseReference bodyMeasurementsRef;
    private FirebaseAuth mAuth;

    private WorkoutViewModel workoutViewModel;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User is not logged in. Redirecting to login.");
            redirectToLogin();
            return view;
        }
        String userId = currentUser.getUid();

        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        // Initialize views
        userPhoto = view.findViewById(R.id.user_photo);
        changePhotoButton = view.findViewById(R.id.change_photo_button);
        name = view.findViewById(R.id.name);
        gender = view.findViewById(R.id.gender);
        age = view.findViewById(R.id.age);
        heightDisplay = view.findViewById(R.id.height_display);
        weightDisplay = view.findViewById(R.id.weight_display);
        stepGoalDisplay = view.findViewById(R.id.step_goal_display);
        bmiDisplay = view.findViewById(R.id.bmi_display);
        bodyFatPercentageDisplay = view.findViewById(R.id.body_fat_percentage_display);
        muscleMassDisplay = view.findViewById(R.id.muscle_mass_display);

        editHeight = view.findViewById(R.id.edit_height);
        editWeight = view.findViewById(R.id.edit_weight);
        editStepGoal = view.findViewById(R.id.edit_step_goal);
        editBodyFatPercentage = view.findViewById(R.id.edit_body_fat_percentage);
        editMuscleMass = view.findViewById(R.id.edit_muscle_mass);

        editHeightButton = view.findViewById(R.id.edit_height_button);
        editWeightButton = view.findViewById(R.id.edit_weight_button);
        editStepGoalButton = view.findViewById(R.id.edit_step_goal_button);
        editBodyFatButton = view.findViewById(R.id.edit_body_fat_button);
        editMuscleMassButton = view.findViewById(R.id.edit_muscle_mass_button);
        saveButton = view.findViewById(R.id.save_button);
        logoutButton = view.findViewById(R.id.logout_button);

        // Initialize Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users").child(userId);
        bodyMeasurementsRef = database.getReference("BodyMeasurements").child(userId);

        // Fetch data from Firebase
        fetchUserData(userId);

        // Set up button click listeners
        setUpButtonListeners();

        workoutViewModel.getPhotoUri().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                    userPhoto.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void fetchUserData(String userId) {
        Log.d(TAG, "Fetching user data.");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userGender = snapshot.child("gender").getValue(String.class);
                    Long userAge = snapshot.child("age").getValue(Long.class);
                    Long userStepGoal = snapshot.child("step_goal").getValue(Long.class);

                    name.setText(userName);
                    gender.setText(userGender);
                    age.setText(userAge != null ? String.valueOf(userAge) : "N/A");
                    stepGoalDisplay.setText(userStepGoal != null ? String.valueOf(userStepGoal) + " steps" : "N/A");

                    // Fetch body measurements
                    fetchBodyMeasurements();
                } else {
                    Log.e(TAG, "User data snapshot does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch user data.", error.toException());
                Toast.makeText(getContext(), "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBodyMeasurements() {
        Log.d(TAG, "Fetching body measurements.");
        bodyMeasurementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long userHeight = snapshot.child("height").getValue(Long.class);
                    Long userWeight = snapshot.child("weight").getValue(Long.class);
                    Float bodyFat = snapshot.child("body_fat_percentage").getValue(Float.class);
                    Float muscleMass = snapshot.child("muscle_mass").getValue(Float.class);

                    heightDisplay.setText(userHeight != null ? String.valueOf(userHeight) + " cm" : "N/A");
                    weightDisplay.setText(userWeight != null ? String.valueOf(userWeight) + " kg" : "N/A");
                    bodyFatPercentageDisplay.setText(bodyFat != null ? String.valueOf(bodyFat) + "%" : "N/A");
                    muscleMassDisplay.setText(muscleMass != null ? String.valueOf(muscleMass) + " kg" : "N/A");

                    // Calculate BMI locally
                    if (userHeight != null && userWeight != null) {
                        float heightInMeters = userHeight / 100f;
                        float bmi = userWeight / (heightInMeters * heightInMeters);
                        bmiDisplay.setText(String.format(Locale.getDefault(), "BMI: %.2f", bmi));
                        updateBmiColor(bmi);
                    } else {
                        bmiDisplay.setText("N/A");
                    }
                } else {
                    Log.e(TAG, "Body measurements snapshot does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch body measurements.", error.toException());
                Toast.makeText(getContext(), "Failed to fetch body measurements.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpButtonListeners() {
        changePhotoButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                openImageSelector();
            }
        });

        editHeightButton.setOnClickListener(v -> toggleEditVisibility(editHeight, heightDisplay));
        editWeightButton.setOnClickListener(v -> toggleEditVisibility(editWeight, weightDisplay));
        editStepGoalButton.setOnClickListener(v -> toggleEditVisibility(editStepGoal, stepGoalDisplay));
        editBodyFatButton.setOnClickListener(v -> toggleEditVisibility(editBodyFatPercentage, bodyFatPercentageDisplay));
        editMuscleMassButton.setOnClickListener(v -> toggleEditVisibility(editMuscleMass, muscleMassDisplay));
        saveButton.setOnClickListener(v -> saveUserData());

        logoutButton.setOnClickListener(v -> logout());
    }

    private void toggleEditVisibility(EditText editText, TextView textView) {
        if (editText.getVisibility() == View.GONE) {
            editText.setVisibility(View.VISIBLE);
            editText.setText(textView.getText().toString().replaceAll("[^\\d.]", ""));
        } else {
            editText.setVisibility(View.GONE);
        }
    }

    private void saveUserData() {
        String heightText = editHeight.getText().toString();
        String weightText = editWeight.getText().toString();
        String stepGoalText = editStepGoal.getText().toString();
        String bodyFatText = editBodyFatPercentage.getText().toString();
        String muscleMassText = editMuscleMass.getText().toString();

        if (!TextUtils.isEmpty(heightText)) {
            try {
                int height = Integer.parseInt(heightText);
                bodyMeasurementsRef.child("height").setValue(height);
                heightDisplay.setText(heightText + " cm");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid height value", Toast.LENGTH_SHORT).show();
            }
        }

        if (!TextUtils.isEmpty(weightText)) {
            try {
                int weight = Integer.parseInt(weightText);
                bodyMeasurementsRef.child("weight").setValue(weight);
                weightDisplay.setText(weightText + " kg");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid weight value", Toast.LENGTH_SHORT).show();
            }
        }

        if (!TextUtils.isEmpty(stepGoalText)) {
            try {
                int stepGoal = Integer.parseInt(stepGoalText);
                userRef.child("step_goal").setValue(stepGoal);
                stepGoalDisplay.setText(stepGoalText + " steps");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid step goal value", Toast.LENGTH_SHORT).show();
            }
        }

        if (!TextUtils.isEmpty(bodyFatText)) {
            try {
                float bodyFat = Float.parseFloat(bodyFatText);
                bodyMeasurementsRef.child("body_fat_percentage").setValue(bodyFat);
                bodyFatPercentageDisplay.setText(bodyFatText + "%");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid body fat percentage value", Toast.LENGTH_SHORT).show();
            }
        }

        if (!TextUtils.isEmpty(muscleMassText)) {
            try {
                float muscleMass = Float.parseFloat(muscleMassText);
                bodyMeasurementsRef.child("muscle_mass").setValue(muscleMass);
                muscleMassDisplay.setText(muscleMassText + " kg");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid muscle mass value", Toast.LENGTH_SHORT).show();
            }
        }

        editHeight.setVisibility(View.GONE);
        editWeight.setVisibility(View.GONE);
        editStepGoal.setVisibility(View.GONE);
        editBodyFatPercentage.setVisibility(View.GONE);
        editMuscleMass.setVisibility(View.GONE);

        fetchBodyMeasurements();
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            workoutViewModel.setPhotoUri(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                userPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBmiColor(float bmi) {
        int color;
        if (bmi < 18.5) {
            color = getResources().getColor(R.color.colorUnderweight);
        } else if (bmi >= 18.5 && bmi <= 24.9) {
            color = getResources().getColor(R.color.colorNormalWeight);
        } else if (bmi >= 25 && bmi <= 29.9) {
            color = getResources().getColor(R.color.colorOverweight);
        } else {
            color = getResources().getColor(R.color.colorObese);
        }
        bmiDisplay.setTextColor(color);
    }

    private void logout() {
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), Login_Activity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
