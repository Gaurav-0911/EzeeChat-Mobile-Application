package com.example.ezchat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    RecyclerView mainUserRecyclerView;
    UserAdpter adapter;
    ArrayList<Users> usersArrayList;
    ImageView imglogout, settingButton;
    private Dialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        imglogout = findViewById(R.id.logoutimg);
        settingButton = findViewById(R.id.settingBut);

        usersArrayList = new ArrayList<>();
        adapter = new UserAdpter(this, usersArrayList);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainUserRecyclerView.setAdapter(adapter);

        DatabaseReference reference = database.getReference().child("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);

                    // ✅ Null safety added here
                    if (users != null && users.getUserId() != null && auth.getCurrentUser() != null) {
                        if (!users.getUserId().equals(auth.getCurrentUser().getUid())) {

                            // ✅ Fetch last message from chats
                            String senderRoom = auth.getUid() + users.getUserId();
                            DatabaseReference lastMsgRef = database.getReference("chats")
                                    .child(senderRoom).child("messages");

                            lastMsgRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String lastMsg = "No messages yet";
                                    for (DataSnapshot msgSnap : snapshot.getChildren()) {
                                        msgModelclass msg = msgSnap.getValue(msgModelclass.class);
                                        if (msg != null && msg.getMessage() != null) {
                                            lastMsg = msg.getMessage();
                                        }
                                    }
                                    users.setLastMessage(lastMsg);
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("LastMessage", error.getMessage());
                                }
                            });

                            usersArrayList.add(users);
                        }
                    }
                }

                if (!isFinishing()) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imglogout.setOnClickListener(v -> showLogoutDialog());

        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);
        });

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, login.class));
            finish();
        }
    }

    private void showLogoutDialog() {
        logoutDialog = new Dialog(MainActivity.this, R.style.dialoge);
        logoutDialog.setContentView(R.layout.dialog_layout);
        Button yes = logoutDialog.findViewById(R.id.yesbnt);
        Button no = logoutDialog.findViewById(R.id.nobnt);

        yes.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, login.class));
            finish();
        });

        no.setOnClickListener(view -> logoutDialog.dismiss());
        logoutDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
        }
    }
}
