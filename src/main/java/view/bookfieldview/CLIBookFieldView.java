package view.bookfieldview;

import controller.ApplicationController;
import controller.BookFieldController;
import model.bean.FieldBean;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static model.utils.Constants.FOUND_PREFIX;
import static model.utils.Constants.SEPARATOR;

public class CLIBookFieldView implements BookFieldView {
    private final BookFieldController bookFieldController;
    private final Scanner scanner;
    private boolean running;

    public CLIBookFieldView(BookFieldController bookFieldController) {
        this.bookFieldController = bookFieldController;
        this.scanner = new Scanner(System.in);
        this.running = false;
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // Intentionally empty
    }

    @Override
    public void display() {
        running = true;
        displayHeader();
        List<FieldBean> fields;
        if (bookFieldController.isStandaloneMode()) {
            fields = promptStandaloneSearch();
            if (fields == null) {
                bookFieldController.navigateBack();
                return;
            }
        } else {
            System.out.println("\nSearching for available fields...");
            fields = bookFieldController.searchAvailableFields();
        }
        if (fields == null || fields.isEmpty()) {
            displayError("No fields available for the selected criteria.");
            bookFieldController.navigateBack();
            return;
        }
        displaySuccess(FOUND_PREFIX + fields.size() + " available fields!");
        while (running) {
            displayAvailableFields();
            displayMenu();
            String choice = scanner.nextLine().trim();
            handleMenuChoice(choice);
        }
    }

