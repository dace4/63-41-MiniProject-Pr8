package Components.Reservation;

import Components.Customer;
import Components.PaymentStatus;
import Components.Rooms.Room;
import Components.Rooms.RoomCategory;
import Components.Rooms.RoomStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReservationManager {
    private final ArrayList<Room> rooms;
    private final Map<Integer, Reservation> reservationsById;
    private final AtomicInteger nextReservationId;

    public ReservationManager(ArrayList<Room> rooms) {
        this.rooms = new ArrayList<>(rooms);
        this.reservationsById = new ConcurrentHashMap<>();
        this.nextReservationId = new AtomicInteger(1);
    }

    public List<Room> searchAvailableRooms(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        ArrayList<Room> availableRooms = new ArrayList<>();

        for (Room room : rooms) {
            if (room.getRoomCategory() == category && room.isAvailable(checkIn, checkOut)) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    public Reservation bookFirstAvailableRoom(Customer customer, RoomCategory category,
                                              LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        for (Room room : rooms) {
            if (room.getRoomCategory() == category) {
                Reservation reservation = new Reservation(
                        nextReservationId.getAndIncrement(),
                        checkIn,
                        checkOut,
                        room,
                        customer,
                        PaymentStatus.Pending,
                        ReservationStatus.Active
                );

                if (room.tryAddReservation(reservation)) {
                    reservationsById.put(reservation.getReservationId(), reservation);
                    return reservation;
                }
            }
        }

        return null;
    }

    public boolean cancelReservation(int reservationId) {
        Reservation reservation = reservationsById.get(reservationId);

        if (reservation == null) {
            return false;
        }

        if (reservation.getReservationStatus() == ReservationStatus.Cancelled) {
            return false;
        }

        reservation.setReservationStatus(ReservationStatus.Cancelled);
        return true;
    }

    public boolean updateRoomStatus(int roomNumber, RoomStatus newStatus) {
        Room room = findRoomByNumber(roomNumber);

        if (room == null) {
            return false;
        }

        room.setStatus(newStatus);
        return true;
    }

    public boolean updateRoomPrice(int roomNumber, float newPrice) {
        Room room = findRoomByNumber(roomNumber);

        if (room == null) {
            return false;
        }

        room.setPrice(newPrice);
        return true;
    }

    public int getReservationCount() {
        return reservationsById.size();
    }

    public List<Room> getRooms() {
        return rooms;
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
            throw new IllegalArgumentException("Check-in must be before check-out.");
        }
    }
}
