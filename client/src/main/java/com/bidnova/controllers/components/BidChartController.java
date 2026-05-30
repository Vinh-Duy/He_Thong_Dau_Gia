package com.bidnova.controllers.components;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bidnova.models.BidHistory;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class BidChartController {
    @FXML private LineChart<Number, Number> bidChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button btnZoomIn;
    @FXML private Button btnZoomOut;
    @FXML private Button btnResetView;
    @FXML private Button btnScrollEnd;
    @FXML private Label lblDataInfo;

    private final XYChart.Series<Number, Number> series = new XYChart.Series<>();
    private final DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Lưu label thời gian tương ứng với mỗi index */
    private final List<String> timeLabels = new ArrayList<>();

    /** Số điểm hiển thị trong cửa sổ (window size) */
    private int windowSize = 20;
    private static final int MIN_WINDOW = 5;
    private static final int MAX_WINDOW = 100;

    /** Vị trí hiện tại của cửa sổ (index cuối cùng hiển thị) */
    private int windowEnd = 0;

    /** Biến cho drag-to-pan */
    private double dragStartX = 0;
    private double dragStartLower = 0;

    @FXML
    public void initialize() {
        series.setName("Giá đấu");
        bidChart.getData().add(series);
        bidChart.setAnimated(false);
        bidChart.setLegendVisible(false);
        bidChart.setCreateSymbols(true);
        yAxis.setForceZeroInRange(false);

        // Cấu hình xAxis: không autoRanging, dùng tick format custom
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelRotation(-45);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number index) {
                int i = index.intValue();
                if (i >= 0 && i < timeLabels.size()) {
                    return timeLabels.get(i);
                }
                return "";
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        // Mouse scroll => zoom
        bidChart.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                zoomIn();
            } else {
                zoomOut();
            }
            event.consume();
        });

        // Mouse drag => pan
        bidChart.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartLower = xAxis.getLowerBound();
            event.consume();
        });

        bidChart.setOnMouseDragged(event -> {
            double deltaX = event.getX() - dragStartX;
            // Tính số data points tương ứng với pixel kéo
            double chartWidth = bidChart.getWidth();
            double visibleRange = xAxis.getUpperBound() - xAxis.getLowerBound();
            double dataDelta = -(deltaX / chartWidth) * visibleRange;

            double newLower = dragStartLower + dataDelta;
            applyWindow(newLower, newLower + visibleRange);
            event.consume();
        });

        updateDataInfo();
    }

    public void loadChartData(String auctionId) {
        new Thread(() -> {
            try {
                Request request = new Request("GET_BID_HISTORY", auctionId);
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                            .create();

                    List<BidHistory> history = gson.fromJson((String) response.getData(),
                            new TypeToken<List<BidHistory>>() {}.getType());

                    Platform.runLater(() -> {
                        series.getData().clear();
                        timeLabels.clear();
                        if (history != null) {
                            int index = 0;
                            for (BidHistory bid : history) {
                                if (bid != null && bid.getBidTime() != null) {
                                    String timeLabel = labelFormatter.format(bid.getBidTime());
                                    timeLabels.add(timeLabel);
                                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(index, bid.getBidAmount());
                                    series.getData().add(dataPoint);
                                    installTooltip(dataPoint, timeLabel, bid.getBidAmount());
                                    index++;
                                }
                            }
                        }
                        windowEnd = timeLabels.size();
                        updateDataInfo();
                        scrollToEnd();
                    });
                }
            } catch (Exception e) {
                System.err.println("Lỗi load chart history: " + e.getMessage());
            }
        }).start();
    }

    public void appendBidPoint(double bidValue) {
        Platform.runLater(() -> {
            String timeLabel = labelFormatter.format(LocalDateTime.now());
            int index = timeLabels.size();
            timeLabels.add(timeLabel);
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(index, bidValue);
            series.getData().add(dataPoint);
            installTooltip(dataPoint, timeLabel, bidValue);

            updateDataInfo();
            scrollToEnd();
        });
    }

    /** Áp dụng cửa sổ hiển thị, clamp trong giới hạn dữ liệu */
    private void applyWindow(double lower, double upper) {
        int totalPoints = timeLabels.size();
        if (totalPoints <= 1) {
            xAxis.setLowerBound(-0.5);
            xAxis.setUpperBound(0.5);
            xAxis.setTickUnit(1);
            return;
        }

        double range = upper - lower;

        // Clamp
        if (lower < -0.5) {
            lower = -0.5;
            upper = lower + range;
        }
        if (upper > totalPoints - 0.5) {
            upper = totalPoints - 0.5;
            lower = upper - range;
        }
        if (lower < -0.5) {
            lower = -0.5;
        }

        xAxis.setLowerBound(lower);
        xAxis.setUpperBound(upper);

        // Tick unit tự điều chỉnh: không quá 10 ticks trên trục
        double visibleRange = upper - lower;
        int tickUnit = Math.max(1, (int) Math.ceil(visibleRange / 10));
        xAxis.setTickUnit(tickUnit);
    }

    @FXML
    private void zoomIn() {
        if (windowSize <= MIN_WINDOW) return;
        windowSize = Math.max(MIN_WINDOW, windowSize - 3);
        double center = (xAxis.getLowerBound() + xAxis.getUpperBound()) / 2.0;
        double halfWindow = windowSize / 2.0;
        applyWindow(center - halfWindow, center + halfWindow);
    }

    @FXML
    private void zoomOut() {
        if (windowSize >= MAX_WINDOW) return;
        windowSize = Math.min(MAX_WINDOW, windowSize + 3);
        double center = (xAxis.getLowerBound() + xAxis.getUpperBound()) / 2.0;
        double halfWindow = windowSize / 2.0;
        applyWindow(center - halfWindow, center + halfWindow);
    }

    @FXML
    private void resetView() {
        int totalPoints = timeLabels.size();
        windowSize = 20;
        if (totalPoints <= windowSize) {
            applyWindow(-0.5, totalPoints - 0.5);
        } else {
            scrollToEnd();
        }
    }

    @FXML
    private void scrollToEnd() {
        int totalPoints = timeLabels.size();
        if (totalPoints == 0) {
            applyWindow(-0.5, 0.5);
            return;
        }
        double upper = totalPoints - 0.5;
        double lower = upper - windowSize;
        if (lower < -0.5) lower = -0.5;
        applyWindow(lower, upper);
    }

    private void updateDataInfo() {
        if (lblDataInfo != null) {
            lblDataInfo.setText(timeLabels.size() + " điểm dữ liệu");
        }
    }

    /** Gắn tooltip hiển thị thời gian + giá khi hover vào data point */
    private void installTooltip(XYChart.Data<Number, Number> dataPoint, String time, double price) {
        // Node có thể chưa tồn tại ngay, cần lắng nghe khi nó được tạo
        if (dataPoint.getNode() != null) {
            applyTooltipToNode(dataPoint.getNode(), time, price);
        } else {
            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    applyTooltipToNode(newNode, time, price);
                }
            });
        }
    }

    private void applyTooltipToNode(Node node, String time, double price) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String tooltipText = "⏱ " + time + "\n💰 " + currencyFormat.format(price);

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setStyle(
            "-fx-background-color: rgba(44, 62, 80, 0.95);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 12;" +
            "-fx-background-radius: 8;"
        );

        Tooltip.install(node, tooltip);

        // Hiệu ứng hover: phóng to điểm khi hover
        node.setOnMouseEntered(e -> node.setScaleX(1.5));
        node.setOnMouseEntered(e -> { node.setScaleX(1.5); node.setScaleY(1.5); });
        node.setOnMouseExited(e -> { node.setScaleX(1.0); node.setScaleY(1.0); });
    }
}
