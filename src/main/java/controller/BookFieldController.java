package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.converter.FieldConverter;
import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;
import model.utils.Constants;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for booking fields for matches.
 * Handles field search, availability checking, and field selection.
 */
public class BookFieldController {
    private final ApplicationController applicationController;
    private final FieldDAO fieldDAO;
    private final MatchBean currentMatchBean;
    private List<FieldBean> availableFields;
    private FieldBean selectedField;
    private boolean standaloneMode;

    /**
     * Constructs a new BookFieldController with the given match context.
     *
     * @param applicationController the main application controller
     * @param matchBean             the match bean containing match details
     */
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

    /**
     * Searches for available fields matching the current match parameters.
     *
     * @return list of available field beans
     */
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

    public List<FieldBean> searchFieldsForDirectBooking(Sport sport, String city, LocalDate date,
            LocalTime time) {
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

    private List<FieldBean> convertToFieldBeans(List<Field> fields) {
        return fields.stream()
                .map(FieldConverter::toBean)
                .toList();
    }

    public void updateMatchParameters(Sport sport, String city, LocalDate date,
            LocalTime time) {
        if (currentMatchBean == null) {
            throw new IllegalStateException("MatchBean must be provided via constructor");
        }
        currentMatchBean.setSport(sport);
        currentMatchBean.setCity(city);
        currentMatchBean.setMatchDate(date);
        currentMatchBean.setMatchTime(time);
    }
}