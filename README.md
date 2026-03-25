# 63-41-MiniProject Pr 8 GrandResort: Multi-Date Hotel Reservation Management

## Problem Summary
The objective of this project is to simulate and manage reservations for a large hotel complex featuring various room categories (Standard, Deluxe, Suite). The core challenge is handling concurrency and time-interval competition. The system must strictly prevent double-booking for overlapping periods by implementing a robust search and locking mechanism (using `ReadWriteLock`). This ensures that availability for an entire requested stay is verified before a transaction is validated, while concurrently allowing hundreds of customers, front desk staff, platform synchronizers, and dynamic pricing systems to query and update the inventory in real time.

## Collaborators
* Vuk Vasic
* Rinor Murtezani

## How to Fork, Pull, and Use the Code

### 1. Fork the Repository
Click the **Fork** button at the top right of this repository's GitHub page to create a copy of the project under your own account.

### 2. Clone Your Fork
Open your terminal and clone the forked repository to your local machine:
```bash
git clone [https://github.com/YOUR-USERNAME/GrandResort-Reservation-System.git](https://github.com/YOUR-USERNAME/GrandResort-Reservation-System.git)
cd GrandResort-Reservation-System