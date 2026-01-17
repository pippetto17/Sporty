package view.fieldmanagerview;

import controller.FieldManagerController;
import model.bean.BookingBean;
import model.domain.User;

import java.util.List;
import java.util.Scanner;

/**
 * CLI implementation of Field Manager Dashboard.
 */
public class CLIFieldManagerView implements FieldManagerView {
    private final FieldManagerController controller;
    private final User fieldManager;
    private controller.ApplicationController applicationController;
    private final Scanner scanner;

    public CLIFieldManagerView(FieldManagerController controller, User fieldManager) {
        this.controller = controller;
        this.fieldManager = fieldManager;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(controller.ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println("  FIELD MANAGER DASHBOARD");
            System.out.println("  Manager: " + fieldManager.getName() + " " + fieldManager.getSurname());
            System.out.println("========================================");

            // Display stats
            displayDashboardStats();

            System.out.println("\n1) View Pending Booking Requests");
            System.out.println("2) Approve Booking");
            System.out.println("3) Reject Booking");
            System.out.println("4) My Fields");
            System.out.println("5) Set Availability");
            System.out.println("6) View All Bookings");
            System.out.println("0) Back");
            System.out.print("\nYour choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewPendingRequests();
                case "2" -> approveBooking();
                case "3" -> rejectBooking();
                case "4" -> System.out.println("Manage Fields - Not yet implemented");
                case "5" -> System.out.println("Set Availability - Not yet implemented");
                case "6" -> System.out.println("View All Bookings - Not yet implemented");
                case "0" -> running = false;
                default -> System.out.println("Invalid choice");
            }
        }
    }

    @Override
    public void close() {
        // CLI doesn't need cleanup
    }

    private void displayDashboardStats() {
        try {
            FieldManagerController.DashboardData data = controller.getDashboardData();
            System.out.println("\nSTATISTICS:");
            System.out.println("  Total Fields: " + data.getTotalFields());
            System.out.println("  Pending Requests: " + data.getPendingRequests());
            System.out.println("  Today's Bookings: " + data.getTodayBookings());
        } catch (Exception e) {
            System.out.println("Error loading stats: " + e.getMessage());
        }
    }

    private void viewPendingRequests() {
        try {
            List<BookingBean> pending = controller.getPendingRequests();

            if (pending.isEmpty()) {
                System.out.println("\nNo pending booking requests.");
                return;
            }

            System.out.println("\nPENDING BOOKING REQUESTS:");
            System.out.println("----------------------------------------");
            for (int i = 0; i < pending.size(); i++) {
                BookingBean booking = pending.get(i);
                System.out.printf("%d) Field: %s | Requester: %s | Date: %s | Time: %s-%s | Price: €%.2f%n",
                        i + 1,
                        booking.getFieldName(),
                        booking.getRequesterUsername(),
                        booking.getBookingDate(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        booking.getTotalPrice());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void approveBooking() {
        System.out.print("Enter booking ID to approve: ");
        try {
            int bookingId = Integer.parseInt(scanner.nextLine().trim());
            controller.approveBooking(bookingId);
            System.out.println("✓ Booking approved successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid booking ID");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void rejectBooking() {
        System.out.print("Enter booking ID to reject: ");
        try {
            int bookingId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Rejection reason: ");
            String reason = scanner.nextLine().trim();

            if (reason.isEmpty()) {
                System.out.println("Rejection reason is required");
                return;
            }

            controller.rejectBooking(bookingId, reason);
            System.out.println("✓ Booking rejected");
        } catch (NumberFormatException e) {
            System.out.println("Invalid booking ID");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
