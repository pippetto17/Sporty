package view.myfieldsview;

import controller.FieldManagerController;
import model.bean.FieldBean;

import java.util.List;

/**
 * CLI implementation of MyFieldsView showing all fields owned by manager.
 */
public class CLIMyFieldsView implements MyFieldsView {
    private final FieldManagerController controller;
    @SuppressWarnings("unused")
    private controller.ApplicationController applicationController;

    public CLIMyFieldsView(FieldManagerController controller) {
        this.controller = controller;
    }

    @Override
    public void setApplicationController(controller.ApplicationController appController) {
        this.applicationController = appController;
    }

    @Override
    public void display() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("MY FIELDS");
        System.out.println("=".repeat(50));

        try {
            List<FieldBean> fields = controller.getMyFields();
            displayFields(fields);
        } catch (Exception e) {
            displayError("Error loading fields: " + e.getMessage());
        }
    }

    @Override
    public void displayFields(List<FieldBean> fields) {
        if (fields == null || fields.isEmpty()) {
            System.out.println("\nNo fields yet. Add your first field to get started!");
            return;
        }

        System.out.println();
        double totalRevenue = 0;

        for (int i = 0; i < fields.size(); i++) {
            FieldBean field = fields.get(i);
            System.out.println((i + 1) + ". " + field.getName());
            System.out.println("   Sport: " + field.getSport().getDisplayName() +
                    " (" + (field.isIndoor() ? "Indoor" : "Outdoor") + ")");

            if (field.getStructureName() != null) {
                System.out.println("   Structure: " + field.getStructureName());
            }

            System.out.println("   Location: " + field.getAddress() + ", " + field.getCity());
            System.out.println("   Price: €" + String.format("%.2f", field.getPricePerHour()) + "/hour");

            if (field.isAutoApprove()) {
                System.out.println("   ✓ Auto-approve enabled");
            }

            // TODO: Add statistics when available
            // System.out.println(" Bookings: X | Revenue: €X.XX");

            System.out.println();
        }

        System.out.println("─".repeat(50));
        System.out.println("Total Fields: " + fields.size());
        if (totalRevenue > 0) {
            System.out.println("Total Revenue: €" + String.format("%.2f", totalRevenue));
        }
        System.out.println();
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
