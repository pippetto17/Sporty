package view.recapview;

import controller.ApplicationController;
import model.bean.MatchBean;
import model.utils.Constants;

import java.util.Scanner;

public class CLIRecapView implements RecapView {
    private ApplicationController applicationController;
    private MatchBean matchBean;
    private final Scanner scanner;

    public CLIRecapView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @Override
    public void setMatchBean(MatchBean matchBean) {
        this.matchBean = matchBean;
    }

    @Override
    public void display() {
        if (matchBean == null) {
            System.out.println("\n" + Constants.ERROR_MATCHBEAN_NULL);
            return;
        }

        displayRecap();
        displayMenu();
    }

    private void displayRecap() {
        System.out.println("\n" + Constants.SEPARATOR);
        System.out.println("    MATCH CREATED SUCCESSFULLY!");
        System.out.println(Constants.SEPARATOR);
        System.out.println();
        System.out.println("Match Details:");
        System.out.println("  Sport:        " + (matchBean.getSport() != null ? matchBean.getSport().getDisplayName() : "N/A"));
        System.out.println("  Date:         " + (matchBean.getMatchDate() != null ? matchBean.getMatchDate() : "N/A"));
        System.out.println("  Time:         " + (matchBean.getMatchTime() != null ? matchBean.getMatchTime() : "N/A"));
        System.out.println("  Location:     " + (matchBean.getCity() != null ? matchBean.getCity() : "N/A"));
        System.out.println("  Organizer:    " + (matchBean.getOrganizerUsername() != null ? matchBean.getOrganizerUsername() : "N/A"));
        System.out.println("  Players:      0/" + matchBean.getRequiredParticipants());

        if (matchBean.getFieldId() != null) {
            System.out.println("  Field:        " + matchBean.getFieldId());
        }

        if (matchBean.getPricePerPerson() != null) {
            System.out.println("  Price/Person: €" + String.format("%.2f", matchBean.getPricePerPerson()));
        }

        System.out.println("  Status:       " + (matchBean.getStatus() != null ? matchBean.getStatus() : "CONFIRMED"));
        System.out.println(Constants.SEPARATOR);
    }

    private void displayMenu() {
        System.out.println("\nOptions:");
        System.out.println("1. Invite players (coming soon)");
        System.out.println("2. Back to Home");
        System.out.print(Constants.PROMPT_CHOOSE_OPTION);

        String choice = scanner.nextLine().trim();
        handleChoice(choice);
    }

    private void handleChoice(String choice) {
        switch (choice) {
            case "1" -> {
                System.out.println(Constants.INFO_INVITE_COMING_SOON);
                displayMenu();
            }
            case "2" -> navigateHome();
            default -> {
                System.out.println(Constants.ERROR_INVALID_OPTION);
                displayMenu();
            }
        }
    }

    private void navigateHome() {
        if (applicationController != null) {
            for (int i = 0; i < 4; i++) {
                applicationController.back();
            }
        }
    }

    @Override
    public void close() {
        // No resources to close in CLI
    }
}
