package view.homeview;

import controller.ApplicationController;
import controller.HomeController;
import model.bean.MatchBean;
import model.bean.UserBean;
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
            if (homeController.isViewingAsPlayer()) {
                running = handlePlayerChoice(choice);
            } else {
                running = handleOrganizerChoice(choice);
            }
        }
    }

    @Override
    public void close() {
        running = false;
        System.out.println("\n" + Constants.STATUS_LOGGING_OUT);
    }

    @Override
    public void displayWelcome() {
        UserBean user = homeController.getCurrentUser();
        System.out.println("\n" + Constants.SEPARATOR);
        System.out.println("    HOME - SPORTY");
        System.out.println(Constants.SEPARATOR);
        System.out.println(Constants.LABEL_WELCOME_PREFIX + user.getName() + " " + user.getSurname()
                + Constants.LABEL_WELCOME_SUFFIX);
        System.out.println("Role: " + homeController.getUserRoleName());
        System.out.println(Constants.SEPARATOR);
    }

    @Override
    public void displayMatches(java.util.List<MatchBean> matches) {
        System.out.println("\n--- AVAILABLE MATCHES ---");
        if (matches == null || matches.isEmpty()) {
            System.out.println(Constants.LABEL_NO_MATCHES);
        } else {
            for (int i = 0; i < matches.size(); i++) {
                MatchBean match = matches.get(i);
                String matchStr = String.format("%d. %s %s - %s - %s at %s | 💰 €%.2f/persona (%d players)",
                        i + 1,
                        view.ViewUtils.getSportEmoji(match.getSport()),
                        match.getSport().getDisplayName(),
                        match.getCity(),
                        match.getMatchDate(),
                        match.getMatchTime(),
                        match.getCostPerPerson(),
                        view.ViewUtils.getCurrentParticipants(match));

                // Add status for organizer view
                if (!homeController.isViewingAsPlayer() && match.getStatus() != null) {
                    matchStr += " [" + match.getStatus().name() + "]";
                }

                System.out.println(matchStr);
            }
        }
    }

    @Override
    public void displayMenu() {
        if (homeController.isViewingAsPlayer()) {
            System.out.println("1. View available matches");
            System.out.println("2. Join match");
            if (homeController.getCurrentUser().isOrganizer()) {
                System.out.println("3. Switch to Organizer View");
                System.out.println("4. Logout");
            } else {
                System.out.println("3. Logout");
            }
        } else {
            System.out.println("1. View available matches");
            System.out.println("2. Organize match");
            System.out.println("3. Book field");
            System.out.println("4. Switch to Player View");
            System.out.println("5. Logout");
        }
    }

    private boolean handlePlayerChoice(String choice) {
        switch (choice) {
            case "1" -> {
                java.util.List<model.bean.MatchBean> matches = homeController.getMatches();
                displayMatches(matches);
            }
            case "2" -> handleJoinMatchInteraction();
            case "3" -> {
                if (homeController.getCurrentUser().isOrganizer()) {
                    homeController.switchRole();
                    displaySuccess("Switched to Organizer View");
                } else {
                    applicationController.logout();
                    return false;
                }
            }
            case "4" -> {
                if (homeController.getCurrentUser().isOrganizer()) {
                    applicationController.logout();
                    return false;
                } else {
                    System.out.println(Constants.ERROR_INVALID_OPTION);
                }
            }
            default -> System.out.println(Constants.ERROR_INVALID_OPTION);
        }
        return true;
    }

    private void handleJoinMatchInteraction() {
        java.util.List<MatchBean> matches = homeController.getMatches();
        displayMatches(matches);
        if (matches != null && !matches.isEmpty()) {
            System.out.print("\nEnter match number to join (0 to cancel): ");
            try {
                int matchNum = Integer.parseInt(scanner.nextLine().trim());
                if (matchNum > 0 && matchNum <= matches.size()) {
                    MatchBean selectedMatch = matches.get(matchNum - 1);
                    attemptJoinMatch(selectedMatch);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
    }

    private boolean handleOrganizerChoice(String choice) {
        switch (choice) {
            case "1" -> {
                java.util.List<MatchBean> matches = homeController.getMatches();
                displayMatches(matches);
            }
            case "2" -> {
                System.out.println("\n--- ORGANIZE MATCH ---");
                homeController.organizeMatch();
            }
            case "3" -> System.out.println("Feature disabled in this version.");
            case "4" -> {
                homeController.switchRole();
                displaySuccess("Switched to Player View");
            }
            case "5" -> {
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
            MatchBean match = homeController.getMatchById(matchId);
            if (match == null) {
                displayError(Constants.ERROR_MATCH_NOT_FOUND);
                return;
            }
            System.out.println("\n" + Constants.SEPARATOR);
            System.out.println("MATCH DETAILS");
            System.out.println(Constants.SEPARATOR);
            System.out.println("Sport: " + match.getSport().getDisplayName());
            System.out.println("Date: " + match.getMatchDate() + " at " + match.getMatchTime());
            System.out.println("City: " + match.getCity());
            System.out.println("Organizer: " + match.getOrganizerName());
            System.out.println("Players: " + view.ViewUtils.getCurrentParticipants(match)
                    + "/" + match.getSport().getRequiredPlayers());
            System.out.println("Status: " + match.getStatus().name());
            System.out.println(Constants.SEPARATOR);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
        } catch (Exception e) {
            displayError(Constants.MSG_ERROR_MATCH_DETAILS + e.getMessage());
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

    private void attemptJoinMatch(MatchBean selectedMatch) {
        try {
            homeController.joinMatch();
        } catch (exception.ValidationException e) {
            System.out.println("Error joining match: " + e.getMessage());
        }
    }
}