    private List<FieldBean> promptStandaloneSearch() {
        System.out.println("\n=== BOOK FIELD - SEARCH PARAMETERS ===");
        System.out.print("Select sport (");
        model.domain.Sport[] sports = model.domain.Sport.values();
        for (int i = 0; i < sports.length; i++) {
            System.out.print((i + 1) + "=" + sports[i].getDisplayName());
            if (i < sports.length - 1)
                System.out.print(", ");
        }
        System.out.print("): ");
        int sportChoice;
        try {
            sportChoice = Integer.parseInt(scanner.nextLine().trim());
            if (sportChoice < 1 || sportChoice > sports.length) {
                displayError("Invalid sport selection");
                return Collections.emptyList();
            }
        } catch (NumberFormatException e) {
            displayError("Invalid input");
            return Collections.emptyList();
        }
        System.out.print("Enter city: ");
        String city = scanner.nextLine().trim();
        if (city.isEmpty()) {
            displayError("City cannot be empty");
            return Collections.emptyList();
        }
        System.out.print("Enter date (yyyy-mm-dd): ");
        String dateStr = scanner.nextLine().trim();
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            bookFieldController.getCurrentMatchBean().setMatchDate(date);
        } catch (Exception e) {
            displayError("Invalid date format");
            return Collections.emptyList();
        }
        System.out.print("Enter time (HH:mm): ");
        String timeStr = scanner.nextLine().trim();
        try {
            java.time.LocalTime time = java.time.LocalTime.parse(timeStr);
            bookFieldController.getCurrentMatchBean().setMatchTime(time);
        } catch (Exception e) {
            displayError("Invalid time format");
            return Collections.emptyList();
        }
        System.out.println("\nSearching for available fields...");
        return bookFieldController.searchFieldsForDirectBooking(
                sports[sportChoice - 1],
                city,
                bookFieldController.getCurrentMatchBean().getMatchDate(),
                bookFieldController.getCurrentMatchBean().getMatchTime());
    }

    @Override
    public void close() {
        running = false;
    }

    @Override
    public void displayAvailableFields() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("    AVAILABLE FIELDS");
        System.out.println(SEPARATOR);
        List<FieldBean> fields = bookFieldController.getAvailableFields();
        if (fields == null || fields.isEmpty()) {
            System.out.println("No fields to display.");
            return;
        }
        for (int i = 0; i < fields.size(); i++) {
            FieldBean field = fields.get(i);
            System.out.printf("%n%d. %s%n", (i + 1), field.getName());
            System.out.printf("   Sport: %s%n", field.getSport().getDisplayName());
            System.out.printf("   City: %s%n", field.getCity());
        }
        System.out.println("\n" + SEPARATOR);
    }

    @Override
    public void displayFieldDetails(int fieldIndex) {
        List<FieldBean> fields = bookFieldController.getAvailableFields();
        if (fields == null || fieldIndex < 0 || fieldIndex >= fields.size()) {
            displayError("Invalid field selection.");
            return;
        }
        FieldBean field = fields.get(fieldIndex);
        System.out.println("\n" + SEPARATOR);
        System.out.println("    FIELD DETAILS");
        System.out.println(SEPARATOR);
        System.out.println("Name: " + field.getName());
        System.out.println("Sport: " + field.getSport().getDisplayName());
        System.out.println("City: " + field.getCity());
        System.out.println(SEPARATOR);
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
        System.out.println("\n" + SEPARATOR);
        System.out.println("    BOOK FIELD");
        System.out.println(SEPARATOR);
        System.out.println("Match: " + bookFieldController.getCurrentMatchBean().getSport().getDisplayName());
        System.out.println("Date: " + bookFieldController.getCurrentMatchBean().getMatchDate());
        System.out.println("Time: " + bookFieldController.getCurrentMatchBean().getMatchTime());
        System.out.println("City: " + bookFieldController.getCurrentMatchBean().getCity());
        System.out
                .println("Participants: " + bookFieldController.getCurrentMatchBean().getSport().getRequiredPlayers());
        System.out.println(SEPARATOR);
    }

    private void displayMenu() {
        System.out.println("\n--- OPTIONS ---");
        System.out.println("1. Select a field (enter field number)");
        System.out.println("2. View field details");
        System.out.println("3. Refresh search");
        System.out.println("4. Back");
        System.out.print("Choose an option: ");
    }

    private void handleMenuChoice(String choice) {
        switch (choice) {
            case "1" -> {
                System.out.print("Enter field number to select: ");
                try {
                    int fieldNum = Integer.parseInt(scanner.nextLine().trim());
                    selectField(fieldNum - 1);
                } catch (NumberFormatException e) {
                    displayError("Invalid field number.");
                }
            }
            case "2" -> {
                System.out.print("Enter field number: ");
                try {
                    int fieldNum = Integer.parseInt(scanner.nextLine().trim());
                    displayFieldDetails(fieldNum - 1);
                } catch (NumberFormatException e) {
                    displayError("Invalid field number.");
                }
            }
            case "3" -> {
                bookFieldController.searchAvailableFields();
                displaySuccess("Search refreshed.");
            }
            case "4" -> {
                bookFieldController.navigateBack();
                running = false;
            }
            default -> displayError("Invalid option. Please try again.");
        }
    }

    private void selectField(int fieldIndex) {
        List<FieldBean> fields = bookFieldController.getAvailableFields();
        if (fields == null || fieldIndex < 0 || fieldIndex >= fields.size()) {
            displayError("Invalid field selection.");
            return;
        }
        FieldBean selectedField = fields.get(fieldIndex);
        bookFieldController.setSelectedField(selectedField);
        System.out.println("\n" + SEPARATOR);
        System.out.println("    FIELD SELECTED");
        System.out.println(SEPARATOR);
        System.out.println("Field: " + selectedField.getName());
        System.out.println(SEPARATOR);
        System.out.print("\nConfirm selection? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y") || confirm.equals("yes")) {
            try {
                displaySuccess("Field booked successfully!");
                System.out.println("\nProceeding to payment...");
                bookFieldController.proceedToPayment();
                running = false;
            } catch (exception.ValidationException e) {
                displayError(e.getMessage());
            }
        } else {
            bookFieldController.setSelectedField(null);
            displaySuccess("Selection cancelled.");
        }
    }
}