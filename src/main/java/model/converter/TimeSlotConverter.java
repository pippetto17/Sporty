package model.converter;

import model.bean.TimeSlotBean;
import model.domain.SlotStatus;
import model.domain.TimeSlot;

/**
 * Converter between TimeSlot domain and TimeSlotBean.
 */
public class TimeSlotConverter {

    private TimeSlotConverter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert TimeSlot domain object to TimeSlotBean.
     */
    public static TimeSlotBean toTimeSlotBean(TimeSlot slot) {
        if (slot == null) {
            return null;
        }

        TimeSlotBean bean = new TimeSlotBean();
        bean.setSlotId(slot.getSlotId());
        bean.setFieldId(slot.getFieldId());
        bean.setDayOfWeek(slot.getDayOfWeek());
        bean.setBookingDate(slot.getBookingDate());
        bean.setStartTime(slot.getStartTime());
        bean.setEndTime(slot.getEndTime());
        bean.setStatus(slot.getStatus().getDisplayName());
        bean.setBookingId(slot.getBookingId());

        return bean;
    }

    /**
     * Convert TimeSlotBean to TimeSlot domain object.
     * Note: enriched fields (fieldName) are not transferred back.
     */
    public static TimeSlot toTimeSlot(TimeSlotBean bean) {
        if (bean == null) {
            return null;
        }

        TimeSlot slot = new TimeSlot();
        slot.setSlotId(bean.getSlotId());
        slot.setFieldId(bean.getFieldId());
        slot.setDayOfWeek(bean.getDayOfWeek());
        slot.setBookingDate(bean.getBookingDate());
        slot.setStartTime(bean.getStartTime());
        slot.setEndTime(bean.getEndTime());

        // Convert status string back to enum
        for (SlotStatus status : SlotStatus.values()) {
            if (status.getDisplayName().equals(bean.getStatus())) {
                slot.setStatus(status);
                break;
            }
        }

        slot.setBookingId(bean.getBookingId());

        return slot;
    }
}
