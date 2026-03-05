package com.oceanview.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reservation model - represents a guest booking.
 */
public class Reservation {

    private int reservationId;
    private String reservationNumber;
    private int guestId;
    private String guestName;
    private String address;
    private String contactNumber;
    private String email;
    private int roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal ratePerNight;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private BigDecimal totalAmount;
    private String status;
    private String specialRequests;
    private String createdAt;

    // ── Constructors ──────────────────────────────────────────

    public Reservation() {}

    public Reservation(String guestName, String address, String contactNumber,
                       String email, int roomId, LocalDate checkInDate,
                       LocalDate checkOutDate, int numGuests, String specialRequests) {
        this.guestName       = guestName;
        this.address         = address;
        this.contactNumber   = contactNumber;
        this.email           = email;
        this.roomId          = roomId;
        this.checkInDate     = checkInDate;
        this.checkOutDate    = checkOutDate;
        this.numGuests       = numGuests;
        this.specialRequests = specialRequests;
    }

    // ── Derived ───────────────────────────────────────────────

    public long getNumNights() {
        if (checkInDate != null && checkOutDate != null) {
            return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        return 0;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getReservationId()                    { return reservationId; }
    public void setReservationId(int id)             { this.reservationId = id; }

    public String getReservationNumber()             { return reservationNumber; }
    public void setReservationNumber(String n)       { this.reservationNumber = n; }

    public int getGuestId()                          { return guestId; }
    public void setGuestId(int id)                   { this.guestId = id; }

    public String getGuestName()                     { return guestName; }
    public void setGuestName(String n)               { this.guestName = n; }

    public String getAddress()                       { return address; }
    public void setAddress(String a)                 { this.address = a; }

    public String getContactNumber()                 { return contactNumber; }
    public void setContactNumber(String c)           { this.contactNumber = c; }

    public String getEmail()                         { return email; }
    public void setEmail(String e)                   { this.email = e; }

    public int getRoomId()                           { return roomId; }
    public void setRoomId(int id)                    { this.roomId = id; }

    public String getRoomNumber()                    { return roomNumber; }
    public void setRoomNumber(String n)              { this.roomNumber = n; }

    public String getRoomType()                      { return roomType; }
    public void setRoomType(String t)                { this.roomType = t; }

    public BigDecimal getRatePerNight()              { return ratePerNight; }
    public void setRatePerNight(BigDecimal r)        { this.ratePerNight = r; }

    public LocalDate getCheckInDate()                { return checkInDate; }
    public void setCheckInDate(LocalDate d)          { this.checkInDate = d; }

    public LocalDate getCheckOutDate()               { return checkOutDate; }
    public void setCheckOutDate(LocalDate d)         { this.checkOutDate = d; }

    public int getNumGuests()                        { return numGuests; }
    public void setNumGuests(int n)                  { this.numGuests = n; }

    public BigDecimal getTotalAmount()               { return totalAmount; }
    public void setTotalAmount(BigDecimal t)         { this.totalAmount = t; }

    public String getStatus()                        { return status; }
    public void setStatus(String s)                  { this.status = s; }

    public String getSpecialRequests()               { return specialRequests; }
    public void setSpecialRequests(String r)         { this.specialRequests = r; }

    public String getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(String c)               { this.createdAt = c; }

    @Override
    public String toString() {
        return "Reservation{" +
               "number='" + reservationNumber + '\'' +
               ", guest='" + guestName + '\'' +
               ", room='"  + roomNumber + '\'' +
               ", checkIn=" + checkInDate +
               ", checkOut=" + checkOutDate +
               ", status='" + status + '\'' +
               '}';
    }
}
