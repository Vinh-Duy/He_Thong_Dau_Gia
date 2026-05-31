package com.bidnova.dao;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.bidnova.models.Auction;
import java.util.List;

/**
 * Unit tests for AuctionDAO
 * Test error handling và default values khi connection null
 */
class AuctionDAOTest {

    private final AuctionDAO auctionDAO = new AuctionDAO();

    @BeforeEach
    void setUp() {
        // Sử dụng null connection supplier để test error handling
        auctionDAO.setConnectionSupplier(() -> null);
    }

    @AfterEach
    void tearDown() {
        // Reset về default connection supplier
        auctionDAO.setConnectionSupplier(com.bidnova.database.DatabaseConnection::getConnection);
    }

    @Test
    void testFindById_ReturnsNullForInvalidId() {
        // Kiểm tra xem hệ thống có trả về null khi truyền ID không tồn tại thay vì văng lỗi không
        Auction result = auctionDAO.findById("non-existent-12345");
        assertNull(result, "Phải trả về null nếu không tìm thấy Auction");
    }

    @Test
    void testFindById_ReturnsNullOnConnectionError() {
        // Test khi connection lỗi
        auctionDAO.setConnectionSupplier(() -> null);
        Auction result = auctionDAO.findById("any-id");
        assertNull(result, "Phải trả về null khi connection null");
    }

    @Test
    void testGetMinBidIncrement_ReturnsDefaultValue() {
        // Kiểm tra logic: Nếu auction không tồn tại hoặc lỗi, phải trả về giá trị mặc định (1000)
        double increment = auctionDAO.getMinBidIncrement("dummy_id");
        assertEquals(1000.0, increment, "Bước giá mặc định phải là 1000.0");
    }

    @Test
    void testGetPriceCeiling_HandlesNullCorrectly() {
        // Kiểm tra logic lấy giá trần
        Double ceiling = auctionDAO.getPriceCeiling("dummy_id");
        assertNull(ceiling, "Giá trần của phiên không tồn tại phải là null");
    }

    @Test
    void testGetMinBidIncrement_CheckLogic() {
        // Kiểm tra xem bước giá có được trả về đúng (mặc định 1000 nếu ko tìm thấy)
        double result = auctionDAO.getMinBidIncrement("dummy_id");
        assertEquals(1000.0, result);
        
        assertTrue(result >= 0, "Bước giá không được âm");
    }

    @Test
    void testGetAllActiveAuctions_IsNotNull() {
        // Đảm bảo hàm trả về một List (có thể rỗng) chứ không phải null để tránh NullPointerException ở Service
        List<Auction> auctions = auctionDAO.getAllActiveAuctions();
        assertNotNull(auctions, "Danh sách auction trả về không được là null");
    }

    @Test
    void testGetAllActiveAuctions_ReturnsEmptyListOnConnectionError() {
        // Test khi connection lỗi
        auctionDAO.setConnectionSupplier(() -> null);
        List<Auction> auctions = auctionDAO.getAllActiveAuctions();
        assertNotNull(auctions, "Phải trả về list rỗng khi connection null");
        assertTrue(auctions.isEmpty(), "List phải rỗng khi connection null");
    }

    @Test
    void testGetAuctionsBySellerId_EmptyListForUnknownSeller() {
        // Kiểm tra với seller ID không tồn tại
        List<Auction> auctions = auctionDAO.getAuctionsBySellerId(-1);
        assertTrue(auctions.isEmpty(), "Seller không tồn tại phải trả về danh sách rỗng");
    }

    @Test
    void testGetAuctionsBySellerId_ReturnsEmptyListOnConnectionError() {
        // Test khi connection lỗi
        auctionDAO.setConnectionSupplier(() -> null);
        List<Auction> auctions = auctionDAO.getAuctionsBySellerId(1);
        assertNotNull(auctions, "Phải trả về list rỗng khi connection null");
        assertTrue(auctions.isEmpty(), "List phải rỗng khi connection null");
    }

    @Test
    void testUpdateStatus_DoesNotThrowException() {
        // Đảm bảo phương thức update status không gây crash app khi gặp lỗi DB
        assertDoesNotThrow(() -> {
            auctionDAO.updateStatus("test_id", "FINISHED");
        }, "Phương thức updateStatus không được văng Exception");
    }

    @Test
    void testUpdateStatus_DoesNotThrowExceptionOnConnectionError() {
        // Test khi connection lỗi
        auctionDAO.setConnectionSupplier(() -> null);
        assertDoesNotThrow(() -> {
            auctionDAO.updateStatus("test_id", "FINISHED");
        }, "Phương thức updateStatus không được văng Exception ngay cả khi connection null");
    }

    @Test
    void testUpdateHighestBid_NoCrash() {
        assertDoesNotThrow(() -> {
            auctionDAO.updateHighestBid("auc_id", 50000.0);
        });
    }

    @Test
    void testUpdateHighestBid_DoesNotThrowExceptionOnConnectionError() {
        // Test khi connection lỗi
        auctionDAO.setConnectionSupplier(() -> null);
        assertDoesNotThrow(() -> {
            auctionDAO.updateHighestBid("auc_id", 50000.0);
        }, "Phương thức updateHighestBid không được văng Exception ngay cả khi connection null");
    }
}