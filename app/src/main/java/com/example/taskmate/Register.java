package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

public class Register extends AppCompatActivity {

    EditText username, password, confirmPassword;
    Button registerButton;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbHelper = new DBHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmpassword);
        registerButton = findViewById(R.id.loginButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameText = username.getText().toString();
                String passwordText = password.getText().toString();
                String confirmPasswordText = confirmPassword.getText().toString();
                if (usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                    Snackbar.make(v, "Please fill all fields", Snackbar.LENGTH_LONG).show();
                    return;
                }

                    if (!passwordText.equals(confirmPasswordText)) {
                        Snackbar.make(v, "Passwords do not match", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    String hashedPassword = PasswordUtil.hashPassword(password.getText().toString());
                    User currentUser = new User(usernameText,hashedPassword);
                    long result = dbHelper.registerUser(currentUser);
                    if (result == -1) {
                        Snackbar.make(v, "Username already exists", Snackbar.LENGTH_LONG).show();
                    }
                        else {
                        Snackbar.make(v, "Registration successful", Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

            }
            });

    }
}