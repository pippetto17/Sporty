package view.fieldmanagerview;

import controller.ApplicationController;
import controller.FieldManagerController;
import model.bean.MatchBean;
import model.domain.User;

import java.util.List;
import java.util.Scanner;

public class CLIFieldManagerView implements FieldManagerView {

    private final FieldManagerController controller;
    private final User manager;
    private final Scanner scanner;

    public CLIFieldManagerView(FieldManagerController controller, User manager) {
        this.controller = controller;
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(ApplicationController appController) {
        // Se la CLI non naviga (non cambia scene), questo metodo può rimanere vuoto o
        // rimosso
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            printHeader();
            printStats();
            printMenu();

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listPendingRequests();
                case "2" -> handleApprove();
                case "3" -> handleReject();
                case "0" -> running = false;
                default -> System.out.println("⚠ Invalid choice, try again.");
            }
        }
    }

    @Override
    public void close() {
        // No-op per CLI
    }

    // --- DISPLAY HELPERS ---

    private void printHeader() {
        System.out.println("\n========================================");
        System.out.println("  FIELD MANAGER: " + manager.getName() + " " + manager.getSurname());
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
        System.out.println("2) Approve Match");
        System.out.println("3) Reject Match");
        System.out.println("0) Logout");
        System.out.print("> ");
    }

    // --- ACTIONS ---

    private void listPendingRequests() {
        try {
            List<MatchBean> pending = controller.getPendingRequests();
            if (pending.isEmpty()) {
                System.out.println("\n✓ No pending requests.");
                return;
            }

            System.out.println("\n--- PENDING REQUESTS ---");
            // Intestazione Tabella
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

    private void handleApprove() {
        int id = readInt("Enter Match ID to APPROVE: ");
        if (id == -1)
            return;

        try {
            controller.approveMatch(id);
            System.out.println("✓ Match " + id + " approved.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleReject() {
        int id = readInt("Enter Match ID to REJECT: ");
        if (id == -1)
            return;

        try {
            controller.rejectMatch(id);
            System.out.println("✓ Match " + id + " rejected.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- UTILS ---

    // Legge un intero gestendo l'errore di formato
    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("⚠ Invalid number format.");
            return -1;
        }
    }

    // Taglia le stringhe troppo lunghe per non rompere la tabella
    private String truncate(String input, int length) {
        if (input == null)
            return "";
        return input.length() > length ? input.substring(0, length - 2) + ".." : input;
    }
}