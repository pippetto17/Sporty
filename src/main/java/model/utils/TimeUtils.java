package model.utils;

import java.time.LocalTime;

/**
 * Utility class for time-related calculations and operations.
 * Centralizes common time logic to avoid code duplication.
 */
public final class TimeUtils {

    private TimeUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculate duration in hours between two LocalTime instances.
     * 
     * @param startTime Start time
     * @param endTime   End time
     * @return Duration in hours, or 0.0 if either parameter is null
     */
    public static double calculateDurationHours(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return 0.0;
        }
        return (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / 3600.0;
    }

    /**
     * Check if a time range overlaps with another time range.
     * 
     * @param start1 Start of first range
     * @param end1   End of first range
     * @param start2 Start of second range
     * @param end2   End of second range
     * @return true if ranges overlap, false otherwise
     */
    public static boolean timeRangesOverlap(LocalTime start1, LocalTime end1,
            LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Format a time range as a string.
     * 
     * @param startTime Start time
     * @param endTime   End time
     * @return Formatted string like "09:00-11:00", or empty string if null
     */
    public static String formatTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return "";
        }
        return String.format("%s-%s", startTime, endTime);
    }
}
