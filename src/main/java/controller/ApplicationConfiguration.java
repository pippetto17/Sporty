package controller;

import model.dao.DAOFactory;
import model.dao.dbms.DbmsDAOFactory;
import model.dao.filesystem.FileSystemDAOFactory;
import model.dao.memory.MemoryDAOFactory;
import view.factory.CLIViewFactory;
import view.factory.GraphicViewFactory;
import view.factory.ViewFactory;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Handles application configuration setup including persistence layer and UI
 * selection.
 * This class encapsulates the configuration logic previously scattered in
 * ApplicationController.
 */
public class ApplicationConfiguration {
    private static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getName());

    private final DAOFactory daoFactory;
    private final ViewFactory viewFactory;

    public ApplicationConfiguration(DAOFactory daoFactory, ViewFactory viewFactory) {
        this.daoFactory = daoFactory;
        this.viewFactory = viewFactory;
    }

    /**
     * Creates application configuration by prompting user for choices.
     * 
     * @return configured ApplicationConfiguration instance
     */
    public static ApplicationConfiguration create() {
        Scanner scanner = new Scanner(System.in);

        DAOFactory daoFactory = selectDAOFactory(scanner);
        ViewFactory viewFactory = selectViewFactory(scanner);

        return new ApplicationConfiguration(daoFactory, viewFactory);
    }

    /**
     * Prompts user to select the persistence layer.
     * 
     * @param scanner Scanner for user input
     * @return selected DAOFactory instance
     */
    private static DAOFactory selectDAOFactory(Scanner scanner) {
        System.out.println("=== SPORTY CONFIGURATION ===");
        System.out.println("Select version:");
        System.out.println("1. DEMO (No persistence)");
        System.out.println("2. FULL (Persistence)");
        System.out.print("> ");

        String versionChoice = scanner.nextLine().trim();

        if ("1".equals(versionChoice)) {
            logger.info("App started in DEMO mode");
            return new MemoryDAOFactory();
        }

        // FULL version - select storage type
        System.out.println("Select storage:");
        System.out.println("1. FileSystem");
        System.out.println("2. Database (DBMS)");
        System.out.print("> ");

        String storageChoice = scanner.nextLine().trim();

        return switch (storageChoice) {
            case "2" -> {
                logger.info("App started with persistence: DBMS");
                yield new DbmsDAOFactory();
            }
            case "1" -> {
                logger.info("App started with persistence: FILESYSTEM");
                yield new FileSystemDAOFactory(new DbmsDAOFactory());
            }
            default -> {
                logger.warning("Invalid storage choice, defaulting to DBMS");
                yield new DbmsDAOFactory();
            }
        };
    }

    /**
     * Prompts user to select the user interface type.
     * 
     * @param scanner Scanner for user input
     * @return selected ViewFactory instance
     */
    private static ViewFactory selectViewFactory(Scanner scanner) {
        System.out.println("Select Interface:");
        System.out.println("1. Graphic (JavaFX)");
        System.out.println("2. CLI (Console)");
        System.out.print("> ");

        String choice = scanner.nextLine().trim();

        return "1".equals(choice)
                ? new GraphicViewFactory()
                : new CLIViewFactory();
    }

    public DAOFactory getDaoFactory() {
        return daoFactory;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    /**
     * Returns a human-readable description of the current configuration.
     * 
     * @return configuration info string
     */
    public String getConfigurationInfo() {
        String persistenceInfo;
        if (daoFactory instanceof MemoryDAOFactory) {
            persistenceInfo = "DEMO version (data will not be persisted)";
        } else if (daoFactory instanceof DbmsDAOFactory) {
            persistenceInfo = "FULL version with DBMS persistence";
        } else {
            persistenceInfo = "FULL version with FILESYSTEM persistence";
        }

        String interfaceInfo = viewFactory instanceof GraphicViewFactory
                ? "Graphic (JavaFX)"
                : "CLI (Console)";

        return String.format("Running %s with %s interface", persistenceInfo, interfaceInfo);
    }
}
