package view.bookfieldview;

import controller.ApplicationController;
import controller.BookFieldController;
import model.bean.FieldBean;
import model.utils.Constants;

import java.util.List;
import java.util.Scanner;

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
        // Not needed for CLI version
    }

    @Override
    public void display() {
        running = true;
        displayHeader();

        // Search for available fields
        System.out.println("\nSearching for available fields...");
        List<FieldBean> fields = bookFieldController.searchAvailableFields();

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
            System.out.printf("\n%d. %s\n", (i + 1), field.getName());
            System.out.printf("   Sport: %s\n", field.getSport().getDisplayName());
            System.out.printf("   Address: %s\n", field.getAddress());
            System.out.printf("   Price: €%.2f/hour (€%.2f/person)\n",
                    field.getPricePerHour(), field.getPricePerPerson());
            System.out.printf("   Type: %s\n", field.isIndoor() ? "Indoor" : "Outdoor");

            if (field.getLatitude() != null && field.getLongitude() != null) {
                System.out.printf("   Location: %.4f, %.4f\n", field.getLatitude(), field.getLongitude());
            }
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
        System.out.println("Address: " + field.getAddress() + ", " + field.getCity());
        System.out.printf("Price per hour: €%.2f\n", field.getPricePerHour());
        System.out.printf("Price per person: €%.2f\n", field.getPricePerPerson());
        System.out.println("Type: " + (field.isIndoor() ? "Indoor" : "Outdoor"));

        if (field.getLatitude() != null && field.getLongitude() != null) {
            System.out.printf("Coordinates: %.4f, %.4f\n", field.getLatitude(), field.getLongitude());
            System.out.println("(Map view will be available in GUI version)");
        }
        System.out.println(SEPARATOR);
    }

    @Override
    public void displaySortOptions() {
        System.out.println("\n--- SORT OPTIONS ---");
        System.out.println("1. Price (Low to High)");
        System.out.println("2. Price (High to Low)");
        System.out.println("3. Name");
        System.out.println("4. Distance (coming soon)");
        System.out.print("Choose sorting option: ");

        String choice = scanner.nextLine().trim();
        model.service.FieldService.SortCriteria criteria = switch (choice) {
            case "1" -> model.service.FieldService.SortCriteria.PRICE_ASC;
            case "2" -> model.service.FieldService.SortCriteria.PRICE_DESC;
            case "3" -> model.service.FieldService.SortCriteria.NAME;
            case "4" -> model.service.FieldService.SortCriteria.DISTANCE;
            default -> null;
        };

        if (criteria != null) {
            bookFieldController.sortFields(criteria);
            displaySuccess("Fields sorted by " + criteria.getDisplayName());
        } else {
            displayError("Invalid sorting option.");
        }
    }

    @Override
    public void displayFilterOptions() {
        System.out.println("\n--- FILTER OPTIONS ---");
        System.out.println("1. Filter by price range");
        System.out.println("2. Filter by indoor/outdoor");
        System.out.println("3. Clear filters");
        System.out.print("Choose filter option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> filterByPrice();
            case "2" -> filterByIndoor();
            case "3" -> {
                bookFieldController.searchAvailableFields();
                displaySuccess("Filters cleared.");
            }
            default -> displayError("Invalid filter option.");
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
        System.out.println("\n" + SEPARATOR);
        System.out.println("    BOOK FIELD");
        System.out.println(SEPARATOR);
        System.out.println("Match: " + bookFieldController.getCurrentMatchBean().getSport().getDisplayName());
        System.out.println("Date: " + bookFieldController.getCurrentMatchBean().getMatchDate());
        System.out.println("Time: " + bookFieldController.getCurrentMatchBean().getMatchTime());
        System.out.println("City: " + bookFieldController.getCurrentMatchBean().getCity());
        System.out.println("Participants: " + bookFieldController.getCurrentMatchBean().getRequiredParticipants());
        System.out.println(SEPARATOR);
    }

    private void displayMenu() {
        System.out.println("\n--- OPTIONS ---");
        System.out.println("1. Select a field (enter field number)");
        System.out.println("2. Sort fields");
        System.out.println("3. Filter fields");
        System.out.println("4. View field details");
        System.out.println("5. Refresh search");
        System.out.println("6. Back");
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
            case "2" -> displaySortOptions();
            case "3" -> displayFilterOptions();
            case "4" -> {
                System.out.print("Enter field number: ");
                try {
                    int fieldNum = Integer.parseInt(scanner.nextLine().trim());
                    displayFieldDetails(fieldNum - 1);
                } catch (NumberFormatException e) {
                    displayError("Invalid field number.");
                }
            }
            case "5" -> {
                bookFieldController.searchAvailableFields();
                displaySuccess("Search refreshed.");
            }
            case "6" -> {
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
        System.out.printf("Total cost: €%.2f (€%.2f per person)\n",
                selectedField.getPricePerHour(), selectedField.getPricePerPerson());
        System.out.println(SEPARATOR);

        System.out.print("\nConfirm selection? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            displaySuccess("Field booked successfully!");
            System.out.println("\nProceeding to payment...");
            bookFieldController.proceedToPayment();
            running = false;
        } else {
            bookFieldController.setSelectedField(null);
            displaySuccess("Selection cancelled.");
        }
    }

    private void filterByPrice() {
        System.out.print("Enter minimum price per person: ");
        try {
            double minPrice = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter maximum price per person: ");
            double maxPrice = Double.parseDouble(scanner.nextLine().trim());

            List<FieldBean> filtered = bookFieldController.filterByPriceRange(minPrice, maxPrice);

            if (filtered.isEmpty()) {
                displayError("No fields found in this price range.");
            } else {
                displaySuccess(FOUND_PREFIX + filtered.size() + " fields in price range €" +
                        minPrice + " - €" + maxPrice);
            }
        } catch (NumberFormatException e) {
            displayError("Invalid price format.");
        }
    }

    private void filterByIndoor() {
        System.out.println("1. Indoor only");
        System.out.println("2. Outdoor only");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1") || choice.equals("2")) {
            boolean indoor = choice.equals("1");
            List<FieldBean> filtered = bookFieldController.filterByIndoor(indoor);

            if (filtered.isEmpty()) {
                displayError("No " + (indoor ? "indoor" : "outdoor") + " fields found.");
            } else {
                displaySuccess(FOUND_PREFIX + filtered.size() + " " +
                        (indoor ? "indoor" : "outdoor") + " fields.");
            }
        } else {
            displayError("Invalid choice.");
        }
    }
}
