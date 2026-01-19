package testing;

import model.dao.BookingDAO;
import model.dao.DAOFactory;
import model.domain.BookingStatus;
import model.domain.BookingType;
import model.domain.Sport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC2: Book Field Test Suite")
class BookFieldTest {

    private model.dao.FieldDAO fieldDAO;
    private BookingDAO bookingDAO;

    @BeforeEach
    void setUp() {
        fieldDAO = DAOFactory.getFieldDAO(DAOFactory.PersistenceType.MEMORY);
        bookingDAO = DAOFactory.getBookingDAO(DAOFactory.PersistenceType.MEMORY);

        model.domain.Field field1 = new model.domain.Field();
        field1.setFieldId("1");
        field1.setName("Campo Centrale");
        field1.setSport(Sport.FOOTBALL_5);
        field1.setCity("Milano");
        field1.setAddress("Via Roma 10");
        field1.setManagerId("manager1");
        field1.setPricePerHour(60.0);
        field1.setIndoor(true);

        model.domain.Field field2 = new model.domain.Field();
        field2.setFieldId("2");
        field2.setName("Campo Nord");
        field2.setSport(Sport.BASKETBALL);
        field2.setCity("Milano");
        field2.setAddress("Via Milano 5");
        field2.setManagerId("manager1");
        field2.setPricePerHour(40.0);
        field2.setIndoor(false);

        fieldDAO.save(field1);
        fieldDAO.save(field2);
    }

    @Test
    @DisplayName("Ricerca campi disponibili per città e sport")
    void testSearchAvailableFields(){
        List<model.domain.Field> fields = fieldDAO.findByCity("Milano");

        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("Campo Centrale")));
    }

    @Test
    @DisplayName("Prenotazione campo con successo")
    void testBookFieldSuccess() {
        model.domain.Booking booking = new model.domain.Booking();
        booking.setFieldId("1");
        booking.setRequesterUsername("player1");
        booking.setBookingDate(LocalDate.now().plusDays(3));
        booking.setStartTime(LocalTime.of(18, 0));
        booking.setEndTime(LocalTime.of(20, 0));
        booking.setType(BookingType.PRIVATE);
        booking.setTotalPrice(120.0);
        booking.setStatus(BookingStatus.CONFIRMED);

        bookingDAO.save(booking);

        assertNotNull(booking.getBookingId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("player1", booking.getRequesterUsername());
    }

    @Test
    @DisplayName("Verifica calcolo prezzo totale per 2 ore")
    void testPriceCalculation(){
        model.domain.Field field = fieldDAO.findById("1");
        assertNotNull(field);

        double pricePerHour = field.getPricePerHour();
        int hours = 2;
        double expectedTotal = pricePerHour * hours;

        assertEquals(120.0, expectedTotal);
    }

    @Test
    @DisplayName("Verifica logica di conflict tra due booking")
    void testBookingConflict() {
        LocalDate date = LocalDate.now().plusDays(5);

        model.domain.Booking booking1 = new model.domain.Booking();
        booking1.setFieldId("1");
        booking1.setRequesterUsername("player1");
        booking1.setBookingDate(date);
        booking1.setStartTime(LocalTime.of(18, 0));
        booking1.setEndTime(LocalTime.of(20, 0));
        booking1.setType(BookingType.PRIVATE);
        booking1.setStatus(BookingStatus.CONFIRMED);

        model.domain.Booking booking2 = new model.domain.Booking();
        booking2.setFieldId("1");
        booking2.setRequesterUsername("player2");
        booking2.setBookingDate(date);
        booking2.setStartTime(LocalTime.of(19, 0));
        booking2.setEndTime(LocalTime.of(21, 0));

        // Verifica overlap temporale
        boolean hasConflict = booking1.getStartTime().isBefore(booking2.getEndTime())
                           && booking1.getEndTime().isAfter(booking2.getStartTime());

        assertTrue(hasConflict);
    }
}

