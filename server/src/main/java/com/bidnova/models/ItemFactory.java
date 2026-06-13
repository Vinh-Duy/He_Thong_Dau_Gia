package com.bidnova.models;

import java.util.HashMap;
import java.util.Map;

import com.bidnova.patterns.factory.ItemCreator;

/**
 * Refactored ItemFactory using Registry Pattern to support Open/Closed Principle.
 */
public class ItemFactory {
    private static final Map<String, ItemCreator> creators = new HashMap<>();

    // Đăng ký các Creator vào Registry (có thể mở rộng dễ dàng)
    static {
        // Lưu ý: Bạn cần implement các class cụ thể như RealEstateCreator, VehicleCreator... 
        // Nếu chưa có, ta vẫn giữ logic cũ nhưng bọc lại cho sạch hơn.
    }

    public static void registerCreator(String category, ItemCreator creator) {
        creators.put(category, creator);
    }
    
    public static Item createItem(String category, int id, String name, String description, double startingPrice, Object... extraParams) {
        ItemCreator creator = creators.get(category);
        if (creator != null) {
            return creator.createItem(id, name, description, startingPrice, extraParams);
        }

        // Fallback logic cho đến khi bạn chuyển hết sang ItemCreator cụ thể
        switch (category) {
            case "Bất động sản":
                return new RealEstate(id, name, description, startingPrice, 
                    (double) extraParams[0], (String) extraParams[1], (String) extraParams[2]);
            case "Phương tiện - xe cộ":
                return new Vehicle(id, name, description, startingPrice, 
                    (String) extraParams[0], (int) extraParams[1], (int) extraParams[2]);
            default:
                throw new IllegalArgumentException("Danh mục không hỗ trợ hoặc chưa đăng ký Creator: " + category);
        }
    }
}