package Components.Rooms;

import Components.Reservation.Reservation;
import Components.Reservation.ReservationStatus;

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
        lock.readLock().lock();
        try {
            return status;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> getAmenities() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(amenities));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Reservation> getReservations() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(reservations));
        } finally {
            lock.readLock().unlock();
        }
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public float getPrice() {
        lock.readLock().lock();
        try {
            return price;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setStatus(RoomStatus status) {
        lock.writeLock().lock();
        try {
            this.status = status;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setPrice(float price) {
        lock.writeLock().lock();
        try {
            this.price = price;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addAmenity(String amenity) {
        lock.writeLock().lock();
        try {
            amenities.add(amenity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isAvailable(LocalDate requestedCheckIn, LocalDate requestedCheckOut) {
        lock.readLock().lock();
        try {
            if (status == RoomStatus.OutOfService || status == RoomStatus.Cleaning) {
                return false;
            }

            for (Reservation reservation : reservations) {
                if (reservation.getReservationStatus() != ReservationStatus.Cancelled
                        && reservation.overlaps(requestedCheckIn, requestedCheckOut)) {
                    return false;
                }
            }

            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addReservation(Reservation reservation) {
        lock.writeLock().lock();
        try {
            reservations.add(reservation);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean tryAddReservation(Reservation reservation) {
        lock.writeLock().lock();
        try {
            if (status == RoomStatus.OutOfService || status == RoomStatus.Cleaning) {
                return false;
            }

            for (Reservation existingReservation : reservations) {
                if (existingReservation.getReservationStatus() != ReservationStatus.Cancelled
                        && existingReservation.overlaps(
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate())) {
                    return false;
                }
            }

            reservations.add(reservation);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
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
