package com.example.ezchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registration extends AppCompatActivity {
    TextView loginbutton;
    EditText rg_username, rg_email, rg_password, rg_rePassword;
    Button rg_signup;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        loginbutton = findViewById(R.id.loginbutton);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_rePassword = findViewById(R.id.rgrePassword);
        rg_signup = findViewById(R.id.signupbutton);

        loginbutton.setOnClickListener(v -> {
            startActivity(new Intent(registration.this, login.class));
            finish();
        });

        rg_signup.setOnClickListener(v -> {
            String name = rg_username.getText().toString();
            String email = rg_email.getText().toString();
            String pass = rg_password.getText().toString();
            String rePass = rg_rePassword.getText().toString();
            String status = "Hey, I'm using this Application";

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(rePass)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!name.matches("[a-zA-Z ]+")) {
                rg_username.setError("Invalid username ");
                rg_username.requestFocus();
                return;
            }

            if (!email.matches(emailPattern)) {
                rg_email.setError("Invalid Email");
                return;
            }
            if (pass.length() < 6) {
                rg_password.setError("Password too short");
                return;
            }
            if (!pass.equals(rePass)) {
                rg_rePassword.setError("Passwords do not match");
                return;
            }

            progressDialog.show();

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String id = task.getResult().getUser().getUid();
                    DatabaseReference reference = database.getReference().child("user").child(id);

                    Users users = new Users(email, name, pass, id, status);

                    reference.setValue(users).addOnCompleteListener(dbTask -> {
                        progressDialog.dismiss();
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, login.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Database Error: " + dbTask.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
