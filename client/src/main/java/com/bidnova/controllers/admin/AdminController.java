package com.bidnova.controllers.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.bidnova.models.Auction;
import com.bidnova.models.User;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class AdminController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;

    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> auctionIdCol;
    @FXML private TableColumn<Auction, String> auctionNameCol;
    @FXML private TableColumn<Auction, String> auctionCategoryCol;
    @FXML private TableColumn<Auction, Double> auctionStartPriceCol;
    @FXML private TableColumn<Auction, Double> auctionCurrentPriceCol;
    @FXML private TableColumn<Auction, String> auctionStatusCol;
    @FXML private TableColumn<Auction, String> auctionStartTimeCol;
    @FXML private TableColumn<Auction, String> auctionEndTimeCol;

    @FXML private VBox userPanel;
    @FXML private VBox auctionPanel;
    @FXML private Button btnUserTab;
    @FXML private Button btnAuctionTab;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<Auction> auctionList = FXCollections.observableArrayList();
    private Gson gson = new Gson();
    private Gson auctionGson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @FXML
    public void initialize() {
        // User columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Auction columns
        auctionIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        auctionNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        auctionCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        auctionStartPriceCol.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        auctionCurrentPriceCol.setCellValueFactory(new PropertyValueFactory<>("currentHighestBid"));
        auctionStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        auctionStartTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        auctionEndTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        userTable.setItems(userList);
        auctionTable.setItems(auctionList);

        // Configure resize
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        auctionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Load data
        loadUsers();
        loadAuctions();

        // 🔴 Lắng nghe real-time updates từ server
        NetworkClient.getInstance().onMessageReceived(message -> {
            try {
                JsonObject data = JsonParser.parseString(message).getAsJsonObject();
                String action = data.has("action") ? data.get("action").getAsString() : null;
                if ("AUCTION_LIST_UPDATE".equals(action)) {
                    Platform.runLater(this::loadAuctions);
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý broadcast message: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.showAndWait();
    }

    // ============ TAB SWITCHING ============

    @FXML
    private void switchToUserTab() {
        userPanel.setVisible(true);
        userPanel.setManaged(true);
        auctionPanel.setVisible(false);
        auctionPanel.setManaged(false);
        btnUserTab.getStyleClass().add("tab-active");
        btnAuctionTab.getStyleClass().remove("tab-active");
    }

    @FXML
    private void switchToAuctionTab() {
        userPanel.setVisible(false);
        userPanel.setManaged(false);
        auctionPanel.setVisible(true);
        auctionPanel.setManaged(true);
        btnAuctionTab.getStyleClass().add("tab-active");
        btnUserTab.getStyleClass().remove("tab-active");
    }

    // ============ USER MANAGEMENT ============

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                Request request = new Request("GET_ALL_USERS", "");
                Response response = NetworkClient.getInstance().sendRequest(request);

                if ("SUCCESS".equals(response.getStatus())) {
                    List<User> list = gson.fromJson((String) response.getData(),
                                      new TypeToken<List<User>>(){}.getType());
                    Platform.runLater(() -> userList.setAll(list));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn một người dùng để xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
            "Bạn có chắc muốn xóa người dùng " + selectedUser.getUsername() + "?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("userId", selectedUser.getId());
                    Request request = new Request("DELETE_USER", payload.toString());
                    Response response = NetworkClient.getInstance().sendRequest(request);
                    if ("SUCCESS".equals(response.getStatus())) {
                        Platform.runLater(() -> {
                            userList.remove(selectedUser);
                            showAlert("Thông báo", response.getMessage());
                        });
                    } else {
                        Platform.runLater(() -> showError("Lỗi", response.getMessage()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showError("Lỗi", "Lỗi khi xóa người dùng!"));
                }
            }).start();
        }
    }

    // ============ AUCTION MANAGEMENT ============

    @FXML
    private void handleRefreshAuctions() {
        loadAuctions();
    }

    private void loadAuctions() {
        new Thread(() -> {
            try {
                Request request = new Request("GET_ALL_AUCTIONS", "", SessionManager.getToken());
                Response response = NetworkClient.getInstance().sendRequest(request);

                if ("SUCCESS".equals(response.getStatus())) {
                    List<Auction> list = auctionGson.fromJson((String) response.getData(),
                                        new TypeToken<List<Auction>>(){}.getType());
                    Platform.runLater(() -> auctionList.setAll(list));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleDeleteAuction() {
        Auction selectedAuction = auctionTable.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            showAlert("Thông báo", "Vui lòng chọn một phiên đấu giá để xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
            "Bạn có chắc muốn xóa phiên đấu giá [" + selectedAuction.getProductName() + "]?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    Request request = new Request("DELETE_PRODUCT", selectedAuction.getId());
                    Response response = NetworkClient.getInstance().sendRequest(request);
                    if ("SUCCESS".equals(response.getStatus())) {
                        Platform.runLater(() -> {
                            auctionList.remove(selectedAuction);
                            showAlert("Thành công", "Đã xóa phiên đấu giá!");
                        });
                    } else {
                        Platform.runLater(() -> showError("Lỗi", response.getMessage()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showError("Lỗi", "Lỗi khi xóa phiên đấu giá!"));
                }
            }).start();
        }
    }
}