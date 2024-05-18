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
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NutritionsFragment extends Fragment {

    private static final String TAG = "NutritionFragment";

    private AutoCompleteTextView foodDropdown;
    private EditText quantityEditText;
    private Button addButton;
    private TextView summaryTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Map<String, Food> foodData;
    private String userId;

    // Food data should be populated with actual values (calories, proteins, fats, carbs)
    private void initializeFoodData() {
        foodData = new HashMap<>();
        foodData.put("Roti", new Food(100, 2, 0.4, 20, 1));
        foodData.put("Apple", new Food(50, 0.3, 0.2, 14, 1));
        foodData.put("Chicken Biryani", new Food(340, 26, 15, 25, 1));
        foodData.put("Chapati", new Food(70, 3, 1, 13, 1));
        foodData.put("Chicken Curry", new Food(240, 23, 12, 8, 1));
        foodData.put("Pakistani Pulao", new Food(400, 14, 15, 50, 1));
        foodData.put("Beef Karahi", new Food(300, 25, 17, 10, 1));
        foodData.put("Chicken Tikka", new Food(150, 20, 8, 4, 1));
        foodData.put("Beef Seekh Kebab", new Food(250, 18, 15, 7, 1));
        foodData.put("Vegetable Biryani", new Food(300, 10, 12, 30, 1));
        foodData.put("Lamb Biryani", new Food(400, 22, 18, 25, 1));
        foodData.put("Shami Kebab", new Food(150, 10, 8, 6, 1));
        foodData.put("Nihari", new Food(400, 30, 25, 10, 1));
        foodData.put("Halwa Puri", new Food(500, 10, 25, 65, 1));
        foodData.put("Aloo Paratha", new Food(250, 5, 15, 30, 1));
        foodData.put("Samosa", new Food(150, 5, 10, 15, 1));
        foodData.put("Chicken Karahi", new Food(280, 22, 14, 12, 1));
        foodData.put("Mutton Karahi", new Food(320, 28, 16, 10, 1));
        foodData.put("Beef Biryani", new Food(350, 20, 18, 20, 1));
        foodData.put("Chana Chaat", new Food(200, 7, 8, 30, 1));
        foodData.put("Dahi Bhalla", new Food(180, 5, 10, 20, 1));
        foodData.put("Palak Paneer", new Food(200, 10, 15, 8, 1));
        foodData.put("Mutton Pulao", new Food(400, 18, 20, 35, 1));
        foodData.put("Bhindi Masala", new Food(120, 4, 8, 15, 1));
        foodData.put("Fried Fish", new Food(200, 18, 12, 6, 1));
        foodData.put("Daal Chawal", new Food(300, 15, 10, 40, 1));
        foodData.put("Kheer", new Food(250, 5, 15, 30, 1));
        foodData.put("Gajar Ka Halwa", new Food(300, 8, 20, 25, 1));
        foodData.put("Chicken Roll", new Food(250, 15, 10, 20, 1));
        foodData.put("Beef Burger", new Food(400, 25, 20, 30, 1));
        foodData.put("Vegetable Soup", new Food(100, 3, 2, 15, 1));
        foodData.put("Mixed Fruit Salad", new Food(120, 2, 0.5, 30, 1));
        foodData.put("Omelette", new Food(150, 13, 10, 1, 1));
        foodData.put("Lassi", new Food(100, 6, 4, 12, 1));
        foodData.put("Fruit Chaat", new Food(150, 2, 0.5, 35, 1));
        foodData.put("Chapli Kebab", new Food(200, 16, 12, 8, 1));
        foodData.put("Achar Gosht", new Food(300, 20, 15, 10, 1));
        foodData.put("Chicken Sandwich", new Food(250, 18, 10, 25, 1));
        foodData.put("Beef Kofta", new Food(220, 18, 14, 6, 1));
        foodData.put("Paya", new Food(400, 30, 25, 8, 1));
        foodData.put("Fried Chicken", new Food(300, 20, 15, 10, 1));
        foodData.put("Fried Rice", new Food(250, 10, 8, 30, 1));
        foodData.put("Chicken Soup", new Food(150, 12, 6, 10, 1));
        foodData.put("Shawarma", new Food(300, 20, 12, 25, 1));
        foodData.put("Pulao", new Food(300, 15, 10, 35, 1));
        foodData.put("Mango Shake", new Food(200, 5, 6, 30, 1));
        foodData.put("Pasta", new Food(300, 15, 8, 40, 1));
        foodData.put("Chicken Tandoori", new Food(200, 22, 10, 5, 1));
        foodData.put("Fish Curry", new Food(250, 18, 12, 8, 1));
        foodData.put("Beef Stew", new Food(300, 25, 15, 10, 1));
        foodData.put("Chicken Shawarma", new Food(280, 18, 12, 20, 1));
        foodData.put("Fruit Smoothie", new Food(150, 5, 2, 30, 1));

        // Add more food items here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);

        try {
            foodDropdown = view.findViewById(R.id.foodDropdown);
            quantityEditText = view.findViewById(R.id.quantityEditText);
            addButton = view.findViewById(R.id.addButton);
            summaryTextView = view.findViewById(R.id.summaryTextView);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            userId = mAuth.getCurrentUser().getUid();

            initializeFoodData();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, new ArrayList<>(foodData.keySet()));
            foodDropdown.setAdapter(adapter);

            addButton.setOnClickListener(v -> addFoodIntake());

            // Fetch and display existing nutrition data
            fetchAndDisplayExistingData();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing view: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing view", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void addFoodIntake() {
        try {
            String selectedFood = foodDropdown.getText().toString();
            String quantityStr = quantityEditText.getText().toString();

            if (selectedFood.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both food and quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            if (!foodData.containsKey(selectedFood)) {
                Toast.makeText(getContext(), "Food not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Food food = foodData.get(selectedFood);
            int totalCalories = food.getCalories() * quantity;
            double totalProteins = food.getProteins() * quantity;
            double totalFats = food.getFats() * quantity;
            double totalCarbs = food.getCarbs() * quantity;

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            DatabaseReference nutritionRef = mDatabase.child("nutrition").child(userId).child(date).child(selectedFood);
            nutritionRef.child("calories").setValue(totalCalories);
            nutritionRef.child("proteins").setValue(totalProteins);
            nutritionRef.child("fats").setValue(totalFats);
            nutritionRef.child("carbs").setValue(totalCarbs);
            nutritionRef.child("quantity").setValue(quantity);

            updateSummaryTextView();
        } catch (Exception e) {
            Log.e(TAG, "Error adding food intake: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error adding food intake", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndDisplayExistingData() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference userNutritionRef = mDatabase.child("nutrition").child(userId).child(date);
        userNutritionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    StringBuilder summary = new StringBuilder("Today's intake:\n");
                    for (DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                        String foodName = foodSnapshot.getKey();
                        int quantity = foodSnapshot.child("quantity").getValue(Integer.class);
                        int calories = foodSnapshot.child("calories").getValue(Integer.class);
                        double proteins = foodSnapshot.child("proteins").getValue(Double.class);
                        double fats = foodSnapshot.child("fats").getValue(Double.class);
                        double carbs = foodSnapshot.child("carbs").getValue(Double.class);

                        summary.append(foodName)
                                .append(": ")
                                .append(quantity)
                                .append(" (Calories: ")
                                .append(calories)
                                .append(", Proteins: ")
                                .append(proteins)
                                .append(", Fats: ")
                                .append(fats)
                                .append(", Carbs: ")
                                .append(carbs)
                                .append(")\n");
                    }
                    summaryTextView.setText(summary.toString());
                } else {
                    summaryTextView.setText("No intake recorded for today.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching nutrition data: " + databaseError.getMessage(), databaseError.toException());
                Toast.makeText(getContext(), "Error fetching nutrition data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummaryTextView() {
        // Update summary after adding new food intake
        fetchAndDisplayExistingData();
    }

    // Food class
    public static class Food {
        private int calories;
        private double proteins;
        private double fats;
        private double carbs;
        private int quantity;

        public Food() {
            // Default constructor required for calls to DataSnapshot.getValue(Food.class)
        }

        public Food(int calories, double proteins, double fats, double carbs, int quantity) {
            this.calories = calories;
            this.proteins = proteins;
            this.fats = fats;
            this.carbs = carbs;
            this.quantity = quantity;
        }

        public int getCalories() {
            return calories;
        }

        public double getProteins() {
            return proteins;
        }

        public double getFats() {
            return fats;
        }

        public double getCarbs() {
            return carbs;
        }

        public int getQuantity() {
            return quantity;
        }

        public void addQuantity(int quantity) {
            this.quantity += quantity;
        }
    }
}
