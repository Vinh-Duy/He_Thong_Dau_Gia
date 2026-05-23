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
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BidChartController {
    @FXML private LineChart<String, Number> bidChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final XYChart.Series<String, Number> series = new XYChart.Series<>();
    private final DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        series.setName("Giá đấu");
        bidChart.getData().add(series);
        bidChart.setAnimated(false);
        bidChart.setLegendVisible(false);
        bidChart.setCreateSymbols(true);
        xAxis.setLabel("Thời gian");
        yAxis.setLabel("Giá (VNĐ)");
        yAxis.setForceZeroInRange(false);
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

                    List<BidHistory> history = gson.fromJson(response.getData().toString(),
                            new TypeToken<List<BidHistory>>() {}.getType());

                    Platform.runLater(() -> {
                        series.getData().clear();
                        if (history != null) {
                            for (BidHistory bid : history) {
                                if (bid != null && bid.getBidTime() != null) {
                                    series.getData().add(new XYChart.Data<>(labelFormatter.format(bid.getBidTime()), bid.getBidAmount()));
                                }
                            }
                        }
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
            series.getData().add(new XYChart.Data<>(timeLabel, bidValue));
            if (series.getData().size() > 30) {
                series.getData().remove(0);
            }
        });
    }
}
