package model.converter;

import model.bean.BookingBean;
import model.domain.Booking;

/**
 * Converter between Booking domain and BookingBean.
 */
public class BookingConverter {

    private BookingConverter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert Booking domain object to BookingBean.
     */
    public static BookingBean toBean(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingBean bean = new BookingBean();
        bean.setBookingId(booking.getBookingId());
        bean.setFieldId(booking.getFieldId());
        bean.setRequesterUsername(booking.getRequesterUsername());
        bean.setBookingDate(booking.getBookingDate());
        bean.setStartTime(booking.getStartTime());
        bean.setEndTime(booking.getEndTime());
        bean.setType(booking.getType().getDisplayName());
        bean.setStatus(booking.getStatus().getDisplayName());
        bean.setTotalPrice(booking.getTotalPrice());
        bean.setRequestedAt(booking.getRequestedAt());
        bean.setConfirmedAt(booking.getConfirmedAt());

        return bean;
    }
}
