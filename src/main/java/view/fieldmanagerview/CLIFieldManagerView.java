package view.fieldmanagerview;

import controller.ApplicationController;
import controller.FieldManagerController;
import model.bean.MatchBean;
import model.utils.Constants;

import java.util.List;
import java.util.Scanner;

public class CLIFieldManagerView implements FieldManagerView {
    private final FieldManagerController controller;

    private final Scanner scanner;
    private model.notification.NotificationService notificationService;

    public CLIFieldManagerView(FieldManagerController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(ApplicationController appController) {
        this.notificationService = appController.getNotificationService();
        this.notificationService.subscribe(new model.notification.FieldManagerObserver());
    }

    @Override
    public void display() {
        showUnreadNotifications();
        boolean running = true;
        while (running) {
            printHeader();
            printStats();
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listPendingRequests();
                case "2" -> viewRequestDetails();
                case "3" -> handleApprove();
                case "4" -> handleReject();
                case "5" -> showUnreadNotifications();
                case "0" -> running = false;
                default -> System.out.println("⚠ Invalid choice, try again.");
            }
        }
    }

    @Override
    public void close() {
        // Intentionally empty
    }

    private void printHeader() {
        System.out.println("\n========================================");
        System.out.println("  FIELD MANAGER: " + controller.getFieldManager().getName() + " "
                + controller.getFieldManager().getSurname());
        System.out.println("========================================");
    }

    private void printStats() {
        try {
            var data = controller.getDashboardData();
            System.out.printf("Fields: %d | Pending: %d | Today's Bookings: %d%n",
                    data.totalFields(), data.pendingRequests(), data.todayBookings());
        } catch (Exception e) {
            System.out.println("Stats unavailable: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("\n1) List Pending Requests");
        System.out.println("2) View Request Details");
        System.out.println("3) Approve Match");
        System.out.println("4) Reject Match");
        System.out.println("5) View Notifications");
        System.out.println("0) Logout");
        System.out.print("> ");
    }

    private void listPendingRequests() {
        try {
            List<MatchBean> pending = controller.getPendingRequests();
            if (pending.isEmpty()) {
                System.out.println("\n✓ No pending requests.");
                return;
            }
            System.out.println("\n--- PENDING REQUESTS ---");
            System.out.printf("%-8s %-15s %-15s %-15s %-12s %-12s%n", "ID", "FIELD", "ORGANIZER", "SPORT", "DATE",
                    "TIME");
            for (MatchBean m : pending) {
                System.out.printf("[%-6d] %-15s %-15s %-15s %-12s %s%n",
                        m.getMatchId(),
                        truncate(m.getFieldName(), 15),
                        truncate(m.getOrganizerName(), 15),
                        truncate(m.getSport().name(), 15),
                        m.getMatchDate(),
                        m.getMatchTime());
            }
        } catch (Exception e) {
            System.out.println("Error fetching list: " + e.getMessage());
        }
    }

    private void viewRequestDetails() {
        int id = readInt("Enter Match ID to view details: ");
        if (id == -1)
            return;
        try {
            MatchBean match = controller.getRequestDetails(id);
            System.out.println("\n========== MATCH REQUEST DETAILS ==========");
            System.out.printf("Match ID:       %d%n", match.getMatchId());
            System.out.printf("Field:          %s%n", match.getFieldName());
            System.out.printf("Organizer:      %s%n", match.getOrganizerName());
            System.out.printf("Sport:          %s%n", match.getSport().getDisplayName());
            System.out.printf("Date:           %s%n", match.getMatchDate());
            System.out.printf("Time:           %s%n", match.getMatchTime());
            System.out.printf("Missing Players:%d%n", match.getMissingPlayers());
            System.out.printf("Status:         %s%n", match.getStatus());
            System.out.printf("Price/Hour:     €%.2f%n", match.getPricePerHour());
            System.out.println("==========================================");
        } catch (Exception e) {
            System.out.println(Constants.ERROR_UNEXPECTED + e.getMessage());
        }
    }

    private void handleApprove() {
        int id = readInt("Enter Match ID to APPROVE: ");
        if (id == -1)
            return;
        try {
            // Get details before approving for confirmation
            MatchBean match = controller.getRequestDetails(id);
            System.out.printf("%n✓ Approving match at '%s' organized by %s on %s at %s%n",
                    match.getFieldName(), match.getOrganizerName(),
                    match.getMatchDate(), match.getMatchTime());

            controller.approveMatch(id);
            System.out.println("✓ Match " + id + " has been APPROVED successfully!");
        } catch (Exception e) {
            System.out.println(Constants.ERROR_UNEXPECTED + e.getMessage());
        }
    }

    private void handleReject() {
        int id = readInt("Enter Match ID to REJECT: ");
        if (id == -1)
            return;
        try {
            // Get details before rejecting for confirmation
            MatchBean match = controller.getRequestDetails(id);
            System.out.printf("%n✗ Rejecting match at '%s' organized by %s on %s at %s%n",
                    match.getFieldName(), match.getOrganizerName(),
                    match.getMatchDate(), match.getMatchTime());

            controller.rejectMatch(id);
            System.out.println("✓ Match " + id + " has been REJECTED successfully!");
        } catch (Exception e) {
            System.out.println(Constants.ERROR_UNEXPECTED + e.getMessage());
        }
    }

    private void showUnreadNotifications() {
        if (notificationService == null)
            return;
        List<String> unread = notificationService.getUnreadNotifications(controller.getFieldManager().getUsername());
        if (unread.isEmpty())
            return;
        System.out.println("\n🔔 YOU HAVE " + unread.size() + " NEW NOTIFICATION(S):");
        unread.forEach(msg -> System.out.println("  • " + msg));
        notificationService.markAllAsRead(controller.getFieldManager().getUsername());
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid number format.");
            return -1;
        }
    }

    private String truncate(String input, int length) {
        if (input == null)
            return "";
        return input.length() > length ? input.substring(0, length - 2) + ".." : input;
    }
}