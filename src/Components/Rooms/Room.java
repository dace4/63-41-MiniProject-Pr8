package Components.Rooms;

import Components.Reservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Room {
    private int roomNumber;
    private RoomCategory roomCategory;
    private RoomStatus status;
    private List<String> amenities;
    private List<Reservation> reservations;
    private ReadWriteLock lock;
    private float price;

    public Room(int roomNumber, RoomCategory roomCategory, RoomStatus status, float price) {
        this.roomNumber = roomNumber;
        this.roomCategory = roomCategory;
        this.status = status;
        this.amenities = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
        this.price = price;
    }

    public Room(int roomNumber, RoomCategory roomCategory, RoomStatus status, List<String> amenities, float price) {
        this.roomNumber = roomNumber;
        this.roomCategory = roomCategory;
        this.status = status;
        this.amenities = new ArrayList<>(amenities);
        this.reservations = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
        this.price = price;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getRoomCategory() {
        return roomCategory;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public List<String> getAmenities() {
        return Collections.unmodifiableList(amenities);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public float getPrice() {
        return price;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void addAmenity(String amenity) {
        amenities.add(amenity);
    }

    public boolean isAvailable(LocalDate requestedCheckIn, LocalDate requestedCheckOut) {
        if (status == RoomStatus.OutOfService || status == RoomStatus.Cleaning) {
            return false;
        }

        for (Reservation reservation : reservations) {
            if (reservation.getReservationStatus() != Components.ReservationStatus.Cancelled
                    && reservation.overlaps(requestedCheckIn, requestedCheckOut)) {
                return false;
            }
        }

        return true;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomCategory=" + roomCategory +
                ", status=" + status +
                ", price=" + price +
                ", amenities=" + amenities +
                '}';
    }
}
