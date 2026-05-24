package com.bidnova.models;

public abstract class Item {
    protected int id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected String category;

    public Item(int id, String name, String description, double startingPrice, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.category = category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public String getCategory() { return category; }
    public abstract String getDetailedInfo();
}