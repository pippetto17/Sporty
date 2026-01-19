package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BookFieldController {
    private final ApplicationController applicationController;
    private final model.dao.FieldDAO fieldDAO;
    private MatchBean currentMatchBean;
    private List<FieldBean> availableFields;
    private FieldBean selectedField;
    private boolean standaloneMode;

    public BookFieldController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        this.fieldDAO = model.dao.DAOFactory.getFieldDAO(applicationController.getPersistenceType());
    }

    public void setMatchBean(MatchBean matchBean) {
        this.currentMatchBean = matchBean;
    }

    public void setStandaloneMode(boolean standalone) {
        this.standaloneMode = standalone;
    }

    public boolean isStandaloneMode() {
        return standaloneMode;
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    public FieldBean getSelectedField() {
        return selectedField;
    }

    public void setSelectedField(FieldBean field) {
        this.selectedField = field;
        if (field == null || currentMatchBean == null) {
            return;
        }
        currentMatchBean.setFieldId(field.getFieldId());
        currentMatchBean.setPricePerPerson(field.getPricePerPerson());
    }

    public List<FieldBean> searchAvailableFields() {
        if (currentMatchBean == null) {
            return List.of();
        }

        var fields = fieldDAO.findAvailableFields(
                currentMatchBean.getSport(),
                currentMatchBean.getCity(),
                currentMatchBean.getMatchDate(),
                currentMatchBean.getMatchTime());

        this.availableFields = convertToFieldBeans(fields);
        return availableFields;
    }

    public List<FieldBean> searchFieldsForDirectBooking(model.domain.Sport sport, String city, java.time.LocalDate date,
            java.time.LocalTime time) {
        var fields = fieldDAO.findAvailableFields(sport, city, date, time);
        this.availableFields = convertToFieldBeans(fields);
        return availableFields;
    }

    public void sortFieldsByPrice(boolean ascending) {
        if (availableFields == null || availableFields.isEmpty()) {
            return;
        }

        var sorted = new ArrayList<>(availableFields);
        sorted.sort((f1, f2) -> {
            int result = Double.compare(f1.getPricePerPerson(), f2.getPricePerPerson());
            return ascending ? result : -result;
        });
        this.availableFields = sorted;
    }

    public List<FieldBean> filterByPriceRange(double minPrice, double maxPrice) {
        return getAvailableFields().stream()
                .filter(f -> f.getPricePerPerson() >= minPrice && f.getPricePerPerson() <= maxPrice)
                .toList();
    }

    public List<FieldBean> filterByIndoor(boolean indoor) {
        return getAvailableFields().stream()
                .filter(f -> f.isIndoor() == indoor)
                .toList();
    }

    public void proceedToPayment() throws ValidationException {
        if (selectedField == null) {
            throw new ValidationException(Constants.ERROR_NO_FIELD_SELECTED);
        }

        if (standaloneMode) {
            applicationController.navigateToPaymentForBooking(selectedField, currentMatchBean);
            return;
        }

        if (currentMatchBean.getPricePerPerson() == null) {
            currentMatchBean.setPricePerPerson(selectedField.getPricePerPerson());
        }
        applicationController.navigateToPayment(currentMatchBean);
    }

    public void navigateBack() {
        applicationController.back();
    }

    public List<FieldBean> getAvailableFields() {
        return availableFields != null ? availableFields : List.of();
    }

    private List<FieldBean> convertToFieldBeans(List<model.domain.Field> fields) {
        return fields.stream()
                .map(model.converter.FieldConverter::toBean)
                .toList();
    }
}
