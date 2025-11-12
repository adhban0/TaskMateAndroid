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

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button loginButton;
    TextView signUpText;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbHelper = new DBHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signupText);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameText = username.getText().toString();
                String passwordText = password.getText().toString();
                if (usernameText.isEmpty() || passwordText.isEmpty() ) {
                    Snackbar.make(v, "Please fill all fields", Snackbar.LENGTH_LONG).show();
                    return;
                }
                    String hashedPassword =  PasswordUtil.hashPassword(password.getText().toString());
                    User currentUser = new User(usernameText,hashedPassword);
                    if (dbHelper.validateUser(currentUser)) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("username", usernameText);
                        startActivity(intent);
                        finish();
                    } else {
                        Snackbar.make(v, "Wrong username or password", Snackbar.LENGTH_LONG).show();
                    }
                }



        });
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
                finish();
            }
        });

    }
}