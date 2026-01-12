package com.example.ezchat;

public class Users {
    private String mail;
    private String username;
    private String password;
    private String userId;
    private String status;

    // 🔹 Firebase के लिए खाली constructor जरूरी
    public Users() {
    }

    // 🔹 Optional Constructor
    public Users(String mail, String username, String password, String userId, String status) {
        this.mail = mail;
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.status = status;
    }
    private String lastMessage;

    // 🔹 Getters and Setters
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter for lastMessage
    public String getLastMessage() {
        return lastMessage;
    }

    // Setter (optional)
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

}
