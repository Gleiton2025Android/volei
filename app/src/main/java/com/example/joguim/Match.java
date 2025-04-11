package com.example.joguim;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Match {
    private long id;
    private Date dateTime;
    private String location;
    private String description;
    private MatchStatus status;

    public enum MatchStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    public Match(Date dateTime, String location, String description) {
        this.dateTime = dateTime;
        this.location = location;
        this.description = description;
        this.status = MatchStatus.PENDING;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getFormattedDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(dateTime);
    }
} 