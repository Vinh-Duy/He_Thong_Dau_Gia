package com.bidnova.patterns.factory;

import com.bidnova.models.Item;

/**
 * Factory Pattern Interface - Item Creator
 * 
 * @author Design Patterns Team
 */
public interface ItemCreator {
    
    /**
     * Tạo item mới với các tham số cơ bản
     * @param id ID của item
     * @param name Tên item
     * @param description Mô tả
     * @param startingPrice Giá khởi điểm
     * @param extraParams Các tham số bổ sung tùy loại item
     * @return Item mới được tạo
     */
    Item createItem(int id, String name, String description, double startingPrice, Object... extraParams);
    
    /**
     * Kiểm tra loại item này có hỗ trợ category không
     * @param category Danh mục cần kiểm tra
     * @return true nếu hỗ trợ
     */
    boolean supportsCategory(String category);
    
    /**
     * Lấy tên loại item
     * @return Tên loại item
     */
    String getItemTypeName();
}
