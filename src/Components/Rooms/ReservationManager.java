package Components.Rooms;

import Components.Customer;
import Components.PaymentStatus;
import Components.Reservation;
import Components.ReservationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReservationManager {
    private final List<Room> rooms;
    private final Map<Integer, Reservation> reservationsById;
    private final AtomicInteger reservationIdGenerator;

    public ReservationManager(List<Room> rooms) {
        this.rooms = new ArrayList<>(rooms);
        this.reservationsById = new ConcurrentHashMap<>();
        this.reservationIdGenerator = new AtomicInteger(1);
    }

    public List<Room> searchAvailableRooms(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        List<Room> availableRooms = new ArrayList<>();

        for (Room room : rooms) {
            if (room.getRoomCategory() != category) {
                continue;
            }

            room.getLock().readLock().lock();
            try {
                if (room.isAvailable(checkIn, checkOut)) {
                    availableRooms.add(room);
                }
            } finally {
                room.getLock().readLock().unlock();
            }
        }

        availableRooms.sort(Comparator.comparingInt(Room::getRoomNumber));
        return availableRooms;
    }

    public Optional<Reservation> bookFirstAvailableRoom(Customer customer, RoomCategory category,
                                                        LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        for (Room room : rooms) {
            if (room.getRoomCategory() != category) {
                continue;
            }

            room.getLock().writeLock().lock();
            try {
                if (!room.isAvailable(checkIn, checkOut)) {
                    continue;
                }

                Reservation reservation = new Reservation(
                        reservationIdGenerator.getAndIncrement(),
                        checkIn,
                        checkOut,
                        room,
                        customer,
                        PaymentStatus.Pending,
                        ReservationStatus.Active
                );

                room.addReservation(reservation);
                reservationsById.put(reservation.getReservationId(), reservation);
                return Optional.of(reservation);
            } finally {
                room.getLock().writeLock().unlock();
            }
        }

        return Optional.empty();
    }

    public boolean cancelReservation(int reservationId) {
        Reservation reservation = reservationsById.get(reservationId);
        if (reservation == null) {
            return false;
        }

        Room room = reservation.getRoom();
        room.getLock().writeLock().lock();
        try {
            if (reservation.getReservationStatus() == ReservationStatus.Cancelled) {
                return false;
            }

            reservation.setReservationStatus(ReservationStatus.Cancelled);
            return true;
        } finally {
            room.getLock().writeLock().unlock();
        }
    }

    public boolean updateRoomStatus(int roomNumber, RoomStatus newStatus) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }

        room.getLock().writeLock().lock();
        try {
            room.setStatus(newStatus);
            return true;
        } finally {
            room.getLock().writeLock().unlock();
        }
    }

    public boolean updateRoomPrice(int roomNumber, float newPrice) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }

        room.getLock().writeLock().lock();
        try {
            room.setPrice(newPrice);
            return true;
        } finally {
            room.getLock().writeLock().unlock();
        }
    }

    public double calculateOccupancyRate(RoomCategory category, LocalDate day) {
        int total = 0;
        int occupied = 0;

        for (Room room : rooms) {
            if (room.getRoomCategory() != category) {
                continue;
            }

            total++;
            room.getLock().readLock().lock();
            try {
                boolean isOccupiedOnDay = room.getReservations().stream()
                        .filter(reservation -> reservation.getReservationStatus() != ReservationStatus.Cancelled)
                        .anyMatch(reservation -> !day.isBefore(reservation.getCheckInDate())
                                && day.isBefore(reservation.getCheckOutDate()));

                if (isOccupiedOnDay) {
                    occupied++;
                }
            } finally {
                room.getLock().readLock().unlock();
            }
        }

        if (total == 0) {
            return 0.0;
        }

        return (occupied * 100.0) / total;
    }

    public Map<RoomCategory, Double> snapshotOccupancy(LocalDate day) {
        Map<RoomCategory, Double> occupancy = new HashMap<>();
        for (RoomCategory category : RoomCategory.values()) {
            occupancy.put(category, calculateOccupancyRate(category, day));
        }
        return occupancy;
    }

    public int getReservationCount() {
        return reservationsById.size();
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    private Room findRoomByNumber(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date.");
        }
    }
}
