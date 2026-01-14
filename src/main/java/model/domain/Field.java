package model.domain;

public class Field {
    private String fieldId;
    private String name;
    private Sport sport;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private Double pricePerHour;
    private String availability; // JSON or structured data of available time slots
    private boolean indoor;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public double calculatePricePerPerson(int numberOfParticipants, double hoursBooked) {
        if (pricePerHour == null || numberOfParticipants <= 0 || hoursBooked <= 0) {
            throw new IllegalArgumentException("Invalid parameters for price calculation");
        }
        return (pricePerHour * hoursBooked) / numberOfParticipants;
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
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
