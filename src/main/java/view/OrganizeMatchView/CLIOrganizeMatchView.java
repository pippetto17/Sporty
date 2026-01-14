package view.OrganizeMatchView;

import controller.ApplicationController;
import controller.OrganizeMatchController;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CLIOrganizeMatchView implements OrganizeMatchView {
    private final OrganizeMatchController organizeMatchController;
    private ApplicationController applicationController;
    private final Scanner scanner;
    private boolean running;

    public CLIOrganizeMatchView(OrganizeMatchController organizeMatchController) {
        this.organizeMatchController = organizeMatchController;
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
        displayHeader();

        while (running) {
            System.out.println("\n=================================");
            System.out.println("    ORGANIZE MATCH");
            System.out.println("=================================");
            System.out.println("1. View my matches (Work in progress)");
            System.out.println("2. Create new match");
            System.out.println("3. Back to home");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            handleMenuChoice(choice);
        }
    }

    @Override
    public void close() {
        running = false;
    }

    @Override
    public void displayMatchList() {
        System.out.println("\n--- MY MATCHES ---");
        System.out.println("This feature is work in progress...");
        System.out.println("You will be able to see all your created matches here.");
    }

    @Override
    public void displayNewMatchForm() {
        System.out.println("\n=================================");
        System.out.println("    CREATE NEW MATCH");
        System.out.println("=================================");

        organizeMatchController.startNewMatch();

        // Step 1: Select Sport
        Sport selectedSport = selectSport();
        if (selectedSport == null) {
            displayError("Invalid sport selection. Operation cancelled.");
            return;
        }

        // Step 2: Select Date
        LocalDate selectedDate = selectDate();
        if (selectedDate == null) {
            displayError("Invalid date. Operation cancelled.");
            return;
        }

        // Step 3: Select Time
        LocalTime selectedTime = selectTime();
        if (selectedTime == null) {
            displayError("Invalid time. Operation cancelled.");
            return;
        }

        // Step 4: Enter City
        String city = enterCity();
        if (city == null || city.trim().isEmpty()) {
            displayError("Invalid city. Operation cancelled.");
            return;
        }

        // Step 5: Enter number of participants
        int participants = enterParticipants(selectedSport);
        if (participants == -1) {
            displayError("Invalid number of participants. Operation cancelled.");
            return;
        }

        // Validate and save match details
        if (organizeMatchController.validateMatchDetails(selectedSport, selectedDate, selectedTime, city, participants)) {
            organizeMatchController.setMatchDetails(selectedSport, selectedDate, selectedTime, city, participants);
            displaySuccess("Match details saved successfully!");
            displayMatchSummary();

            System.out.println("\nProceeding to field selection...");
            organizeMatchController.proceedToFieldSelection();
        } else {
            displayError("Invalid match details. Please check your inputs.");
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

    private void displayHeader() {
        System.out.println("\n=================================");
        System.out.println("    MATCH ORGANIZATION");
        System.out.println("=================================");
        System.out.println("Organizer: " + organizeMatchController.getOrganizer().getName() +
                          " " + organizeMatchController.getOrganizer().getSurname());
        System.out.println("=================================");
    }

    private void handleMenuChoice(String choice) {
        switch (choice) {
            case "1" -> displayMatchList();
            case "2" -> displayNewMatchForm();
            case "3" -> {
                organizeMatchController.navigateBack();
                running = false;
            }
            default -> displayError("Invalid option. Please try again.");
        }
    }

    private Sport selectSport() {
        System.out.println("\n--- SELECT SPORT ---");
        Sport[] sports = organizeMatchController.getAvailableSports();

        for (int i = 0; i < sports.length; i++) {
            System.out.println((i + 1) + ". " + sports[i].toString());
        }

        System.out.print("\nSelect sport (1-" + sports.length + "): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice >= 1 && choice <= sports.length) {
                return sports[choice - 1];
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private LocalDate selectDate() {
        System.out.println("\n--- SELECT DATE ---");
        System.out.print("Enter match date (format: dd/MM/yyyy, e.g., 15/01/2026): ");

        try {
            String dateInput = scanner.nextLine().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateInput, formatter);

            if (date.isBefore(LocalDate.now())) {
                displayError("Date cannot be in the past.");
                return null;
            }

            return date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalTime selectTime() {
        System.out.println("\n--- SELECT TIME ---");
        System.out.print("Enter match time (format: HH:mm, e.g., 18:30): ");

        try {
            String timeInput = scanner.nextLine().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeInput, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String enterCity() {
        System.out.println("\n--- SELECT CITY ---");
        System.out.print("Enter city name: ");
        return scanner.nextLine().trim();
    }

    private int enterParticipants(Sport sport) {
        System.out.println("\n--- NUMBER OF ADDITIONAL PARTICIPANTS ---");
        System.out.println("Sport: " + sport.getDisplayName());
        System.out.println("Total players needed: " + sport.getRequiredPlayers());
        System.out.println("You (organizer) count as the first player.");

        int maxAdditional = sport.getAdditionalParticipantsNeeded();
        System.out.print("Enter number of additional participants needed (1-" + maxAdditional + "): ");

        try {
            int participants = Integer.parseInt(scanner.nextLine().trim());
            if (sport.isValidAdditionalParticipants(participants)) {
                return participants;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private void displayMatchSummary() {
        System.out.println("\n=================================");
        System.out.println("    MATCH SUMMARY");
        System.out.println("=================================");
        System.out.println("Sport: " + organizeMatchController.getCurrentMatchBean().getSport().getDisplayName());
        System.out.println("Date: " + organizeMatchController.getCurrentMatchBean().getMatchDate());
        System.out.println("Time: " + organizeMatchController.getCurrentMatchBean().getMatchTime());
        System.out.println("City: " + organizeMatchController.getCurrentMatchBean().getCity());
        System.out.println("Participants: " + organizeMatchController.getCurrentMatchBean().getRequiredParticipants());
        System.out.println("=================================");
    }
}

