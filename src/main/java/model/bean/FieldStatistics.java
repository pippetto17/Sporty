package model.bean;

/**
 * Bean class representing statistics for a sports field.
 * Used to display analytics in the Field Manager's "My Fields" view.
 */
public class FieldStatistics {
    private int totalBookings;
    private int pendingBookings;
    private int approvedBookings;
    private double totalRevenue;

    public FieldStatistics() {
        // Default constructor
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(int pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public int getApprovedBookings() {
        return approvedBookings;
    }

    public void setApprovedBookings(int approvedBookings) {
        this.approvedBookings = approvedBookings;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
