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
            FieldBean field = new FieldBean();

            // Structure Name
            System.out.print("Structure Name: ");
            String structureName = scanner.nextLine().trim();
            if (structureName.isEmpty()) {
                displayError("Structure name is required");
                return;
            }
            field.setStructureName(structureName);

            // Field Name
            System.out.print("Field Name: ");
            String fieldName = scanner.nextLine().trim();
            if (fieldName.isEmpty()) {
                displayError("Field name is required");
                return;
            }
            field.setName(fieldName);

            // Address
            System.out.print("Address: ");
            String address = scanner.nextLine().trim();
            if (address.isEmpty()) {
                displayError("Address is required");
                return;
            }
            field.setAddress(address);

            // City
            System.out.print("City: ");
            String city = scanner.nextLine().trim();
            if (city.isEmpty()) {
                displayError("City is required");
                return;
            }
            field.setCity(city);

            // Sport
            System.out.println("\nAvailable Sports:");
            Sport[] sports = Sport.values();
            for (int i = 0; i < sports.length; i++) {
                System.out.printf("%d - %s%n", i, sports[i].getDisplayName());
            }
            System.out.print("Select sport (number): ");
            int sportIndex = Integer.parseInt(scanner.nextLine().trim());
            if (sportIndex < 0 || sportIndex >= sports.length) {
                displayError("Invalid sport selection");
                return;
            }
            field.setSport(sports[sportIndex]);

            // Indoor/Outdoor
            System.out.print("Indoor (y/n): ");
            String indoorInput = scanner.nextLine().trim().toLowerCase();
            field.setIndoor(indoorInput.equals("y") || indoorInput.equals("yes"));

            // Price
            System.out.print("Price per hour (€): ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            if (price <= 0) {
                displayError("Price must be positive");
                return;
            }
            field.setPricePerHour(price);

            // Latitude (optional)
            System.out.print("Latitude (press Enter to skip): ");
            String latInput = scanner.nextLine().trim();
            if (!latInput.isEmpty()) {
                field.setLatitude(Double.parseDouble(latInput));
            }

            // Longitude (optional)
            System.out.print("Longitude (press Enter to skip): ");
            String lonInput = scanner.nextLine().trim();
            if (!lonInput.isEmpty()) {
                field.setLongitude(Double.parseDouble(lonInput));
            }

            // Auto-approve
            System.out.print("Auto-approve bookings (y/n): ");
            String autoApprove = scanner.nextLine().trim().toLowerCase();
            field.setAutoApprove(autoApprove.equals("y") || autoApprove.equals("yes"));

            // Confirm
            System.out.print("\nConfirm creation (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y") && !confirm.equals("yes")) {
                System.out.println("Cancelled.");
                return;
            }

            // Add field
            controller.addNewField(field);
            displaySuccess("Field added successfully!");

        } catch (NumberFormatException e) {
            displayError("Invalid number format");
        } catch (Exception e) {
            displayError("Error adding field: " + e.getMessage());
        }
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
