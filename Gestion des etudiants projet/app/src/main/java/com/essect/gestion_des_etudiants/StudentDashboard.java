package com.essect.gestion_des_etudiants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Inside your StudentDashboard activity
public class StudentDashboard extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    private Spinner spinnerSubjects;
    private DatabaseReference databaseReference;
    private TableLayout tableLayout;
    private ArrayList<String> attendance = new ArrayList<>();
    private int nbattendance = 0;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.etudiantactivity);
        Intent receivedIntent = getIntent();
        sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

        UserID = sharedPreferences.getString("UserID", "");

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("absences");

        // Initialize UI components
        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        tableLayout = findViewById(R.id.tableLayoutAbsenceDetails);

        ImageView imageLogout = findViewById(R.id.imageLogout);
        imageLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Clear shared preferences data
                SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Navigate to the home page (replace HomeActivity with your actual home page activity)
                Intent intent = new Intent(StudentDashboard.this, LoginActivity.class);
                startActivity(intent);
            }
                                       });

        // Set up spinner (subject dropdown menu)
        setUpSubjectSpinner();

        // Add listener to handle subject selection
        spinnerSubjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedSubject = adapterView.getItemAtPosition(position).toString();
                fetchsubjectkey(selectedSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle when nothing is selected
            }
        });

    }

    private void fetchsubjectkey(String selectedSubject) {
        DatabaseReference subjectsRef = FirebaseDatabase.getInstance().getReference().child("subjects");

        // Query to find the key of the selectedSubject
        subjectsRef.orderByChild("subjectName").equalTo(selectedSubject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Iterate through the results to get the key (should be unique)
                    String selectedSubjectKey = null;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        selectedSubjectKey = childSnapshot.getKey();
                        // Now you have the key of the selectedSubject, you can use it

                        // Proceed to fetch attendance data using the retrieved key
                    }
                    fetchAttendanceData(selectedSubjectKey,UserID);
                } else {
                    // Handle the case where the selected subject doesn't exist
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }    private void fetchAttendanceData(String selectedSubject,String UserID) {
        // Get the current user ID (you'll need to replace this with your user authentication logic)
        String currentUserID = UserID;
        attendance.clear();
        nbattendance = 0;
        databaseReference.child(currentUserID).child(selectedSubject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous data
                tableLayout.removeAllViews();

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    String status = dateSnapshot.getValue(String.class);
                    attendance.add(status);


                    // Display the date and status in the table
                    displayAttendanceData(date, status);
                }
                TextView textYourAttendance = findViewById(R.id.textYourAttendance);
                textYourAttendance.setText("Your Attendance is : " + nbattendance + "/" + attendance.size());

                TextView textStatus = findViewById(R.id.textStatus);
             String status = ((attendance.size()- nbattendance)>=7)?"CONTROLE":((attendance.size()- nbattendance)>=3)?"CONTROLE".toUpperCase():"GOOD";
                String statusText = "Your Status is: "+status;
                textStatus.setText(statusText);
                textStatus.setTextColor((status.equals("EXCLUDED"))? Color.RED:(status.equals("GOOD"))?Color.GREEN:Color.parseColor("#FFA500"));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
    private void setUpSubjectSpinner() {
        DatabaseReference subjectsRef = FirebaseDatabase.getInstance().getReference().child("subjects");

        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> subjectsList = new ArrayList<>();

                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subjectName = subjectSnapshot.child("subjectName").getValue(String.class);
                    subjectsList.add(subjectName);
                }

                // Creating an ArrayAdapter using the subjectsList
                ArrayAdapter<String> adapter = new ArrayAdapter<>(StudentDashboard.this, android.R.layout.simple_spinner_item, subjectsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Setting the adapter to the spinner
                spinnerSubjects.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Display attendance data in the table
    private void displayAttendanceData(String date, String status) {

        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        TextView dateTextView = new TextView(this);
        dateTextView.setText(date);
        dateTextView.setTextColor(Color.BLACK);
        dateTextView.setTextSize(20); // Adjust the font size as needed
        dateTextView.setPadding(32, 16, 8, 16); // Adjust padding for date text
        dateTextView.setGravity(Gravity.CENTER); // Center the date text vertically
        dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Make date take half the space
       nbattendance+=  status.equals("P") ? 1 : 0;
        TextView statusTextView = new TextView(this);
        statusTextView.setText(status);
        statusTextView.setTextColor(status.equals("P") ? Color.GREEN : Color.RED);
        statusTextView.setTextSize(20); // Adjust the font size as needed
        statusTextView.setPadding(8, 16, 32, 16); // Adjust padding for status text
        statusTextView.setGravity(Gravity.CENTER); // Center the status text vertically
        statusTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Make status take half the space

        row.addView(dateTextView);
        row.addView(statusTextView);

        tableLayout.addView(row);
    }

}
