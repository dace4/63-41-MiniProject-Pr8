package Components.Reservation;

import Components.Customer;
import Components.PaymentStatus;
import Components.Rooms.Room;

import java.time.LocalDate;

public class Reservation {
    private int reservationId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Room room;
    private Customer customer;
    private PaymentStatus paymentStatus;
    private ReservationStatus reservationStatus;

    public Reservation(int reservationId, LocalDate checkInDate, LocalDate checkOutDate,
                       Room room, Customer customer,
                       PaymentStatus paymentStatus, ReservationStatus reservationStatus) {
        this.reservationId = reservationId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.room = room;
        this.customer = customer;
        this.paymentStatus = paymentStatus;
        this.reservationStatus = reservationStatus;
    }

    public int getReservationId() {
        return reservationId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public Room getRoom() {
        return room;
    }

    public Customer getCustomer() {
        return customer;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public boolean overlaps(LocalDate requestedCheckIn, LocalDate requestedCheckOut) {
        return requestedCheckIn.isBefore(this.checkOutDate) &&
                requestedCheckOut.isAfter(this.checkInDate);
    }


}