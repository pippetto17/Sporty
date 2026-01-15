package controller;

import model.bean.FieldBean;
import model.bean.MatchBean;
import model.service.FieldService;
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
    private final FieldService fieldService;
    private MatchBean currentMatchBean;
    private List<FieldBean> availableFields;
    private FieldBean selectedField;

    public BookFieldController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        try {
            this.fieldService = new FieldService(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new ServiceInitializationException("Errore nell'inizializzazione di FieldService: " + e.getMessage(),
                    e);
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

    /**
     * Cerca i campi disponibili in base ai criteri del match corrente.
     * Delega la ricerca e il calcolo dei prezzi a FieldService.
     */
    public List<FieldBean> searchAvailableFields() {
        availableFields = fieldService.searchAvailableFields(currentMatchBean);
        return availableFields;
    }

    /**
     * Ordina i campi disponibili secondo il criterio specificato.
     */
    public List<FieldBean> sortFields(FieldService.SortCriteria criteria) {
        availableFields = fieldService.sortFields(availableFields, criteria);
        return availableFields;
    }

    /**
     * Filtra i campi in base a un range di prezzo.
     */
    public List<FieldBean> filterByPriceRange(double minPrice, double maxPrice) {
        return fieldService.filterByPriceRange(availableFields, minPrice, maxPrice);
    }

    /**
     * Filtra i campi in base al tipo (indoor/outdoor).
     */
    public List<FieldBean> filterByIndoor(boolean indoor) {
        return fieldService.filterByIndoor(availableFields, indoor);
    }

    /**
     * Procede alla schermata di pagamento dopo la selezione del campo.
     */
    public void proceedToPayment() {
        if (selectedField == null) {
            throw new IllegalStateException("Nessun campo selezionato");
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
}
