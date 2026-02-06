package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.utils.Constants;
import java.util.List;

public class BookFieldController {
    private final ApplicationController applicationController;
    private final model.dao.FieldDAO fieldDAO;
    private final MatchBean currentMatchBean;
    private List<FieldBean> availableFields;
    private FieldBean selectedField;
    private boolean standaloneMode;

    public BookFieldController(ApplicationController applicationController, MatchBean matchBean) {
        this.applicationController = applicationController;
        this.fieldDAO = applicationController.getDaoFactory().getFieldDAO();
        this.currentMatchBean = matchBean;
    }

    public boolean isStandaloneMode() {
        return standaloneMode;
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    public void setSelectedField(FieldBean field) {
        this.selectedField = field;
        if (field == null || currentMatchBean == null) {
            return;
        }
        currentMatchBean.setFieldId(field.getFieldId());
        currentMatchBean.setFieldName(field.getName());
        currentMatchBean.setFieldAddress(field.getAddress());
        currentMatchBean.setPricePerHour(field.getPricePerHour());
    }

    public List<FieldBean> searchAvailableFields() {
        if (currentMatchBean == null) {
            return List.of();
        }
        var fields = fieldDAO.findAvailableFields(
                currentMatchBean.getCity(),
                currentMatchBean.getSport(),
                currentMatchBean.getMatchDate(),
                currentMatchBean.getMatchTime());
        this.availableFields = convertToFieldBeans(fields);
        return availableFields;
    }

    public List<FieldBean> searchFieldsForDirectBooking(model.domain.Sport sport, String city, java.time.LocalDate date,
            java.time.LocalTime time) {
        var fields = fieldDAO.findAvailableFields(city, sport, date, time);
        this.availableFields = convertToFieldBeans(fields);
        return availableFields;
    }

    public void proceedToPayment() throws ValidationException {
        if (selectedField == null) {
            throw new ValidationException(Constants.ERROR_NO_FIELD_SELECTED);
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

    public void updateMatchParameters(model.domain.Sport sport, String city, java.time.LocalDate date,
            java.time.LocalTime time) {
        if (currentMatchBean == null) {
            throw new IllegalStateException("MatchBean must be provided via constructor");
        }
        currentMatchBean.setSport(sport);
        currentMatchBean.setCity(city);
        currentMatchBean.setMatchDate(date);
        currentMatchBean.setMatchTime(time);
    }
}