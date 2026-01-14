package org.example;

import controller.ApplicationController;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Selezione versione
            System.out.println("Select application version:");
            System.out.println("1. DEMO (no persistence)");
            System.out.println("2. FULL (with persistence)");
            System.out.print("Choice: ");
            String versionChoice = scanner.nextLine();

            ApplicationController.AppVersion version = versionChoice.equals("1")
                ? ApplicationController.AppVersion.DEMO
                : ApplicationController.AppVersion.FULL;

            // Selezione persistenza (solo per FULL)
            ApplicationController.PersistenceMode persistenceMode = ApplicationController.PersistenceMode.FILESYSTEM;
            if (version == ApplicationController.AppVersion.FULL) {
                System.out.println("\nSelect persistence mode:");
                System.out.println("1. FileSystem");
                System.out.println("2. DBMS");
                System.out.print("Choice: ");
                String persistenceChoice = scanner.nextLine();

                persistenceMode = persistenceChoice.equals("2")
                    ? ApplicationController.PersistenceMode.DBMS
                    : ApplicationController.PersistenceMode.FILESYSTEM;
            }

            System.out.println();

            // Crea e avvia ApplicationController
            ApplicationController applicationController = new ApplicationController(version, persistenceMode);
            applicationController.start();

            scanner.close();

        } catch (Exception e) {
            System.err.println("Error starting the application: " + e.getMessage());
        }
    }
}
