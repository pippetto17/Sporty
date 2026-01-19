package view.joinmatchview;

import controller.ApplicationController;
import controller.JoinMatchController;
import model.domain.Match;

import java.util.Scanner;

public class CLIJoinMatchView implements JoinMatchView {
    private final JoinMatchController controller;
    private final Scanner scanner;
    private ApplicationController applicationController;

    public CLIJoinMatchView(JoinMatchController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        displayMatchDetails();
        showMenu();
    }

    @Override
    public void displayMatchDetails() {
        Match match = controller.getMatch();

        System.out.println("\n=== JOIN MATCH ===");
        System.out.println("Sport: " + match.getSport().getDisplayName());
        System.out.println("Date: " + match.getMatchDate());
        System.out.println("Time: " + match.getMatchTime());
        System.out.println("City: " + match.getCity());
        System.out.println("Organizer: " + match.getOrganizerUsername());
        System.out.println("Participants: " + match.getParticipantCount() + "/" + match.getRequiredParticipants());
        System.out.println("Available slots: " + controller.getAvailableSlots());
        System.out.printf("Cost per person: €%.2f%n", controller.calculatePlayerCost());
        System.out.println();
    }

    private void showMenu() {
        if (!controller.canJoin()) {
            displayError("You cannot join this match (full, already joined, or not available)");
            promptBack();
            return;
        }

        System.out.println("1. Join and pay");
        System.out.println("2. Back");
        System.out.print("> ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                try {
                    controller.proceedToPayment();
                } catch (exception.ValidationException e) {
                    displayError(e.getMessage());
                }
            }
            case "2" -> applicationController.back();
            default -> {
                displayError("Invalid option");
                showMenu();
            }
        }
    }

    private void promptBack() {
        System.out.println("\nPress Enter to go back...");
        scanner.nextLine();
        applicationController.back();
    }

    @Override
    public void displayError(String message) {
        System.err.println("ERROR: " + message);
    }

    @Override
    public void displaySuccess(String message) {
        System.out.println("SUCCESS: " + message);
    }

    @Override
    public void close() {
        // Nothing to close for CLI
    }
}
