package view.LoginView;

import controller.ApplicationController;
import controller.LoginController;
import model.bean.UserBean;
import model.domain.User;

import java.util.Scanner;

public class CLILoginView implements LoginView {
    private final LoginController loginController;
    private ApplicationController applicationController;
    private final Scanner scanner;
    private boolean running;

    public CLILoginView(LoginController loginController) {
        this.loginController = loginController;
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
            displayMainMenu();

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "3" -> {
                    running = false;
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    @Override
    public void close() {
        running = false;
    }

    private void displayHeader() {
        System.out.println("\n=================================");
        System.out.println("    SPORTY APPLICATION");
        System.out.println("=================================");
        System.out.println(applicationController.getConfigurationInfo());
        System.out.println("=================================");
        System.out.println("    Login System");
        System.out.println("=================================");
    }

    private void displayMainMenu() {
        System.out.println("\n1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private void handleLogin() {
        System.out.println("\n--- LOGIN ---");
        UserBean userBean = getUserCredentials();

        try {
            User user = loginController.login(userBean);
            if (user != null) {
                displayLoginSuccess(user.getUsername());
                System.out.println("Name: " + user.getName() + " " + user.getSurname());
                System.out.println("Role: " + getRoleString(user.getRole()));

                // Naviga alla home tramite ApplicationController
                running = false; // Ferma il loop corrente
                applicationController.navigateToHome(user);
            } else {
                displayLoginError("Invalid username or password");
            }
        } catch (Exception e) {
            displayLoginError(e.getMessage());
        }
    }

    private void handleRegister() {
        System.out.println("\n--- REGISTER ---");
        UserBean userBean = getUserCredentials();

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Surname: ");
        String surname = scanner.nextLine();

        System.out.print("Role (1=PLAYER, 2=ORGANIZER): ");
        int role;
        try {
            role = Integer.parseInt(scanner.nextLine());
            if (role < 1 || role > 2) {
                displayLoginError("Invalid role. Please enter 1 or 2.");
                return;
            }
        } catch (NumberFormatException e) {
            displayLoginError("Invalid role. Please enter a number.");
            return;
        }

        try {
            loginController.register(userBean, name, surname, role);
            System.out.println("✓ Registration successful! You can now login.");
        } catch (Exception e) {
            displayLoginError(e.getMessage());
        }
    }

    @Override
    public UserBean getUserCredentials() {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        return new UserBean(username, password);
    }

    @Override
    public void displayLoginSuccess(String username) {
        System.out.println("\n✓ Welcome, " + username + "!");
    }

    @Override
    public void displayLoginError(String message) {
        System.out.println("\n✗ Error: " + message);
    }

    private String getRoleString(int role) {
        return switch (role) {
            case 1 -> "PLAYER";
            case 2 -> "ORGANIZER";
            default -> "UNKNOWN";
        };
    }
}
