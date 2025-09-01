package com.example.commnet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commnet.R;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button btnAdminLogin = findViewById(R.id.btnAdminLogin), btnUserLogin = findViewById(R.id.btnUserLogin);

        btnAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        btnUserLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, UserLoginActivity.class));
        });
    }
}
