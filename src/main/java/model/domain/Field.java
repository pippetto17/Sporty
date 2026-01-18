package model.domain;

public class Field {
    private String fieldId;
    private String name;
    private Sport sport;
    private String address;
    private String city;
    private Double pricePerHour;
    private String availability; // JSON or structured data of available time slots
    private boolean indoor;

    // Field Manager fields
    private String managerId; // Username of field manager who owns this field
    private String structureName; // Name of the sports structure/facility
    private Boolean autoApprove; // Auto approve booking requests (default: false)

    public Field() {
    }

    public Field(String fieldId, String name, Sport sport, String address, String city) {
        this.fieldId = fieldId;
        this.name = name;
        this.sport = sport;
        this.address = address;
        this.city = city;
    }

    // Getters and Setters
    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public boolean isIndoor() {
        return indoor;
    }

    public void setIndoor(boolean indoor) {
        this.indoor = indoor;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getStructureName() {
        return structureName;
    }

    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }

    public Boolean getAutoApprove() {
        return autoApprove != null && autoApprove;
    }

    public void setAutoApprove(Boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    /**
     * Verifica se il campo ha disponibilità definite.
     * In una implementazione completa, questo metodo verificherebbe gli slot
     * temporali disponibili.
     */
    public boolean isAvailable() {
        return this.availability != null && !this.availability.isEmpty();
    }

    public boolean isInCity(String cityToCheck) {
        return this.city != null && this.city.equalsIgnoreCase(cityToCheck);
    }

    public boolean supportsNumberOfPlayers(int numberOfPlayers) {
        return this.sport != null && this.sport.getRequiredPlayers() >= numberOfPlayers;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s) - €%.2f/h - %s, %s",
                fieldId,
                name,
                sport.getDisplayName(),
                pricePerHour,
                address,
                city);
    }
}
