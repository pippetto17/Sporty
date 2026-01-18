package view.addfieldview;

import controller.FieldManagerController;
import model.bean.FieldBean;
import model.domain.Sport;

import java.util.Scanner;

/**
 * CLI implementation of AddFieldView for adding new fields.
 */
public class CLIAddFieldView implements AddFieldView {
    private final FieldManagerController controller;
    private final Scanner scanner;

    public CLIAddFieldView(FieldManagerController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void display() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ADD NEW FIELD");
        System.out.println("=".repeat(50));

        try {
            FieldBean field = createFieldFromUserInput();
            if (field == null) {
                return;
            }

            controller.addNewField(field);
            displaySuccess("Field added successfully!");

        } catch (NumberFormatException e) {
            displayError("Invalid number format");
        } catch (Exception e) {
            displayError("Error adding field: " + e.getMessage());
        }
    }

    private FieldBean createFieldFromUserInput() {
        FieldBean field = new FieldBean();

        if (!collectBasicInfo(field)) {
            return null;
        }

        if (!collectSportInfo(field)) {
            return null;
        }

        if (!collectPricingInfo(field)) {
            return null;
        }

        collectAutoApprove(field);

        if (!confirmCreation()) {
            System.out.println("Cancelled.");
            return null;
        }

        return field;
    }

    private boolean collectBasicInfo(FieldBean field) {
        System.out.print("Structure Name: ");
        String structureName = scanner.nextLine().trim();
        if (structureName.isEmpty()) {
            displayError("Structure name is required");
            return false;
        }
        field.setStructureName(structureName);

        System.out.print("Field Name: ");
        String fieldName = scanner.nextLine().trim();
        if (fieldName.isEmpty()) {
            displayError("Field name is required");
            return false;
        }
        field.setName(fieldName);

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) {
            displayError("Address is required");
            return false;
        }
        field.setAddress(address);

        System.out.print("City: ");
        String city = scanner.nextLine().trim();
        if (city.isEmpty()) {
            displayError("City is required");
            return false;
        }
        field.setCity(city);

        return true;
    }

    private boolean collectSportInfo(FieldBean field) {
        System.out.println("\nAvailable Sports:");
        Sport[] sports = Sport.values();
        for (int i = 0; i < sports.length; i++) {
            System.out.printf("%d - %s%n", i, sports[i].getDisplayName());
        }
        System.out.print("Select sport (number): ");
        int sportIndex = Integer.parseInt(scanner.nextLine().trim());
        if (sportIndex < 0 || sportIndex >= sports.length) {
            displayError("Invalid sport selection");
            return false;
        }
        field.setSport(sports[sportIndex]);

        System.out.print("Indoor (y/n): ");
        String indoorInput = scanner.nextLine().trim().toLowerCase();
        field.setIndoor(indoorInput.equals("y") || indoorInput.equals("yes"));

        return true;
    }

    private boolean collectPricingInfo(FieldBean field) {
        System.out.print("Price per hour (€): ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        if (price <= 0) {
            displayError("Price must be positive");
            return false;
        }
        field.setPricePerHour(price);
        return true;
    }

    private void collectAutoApprove(FieldBean field) {
        System.out.print("Auto-approve bookings (y/n): ");
        String autoApprove = scanner.nextLine().trim().toLowerCase();
        field.setAutoApprove(autoApprove.equals("y") || autoApprove.equals("yes"));
    }

    private boolean confirmCreation() {
        System.out.print("\nConfirm creation (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        return confirm.equals("y") || confirm.equals("yes");
    }

    @Override
    public void displaySuccess(String message) {
        System.out.println("\n✓ " + message);
    }

    @Override
    public void displayError(String message) {
        System.err.println("\n✗ Error: " + message);
    }

    @Override
    public void close() {
        // Nothing to close in CLI
    }
}
