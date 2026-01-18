package org.example;

import java.util.logging.Logger;

import controller.ApplicationController;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            ApplicationController app = new ApplicationController();
            app.start();
        } catch (Exception e) {
            logger.severe("Fatal Error during execution: " + e.getMessage());
        }
    }
}