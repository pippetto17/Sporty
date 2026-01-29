package model.dao.filesystem;

import model.dao.FieldDAO;
import model.domain.Field;
import model.domain.Sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stub implementation of FieldDAO for FileSystem persistence.
 * Created to satisfy Abstract Factory pattern requirements.
 */
public class FieldDAOFileSystem implements FieldDAO {
    private static final Logger logger = Logger.getLogger(FieldDAOFileSystem.class.getName());

    @Override
    public List<Field> findAll() {
        return List.of();
    }

    @Override
    public Field findById(int id) {
        return null;
    }

    @Override
    public List<Field> findByCity(String city) {
        return List.of();
    }

    @Override
    public List<Field> findAvailableFields(String city, Sport sport, LocalDate date, LocalTime time) {
        return List.of();
    }

    @Override
    public List<Field> findByManagerId(int managerId) {
        return List.of();
    }

    @Override
    public void save(Field field) {
        logger.warning("Save Field to FileSystem not implemented.");
    }

    @Override
    public void delete(int id) {
        logger.warning("Delete Field from FileSystem not implemented.");
    }
}
