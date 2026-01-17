package model.converter;

import model.bean.BookingBean;
import model.domain.Booking;
import model.domain.BookingStatus;
import model.domain.BookingType;

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
        bean.setRejectionReason(booking.getRejectionReason());

        return bean;
    }

    /**
     * Convert BookingBean to Booking domain object.
     * Note: enriched fields (fieldName, requesterFullName) are not transferred
     * back.
     */
    public static Booking toBooking(BookingBean bean) {
        if (bean == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setBookingId(bean.getBookingId());
        booking.setFieldId(bean.getFieldId());
        booking.setRequesterUsername(bean.getRequesterUsername());
        booking.setBookingDate(bean.getBookingDate());
        booking.setStartTime(bean.getStartTime());
        booking.setEndTime(bean.getEndTime());

        // Convert type string back to enum
        booking.setType(BookingType.valueOf(bean.getType().toUpperCase().replace(" ", "_")));

        // Note: Status should use setStatus with validation, but for bean conversion
        // we need to set it directly. This is safe when loading from persistence.
        try {
            java.lang.reflect.Field statusField = Booking.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(booking, BookingStatus.valueOf(bean.getStatus().toUpperCase().replace(" ", "_")));
        } catch (Exception e) {
            // Fallback: parse from display name
            for (BookingStatus status : BookingStatus.values()) {
                if (status.getDisplayName().equals(bean.getStatus())) {
                    try {
                        booking.setStatus(status);
                    } catch (IllegalStateException ignored) {
                        // Ignore state transition errors when loading from persistence
                    }
                    break;
                }
            }
        }

        booking.setTotalPrice(bean.getTotalPrice());
        booking.setRequestedAt(bean.getRequestedAt());
        booking.setConfirmedAt(bean.getConfirmedAt());
        booking.setRejectionReason(bean.getRejectionReason());

        return booking;
    }
}
