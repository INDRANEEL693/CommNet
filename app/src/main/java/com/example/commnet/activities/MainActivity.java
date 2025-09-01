package com.example.commnet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.commnet.R;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        registerBtn.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
