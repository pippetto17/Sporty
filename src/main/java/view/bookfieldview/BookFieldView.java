package view.bookfieldview;

import view.View;

public interface BookFieldView extends View {
    void displayAvailableFields();

    void displayFieldDetails(int fieldIndex);
}