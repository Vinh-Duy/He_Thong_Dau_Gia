package com.bidnova.models;

/**
 * 📦 Item (Abstract) - Lớp cơ sở cho tất cả các loại sản phẩm
 * 
 * <h2>Chức Năng:</h2>
 * <p>Định nghĩa cấu trúc chung cho mọi loại sản phẩm có thể đấu giá.</p>
 * <p>Sử dụng <strong>Abstract Class</strong> + <strong>Factory Pattern</strong>
 * để tạo các loại sản phẩm khác nhau.</p>
 * 
 * <h2>Loại Sản Phẩm Hỗ Trợ (Subclasses):</h2>
 * <ul>
 *   <li><b>Vehicle:</b> Phương tiện (oto, xe máy, xe đạp...)</li>
 *   <li><b>RealEstate:</b> Bất động sản (nhà, đất, căn hộ...)</li>
 *   <li><b>ArtCollectible:</b> Đồ nghệ thuật (tranh, tượng, bảo vật...)</li>
 *   <li><b>StateProperty:</b> Tài sản công (tịch thu, đấu giá tài sản nhà nước...)</li>
 * </ul>
 * 
 * <h2>Kiến Trúc Design Pattern:</h2>
 * <pre>
 * ┌──────────────────┐
 * │  Item (Abstract) │ ← Định nghĩa contract
 * └────────┬─────────┘
 *          │
 *    ┌─────┼─────┬──────────────┐
 *    ▼     ▼     ▼              ▼
 * Vehicle RealEstate ArtCollectible StateProperty
 *    │     │     │              │
 *    └─────┴─────┴──────────────┘
 *              ▼
 *      ItemFactory (Factory Pattern)
 *    ┌─────────────────────────┐
 *    │ ItemCreator interface   │
 *    │ + VehicleCreator        │
 *    │ + RealEstateCreator     │
 *    │ + ...                   │
 *    └─────────────────────────┘
 * </pre>
 * 
 * <h2>Ứng Dụng trong Hệ Thống:</h2>
 * <pre>
 * 1. Khi seller tạo phiên đấu giá:
 *    - Chọn category (Vehicle/RealEstate/...)
 *    - AddProductHandler gọi ItemFactory.createItem()
 *    - Factory tạo Item subclass phù hợp với chi tiết cụ thể
 * 
 * 2. Thông tin Item được lưu vào bảng items (hoặc tách riêng theo loại)
 * 
 * 3. Khi client xem chi tiết:
 *    - Lấy Item từ DB
 *    - Gọi item.getDetailedInfo() để hiển thị
 *    - Thông tin khác nhau tùy theo subclass
 * </pre>
 * 
 * <h2>Các Trường Chung:</h2>
 * <ul>
 *   <li>id: ID duy nhất của sản phẩm</li>
 *   <li>name: Tên sản phẩm</li>
 *   <li>description: Mô tả chi tiết</li>
 *   <li>startingPrice: Giá khởi điểm</li>
 *   <li>category: Danh mục (tự động từ subclass)</li>
 * </ul>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see com.bidnova.patterns.factory.ItemFactory
 * @see Vehicle
 * @see RealEstate
 * @see ArtCollectible
 * @see StateProperty
 */
public abstract class Item {
    protected int id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected String category;

    /**
     * Constructor - Khởi tạo sản phẩm với thông tin cơ bản
     * 
     * @param id              ID duy nhất (auto-generated)
     * @param name            Tên sản phẩm
     * @param description     Mô tả chi tiết
     * @param startingPrice   Giá khởi điểm đấu giá
     * @param category        Danh mục sản phẩm (tự được set bởi subclass)
     */
    public Item(int id, String name, String description, double startingPrice, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.category = category;
    }

    // ==================== GETTERS ====================
    
    /**
     * @return ID duy nhất của sản phẩm
     */
    public int getId() { return id; }
    
    /**
     * @return Tên sản phẩm
     */
    public String getName() { return name; }
    
    /**
     * @return Mô tả chi tiết sản phẩm
     */
    public String getDescription() { return description; }
    
    /**
     * @return Giá khởi điểm đấu giá
     */
    public double getStartingPrice() { return startingPrice; }
    
    /**
     * @return Danh mục sản phẩm ("Vehicle", "RealEstate", ...)
     */
    public String getCategory() { return category; }
    
    /**
     * getDetailedInfo() - Trả về thông tin chi tiết sản phẩm theo loại
     * 
     * <p><strong>Phương thức trừu tượng (Abstract):</strong></p>
     * <p>Mỗi subclass phải implement riêng để trả về thông tin phù hợp:</p>
     * 
     * <h3>Ví Dụ Implementations:</h3>
     * <pre>
     * Vehicle.getDetailedInfo():
     *   "Vehicle: Toyota Camry 2020 - Condition: Excellent - Mileage: 15,000 km"
     * 
     * RealEstate.getDetailedInfo():
     *   "RealEstate: 5BR House - Area: 200m² - Location: District 1, HCMC"
     * 
     * ArtCollectible.getDetailedInfo():
     *   "ArtCollectible: Oil Painting - Artist: Unknown - Year: 1950"
     * 
     * StateProperty.getDetailedInfo():
     *   "StateProperty: Seized Property - Reason: Debt Default - Area: 150m²"
     * </pre>
     * 
     * @return Chuỗi mô tả chi tiết sản phẩm
     */
    public abstract String getDetailedInfo();
}