package com.essect.gestion_des_etudiants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboard extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    private Spinner groupSpinner;
    private DatabaseReference databaseReference;
    private String UserID;
    @Override
    public void onBackPressed() {
        // Redirect to the device's home screen
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private DatePicker datePicker;

    private String selectedGroup;
    private String subjectID;

    private TableLayout tableLayout; // Add this member variable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacherdashboard);
        sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

        UserID = sharedPreferences.getString("UserID", "");
        // Define your radio group and submit button

        ImageView imageLogout = findViewById(R.id.imageLogout);
        imageLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the home page (replace HomeActivity with your actual home page activity)


                // Clear shared preferences data
                SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(TeacherDashboard.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        groupSpinner = findViewById(R.id.groupSpinner);

// Within onCreate or wherever suitable
        tableLayout = findViewById(R.id.studentsTableLayout);
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Populate the group spinner
        populateGroupSpinner();

        // Set up listener for group selection
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 selectedGroup = parent.getItemAtPosition(position).toString();
                // Fetch and display students for the selected group
                displayStudentsForSelectedGroup(selectedGroup);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle when nothing is selected
            }
        });
    }

    private void populateGroupSpinner() {
        DatabaseReference classesRef = databaseReference.child("classes");

        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> groupList = new ArrayList<>();

                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    String className = classSnapshot.child("className").getValue(String.class);

                    // Check if the teacher is associated with this class
                    DataSnapshot subjectsSnapshot = classSnapshot.child("subjects");
                    for (DataSnapshot subjectSnapshot : subjectsSnapshot.getChildren()) {
                        String teacherID = subjectSnapshot.getValue(String.class);
                        if (teacherID.equals(UserID)) {
                            subjectID = subjectSnapshot.getKey();
                            groupList.add(className);
                            break; // Exit the loop after finding the class
                        }
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(TeacherDashboard.this,
                        android.R.layout.simple_spinner_item, groupList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                groupSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
    private void displayStudentsForSelectedGroup(String selectedGroup) {
        DatabaseReference classesRef = databaseReference.child("classes");

        classesRef.orderByChild("className").equalTo(selectedGroup).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot selectedClassSnapshot = null;
                    for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                        selectedClassSnapshot = classSnapshot;
                        break; // Assuming class names are unique, exit after finding the match
                    }

                    if (selectedClassSnapshot != null) {
                        DataSnapshot studentsSnapshot = selectedClassSnapshot.child("students");
                        DatabaseReference usersRef = databaseReference.child("users"); // Reference to 'users' node

                        TableLayout studentsTableLayout = findViewById(R.id.studentsTableLayout);
// Reference to 'users' node
                        TableRow headerRow = new TableRow(TeacherDashboard.this);
                        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                        headerRow.setLayoutParams(params);

                        // Create TextViews for column headers
                        TextView cinHeader = new TextView(TeacherDashboard.this);
                        cinHeader.setText("Student CIN");
                        cinHeader.setPadding(8, 8, 8, 25);
                        cinHeader.setTextColor(Color.RED);

                        TextView nameHeader = new TextView(TeacherDashboard.this);
                        nameHeader.setText("Student Full Name");
                        nameHeader.setPadding(8, 8, 8, 25);
                        nameHeader.setTextColor(Color.RED);
                        // Add TextViews to header row
                        headerRow.addView(cinHeader);
                        headerRow.addView(nameHeader);

                        // Add the header row to the table layout
                        studentsTableLayout.addView(headerRow);
                        for (DataSnapshot studentSnapshot : studentsSnapshot.getChildren()) {
                            String studentID = studentSnapshot.getKey();
                            Log.d("student", studentID);

                            usersRef.child(studentID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        String studentCIN = userSnapshot.child("CIN").getValue(String.class);
                                        String studentUsername = userSnapshot.child("username").getValue(String.class);

                                        // Create a new row

                                        TableRow row = new TableRow(TeacherDashboard.this);

                                        // Create TextViews for CIN and username
                                        TextView cinTextView = new TextView(TeacherDashboard.this);
                                        cinTextView.setText(studentCIN);
                                        cinTextView.setTextColor(Color.BLACK);
                                        TextView usernameTextView = new TextView(TeacherDashboard.this);
                                        usernameTextView.setText(studentUsername);
                                        usernameTextView.setTextColor(Color.BLACK);

                                        // Add TextViews to the row
                                        row.addView(cinTextView);
                                        row.addView(usernameTextView);

                                        // Add onClickListener to the row
                                        row.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Handle row click: Show popup
                                                showPopup(studentCIN, studentUsername);
                                            }
                                        });

                                        // Add the row to the TableLayout
                                        tableLayout.addView(row);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }
                    }
                } else {
                    // Handle the case where the selected group doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Method to display a popup
    private void showPopup(String StudentID, String username) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TeacherDashboard.this);
        View view = getLayoutInflater().inflate(R.layout.popup_layout, null); // Replace with your popup layout
        Button submitButton = view.findViewById(R.id.submitButton);
        RadioGroup radioGroupPA = view.findViewById(R.id.radioGroupPA);
         datePicker = view.findViewById(R.id.datePicker);
// Initially disable the Submit button
        submitButton.setEnabled(false);

// Add a listener to the RadioGroup
        radioGroupPA.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Enable the Submit button when a radio button is selected
                submitButton.setEnabled(true);
            }
        });
        // Access views in your popup layout
        TextView usernameTextView = view.findViewById(R.id.popupUsername);
        Button closeButton = view.findViewById(R.id.closeButton);

        usernameTextView.setText(username);
        usernameTextView.setTextColor(Color.WHITE);

        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss(); // Dismiss the popup
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected date from the DatePicker
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();

                String selectedDate = year + "-" + month + "-" + day;
                String status = "";
                int selectedId = radioGroupPA.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    if (selectedId == R.id.radioButtonP) {
                        status = "P";
                    } else if (selectedId == R.id.radioButtonA) {
                        status = "A";
                    }
                }
                // Perform actions with the selected date
                Log.d("SelectedDate", selectedDate);
                addOrUpdateAbsence(StudentID,subjectID,status,selectedDate);
                View loadingLayout = LayoutInflater.from(TeacherDashboard.this).inflate(R.layout.loading_layout, null);
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

                    }

                }, 3000);

                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void addOrUpdateAbsence(String studentCIN, String subjectID, String status, String date) {
        DatabaseReference usersRef = databaseReference.child("users");
        Log.d("data",studentCIN);
        Log.d("data",subjectID);
        Log.d("data",status);
        Log.d("data",date);
        usersRef.orderByChild("CIN").equalTo(studentCIN).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String studentKey = userSnapshot.getKey();

                        DatabaseReference absencesRef = databaseReference.child("absences").child(studentKey);

                        // Check if the student exists in the absences table
                        absencesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot absenceSnapshot) {
                                if (!absenceSnapshot.exists()) {
                                    // Student does not exist in absences table, create a new entry
                                    absencesRef.child(subjectID).child(date).setValue(status);
                                } else {
                                    // Student exists, check if the subject exists for the student
                                    if (!absenceSnapshot.hasChild(subjectID)) {
                                        // Subject doesn't exist, create a new entry for the subject
                                        absencesRef.child(subjectID).child(date).setValue(status);
                                    } else {
                                        // Subject exists, add or update the date and status
                                        absencesRef.child(subjectID).child(date).setValue(status);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle onCancelled
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }
}
