package view;

import controller.ApplicationController;

public interface View {
    void display();

    void close();

    void setApplicationController(ApplicationController controller);

    void displayError(String message);

    void displaySuccess(String message);
}