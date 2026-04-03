package com.daugia.models;

public class StateProperty extends Item {
    private String managingAgency;
    private String decisionNumber;

    public StateProperty(int id, String name, String description, double startingPrice, 
                         String managingAgency, String decisionNumber) {
        super(id, name, description, startingPrice, "Tài sản nhà nước");
        this.managingAgency = managingAgency;
        this.decisionNumber = decisionNumber;
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Tài sản nhà nước [Cơ quan QL: %s, Số QĐ: %s]", 
                             managingAgency, decisionNumber);
    }
}