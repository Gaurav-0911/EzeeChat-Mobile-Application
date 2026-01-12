package com.example.ezchat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.ViewHolder> {

    private Context context;
    private ArrayList<Users> usersArrayList;

    public UserAdpter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = usersArrayList.get(position);

        // ✅ Null safety checks
        String name = users.getUsername();
        String status = users.getStatus();

        if (name == null || name.trim().isEmpty()) {
            name = "Unknown User";
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Hey there! I’m using EZChat";
        }

        holder.username.setText(name);
        holder.userstatus.setText(status);

        // ✅ Optional: Last message दिखाना
        if (users.getLastMessage() != null && !users.getLastMessage().isEmpty()) {
            holder.lastMessage.setText(users.getLastMessage());
        } else {
            holder.lastMessage.setText("No messages yet");
        }

        // ✅ Chat Window open on item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, chatWin.class);
            intent.putExtra("nameeee", users.getUsername());
            intent.putExtra("uid", users.getUserId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList != null ? usersArrayList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, userstatus, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}
