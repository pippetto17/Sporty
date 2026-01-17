package model.observer;

import model.domain.Booking;

/**
 * Observer interface for booking events.
 * Implements Observer pattern for notification system.
 */
public interface BookingObserver {

    /**
     * Called when a new booking request is created.
     */
    void onBookingRequested(Booking booking);

    /**
     * Called when a booking is approved by field manager.
     */
    void onBookingApproved(Booking booking);

    /**
     * Called when a booking is rejected by field manager.
     */
    void onBookingRejected(Booking booking);

    /**
     * Called when a booking is cancelled by requester.
     */
    void onBookingCancelled(Booking booking);
}
