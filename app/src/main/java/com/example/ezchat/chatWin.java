package com.example.ezchat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar; // <-- make sure material dependency exists
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class chatWin extends AppCompatActivity {

    String reciverUid, reciverName, SenderUID;
    TextView reciverNName;
    CardView sendbtn;
    EditText textmsg;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;

    String senderRoom, reciverRoom;

    RecyclerView messageRecycler;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter messagesAdpter;

    DatabaseReference chatreference;
    ValueEventListener chatListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_win);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        messageRecycler = findViewById(R.id.msgadpter);
        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);

        messageRecycler.setClipToPadding(false);
        messageRecycler.setPadding(0, 0, 0, 120);

        reciverName = getIntent().getStringExtra("nameeee");
        reciverUid = getIntent().getStringExtra("uid");
        SenderUID = firebaseAuth.getUid();

        reciverNName.setText(reciverName);

        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        messagesArrayList = new ArrayList<>();
        messagesAdpter = new messagesAdpter(chatWin.this, messagesArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // ✅ Always show bottom
        messageRecycler.setLayoutManager(linearLayoutManager);
        messageRecycler.setAdapter(messagesAdpter);

        // ✅ Firebase listener
        chatreference = database.getReference("chats").child(senderRoom).child("messages");
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Preserve old size to detect newly added messages
                int oldSize = messagesArrayList.size();

                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    if (messages != null) {
                        messagesArrayList.add(messages);
                    }
                }

                final int newSize = messagesArrayList.size();

                runOnUiThread(() -> {
                    messagesAdpter.notifyDataSetChanged();
                    // ✅ Scroll to bottom every time messages update
                    if (messagesArrayList.size() > 0)
                        messageRecycler.scrollToPosition(messagesArrayList.size() - 1);
                });

                // Check for newly added messages (if any)
                if (newSize > oldSize) {
                    // iterate over new messages which were not previously present
                    for (int i = oldSize; i < newSize; i++) {
                        if (i >= 0 && i < messagesArrayList.size()) {
                            msgModelclass newMsg = messagesArrayList.get(i);
                            if (newMsg != null) {
                                // If the message is from OTHER user, show in-app notification (Snackbar)
                                if (!SenderUID.equals(newMsg.getSenderid())) {
                                    showInAppNotification(newMsg);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(chatWin.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", error.getMessage());
            }
        };
        chatreference.addValueEventListener(chatListener);

        // ✅ Send message
        sendbtn.setOnClickListener(v -> {
            String message = textmsg.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(chatWin.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                return;
            }

            textmsg.setText("");
            msgModelclass messagess = new msgModelclass(message, SenderUID, new Date().getTime());

            DatabaseReference senderRef = database.getReference("chats")
                    .child(senderRoom).child("messages");
            DatabaseReference receiverRef = database.getReference("chats")
                    .child(reciverRoom).child("messages");

            senderRef.push().setValue(messagess).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    receiverRef.push().setValue(messagess);
                }
            });
        });

    }

    // In-app notification using Snackbar (appears at bottom above UI)
    private void showInAppNotification(msgModelclass newMsg) {
        try {
            String content = newMsg.getMessage();
            String title = reciverName != null ? reciverName : "New Message";

            // Using messageRecycler as parent view for Snackbar
            Snackbar snackbar = Snackbar.make(messageRecycler, title + ": " + content, Snackbar.LENGTH_LONG);
            snackbar.setAction("Open", v -> {
                // Already in chat activity — scroll to bottom when user taps Open
                if (messagesArrayList.size() > 0)
                    messageRecycler.scrollToPosition(messagesArrayList.size() - 1);
            });
            snackbar.show();
        } catch (Exception e) {
            Log.e("InAppNotifError", e.getMessage() == null ? "err" : e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatreference != null && chatListener != null) {
            chatreference.removeEventListener(chatListener);
        }
    }
}
