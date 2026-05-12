package com.daugia.models;

public class ItemFactory {

    // Method cũ — giữ nguyên
    public static Item createItem(String category, int id, String name,
            String description, double startingPrice, Object... extraParams) {
        switch (category) {
            case "Bất động sản":
                return new RealEstate(id, name, description, startingPrice,
                    (double)extraParams[0], (String)extraParams[1], (String)extraParams[2]);
            case "Phương tiện - xe cộ":
                return new Vehicle(id, name, description, startingPrice,
                    (String)extraParams[0], (int)extraParams[1], (int)extraParams[2]);
            case "Sưu tầm - nghệ thuật":
                return new ArtCollectible(id, name, description, startingPrice,
                    (String)extraParams[0], (int)extraParams[1], (String)extraParams[2]);
            case "Tài sản nhà nước":
                return new StateProperty(id, name, description, startingPrice,
                    (String)extraParams[0], (String)extraParams[1]);
            default:
                throw new IllegalArgumentException("Danh mục không hợp lệ: " + category);
        }
    }

    // THÊM MỚI — tạo Item từ DB với extraParams mặc định
    // Khi DB chưa có bảng riêng cho từng loại, dùng giá trị placeholder
    public static Item createItemFromDB(int id, String name,
            String description, double startingPrice, String category) {
        switch (category) {
            case "Bất động sản":
                return new RealEstate(id, name, description, startingPrice,
                    0.0, "Chưa cập nhật", "Chưa cập nhật");
            case "Phương tiện - xe cộ":
                return new Vehicle(id, name, description, startingPrice,
                    "Chưa cập nhật", 0, 0);
            case "Sưu tầm - nghệ thuật":
                return new ArtCollectible(id, name, description, startingPrice,
                    "Chưa cập nhật", 0, "Chưa cập nhật");
            case "Tài sản nhà nước":
                return new StateProperty(id, name, description, startingPrice,
                    "Chưa cập nhật", "Chưa cập nhật");
            default:
                // Fallback: tạo RealEstate với giá trị mặc định
                return new RealEstate(id, name, description, startingPrice,
                    0.0, "Khác", "Chưa cập nhật");
        }
    }
}