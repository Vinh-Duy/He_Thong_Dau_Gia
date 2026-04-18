package com.bidnova.models;

public class Product {
    // các thuộc tính phải đúng tên với thuộc tính trong products.json
    private int id;
    private String name;
    private String category;
    private long startingPrice; // Dùng long vì giá trị tài sản lớn
    private String imagePath;

    // default constructor
    public Product() {

    }

    // getter vaf setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(long startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}