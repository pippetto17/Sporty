package view.joinmatchview;

import controller.ApplicationController;

import java.util.Scanner;

public class CLIJoinMatchView implements JoinMatchView {
    private final Scanner scanner;
    private ApplicationController applicationController;

    public CLIJoinMatchView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        System.out.println("\n=== JOIN MATCH ===");
    }

    @Override
    public void displayMatchInfo(String sport, String date, String time, String city,
                                String organizer, int[] participants, double cost) {
        System.out.println("Sport: " + sport);
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.println("City: " + city);
        System.out.println("Organizer: " + organizer);
        System.out.println("Participants: " + participants[0] + "/" + participants[1]);
        System.out.println("Available slots: " + participants[2]);
        System.out.printf("Cost per person: €%.2f%n", cost);
        System.out.println();
    }

    @Override
    public void displayCannotJoin(String reason) {
        displayError(reason);
        System.out.println("\nPress Enter to go back...");
        scanner.nextLine();
        applicationController.back();
    }

    @Override
    public String getUserChoice() {
        System.out.println("1. Join and pay");
        System.out.println("2. Back");
        System.out.print("> ");
        return scanner.nextLine().trim();
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
