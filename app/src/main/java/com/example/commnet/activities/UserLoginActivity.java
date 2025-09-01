package com.example.commnet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.commnet.R;
import com.google.firebase.firestore.*;
import java.util.*;

public class UserLoginActivity extends AppCompatActivity {

    EditText editUserEmail, editUserPassword;
    Button btnUserLogin;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        db = FirebaseFirestore.getInstance();
        editUserEmail = findViewById(R.id.editUserEmail);
        editUserPassword = findViewById(R.id.editUserPassword);
        btnUserLogin = findViewById(R.id.btnUserLogin);

        btnUserLogin.setOnClickListener(v -> {
            String email = editUserEmail.getText().toString().trim();
            String password = editUserPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            Toast.makeText(this, "User login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UserLoginActivity.this, InboxActivity.class);
                            intent.putExtra("userEmail", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show());
        });
    }
}
