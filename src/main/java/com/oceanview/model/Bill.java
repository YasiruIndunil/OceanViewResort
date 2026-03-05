package com.oceanview.model;

import java.math.BigDecimal;

/**
 * Bill model for invoicing.
 */
public class Bill {
    private int billId;
    private int reservationId;
    private String reservationNumber;
    private String guestName;
    private String roomNumber;
    private String roomType;
    private int numNights;
    private BigDecimal roomCharge;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private boolean paid;
    private String generatedAt;

    public Bill() {}

    // Getters & Setters
    public int getBillId()                         { return billId; }
    public void setBillId(int id)                  { this.billId = id; }

    public int getReservationId()                  { return reservationId; }
    public void setReservationId(int id)           { this.reservationId = id; }

    public String getReservationNumber()           { return reservationNumber; }
    public void setReservationNumber(String n)     { this.reservationNumber = n; }

    public String getGuestName()                   { return guestName; }
    public void setGuestName(String n)             { this.guestName = n; }

    public String getRoomNumber()                  { return roomNumber; }
    public void setRoomNumber(String n)            { this.roomNumber = n; }

    public String getRoomType()                    { return roomType; }
    public void setRoomType(String t)              { this.roomType = t; }

    public int getNumNights()                      { return numNights; }
    public void setNumNights(int n)                { this.numNights = n; }

    public BigDecimal getRoomCharge()              { return roomCharge; }
    public void setRoomCharge(BigDecimal r)        { this.roomCharge = r; }

    public BigDecimal getTaxAmount()               { return taxAmount; }
    public void setTaxAmount(BigDecimal t)         { this.taxAmount = t; }

    public BigDecimal getTotalAmount()             { return totalAmount; }
    public void setTotalAmount(BigDecimal t)       { this.totalAmount = t; }

    public boolean isPaid()                        { return paid; }
    public void setPaid(boolean p)                 { this.paid = p; }

    public String getGeneratedAt()                 { return generatedAt; }
    public void setGeneratedAt(String g)           { this.generatedAt = g; }
}
