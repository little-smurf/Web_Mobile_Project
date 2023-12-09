package com.essect.gestion_des_etudiants;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        Button btnAddStudents = findViewById(R.id.btnAddStudents);
        Button btnAddTeachers = findViewById(R.id.btnAddTeachers);

        btnAddStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add students functionality from CSV file
                // Implement the logic to read and add students from the CSV file
            }
        });

        btnAddTeachers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add teachers functionality from CSV file
                // Implement the logic to read and add teachers from the CSV file
            }
        });
    }
}