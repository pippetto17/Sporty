package model.dao;

import model.domain.Field;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface FieldDAO {
    /**
     * Find all fields
     */
    List<Field> findAll();

    /**
     * Find field by ID
     */
    Field findById(String fieldId);

    /**
     * Find fields by city
     */
    List<Field> findByCity(String city);

    /**
     * Find fields by sport
     */
    List<Field> findBySport(Sport sport);

    /**
     * Find available fields matching criteria
     */
    List<Field> findAvailableFields(Sport sport, String city, LocalDate date, LocalTime time);

    /**
     * Find all fields owned by a specific manager
     */
    List<Field> findByManagerId(String managerId);

    /**
     * Save or update a field
     */
    void save(Field field);

    /**
     * Delete a field
     */
    void delete(String fieldId);
}
