package controller;

import exception.ServiceInitializationException;
import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.User;
import model.service.MatchService;
import view.factory.ViewFactory;
import view.factory.CLIViewFactory;
import view.factory.GraphicViewFactory;
import view.loginview.LoginView;
import view.homeview.HomeView;
import view.organizematchview.OrganizeMatchView;
import view.bookfieldview.BookFieldView;
import view.paymentview.PaymentView;
import view.recapview.RecapView;

import view.View;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.logging.Logger;

public class ApplicationController {

    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());

    public enum AppVersion {
        DEMO,
        FULL
    }

    public enum PersistenceMode {
        FILESYSTEM,
        DBMS
    }

    public enum InterfaceType {
        CLI,
        GUI
    }

    private final Deque<View> viewStack = new ArrayDeque<>();
    private ViewFactory viewFactory;
    private final DAOFactory.PersistenceType persistenceType;
    private final AppVersion appVersion;
    private final PersistenceMode persistenceMode;
    private InterfaceType currentInterface;

    public ApplicationController(AppVersion version, PersistenceMode persistenceMode) {
        this.appVersion = version;
        this.persistenceMode = persistenceMode;

        // Determina il tipo di persistenza
        if (version == AppVersion.DEMO) {
            this.persistenceType = DAOFactory.PersistenceType.MEMORY;
        } else {
            this.persistenceType = switch (persistenceMode) {
                case FILESYSTEM -> DAOFactory.PersistenceType.FILESYSTEM;
                case DBMS -> DAOFactory.PersistenceType.DBMS;
            };
        }
    }

    public void start() {
        chooseInterface();
        viewFactory = currentInterface == InterfaceType.GUI
                ? new GraphicViewFactory()
                : new CLIViewFactory();

        navigateToLogin();
    }

    private void chooseInterface() {
        Scanner scanner = new Scanner(System.in);
        boolean validChoice = false;

        while (!validChoice) {
            System.out.println("Choose interface:");
            System.out.println("1) Graphic (JavaFX)");
            System.out.println("2) CLI (Command Line)");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    currentInterface = InterfaceType.GUI;
                    validChoice = true;
                }
                case "2" -> {
                    currentInterface = InterfaceType.CLI;
                    validChoice = true;
                }
                default -> logger.warning("Invalid choice. Please try again.");
            }
        }

        logger.info(() -> "\n" + getConfigurationInfo());
        logger.info(() -> "Interface selected: " + currentInterface + "\n");
    }

    public void navigateToLogin() {
        LoginController loginController;
        loginController = new LoginController(persistenceType);
        LoginView loginView = viewFactory.createLoginView(loginController);
        loginView.setApplicationController(this);
        pushAndDisplay(loginView);
    }

    public void navigateToHome(User user) {
        // Check if user is a Field Manager - route to dashboard instead
        if (user.getRole() == model.domain.Role.FIELD_MANAGER.getCode()) {
            navigateToFieldManagerDashboard(user);
            return;
        }

        // Regular home view for Players and Organizers
        try {
            MatchService matchService = new MatchService(persistenceType);
            HomeController homeController = new HomeController(user, this, matchService);
            HomeView homeView = viewFactory.createHomeView(homeController);
            homeController.setHomeView(homeView); // Connect view to controller
            homeView.setApplicationController(this);
            pushAndDisplay(homeView);
        } catch (ServiceInitializationException | java.sql.SQLException e) {
            logger.severe("Error creating MatchService: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.severe("\tat " + element.toString());
            }
        }
    }

    public void navigateToFieldManagerDashboard(User fieldManager) {
        try {
            FieldManagerController fmController = new FieldManagerController(fieldManager, persistenceType);
            view.fieldmanagerview.FieldManagerView fieldManagerView = viewFactory.createFieldManagerView(fmController);
            fieldManagerView.setApplicationController(this);
            pushAndDisplay(fieldManagerView);
        } catch (Exception e) {
            logger.severe("Error navigating to Field Manager Dashboard: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.severe("\tat " + element.toString());
            }
        }
    }

    public void navigateToOrganizeMatch(User organizer) {
        OrganizeMatchController organizeMatchController = new OrganizeMatchController(organizer, this);
        OrganizeMatchView organizeMatchView = viewFactory.createOrganizeMatchView(organizeMatchController);
        organizeMatchView.setApplicationController(this);
        pushAndDisplay(organizeMatchView);
    }

    public void navigateToBookField(MatchBean matchBean) {
        BookFieldController bookFieldController = new BookFieldController(this);
        bookFieldController.setMatchBean(matchBean);
        BookFieldView bookFieldView = viewFactory.createBookFieldView(bookFieldController);
        bookFieldView.setApplicationController(this);
        pushAndDisplay(bookFieldView);
    }

    public void navigateToPayment(MatchBean matchBean) {
        PaymentController paymentController = new PaymentController(this);
        paymentController.setMatchBean(matchBean);
        PaymentView paymentView = viewFactory.createPaymentView(paymentController);
        paymentView.setApplicationController(this);
        pushAndDisplay(paymentView);
    }

    public void navigateToRecap(MatchBean matchBean) {
        RecapView recapView = viewFactory.createRecapView();
        recapView.setApplicationController(this);
        recapView.setMatchBean(matchBean);
        pushAndDisplay(recapView);
    }

    /**
     * Navigate to Add Field view for Field Managers.
     */
    public void navigateToAddField(FieldManagerController fieldManagerController) {
        try {
            view.addfieldview.AddFieldView addFieldView = viewFactory.createAddFieldView(fieldManagerController);
            pushAndDisplay(addFieldView);
        } catch (Exception e) {
            logger.severe("Error navigating to Add Field view: " + e.getMessage());
        }
    }

    /**
     * Navigate to My Fields view for Field Managers.
     */
    public void navigateToMyFields(FieldManagerController fieldManagerController) {
        try {
            view.myfieldsview.MyFieldsView myFieldsView = viewFactory.createMyFieldsView(fieldManagerController);
            pushAndDisplay(myFieldsView);
        } catch (Exception e) {
            logger.severe("Error navigating to My Fields view: " + e.getMessage());
        }
    }

    private void pushAndDisplay(View view) {
        if (!viewStack.isEmpty()) {
            viewStack.peekFirst().close();
        }
        viewStack.addFirst(view);
        view.display();
    }

    public void back() {
        if (viewStack.size() > 1) {
            View currentView = viewStack.removeFirst();
            currentView.close();
            View previousView = viewStack.peekFirst();
            previousView.display();
        } else {
            logger.info("Cannot go back from login screen.");
        }
    }

    public void logout() {
        // Svuota lo stack e torna al login
        while (viewStack.size() > 1) {
            View view = viewStack.removeFirst();
            view.close();
        }
        // Ricrea la view di login
        if (!viewStack.isEmpty()) {
            viewStack.removeFirst().close();
        }
        navigateToLogin();
    }

    public String getConfigurationInfo() {
        if (appVersion == AppVersion.DEMO) {
            return "Running DEMO version (data will not be persisted)";
        } else {
            return "Running FULL version with " + persistenceMode + " persistence";
        }
    }

    public DAOFactory.PersistenceType getPersistenceType() {
        return persistenceType;
    }
}
