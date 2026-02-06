package controller;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.dao.dbms.DbmsDAOFactory;
import model.dao.filesystem.FileSystemDAOFactory;
import model.dao.memory.MemoryDAOFactory;
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
import exception.AuthorizationException;

public class ApplicationController {
    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());
    private final Deque<View> viewStack = new ArrayDeque<>();
    private ViewFactory viewFactory;
    private DAOFactory daoFactory;
    private model.notification.NotificationService notificationService;

    public ApplicationController() {
    }

    public ApplicationController(model.dao.DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.notificationService = new model.notification.NotificationService(daoFactory.getNotificationDAO());
    }

    public void start() {
        setupConfiguration();
        this.notificationService = new model.notification.NotificationService(daoFactory.getNotificationDAO());
        setupInterface();
        navigateToLogin();
    }

    @SuppressWarnings("resource")
    private void setupConfiguration() {
        var scanner = new Scanner(System.in);
        System.out.println("=== SPORTY CONFIGURATION ===");
        System.out.println("Select version:\n1. DEMO (No persistence)\n2. FULL (Persistence)");
        System.out.print("> ");
        boolean isDemo = scanner.nextLine().trim().equals("1");
        if (isDemo) {
            this.daoFactory = new MemoryDAOFactory();
            logger.info(() -> "App started in DEMO mode");
            return;
        }
        System.out.println("Select storage:\n1. FileSystem\n2. Database (DBMS)");
        System.out.print("> ");
        boolean isDbms = scanner.nextLine().trim().equals("2");
        if (isDbms) {
            this.daoFactory = new DbmsDAOFactory();
            logger.info("App started with persistence: DBMS");
        } else {
            this.daoFactory = new FileSystemDAOFactory();
            logger.info("App started with persistence: FILESYSTEM");
        }
    }

    @SuppressWarnings("resource")
    private void setupInterface() {
        var scanner = new Scanner(System.in);
        System.out.println("Select Interface:\n1. Graphic (JavaFX)\n2. CLI (Console)");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();
        this.viewFactory = choice.equals("1") ? new GraphicViewFactory() : new CLIViewFactory();
    }

    public void navigateToLogin() {
        var controller = new LoginController(daoFactory);
        var view = viewFactory.createLoginView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToHome(model.bean.UserBean userBean) throws ValidationException {
        User user = new User(
                userBean.getId(),
                userBean.getUsername(),
                userBean.getPassword(),
                userBean.getName(),
                userBean.getSurname(),
                Role.fromCode(userBean.getRole()));
        navigateToHome(user);
    }

    public void navigateToHome(User user) throws ValidationException {
        if (user.getRole() == Role.FIELD_MANAGER) {
            navigateToFieldManagerDashboard(user);
        } else {
            navigateToPlayerHome(user);
        }
    }

    private void navigateToPlayerHome(User user) {
        var controller = new HomeController(user, this, daoFactory);
        var view = viewFactory.createHomeView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToFieldManagerDashboard(User manager) throws ValidationException {
        var controller = new FieldManagerController(manager, daoFactory);
        var view = viewFactory.createFieldManagerView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToOrganizeMatch(User organizer) {
        try {
            var controller = new OrganizeMatchController(organizer, this);
            var view = viewFactory.createOrganizeMatchView(controller);
            view.setApplicationController(this);
            pushView(view);
        } catch (AuthorizationException e) {
            logger.warning("Authorization failed for organize match: " + e.getMessage());
        }
    }

    public void navigateToBookField(MatchBean match) {
        var controller = new BookFieldController(this, match);
        var view = viewFactory.createBookFieldView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToPayment(MatchBean matchBean) {
        var controller = new PaymentController(this, matchBean);
        var view = viewFactory.createPaymentView(controller);
        view.setApplicationController(this);
        controller.setView(view);
        pushView(view);
        controller.start();
    }

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

    public DAOFactory getDaoFactory() {
        return daoFactory;
    }

    public model.notification.NotificationService getNotificationService() {
        return notificationService;
    }

    public String getConfigurationInfo() {
        if (daoFactory instanceof MemoryDAOFactory) {
            return "Running DEMO version (data will not be persisted)";
        } else if (daoFactory instanceof DbmsDAOFactory) {
            return "Running FULL version with DBMS persistence";
        } else {
            return "Running FULL version with FILESYSTEM persistence";
        }
    }
}