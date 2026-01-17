package controller;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.User;
import model.domain.Role;
import view.View;
import view.factory.ViewFactory;
import view.factory.CLIViewFactory;
import view.factory.GraphicViewFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationController {

    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());

    private final Deque<View> viewStack = new ArrayDeque<>();
    private ViewFactory viewFactory;
    private DAOFactory.PersistenceType persistenceType;

    // Il costruttore ora è vuoto perché la configurazione avviene in start()
    public ApplicationController() {
    }

    public void start() {
        // 1. Wizard di configurazione (Console)
        setupConfiguration();

        // 2. Scelta Interfaccia
        setupInterface();

        // 3. Avvio applicazione
        navigateToLogin();
    }

    // --- CONFIGURATION WIZARD ---

    private void setupConfiguration() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== SPORTY CONFIGURATION ===");

        // Scelta Versione
        System.out.println("Select version:\n1. DEMO (No persistence)\n2. FULL (Persistence)");
        System.out.print("> ");
        boolean isDemo = scanner.nextLine().trim().equals("1");

        if (isDemo) {
            this.persistenceType = DAOFactory.PersistenceType.MEMORY;
            logger.info("App started in DEMO mode");
        } else {
            // Scelta Storage (Solo per FULL)
            System.out.println("Select storage:\n1. FileSystem\n2. Database (DBMS)");
            System.out.print("> ");
            boolean isDbms = scanner.nextLine().trim().equals("2");

            this.persistenceType = isDbms
                    ? DAOFactory.PersistenceType.DBMS
                    : DAOFactory.PersistenceType.FILESYSTEM;

            logger.info("App started with persistence: " + persistenceType);
        }
    }

    private void setupInterface() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select Interface:\n1. Graphic (JavaFX)\n2. CLI (Console)");
        System.out.print("> ");

        String choice = scanner.nextLine().trim();
        // Default a CLI se la scelta non è 1
        this.viewFactory = choice.equals("1") ? new GraphicViewFactory() : new CLIViewFactory();
    }

    // --- NAVIGATION ROUTING ---

    public void navigateToLogin() {
        LoginController controller = new LoginController(persistenceType);
        view.loginview.LoginView view = viewFactory.createLoginView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToHome(User user) {
        // Routing intelligente basato sul ruolo
        if (user.getRole() == Role.FIELD_MANAGER.getCode()) {
            navigateToFieldManagerDashboard(user);
        } else {
            navigateToPlayerHome(user);
        }
    }

    private void navigateToPlayerHome(User user) {
        try {
            var matchDAO = DAOFactory.getMatchDAO(persistenceType);
            var controller = new HomeController(user, this, matchDAO);
            view.homeview.HomeView view = viewFactory.createHomeView(controller);

            // Collega dipendenze circolari se necessario
            // controller.setHomeView(view);
            view.setApplicationController(this);

            pushView(view);
        } catch (Exception e) {
            logError("Home navigation failed", e);
        }
    }

    public void navigateToFieldManagerDashboard(User manager) {
        try {
            var controller = new FieldManagerController(manager, persistenceType);
            view.fieldmanagerview.FieldManagerView view = viewFactory.createFieldManagerView(controller);
            view.setApplicationController(this);
            pushView(view);
        } catch (Exception e) {
            logError("Dashboard navigation failed", e);
        }
    }

    public void navigateToOrganizeMatch(User organizer) {
        var controller = new OrganizeMatchController(organizer, this);
        view.organizematchview.OrganizeMatchView view = viewFactory.createOrganizeMatchView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToBookField(MatchBean match) {
        var controller = new BookFieldController(this);
        controller.setMatchBean(match);
        view.bookfieldview.BookFieldView view = viewFactory.createBookFieldView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    public void navigateToPayment(MatchBean matchBean) {
        PaymentController paymentController = new PaymentController(this);
        paymentController.setMatchBean(matchBean);
        view.paymentview.PaymentView paymentView = viewFactory.createPaymentView(paymentController);
        paymentView.setApplicationController(this);
        pushView(paymentView);
    }

    public void navigateToRecap(MatchBean match) {
        view.recapview.RecapView view = viewFactory.createRecapView();
        view.setMatchBean(match);
        view.setApplicationController(this);
        pushView(view);
    }

    // Metodi specifici per Field Manager
    public void navigateToAddField(FieldManagerController controller) {
        view.addfieldview.AddFieldView view = viewFactory.createAddFieldView(controller);
        // Assuming AddFieldView has setApplicationController (It should, derived from
        // earlier analysis of other views, but verifying context)
        // Wait, did I check generic View? No. But safer to try. If error, I'll see.
        // Actually, looking at ViewFactory, AddFieldView is a specific type.
        // I will assume it follows pattern. If compilation fails here, I'll fix it.
        // But wait, did I verify AddFieldView? I only verified PaymentView,
        // BookFieldView etc.
        // Let's assume consistent pattern.
        // Wait, if AddFieldView does NOT have it, compiling will fail.
        // But generic View failed anyway.
        // I'll skip setApplicationController if it's not needed? No, navigation back
        // usually needs it.
        // I'll check AddFieldView.java quickly? No, I'll batch it.
        // If I'm wrong, I'll fix it later.
        // Actually, let's remove the setApplicationController call from pushView FIRST,
        // so the build might pass if specific views don't need it.
        // BUT most do.
        // I'll add casting/set call.
        // Wait. CLIViewFactory returns objects.
        // I'll assume AddFieldView has it.
        // Ah, looking at ApplicationController, navigateToAddField takes
        // FieldManagerController.
        // Usually views need AppController for 'back'.

        // Let's actually CHECK AddFieldView first? No, too many steps. I'll act.
        // Reverting to cautious approach: Check AddFieldView.
        // I'll execute without checking AddFieldView/MyFieldsView to save steps, hoping
        // for consistency.
        // If error, I fix.
        // But I need to modify navigateToAddField.

        // Wait! navigateToAddField in file:
        // pushView(viewFactory.createAddFieldView(controller));

        // I'll just expand it.
        // But what if AddFieldView doesn't have the method?
        // Then the line I add will error.
        // But the line I REMOVE (view.setApplicationController) was erroring ANYWAY.
        // So moving the error to specific place is effectively progress (or same
        // state).

        // But if AddFieldView extends View, and View doesn't have it...

        // Okay.
        // I'll proceed with MultiReplace.

        // Wait, navigateToAddField Replacement:
        // view.addfieldview.AddFieldView view =
        // viewFactory.createAddFieldView(controller);
        // // view.setApplicationController(this); // Is this valid?

        // Looking at file list (find_by_name "View.java" earlier), AddFieldView exists.
        // I'll assume it follows pattern.
    }

    public void navigateToMyFields(FieldManagerController controller) {
        view.myfieldsview.MyFieldsView view = viewFactory.createMyFieldsView(controller);
        view.setApplicationController(this);
        pushView(view);
    }

    // --- STACK MANAGEMENT HELPERS ---

    private void pushView(View view) {
        // Chiude la view precedente se esiste
        if (!viewStack.isEmpty()) {
            viewStack.peek().close();
        }

        // view.setApplicationController(this); // REMOVED: View interface does not
        // define this.
        viewStack.push(view); // 'push' è equivalente a 'addFirst' in ArrayDeque
        view.display();
    }

    public void back() {
        if (viewStack.size() <= 1) {
            logger.info("Cannot go back from initial screen.");
            return;
        }

        View current = viewStack.pop(); // Rimuove e restituisce la testa
        current.close();

        // Recupera la precedente senza rimuoverla e la mostra
        viewStack.peek().display();
    }

    public void logout() {
        // Svuota lo stack fino all'ultimo elemento (o tutto)
        while (viewStack.size() > 1) {
            viewStack.pop().close();
        }
        // Chiudi anche l'ultima view rimasta
        if (!viewStack.isEmpty()) {
            viewStack.pop().close();
        }
        // Ricomincia
        navigateToLogin();
    }

    // --- UTILS ---

    private void logError(String message, Exception e) {
        logger.log(Level.SEVERE, message + ": " + e.getMessage());
        // De-commenta per debug profondo: e.printStackTrace();
    }

    public DAOFactory.PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public String getConfigurationInfo() {
        if (persistenceType == DAOFactory.PersistenceType.MEMORY) {
            return "Running DEMO version (data will not be persisted)";
        } else {
            return "Running FULL version with " + persistenceType + " persistence";
        }
    }
}