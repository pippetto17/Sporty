package model.service;

import exception.DataAccessException;
import exception.ServiceInitializationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.converter.FieldConverter;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.domain.Field;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service per la gestione della logica di business relativa ai campi sportivi.
 * Centralizza le operazioni di ricerca, ordinamento e filtraggio dei campi,
 * separando la logica di business dal controller e dal layer di persistenza.
 */
public class FieldService {
    private final FieldDAO fieldDAO;

    public FieldService(DAOFactory.PersistenceType persistenceType) throws SQLException {
        this.fieldDAO = DAOFactory.getFieldDAO(persistenceType);
    }

    /**
     * Cerca i campi disponibili in base ai criteri del match.
     * Calcola automaticamente il prezzo per persona basandosi sul numero di
     * partecipanti
     * e su una durata standard di 2 ore.
     */
    public List<FieldBean> searchAvailableFields(MatchBean matchBean) {
        if (matchBean == null) {
            return new ArrayList<>();
        }

        try {
            List<Field> fields = fieldDAO.findAvailableFields(
                    matchBean.getSport(),
                    matchBean.getCity(),
                    matchBean.getMatchDate(),
                    matchBean.getMatchTime());

            return fields.stream()
                    .map(field -> {
                        FieldBean bean = FieldConverter.toBean(field);
                        double pricePerPerson = field.calculatePricePerPerson(
                                matchBean.getRequiredParticipants(),
                                2.0);
                        bean.setPricePerPerson(pricePerPerson);
                        return bean;
                    })
                    .toList();
        } catch (DataAccessException e) {
            throw new ServiceInitializationException("Errore durante la ricerca dei campi: " + e.getMessage(), e);
        }
    }

    /**
     * Ordina una lista di campi secondo il criterio specificato.
     */
    public List<FieldBean> sortFields(List<FieldBean> fields, SortCriteria criteria) {
        if (fields == null || fields.isEmpty()) {
            return fields;
        }

        List<FieldBean> sortedFields = new ArrayList<>(fields);

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
                sortedFields.sort((f1, f2) -> {
                    double dist1 = calculateDistance(f1);
                    double dist2 = calculateDistance(f2);
                    return Double.compare(dist1, dist2);
                });
                break;
        }

        return sortedFields;
    }

    /**
     * Filtra i campi in base a un range di prezzo per persona.
     */
    public List<FieldBean> filterByPriceRange(List<FieldBean> fields, double minPrice, double maxPrice) {
        if (fields == null) {
            return new ArrayList<>();
        }

        return fields.stream()
                .filter(field -> field.getPricePerPerson() != null)
                .filter(field -> field.getPricePerPerson() >= minPrice && field.getPricePerPerson() <= maxPrice)
                .toList();
    }

    /**
     * Filtra i campi in base al tipo (indoor/outdoor).
     */
    public List<FieldBean> filterByIndoor(List<FieldBean> fields, boolean indoor) {
        if (fields == null) {
            return new ArrayList<>();
        }

        return fields.stream()
                .filter(field -> field.isIndoor() == indoor)
                .toList();
    }

    /**
     * Calcola la distanza di un campo dalla posizione di default (Milano centro).
     * In una futura implementazione potrebbe usare la posizione GPS dell'utente.
     */
    private double calculateDistance(FieldBean field) {
        if (field.getLatitude() == null || field.getLongitude() == null) {
            return Double.MAX_VALUE;
        }
        return MapService.calculateDistance(
                MapService.getDefaultLat(),
                MapService.getDefaultLon(),
                field.getLatitude(),
                field.getLongitude());
    }

    /**
     * Criteri di ordinamento disponibili per i campi.
     */
    public enum SortCriteria {
        PRICE_ASC("Prezzo (crescente)"),
        PRICE_DESC("Prezzo (decrescente)"),
        DISTANCE("Distanza"),
        NAME("Nome");

        private final String displayName;

        SortCriteria(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
