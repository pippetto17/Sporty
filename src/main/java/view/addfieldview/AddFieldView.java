package view.addfieldview;

import view.View;

/**
 * View interface for adding new fields to a Field Manager's portfolio.
 */
public interface AddFieldView extends View {
    /**
     * Display success message after field creation.
     */
    void displaySuccess(String message);

    /**
     * Display error message if field creation fails.
     */
    void displayError(String message);

    /**
     * Close the add field view.
     */
    void close();
}
