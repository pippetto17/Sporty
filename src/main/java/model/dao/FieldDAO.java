package model.dao;

import model.domain.Field;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface FieldDAO {
    List<Field> findAll();

    Field findById(String fieldId);

    List<Field> findByCity(String city);

    List<Field> findBySport(Sport sport);

    List<Field> findAvailableFields(Sport sport, String city, LocalDate date, LocalTime time);

    List<Field> findByManagerId(String managerId);

    void save(Field field);

    void delete(String fieldId);
}
