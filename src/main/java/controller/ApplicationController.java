package controller;

import exception.AuthorizationException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.domain.Role;
import model.domain.User;
import view.View;
import view.factory.ViewFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

public class ApplicationController {
    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());
    private final Deque<View> viewStack = new ArrayDeque<>();
    private ViewFactory viewFactory;
    private DAOFactory daoFactory;

    public ApplicationController() {
    }

    public ApplicationController(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public void start() {
        ApplicationConfiguration config = ApplicationConfiguration.create();
        this.daoFactory = config.getDaoFactory();
        this.viewFactory = config.getViewFactory();
        navigateToLogin();
    }

    public void navigateToLogin() {
        var controller = new LoginController(daoFactory);
        var view = viewFactory.createLoginView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToHome(UserBean userBean) throws ValidationException {
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

    public void navigateToJoinMatchPayment(MatchBean matchBean, User user) {
        var controller = new PaymentController(this, matchBean, user);
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

    public String getConfigurationInfo() {
        ApplicationConfiguration config = new ApplicationConfiguration(daoFactory, viewFactory);
        return config.getConfigurationInfo();
    }
}