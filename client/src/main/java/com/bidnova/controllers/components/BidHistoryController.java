package com.bidnova.controllers.components;

import com.bidnova.models.BidHistory;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BidHistoryController {
    @FXML private TableView<BidHistory> historyTable;
    @FXML private TableColumn<BidHistory, LocalDateTime> colTime;
    @FXML private TableColumn<BidHistory, String> colUser;
    @FXML private TableColumn<BidHistory, Double> colAmount;

    private final ObservableList<BidHistory> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Map dữ liệu từ Model vào cột
        colTime.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));

        // Format hiển thị cho cột Thời gian
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/YYYY");
        colTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : timeFormatter.format(item));
            }
        });

        // Format hiển thị cho cột Giá tiền
        colAmount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("bid-amount-cell"); // Reset style cũ
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", item));
                    getStyleClass().add("bid-amount-cell"); // Áp dụng class từ CSS
                }
            }
        });

        historyTable.setItems(masterData);
    }

    public void setHistoryData(List<BidHistory> data) {
        masterData.setAll(data);
        // Luôn cuộn xuống bản ghi mới nhất (nếu danh sách sắp xếp theo thời gian tăng dần)
        if (!masterData.isEmpty()) {
            historyTable.scrollTo(masterData.size() - 1);
        }
    }

    public void addSingleBid(BidHistory bid) {
        masterData.add(bid);
        historyTable.scrollTo(masterData.size() - 1);
    }

    public void loadHistory(String auctionId) {
        new Thread(() -> {
            try {
                Request request = new Request("GET_BID_HISTORY", auctionId);
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                            .create();

                    List<BidHistory> history = gson.fromJson(response.getData().toString(),
                            new TypeToken<List<BidHistory>>() {}.getType());

                    Platform.runLater(() -> setHistoryData(history));
                }
            } catch (Exception e) {
                System.err.println("Lỗi load lịch sử đấu giá: " + e.getMessage());
            }
        }).start();
    }
}