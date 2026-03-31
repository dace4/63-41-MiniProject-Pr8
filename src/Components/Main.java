package Components;

import Components.Reservation.Reservation;
import Components.Reservation.ReservationManager;
import Components.Reservation.ReservationStatus;
import Components.Rooms.Room;
import Components.Rooms.RoomCategory;
import Components.Rooms.RoomStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main{
    private static final int TOTAL_ROOMS = 100;
    private static final int SIMULATION_RUN_TIME_SECONDS = 15;
    private static final Random RAND = new Random();

    public static void main(String[] args) {
        ArrayList<Room> inventory = new ArrayList<>();
        for (int i = 1; i <= TOTAL_ROOMS; i++) {
            RoomCategory cat;
            int typeSelector = i % 3;
            if(typeSelector == 0){
                cat = RoomCategory.Standard;
            } else if (typeSelector == 1) {
                cat = RoomCategory.Deluxe;
            }else {
                cat = RoomCategory.Suite;
            }

            float price;
            if(cat == RoomCategory.Suite){
                price = 500.0f;
            } else if (cat == RoomCategory.Deluxe) {
                price = 250.0f;
            }else{
                price = 150.0f;
            }

            Room newRoom = new Room(i,cat, RoomStatus.Available,price);
            inventory.add(newRoom);
        }

        ReservationManager reservationManager = new ReservationManager(inventory);
        ExecutorService executor = Executors.newFixedThreadPool(50);

        System.out.println("---Starting GrandResort simulation.---");

        // PARTICIPANT: FRONT DESK STAFF (Priority Threads)
        // Handles check-ins and check-outs in real time
        final Thread staffThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    List<Room> allRooms = reservationManager.getRooms();
                    LocalDate today = LocalDate.now();

                    for (int i = 0; i < allRooms.size(); i++) {
                        Room room = allRooms.get(i);
                        List<Reservation> roomReservation = room.getReservations();

                        for (int j = 0; j < roomReservation.size(); j++) {
                            Reservation reservation = roomReservation.get(j);

                            // CHECK-IN Logic: If reservation is active and start today
                            if(reservation.getReservationStatus() == ReservationStatus.Active &&
                            reservation.getCheckInDate().equals(today)){
                                reservation.setReservationStatus(ReservationStatus.CheckedIn);
                                reservationManager.updateRoomStatus(room.getRoomNumber(), RoomStatus.Occupied);

                                System.out.println("[Staff] CHECK-IN: " + reservation.getCustomer().getName()
                                        + " -> Room " + room.getRoomNumber()
                                        + " (" + room.getRoomCategory() + ")"
                                        + " | Stay: " + reservation.getCheckInDate() + " to " + reservation.getCheckOutDate()
                                        + " | Price: CHF " + room.getPrice());

                            }// CHECK-OUT Logic: If CheckedIn and ends today
                             else if (reservation.getReservationStatus() == ReservationStatus.CheckedIn &&
                            reservation.getCheckOutDate().equals(today)) {

                                reservation.setReservationStatus(ReservationStatus.CheckedOut);
                                reservationManager.updateRoomStatus(room.getRoomNumber(),RoomStatus.Cleaning);
                                System.out.println("[Staff] CHECK-OUT: Room " + room.getRoomNumber()
                                        + " (" + room.getRoomCategory() + ")"
                                        + " | Guest: " + reservation.getCustomer().getName()
                                        + " | Status changed to CLEANING");
                            }
                             /*
                             Because the simulation is running for 15 real-world seconds there will never be
                             check-outs in the console. As we use LocalDate.now() the day never changes for the
                             upcoming day. The core logic is implemented but not used for our simulation.
                              */
                        }
                    }
                    try{
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        // Set highest priority to staff Thread
        staffThread.setPriority(Thread.MAX_PRIORITY);
        staffThread.start();



        // PARTICIPANT: PLATFORM SYNCHRONIZER (Channel Manager)
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    int roomNum = RAND.nextInt(TOTAL_ROOMS)+1;
                    float newPrice = 100.0f +(RAND.nextFloat()*400.0f);
                    reservationManager.updateRoomPrice(roomNum, newPrice);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        // PARTICIPANT: CUSTOMER (Search & Book)
        for (int i = 0; i < 30; i++) {
            final int customerId = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    String guestName = "Guest "+customerId;
                    String guestEmail = "guest"+customerId+"@gmail.com";
                    Customer guest = new Customer(customerId, guestName, guestEmail);

                    while(!Thread.currentThread().isInterrupted()){
                        int startOffset = RAND.nextInt(5);
                        LocalDate start = LocalDate.now().plusDays(startOffset);
                        LocalDate end = start.plusDays(RAND.nextInt(5)+1);

                        RoomCategory pref = RoomCategory.Standard;
                        int pick = RAND.nextInt(3);
                        if(pick == 1){
                            pref = RoomCategory.Deluxe;
                        }
                        if(pick == 2){
                            pref = RoomCategory.Suite;
                        }

                        Reservation reservation = reservationManager.bookFirstAvailableRoom(guest,pref,start,end);
                        if(reservation != null){
                            System.out.println("[Booking] " + guest.getName()
                                    + " booked Room " + reservation.getRoom().getRoomNumber()
                                    + " (" + reservation.getRoom().getRoomCategory() + ")"
                                    + " from " + reservation.getCheckInDate()
                                    + " to " + reservation.getCheckOutDate()
                                    + " | Contact: " + guest.getEmail()
                                    + " | Price: CHF " + reservation.getRoom().getPrice());
                        }
                        try {
                            Thread.sleep(2000);
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
        }

        // PARTICIPANT: DYNAMIC PRICING SYSTEM
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    List<Room> allRooms = reservationManager.getRooms();

                    for (int i = 0; i < allRooms.size(); i++) {
                        Room currentRoom = allRooms.get(i);
                        float currentPrice = currentRoom.getPrice();

                        reservationManager.updateRoomPrice(currentRoom.getRoomNumber(), currentPrice * 1.02f);
                    }

                    System.out.println("Market price updated");
                }
            }
        });

        // MAIN THREAD
        try{
            Thread.sleep(SIMULATION_RUN_TIME_SECONDS * 1000);
        } catch (InterruptedException e) {
            System.out.println("Main simulation thread was interrupted");
            Thread.currentThread().interrupt();
        }finally {
            // Shutdown ExecutorService
            executor.shutdownNow();
            // Stop Staff Thread to prevent code from running forever
            staffThread.interrupt();
            System.out.println("---Simulation ended. Total Reservations: "+reservationManager.getReservationCount()
            +"---");
        }
    }
}
