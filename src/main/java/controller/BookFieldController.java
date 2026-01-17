package controller;

import model.bean.FieldBean;
import model.bean.MatchBean;
import model.utils.Constants;
import exception.ServiceInitializationException;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller per la gestione della selezione del campo sportivo.
 * Si occupa dell'orchestrazione tra la view e il service layer,
 * delegando la logica di business a FieldService.
 */
public class BookFieldController {
    private final ApplicationController applicationController;
    private final model.dao.FieldDAO fieldDAO;
    private MatchBean currentMatchBean;
    private List<FieldBean> availableFields;
    private FieldBean selectedField;

    public BookFieldController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        try {
            this.fieldDAO = model.dao.DAOFactory.getFieldDAO(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new ServiceInitializationException(Constants.ERROR_FIELD_SERVICE_INIT + e.getMessage(), e);
        }
    }

    public void setMatchBean(MatchBean matchBean) {
        this.currentMatchBean = matchBean;
    }

    public MatchBean getCurrentMatchBean() {
        return currentMatchBean;
    }

    public FieldBean getSelectedField() {
        return selectedField;
    }

    public void setSelectedField(FieldBean field) {
        this.selectedField = field;
        if (currentMatchBean != null && field != null) {
            currentMatchBean.setFieldId(field.getFieldId());
            currentMatchBean.setPricePerPerson(field.getPricePerPerson());
        }
    }

    public List<FieldBean> searchAvailableFields() {
        if (currentMatchBean == null)
            return List.of();

        List<model.domain.Field> fields = fieldDAO.findAvailableFields(
                currentMatchBean.getSport(),
                currentMatchBean.getCity(),
                null,
                null);

        this.availableFields = fields.stream()
                .map(model.converter.FieldConverter::toBean)
                .toList();
        return availableFields;
    }

    // sortFields removed as unused and relying on deleted type

    public List<FieldBean> filterByPriceRange(double minPrice, double maxPrice) {
        if (availableFields == null)
            return List.of();
        return availableFields.stream()
                .filter(f -> f.getPricePerPerson() >= minPrice && f.getPricePerPerson() <= maxPrice)
                .toList();
    }

    public List<FieldBean> filterByIndoor(boolean indoor) {
        if (availableFields == null)
            return List.of();
        return availableFields.stream()
                .filter(f -> f.isIndoor() == indoor)
                .toList();
    }

    public void proceedToPayment() {
        if (selectedField == null)
            throw new IllegalStateException(Constants.ERROR_NO_FIELD_SELECTED);

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
}
