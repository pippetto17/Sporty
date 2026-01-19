package view.homeview;

import controller.ApplicationController;
import controller.HomeController;
import model.domain.User;
import model.utils.Constants;

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
            System.out.println("\n" + Constants.SEPARATOR);
            displayMenu();
            System.out.print(Constants.PROMPT_CHOOSE_OPTION);

            String choice = scanner.nextLine();

            if (homeController.getCurrentUser().isPlayer()) {
                running = handlePlayerChoice(choice);
            } else if (homeController.getCurrentUser().isOrganizer()) {
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
        System.out.println("\n" + Constants.SEPARATOR);
        System.out.println("    HOME - SPORTY");
        System.out.println(Constants.SEPARATOR);
        System.out.println("Welcome, " + user.getName() + " " + user.getSurname() + "!");
        System.out.println("Role: " + homeController.getUserRole());
        System.out.println(Constants.SEPARATOR);
    }

    @Override
    public void displayMatches(java.util.List<model.bean.MatchBean> matches) {
        System.out.println("\n--- AVAILABLE MATCHES ---");
        if (matches == null || matches.isEmpty()) {
            System.out.println("No matches available at the moment.");
        } else {
            for (int i = 0; i < matches.size(); i++) {
                model.bean.MatchBean match = matches.get(i);
                String matchStr = String.format("%d. %s %s - %s - %s at %s (%d players)",
                        i + 1,
                        homeController.getSportEmoji(match.getSport()),
                        match.getSport().getDisplayName(),
                        match.getCity(),
                        match.getMatchDate(),
                        match.getMatchTime(),
                        homeController.getCurrentParticipants(match));
                System.out.println(matchStr);
            }
        }
    }

    @Override
    public void displayMenu() {
        if (homeController.getCurrentUser().isPlayer()) {
            System.out.println("1. View available matches");
            System.out.println("2. Join match");
            System.out.println("3. Logout");
        } else if (homeController.getCurrentUser().isOrganizer()) {
            System.out.println("1. View available matches");
            System.out.println("2. Organize match");
            System.out.println("3. Book field");
            System.out.println("4. Logout");
        }
    }

    private boolean handlePlayerChoice(String choice) {
        switch (choice) {
            case "1" -> {
                java.util.List<model.bean.MatchBean> matches = homeController.getMatches();
                displayMatches(matches);
            }
            case "2" -> {
                java.util.List<model.bean.MatchBean> matches = homeController.getMatches();
                displayMatches(matches);
                if (matches != null && !matches.isEmpty()) {
                    System.out.print("\nEnter match number to join (0 to cancel): ");
                    try {
                        int matchNum = Integer.parseInt(scanner.nextLine().trim());
                        if (matchNum > 0 && matchNum <= matches.size()) {
                            model.bean.MatchBean selectedMatch = matches.get(matchNum - 1);
                            attemptJoinMatch(selectedMatch);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input");
                    }
                }
            }
            case "3" -> {
                applicationController.logout();
                return false;
            }
            default -> System.out.println(Constants.ERROR_INVALID_OPTION);
        }
        return true;
    }

    private boolean handleOrganizerChoice(String choice) {
        switch (choice) {
            case "1" -> {
                java.util.List<model.bean.MatchBean> matches = homeController.getMatches();
                displayMatches(matches);
            }
            case "2" -> {
                System.out.println("\n--- ORGANIZE MATCH ---");
                homeController.organizeMatch();
            }
            case "3" -> {
                System.out.println("\n--- BOOK FIELD ---");
                homeController.bookFieldStandalone();
            }
            case "4" -> {
                applicationController.logout();
                return false;
            }
            default -> System.out.println(Constants.ERROR_INVALID_OPTION);
        }
        return true;
    }

    @Override
    public void showMatchDetails(int matchId) {
        try {
            // Find match from controller
            model.bean.MatchBean match = homeController.getMatches().stream()
                    .filter(m -> m.getMatchId() == matchId)
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                displayError("Match not found");
                return;
            }

            System.out.println("\n" + Constants.SEPARATOR);
            System.out.println("MATCH DETAILS");
            System.out.println(Constants.SEPARATOR);
            System.out.println("Sport: " + match.getSport().getDisplayName());
            System.out.println("Date: " + match.getMatchDate() + " at " + match.getMatchTime());
            System.out.println("City: " + match.getCity());
            System.out.println("Organizer: " + match.getOrganizerUsername());
            System.out.println("Players: " + homeController.getCurrentParticipants(match)
                    + "/" + match.getRequiredParticipants());
            System.out.println("Price: €"
                    + (match.getPricePerPerson() != null ? String.format("%.2f", match.getPricePerPerson()) : "Free"));
            System.out.println("Status: " + match.getStatus().name());
            System.out.println(Constants.SEPARATOR);

            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();

        } catch (Exception e) {
            displayError("Error showing match details: " + e.getMessage());
        }
    }

    @Override
    public void displayError(String message) {
        System.out.println("\n[ERROR] " + message);
    }

    @Override
    public void displaySuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }

    @Override
    public void refreshMatches() {
        displayMatches(homeController.getMatches());
    }

    private void attemptJoinMatch(model.bean.MatchBean selectedMatch) {
        try {
            homeController.joinMatch(selectedMatch.getMatchId());
        } catch (exception.ValidationException e) {
            System.out.println("Error joining match: " + e.getMessage());
        }
    }
}
