package model.dao;

import model.domain.Field;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface FieldDAO {
    List<Field> findAll();

    Field findById(int id);

    List<Field> findByCity(String city);

    List<Field> findAvailableFields(String city, Sport sport, LocalDate date, LocalTime time);

    List<Field> findByManagerId(int managerId);

    void save(Field field);

    void delete(int id);
}
