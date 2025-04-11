package com.example.joguim;

public class User {
    private long id;
    private String username;
    private int level;
    private Player.Position position;

    public User(String username) {
        this.username = username;
        this.level = 1;
        this.position = Player.Position.PONTEIRO;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Player.Position getPosition() {
        return position;
    }

    public void setPosition(Player.Position position) {
        this.position = position;
    }
} 