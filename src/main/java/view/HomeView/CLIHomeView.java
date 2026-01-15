package view.homeview;

import controller.ApplicationController;
import controller.HomeController;
import model.domain.User;

import java.util.Scanner;

public class CLIHomeView implements HomeView {
    private final HomeController homeController;
    private ApplicationController applicationController;
    private final Scanner scanner;
    private boolean running;

    public CLIHomeView(HomeController homeController) {
        this.homeController = homeController;
        this.scanner = new Scanner(System.in);
        this.running = false;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void display() {
        running = true;
        displayWelcome();

        while (running) {
            System.out.println("\n=================================");
            displayMenu();
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            if (homeController.isPlayer()) {
                running = handlePlayerChoice(choice);
            } else if (homeController.isOrganizer()) {
                running = handleOrganizerChoice(choice);
            }
        }
    }

    @Override
    public void close() {
        running = false;
        System.out.println("\nLogging out...");
    }

    @Override
    public void displayWelcome() {
        User user = homeController.getCurrentUser();
        System.out.println("\n=================================");
        System.out.println("    HOME - SPORTY");
        System.out.println("=================================");
        System.out.println("Welcome, " + user.getName() + " " + user.getSurname() + "!");
        System.out.println("Role: " + homeController.getUserRole());
        System.out.println("=================================");
    }

    @Override
    public void displayMatches(String[] matches) {
        System.out.println("\n--- AVAILABLE MATCHES ---");
        if (matches == null || matches.length == 0) {
            System.out.println("No matches available at the moment.");
        } else {
            for (int i = 0; i < matches.length; i++) {
                System.out.println((i + 1) + ". " + matches[i]);
            }
        }
    }

    @Override
    public void displayMenu() {
        if (homeController.isPlayer()) {
            System.out.println("1. View available matches");
            System.out.println("2. Logout");
        } else if (homeController.isOrganizer()) {
            System.out.println("1. View available matches");
            System.out.println("2. Organize match");
            System.out.println("3. Logout");
        }
    }

    private boolean handlePlayerChoice(String choice) {
        switch (choice) {
            case "1" -> {
                String[] matches = homeController.getMatches();
                displayMatches(matches);
            }
            case "2" -> {
                applicationController.logout();
                return false; // Ferma il loop
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return true;
    }

    private boolean handleOrganizerChoice(String choice) {
        switch (choice) {
            case "1" -> {
                String[] matches = homeController.getMatches();
                displayMatches(matches);
            }
            case "2" -> {
                System.out.println("\n--- ORGANIZE MATCH ---");
                homeController.organizeMatch();
            }
            case "3" -> {
                applicationController.logout();
                return false; // Ferma il loop
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return true;
    }
}

