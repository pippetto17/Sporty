package model.utils;

/**
 * Utility class for common string formatting operations.
 * Centralizes toString() patterns to avoid code duplication.
 */
public final class StringFormatUtils {

    private StringFormatUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Format an object ID with a label.
     * Handles null values gracefully.
     * 
     * @param label Field label (e.g., "id", "bookingId")
     * @param value Value to format
     * @return Formatted string like "id=123" or "id=null"
     */
    public static String formatField(String label, Object value) {
        return String.format("%s=%s", label, value);
    }

    /**
     * Build a toString representation with class name and fields.
     * 
     * @param className Name of the class
     * @param fields    Field key-value pairs (must be even number of arguments)
     * @return Formatted string like "ClassName[field1=value1, field2=value2]"
     */
    public static String buildToString(String className, Object... fields) {
        if (fields.length % 2 != 0) {
            throw new IllegalArgumentException("Fields must be key-value pairs");
        }

        StringBuilder sb = new StringBuilder(className).append("[");
        for (int i = 0; i < fields.length; i += 2) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(fields[i]).append("=").append(fields[i + 1]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Safely convert an object to string, returning empty string if null.
     * 
     * @param obj Object to convert
     * @return String representation or empty string if null
     */
    public static String safeToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * Truncate a string to a maximum length with ellipsis.
     * 
     * @param str       String to truncate
     * @param maxLength Maximum length
     * @return Truncated string with "..." if longer than maxLength
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
