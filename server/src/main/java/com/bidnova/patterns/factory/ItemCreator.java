package com.bidnova.patterns.factory;

import com.bidnova.models.Item;

/**
 * 🏭 ItemCreator - Factory Pattern Interface
 * 
 * <h2>Chức Năng:</h2>
 * <p>Định nghĩa hợp đồng cho tất cả item creators. Mỗi concrete creator (VehicleCreator,
 * RealEstateCreator, v.v.) phải implement interface này để tạo các loại Item khác nhau.</p>
 * 
 * <h2>Factory Pattern Benefit:</h2>
 * <p>Tách biệt logic tạo objects từ phía client:</p>
 * <ul>
 *   <li>Client không cần biết chi tiết cách tạo từng loại Item</li>
 *   <li>Dễ thêm loại Item mới mà không sửa code client</li>
 *   <li>Tập trung logic tạo Item ở một chỗ</li>
 * </ul>
 * 
 * <h2>Implementations (Concrete Creators):</h2>
 * <table border="1">
 *   <tr><th>Creator Class</th><th>Item Type</th><th>Extra Params</th></tr>
 *   <tr><td>VehicleCreator</td><td>Vehicle</td><td>make, model, year, mileage</td></tr>
 *   <tr><td>RealEstateCreator</td><td>RealEstate</td><td>address, area, bedrooms</td></tr>
 *   <tr><td>ArtCollectibleCreator</td><td>ArtCollectible</td><td>artist, year, medium</td></tr>
 *   <tr><td>StatePropertyCreator</td><td>StateProperty</td><td>reason, area, location</td></tr>
 * </table>
 * 
 * <h2>Ví Dụ Sử Dụng:</h2>
 * <pre>
 * // Tạo một Vehicle
 * ItemCreator vehicleCreator = new VehicleCreator();
 * Item car = vehicleCreator.createItem(
 *     1,                    // id
 *     "Toyota Camry",       // name
 *     "2020 model",         // description
 *     100000000,            // startingPrice
 *     "Toyota",             // make
 *     "Camry",              // model
 *     2020,                 // year
 *     15000                 // mileage
 * );
 * 
 * // Sử dụng Factory Registry
 * ItemFactoryRegistry registry = ItemFactoryRegistry.getInstance();
 * Item house = registry.createItem(
 *     "Bất động sản",  // category
 *     2,               // id
 *     "Căn hộ 2BR",    // name
 *     "Tòa nhà A",     // description
 *     500000000,       // startingPrice
 *     "District 1",    // address
 *     120,             // area
 *     2                // bedrooms
 * );
 * </pre>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see ItemFactoryRegistry
 * @see Item
 * @see com.bidnova.models.Vehicle
 * @see com.bidnova.models.RealEstate
 * @see com.bidnova.models.ArtCollectible
 * @see com.bidnova.models.StateProperty
 */
public interface ItemCreator {
    
    /**
     * createItem() - Tạo một item mới của loại cụ thể
     * 
     * <h3>Tham số:</h3>
     * <ul>
     *   <li><b>id:</b> ID duy nhất của item (auto-generated từ DB)</li>
     *   <li><b>name:</b> Tên item (ví dụ: "Toyota Camry", "Căn hộ 2BR")</li>
     *   <li><b>description:</b> Mô tả chi tiết</li>
     *   <li><b>startingPrice:</b> Giá khởi điểm đấu giá</li>
     *   <li><b>extraParams:</b> Tham số bổ sung theo loại item:
     *     <ul>
     *       <li>Vehicle: make, model, year, mileage, condition</li>
     *       <li>RealEstate: address, area, bedrooms, bathrooms</li>
     *       <li>ArtCollectible: artist, year, medium, dimensions</li>
     *       <li>StateProperty: reason, area, location, legalStatus</li>
     *     </ul>
     *   </li>
     * </ul>
     * 
     * @param id              ID duy nhất của item
     * @param name            Tên item
     * @param description     Mô tả chi tiết
     * @param startingPrice   Giá khởi điểm
     * @param extraParams     Các tham số bổ sung theo loại item
     * @return Item mới được tạo (subclass của Item class)
     * 
     * @throws IllegalArgumentException Nếu tham số không hợp lệ
     */
    Item createItem(int id, String name, String description, double startingPrice, Object... extraParams);
    
    /**
     * supportsCategory() - Kiểm tra loại item này có hỗ trợ danh mục không
     * 
     * <p>Được gọi bởi ItemFactoryRegistry để tìm creator phù hợp.</p>
     * 
     * @param category Danh mục cần kiểm tra
     *                 (ví dụ: "Phương tiện - xe cộ", "Bất động sản", "Sưu tầm - nghệ thuật")
     * @return true nếu creator này hỗ trợ category, false ngược lại
     * 
     * @example
     * <pre>
     * VehicleCreator creator = new VehicleCreator();
     * creator.supportsCategory("Phương tiện - xe cộ") → true
     * creator.supportsCategory("Bất động sản") → false
     * </pre>
     */
    boolean supportsCategory(String category);
    
    /**
     * getItemTypeName() - Lấy tên loại item của creator này
     * 
     * @return Tên loại item (ví dụ: "Phương tiện", "Bất động sản", "Đồ nghệ thuật")
     */
    String getItemTypeName();
}
