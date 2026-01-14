package controller;

import model.bean.FieldBean;
import model.bean.MatchBean;
import model.converter.FieldConverter;
import model.converter.MatchConverter;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.dao.MatchDAO;
import model.domain.Field;
import model.domain.Match;
import model.service.MapService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookFieldController {
    private final ApplicationController applicationController;
    private final FieldDAO fieldDAO;
    private final MatchDAO matchDAO;
    private MatchBean currentMatchBean;
    private List<FieldBean> availableFields;
    private FieldBean selectedField;

    public enum SortCriteria {
        PRICE_ASC("Price (Low to High)"),
        PRICE_DESC("Price (High to Low)"),
        DISTANCE("Distance"),
        NAME("Name");

        private final String displayName;

        SortCriteria(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public BookFieldController(ApplicationController applicationController) {
        this.applicationController = applicationController;
        try {
            this.fieldDAO = DAOFactory.getFieldDAO(applicationController.getPersistenceType());
            this.matchDAO = DAOFactory.getMatchDAO(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs: " + e.getMessage(), e);
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
        // Update match bean with field info
        if (currentMatchBean != null && field != null) {
            currentMatchBean.setFieldId(field.getFieldId());
            currentMatchBean.setPricePerPerson(field.getPricePerPerson());
        }
    }

    /**
     * Search for available fields based on match criteria
     */
    public List<FieldBean> searchAvailableFields() {
        if (currentMatchBean == null) {
            return new ArrayList<>();
        }

        try {
            // Get fields from DAO
            List<Field> fields = fieldDAO.findAvailableFields(
                currentMatchBean.getSport(),
                currentMatchBean.getCity(),
                currentMatchBean.getMatchDate(),
                currentMatchBean.getMatchTime()
            );

            // Convert to beans and calculate price per person
            availableFields = fields.stream()
                .map(field -> {
                    FieldBean bean = FieldConverter.toBean(field);
                    // Calculate price per person (assuming 2 hours booking)
                    double pricePerPerson = field.calculatePricePerPerson(
                        currentMatchBean.getRequiredParticipants(),
                        2.0 // Default 2 hours
                    );
                    bean.setPricePerPerson(pricePerPerson);
                    return bean;
                })
                .toList();

            return availableFields;
        } catch (Exception e) {
            throw new RuntimeException("Error searching fields: " + e.getMessage(), e);
        }
    }

    /**
     * Sort available fields by given criteria
     */
    public List<FieldBean> sortFields(SortCriteria criteria) {
        if (availableFields == null || availableFields.isEmpty()) {
            return availableFields;
        }

        List<FieldBean> sortedFields = new ArrayList<>(availableFields);

        switch (criteria) {
            case PRICE_ASC:
                sortedFields.sort(Comparator.comparing(FieldBean::getPricePerPerson,
                    Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case PRICE_DESC:
                sortedFields.sort(Comparator.comparing(FieldBean::getPricePerPerson,
                    Comparator.nullsFirst(Comparator.reverseOrder())));
                break;
            case NAME:
                sortedFields.sort(Comparator.comparing(FieldBean::getName,
                    Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case DISTANCE:
                // Ordina per distanza dalla posizione di default (Milano centro)
                sortedFields.sort((f1, f2) -> {
                    double dist1 = calculateDistance(f1);
                    double dist2 = calculateDistance(f2);
                    return Double.compare(dist1, dist2);
                });
                break;
            default:
                break;
        }

        availableFields = sortedFields;
        return sortedFields;
    }

    private double calculateDistance(FieldBean field) {
        if (field.getLatitude() == null || field.getLongitude() == null) {
            return Double.MAX_VALUE;
        }
        // Usa posizione di default (Milano centro) - in futuro si può usare la posizione dell'utente
        return MapService.calculateDistance(
            MapService.getDefaultLat(),
            MapService.getDefaultLon(),
            field.getLatitude(),
            field.getLongitude()
        );
    }

    /**
     * Filter fields by price range
     */
    public List<FieldBean> filterByPriceRange(double minPrice, double maxPrice) {
        if (availableFields == null) {
            return new ArrayList<>();
        }

        return availableFields.stream()
            .filter(field -> field.getPricePerPerson() != null)
            .filter(field -> field.getPricePerPerson() >= minPrice && field.getPricePerPerson() <= maxPrice)
            .toList();
    }

    /**
     * Filter fields by indoor/outdoor
     */
    public List<FieldBean> filterByIndoor(boolean indoor) {
        if (availableFields == null) {
            return new ArrayList<>();
        }

        return availableFields.stream()
            .filter(field -> field.isIndoor() == indoor)
            .toList();
    }

    public void proceedToPayment() {
        if (selectedField == null) {
            throw new IllegalStateException("No field selected");
        }
        // TODO: Navigate to payment view
        System.out.println("Proceeding to payment (to be implemented)...");
        // applicationController.navigateToPayment(currentMatchBean);
    }

    /**
     * Confirm booking and save match to database
     */
    public void confirmBooking() {
        if (selectedField == null) {
            throw new IllegalStateException("No field selected");
        }
        if (currentMatchBean == null) {
            throw new IllegalStateException("No match data");
        }

        try {
            // Convert bean to entity
            Match match = MatchConverter.toEntity(currentMatchBean);

            // Save match
            matchDAO.save(match);

            // Update bean with generated ID if any
            if (match.getMatchId() != null) {
                currentMatchBean.setMatchId(match.getMatchId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving match: " + e.getMessage(), e);
        }
    }

    public void navigateBack() {
        applicationController.back();
    }

    public List<FieldBean> getAvailableFields() {
        return availableFields != null ? availableFields : new ArrayList<>();
    }
}

