package com.bidnova.patterns.factory;

import com.bidnova.models.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Factory Registry - Quản lý tất cả Item Creators
 * Singleton Pattern + Registry Pattern
 * 
 * @author Design Patterns Team
 */
public class ItemFactoryRegistry {
    
    private static ItemFactoryRegistry instance;
    private final List<ItemCreator> creators = new CopyOnWriteArrayList<>();
    
    private ItemFactoryRegistry() {
        // Đăng ký tất cả creators mặc định
        registerDefaultCreators();
    }
    
    /**
     * Singleton getInstance
     */
    public static synchronized ItemFactoryRegistry getInstance() {
        if (instance == null) {
            instance = new ItemFactoryRegistry();
        }
        return instance;
    }
    
    /**
     * Đăng ký creator mới
     */
    public void registerCreator(ItemCreator creator) {
        if (creator != null && !creators.contains(creator)) {
            creators.add(creator);
        }
    }
    
    /**
     * Hủy đăng ký creator
     */
    public void unregisterCreator(ItemCreator creator) {
        creators.remove(creator);
    }
    
    /**
     * Tạo item dựa trên category
     */
    public Item createItem(String category, int id, String name, String description, 
                          double startingPrice, Object... extraParams) {
        for (ItemCreator creator : creators) {
            if (creator.supportsCategory(category)) {
                return creator.createItem(id, name, description, startingPrice, extraParams);
            }
        }
        throw new IllegalArgumentException("Không tìm thấy creator cho danh mục: " + category);
    }
    
    /**
     * Kiểm tra category có được hỗ trợ không
     */
    public boolean isCategorySupported(String category) {
        for (ItemCreator creator : creators) {
            if (creator.supportsCategory(category)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy danh sách tất cả categories được hỗ trợ
     */
    public List<String> getSupportedCategories() {
        List<String> categories = new ArrayList<>();
        // Hardcoded cho các categories mặc định
        categories.add("Bất động sản");
        categories.add("Phương tiện - xe cộ");
        categories.add("Sưu tầm - nghệ thuật");
        categories.add("Tài sản nhà nước");
        return categories;
    }
    
    /**
     * Lấy danh sách tên các creators
     */
    public List<String> getRegisteredCreatorNames() {
        List<String> names = new ArrayList<>();
        for (ItemCreator creator : creators) {
            names.add(creator.getItemTypeName());
        }
        return names;
    }
    
    /**
     * Đăng ký creators mặc định
     */
    private void registerDefaultCreators() {
        creators.add(new RealEstateCreator());
        creators.add(new VehicleCreator());
        creators.add(new ArtCollectibleCreator());
        creators.add(new StatePropertyCreator());
    }
    
    /**
     * Lấy số lượng creators đã đăng ký
     */
    public int getCreatorCount() {
        return creators.size();
    }
}
