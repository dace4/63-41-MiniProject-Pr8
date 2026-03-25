package Components;

import java.time.LocalDate;

public class Reservation {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Room room;
    private Customer customer;

    public Reservation(LocalDate checkInDate, LocalDate checkOutDate, Room room, Customer customer) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.room = room;
        this.customer = customer;
    }
}