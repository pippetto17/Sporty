package view.myfieldsview;

import model.bean.FieldBean;
import view.View;
import java.util.List;

/**
 * View interface for displaying fields owned by a Field Manager.
 */
public interface MyFieldsView extends View {
    /**
     * Display the list of fields owned by the manager.
     */
    void displayFields(List<FieldBean> fields);

    /**
     * Display error message.
     */
    void displayError(String message);

    /**
     * Close the my fields view.
     */
    void close();

    /**
     * Set the application controller for navigation.
     */
    void setApplicationController(controller.ApplicationController applicationController);
}
