package view.fieldmanagerview;

import view.View;

/**
 * Interface for Field Manager Dashboard view.
 */
public interface FieldManagerView extends View {
    /**
     * Set the application controller for navigation.
     */
    void setApplicationController(controller.ApplicationController applicationController);
}
