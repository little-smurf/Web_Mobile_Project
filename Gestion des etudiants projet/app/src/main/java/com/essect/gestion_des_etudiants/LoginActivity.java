package com.essect.gestion_des_etudiants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth ;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);
 sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(v -> loginUser());
        firebaseAuth = FirebaseAuth.getInstance();
        String role = sharedPreferences.getString("Role", "");
        String userID = sharedPreferences.getString("UserID", "");

        if (!TextUtils.isEmpty(role) && !TextUtils.isEmpty(userID)) {
            if (role.equals("student")) {
                // Navigate to StudentDashboard
                Intent intent = new Intent(this, StudentDashboard.class);

                startActivity(intent);
                finish(); // Optional: Prevents going back to LoginActivity when pressing the back button
            } else if (role.equals("teacher")) {
                Intent intent = new Intent(this, TeacherDashboard.class);
                startActivity(intent);
                finish(); // Optional: Prevents going back to LoginActivity when pressing the back button
            }
        } else {
            // Handle the case where role or userID is null or empty
            // For example, redirect back to the login page or show an error message
        }

    }
    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                     sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        if (storedPassword.equals(password)) {
                            String role = userSnapshot.child("role").getValue(String.class);
                            if (role != null) {
                                if (role.equals("student")) {
                                    Toast.makeText(LoginActivity.this, "Student login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, StudentDashboard.class);
                                    editor.putString("UserID",userSnapshot.getKey());
                                    editor.putString("Role",role);
                                    editor.apply();
                                    View loadingLayout = LayoutInflater.from(LoginActivity.this).inflate(R.layout.loading_layout, null);
// Add loadingLayout to the activity's layout
                                    ViewGroup rootView = findViewById(android.R.id.content);
                                    rootView.addView(loadingLayout);

// Display the loading layout
                                    loadingLayout.setVisibility(View.VISIBLE);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Hide the loading layout after 3 seconds
                                            loadingLayout.setVisibility(View.GONE);
                                            // Start the TeacherDashboard activity after hiding the loading layout
                                            startActivity(intent);
                                        }
                                    }, 3000);
// Inside the successful login block
                                    editTextUsername.setText(""); // Clear username field
                                    editTextPassword.setText(""); // Clear password field

                                } else if (role.equals("teacher")) {
                                    Toast.makeText(LoginActivity.this, "Teacher login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, TeacherDashboard.class);
                                        editor.putString("UserID",userSnapshot.getKey());
                                    editor.putString("Role",role);
                                    editor.apply();
                                    View loadingLayout = LayoutInflater.from(LoginActivity.this).inflate(R.layout.loading_layout, null);
// Add loadingLayout to the activity's layout
                                    ViewGroup rootView = findViewById(android.R.id.content);
                                    rootView.addView(loadingLayout);

// Display the loading layout
                                    loadingLayout.setVisibility(View.VISIBLE);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Hide the loading layout after 3 seconds
                                            loadingLayout.setVisibility(View.GONE);
                                            // Start the TeacherDashboard activity after hiding the loading layout
                                            startActivity(intent);
                                        }
                                    }, 3000);
                                    // Inside the successful login block
                                    editTextUsername.setText(""); // Clear username field
                                    editTextPassword.setText(""); // Clear password field

                                } else {
                                    Toast.makeText(LoginActivity.this, "Admin login successful", Toast.LENGTH_SHORT).show();
                                    // Handle other roles or scenarios
                                }
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Your password is incorrect!", Toast.LENGTH_SHORT).show();
                            // Password doesn't match
                        }
                    }
                } else {
                    Log.d("MyTag", "No user found");
                    // No user found with provided username
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("MyTag", "Database error: " + databaseError.getMessage());
                // Handle errors or onCancelled event
            }
        });
    }
}