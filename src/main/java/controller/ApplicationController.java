package controller;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.Role;
import model.domain.User;
import view.View;
import view.factory.CLIViewFactory;
import view.factory.GraphicViewFactory;
import view.factory.ViewFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.logging.Logger;

import exception.ValidationException;

public class ApplicationController {

    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());

    private final Deque<View> viewStack = new ArrayDeque<>();
    private ViewFactory viewFactory;
    private DAOFactory.PersistenceType persistenceType;

    public void start() {
        setupConfiguration();
        setupInterface();
        navigateToLogin();
    }

    // --- CONFIGURATION WIZARD ---

    @SuppressWarnings("resource")
    private void setupConfiguration() {
        var scanner = new Scanner(System.in);
        System.out.println("=== SPORTY CONFIGURATION ===");
        System.out.println("Select version:\n1. DEMO (No persistence)\n2. FULL (Persistence)");
        System.out.print("> ");

        boolean isDemo = scanner.nextLine().trim().equals("1");

        if (isDemo) {
            this.persistenceType = DAOFactory.PersistenceType.MEMORY;
            logger.info(() -> "App started in DEMO mode");
            return;
        }

        System.out.println("Select storage:\n1. FileSystem\n2. Database (DBMS)");
        System.out.print("> ");
        boolean isDbms = scanner.nextLine().trim().equals("2");

        this.persistenceType = isDbms ? DAOFactory.PersistenceType.DBMS : DAOFactory.PersistenceType.FILESYSTEM;
        logger.info(() -> String.format("App started with persistence: %s", persistenceType));
    }

    @SuppressWarnings("resource")
    private void setupInterface() {
        var scanner = new Scanner(System.in);
        System.out.println("Select Interface:\n1. Graphic (JavaFX)\n2. CLI (Console)");
        System.out.print("> ");

        String choice = scanner.nextLine().trim();
        this.viewFactory = choice.equals("1") ? new GraphicViewFactory() : new CLIViewFactory();
    }

    // --- NAVIGATION ROUTING ---

    public void navigateToLogin() {
        var controller = new LoginController(persistenceType);
        var view = viewFactory.createLoginView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToHome(User user) throws ValidationException {
        if (user.getRole() == Role.FIELD_MANAGER.getCode()) {
            navigateToFieldManagerDashboard(user);
        } else {
            navigateToPlayerHome(user);
        }
    }

    private void navigateToPlayerHome(User user) {
        var matchDAO = DAOFactory.getMatchDAO(persistenceType);
        var controller = new HomeController(user, this, matchDAO);
        var view = viewFactory.createHomeView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToFieldManagerDashboard(User manager) throws ValidationException {
        var controller = new FieldManagerController(manager, persistenceType);
        var view = viewFactory.createFieldManagerView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToOrganizeMatch(User organizer) {
        var controller = new OrganizeMatchController(organizer, this);
        var view = viewFactory.createOrganizeMatchView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToBookField(MatchBean match) {
        var controller = new BookFieldController(this);
        controller.setMatchBean(match);
        var view = viewFactory.createBookFieldView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToBookFieldStandalone(User user) {
        var controller = new BookFieldController(this);
        controller.setStandaloneMode(true);

        var contextBean = new MatchBean();
        contextBean.setOrganizerUsername(user.getUsername());
        controller.setMatchBean(contextBean);

        var view = viewFactory.createBookFieldView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToPayment(MatchBean matchBean) {
        var controller = new PaymentController(this);
        controller.setMatchBean(matchBean);
        var view = viewFactory.createPaymentView(controller);
        view.setApplicationController(this);
        controller.setView(view);
        pushView(view);
        controller.start();
    }

    public void navigateToPaymentForBooking(model.bean.FieldBean fieldBean, MatchBean contextBean) {
        var controller = new PaymentController(this);
        controller.setBookingMode(fieldBean, contextBean);
        var view = viewFactory.createPaymentView(controller);
        view.setApplicationController(this);
        controller.setView(view);
        pushView(view);
        controller.start();
    }

    public void navigateToPaymentForJoin(MatchBean matchBean, User user) {
        var controller = new PaymentController(this);
        controller.setJoinMode(matchBean, user);
        var view = viewFactory.createPaymentView(controller);
        view.setApplicationController(this);
        controller.setView(view);
        pushView(view);
        controller.start();
    }

    public void navigateToJoinMatch(MatchBean matchBean, User user) {
        var controller = new JoinMatchController(user, this);
        controller.setMatch(matchBean);
        var view = viewFactory.createJoinMatchView(controller);
        view.setApplicationController(this);
        controller.setView(view);
        pushView(view);
        controller.start();
    }

    public void navigateToAddField(FieldManagerController controller) {
        var view = viewFactory.createAddFieldView(controller);
        pushView(view);
    }

    public void navigateToMyFields(FieldManagerController controller) {
        var view = viewFactory.createMyFieldsView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    // --- STACK MANAGEMENT ---

    private void pushView(View view) {
        if (!viewStack.isEmpty()) {
            viewStack.peek().close();
        }
        viewStack.push(view);
        view.display();
    }

    public void back() {
        if (viewStack.size() <= 1) {
            logger.info("Cannot go back from initial screen.");
            return;
        }

        viewStack.pop().close();
        viewStack.peek().display();
    }

    public View getCurrentView() {
        return viewStack.peek();
    }

    public void logout() {
        while (viewStack.size() > 1) {
            viewStack.pop().close();
        }
        if (!viewStack.isEmpty()) {
            viewStack.pop().close();
        }
        navigateToLogin();
    }

    // --- GETTERS ---

    public DAOFactory.PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public String getConfigurationInfo() {
        return persistenceType == DAOFactory.PersistenceType.MEMORY
                ? "Running DEMO version (data will not be persisted)"
                : "Running FULL version with " + persistenceType + " persistence";
    }
}
