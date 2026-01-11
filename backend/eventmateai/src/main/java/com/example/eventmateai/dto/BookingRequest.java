package com.example.eventmateai.dto;

public class BookingRequest {

    private Long userId;
    private Long eventId;
    private Integer tickets;
    private String paymentMode;
    private String status;
    private String eventDateTime;
    private String seats;

    public String getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(String eventDateTime) { this.eventDateTime = eventDateTime; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Integer getTickets() { return tickets; }
    public void setTickets(Integer tickets) { this.tickets = tickets; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }
}