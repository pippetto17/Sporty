package model.bean;

import model.domain.Sport;

public class FieldBean {
    private String fieldId;
    private String name;
    private Sport sport;
    private String address;
    private String city;
    private Double pricePerHour;
    private Double pricePerPerson; // Calculated based on match participants
    private boolean indoor;

    // Field Manager related fields
    private String managerId; // Username of field manager who owns this field
    private String structureName; // Name of the sports structure/facility
    private boolean autoApprove; // Auto-approve booking requests without manager intervention

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

    public Double getPricePerPerson() {
        if (pricePerPerson != null) {
            return pricePerPerson;
        }
        if (pricePerHour != null && sport != null && sport.getRequiredPlayers() > 0) {
            return pricePerHour / sport.getRequiredPlayers();
        }
        return 0.0;
    }

    public void setPricePerPerson(Double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
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

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }
}
