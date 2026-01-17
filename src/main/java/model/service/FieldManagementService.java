package model.service;

import model.bean.FieldBean;
import model.converter.FieldConverter;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.domain.Field;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for field managers to manage their fields.
 */
public class FieldManagementService {
    private final FieldDAO fieldDAO;

    public FieldManagementService(DAOFactory.PersistenceType persistenceType) throws SQLException {
        this.fieldDAO = DAOFactory.getFieldDAO(persistenceType);
    }

    /**
     * Add a new field (by field manager).
     */
    public void addField(FieldBean fieldBean, String managerId) {
        Field field = FieldConverter.toField(fieldBean);
        field.setManagerId(managerId);

        // Generate unique field ID if not present
        if (field.getFieldId() == null || field.getFieldId().trim().isEmpty()) {
            field.setFieldId(generateFieldId(managerId));
        }

        // Validate required fields
        if (field.getName() == null || field.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Field name is required");
        }
        if (field.getPricePerHour() == null || field.getPricePerHour() < 10.0) {
            throw new IllegalArgumentException("Price must be at least €10/hour");
        }
        if (field.getPricePerHour() > 200.0) {
            throw new IllegalArgumentException("Price cannot exceed €200/hour");
        }

        fieldDAO.save(field);
    }

    /**
     * Generate a unique field ID.
     */
    private String generateFieldId(String managerId) {
        // Format: FIELD_<manager>_<timestamp>
        return "FIELD_" + managerId + "_" + System.currentTimeMillis();
    }

    /**
     * Get all fields owned by a manager.
     */
    public List<FieldBean> getManagerFields(String managerId) {
        List<Field> fields = fieldDAO.findByManagerId(managerId);

        List<FieldBean> beans = new ArrayList<>();
        for (Field field : fields) {
            beans.add(FieldConverter.toFieldBean(field));
        }

        return beans;
    }

    /**
     * Update field details.
     */
    public void updateField(FieldBean fieldBean, String managerId) {
        Field field = FieldConverter.toField(fieldBean);

        // Verify manager owns this field
        Field existing = fieldDAO.findById(field.getFieldId());
        if (existing == null) {
            throw new IllegalArgumentException("Field not found: " + field.getFieldId());
        }
        if (!managerId.equals(existing.getManagerId())) {
            throw new IllegalArgumentException("Unauthorized: manager does not own this field");
        }

        // Keep original managerId
        field.setManagerId(managerId);

        fieldDAO.save(field);
    }

    /**
     * Delete a field.
     */
    public void deleteField(String fieldId, String managerId) {
        Field field = fieldDAO.findById(fieldId);
        if (field == null) {
            throw new IllegalArgumentException("Field not found: " + fieldId);
        }
        if (!managerId.equals(field.getManagerId())) {
            throw new IllegalArgumentException("Unauthorized: manager does not own this field");
        }

        // Note: In production, check for active bookings before deleting
        fieldDAO.delete(fieldId);
    }

    /**
     * Get field statistics (placeholder for future implementation).
     */
    public FieldStatistics getFieldStats(String fieldId, LocalDate from, LocalDate to) {
        // TODO: Implement statistics calculation
        // - Total bookings in period
        // - Revenue in period
        // - Utilization rate
        // - Peak hours
        return new FieldStatistics(fieldId, 0, 0.0, 0.0);
    }

    /**
     * Simple statistics class.
     */
    public static class FieldStatistics {
        private final String fieldId;
        private final int totalBookings;
        private final double revenue;
        private final double utilizationRate;

        public FieldStatistics(String fieldId, int totalBookings, double revenue, double utilizationRate) {
            this.fieldId = fieldId;
            this.totalBookings = totalBookings;
            this.revenue = revenue;
            this.utilizationRate = utilizationRate;
        }

        public String getFieldId() {
            return fieldId;
        }

        public int getTotalBookings() {
            return totalBookings;
        }

        public double getRevenue() {
            return revenue;
        }

        public double getUtilizationRate() {
            return utilizationRate;
        }
    }
}
