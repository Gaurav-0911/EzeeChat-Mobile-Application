package com.example.ezchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class setting extends AppCompatActivity {

    EditText settingname, settingstatus;
    Button donebutt;
    ImageView settingprofile;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Hide ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        settingname = findViewById(R.id.settingname);
        settingstatus = findViewById(R.id.settingstatus);
        donebutt = findViewById(R.id.donebutt);
        settingprofile = findViewById(R.id.settingprofile);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("user").child(auth.getUid());

        donebutt.setOnClickListener(v -> {
            String name = settingname.getText().toString().trim();
            String status = settingstatus.getText().toString().trim();

            if (name.isEmpty() || status.isEmpty()) {
                Toast.makeText(setting.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> update = new HashMap<>();
            update.put("username", name);
            update.put("status", status);

            reference.updateChildren(update)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(setting.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        // ✅ Go back to main screen after update
                        Intent intent = new Intent(setting.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(setting.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
