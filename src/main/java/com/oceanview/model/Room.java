package com.oceanview.model;

import java.math.BigDecimal;

/**
 * Room model.
 */
public class Room {

    private int roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal ratePerNight;
    private int maxGuests;
    private boolean available;

    public Room() {}

    public Room(String roomNumber, String roomType, BigDecimal ratePerNight, int maxGuests) {
        this.roomNumber    = roomNumber;
        this.roomType      = roomType;
        this.ratePerNight  = ratePerNight;
        this.maxGuests     = maxGuests;
        this.available     = true;
    }

    // Getters & Setters
    public int getRoomId()                        { return roomId; }
    public void setRoomId(int id)                 { this.roomId = id; }

    public String getRoomNumber()                 { return roomNumber; }
    public void setRoomNumber(String n)           { this.roomNumber = n; }

    public String getRoomType()                   { return roomType; }
    public void setRoomType(String t)             { this.roomType = t; }

    public BigDecimal getRatePerNight()           { return ratePerNight; }
    public void setRatePerNight(BigDecimal r)     { this.ratePerNight = r; }

    public int getMaxGuests()                     { return maxGuests; }
    public void setMaxGuests(int m)               { this.maxGuests = m; }

    public boolean isAvailable()                  { return available; }
    public void setAvailable(boolean a)           { this.available = a; }

    @Override
    public String toString() {
        return roomNumber + " (" + roomType + ") - LKR " + ratePerNight + "/night";
    }
}
