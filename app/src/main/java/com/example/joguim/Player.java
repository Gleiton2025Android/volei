package com.example.joguim;

public class Player {
    private long id;
    private String name;
    private Position position;
    private int level;

    public enum Position {
        LEVANTADOR("Levantador"),
        OPOSTO("Oposto"),
        PONTEIRO("Ponteiro"),
        LIBERO("Líbero");

        private final String displayName;

        Position(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Player(String name, Position position, int level) {
        this.name = name;
        this.position = position;
        this.level = level;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
} 