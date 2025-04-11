package com.example.joguim;

import java.util.List;
import java.util.ArrayList;

public class Team {
    private long id;
    private long matchId;
    private List<Player> players;
    private int teamNumber;

    public Team(long matchId, int teamNumber) {
        this.matchId = matchId;
        this.teamNumber = teamNumber;
        this.players = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMatchId() {
        return matchId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public double getAverageLevel() {
        if (players.isEmpty()) return 0;
        
        int totalLevel = 0;
        for (Player player : players) {
            totalLevel += player.getLevel();
        }
        return (double) totalLevel / players.size();
    }
} 