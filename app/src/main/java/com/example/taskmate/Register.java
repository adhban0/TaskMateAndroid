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
                    Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_LONG).show();
                    return;
                }

                    if (!passwordText.equals(confirmPasswordText)) {
                        Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String hashedPassword = PasswordUtil.hashPassword(password.getText().toString());
                    User currentUser = new User(usernameText,hashedPassword);
                    long result = dbHelper.registerUser(currentUser);
                    if (result == -1) {
                        Toast.makeText(Register.this, "Username already exists", Toast.LENGTH_LONG).show();
                    }
                        else {
                        Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                    }

            }
            });

    }
}