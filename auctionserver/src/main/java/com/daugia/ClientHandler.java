package com.daugia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.daugia.dao.UserDAO;
import com.daugia.models.Auction;
import com.daugia.models.User;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Nhận từ Client: " + inputLine);
                
                Request request = gson.fromJson(inputLine, Request.class);
                Response response = null;

                UserDAO userDAO = new UserDAO();

            switch (request.getAction()) {
                case "LOGIN":

                    JsonObject loginData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                    String user = loginData.get("username").getAsString();
                    String pass = loginData.get("password").getAsString();

                    User loggedInUser = userDAO.checkLogin(user, pass);


                    if (loggedInUser != null) {

                        String userDataJson = gson.toJson(loggedInUser);
                        response = new Response("SUCCESS", "Đăng nhập thành công", userDataJson);
                    } else {

                        response = new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
                    }
                    break;

                case "REGISTER":
                    JsonObject regData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                    String regUser = regData.get("username").getAsString();
                    String regPass = regData.get("password").getAsString();

                    boolean isSuccess = userDAO.register(regUser, regPass);
                    
                    if (isSuccess) {
                        response = new Response("SUCCESS", "Đăng ký thành công", null);
                    } else {
                        response = new Response("ERROR", "Tài khoản đã tồn tại!", null);
                    }
                    break;

                case "PLACE_BID":
                    JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                    int auctionId = bidData.get("auctionId").getAsInt();
                    double amount = bidData.get("amount").getAsDouble();
                    String username = bidData.get("username").getAsString();

                    AuctionManager manager = AuctionManager.getInstance();
                    Auction auction = manager.getAuction(auctionId);

                    if (auction != null) {

                        boolean success = auction.placeBid(username, amount); 
                        
                        if (success) {
                            response = new Response("SUCCESS", "Đặt giá thành công!", String.valueOf(auction.getCurrentHighestBid()));
                        } else {

                            response = new Response("ERROR", "Giá không hợp lệ hoặc phiên đấu giá đã kết thúc!", null);
                        }
                    } else {
                        response = new Response("ERROR", "Không tìm thấy phiên đấu giá này!", null);
                    }
                    break;

                case "GET_ALL_AUCTIONS":

                    String dummyAuctions = "[" +
                        "{\"name\": \"Đồng hồ Rolex Datejust\", \"currentHighestBid\": 320000000}," +
                        "{\"name\": \"Tranh sơn dầu thế kỷ 19\", \"currentHighestBid\": 150000000}" +
                    "]";
                    response = new Response("SUCCESS", "Lấy danh sách thành công", dummyAuctions);
                    break;

                case "GET_ITEMS_BY_CATEGORY":

                    String dummyJsonArray = "[" +
                        "{\"name\": \"Sản phẩm test 1\", \"startingPrice\": 5000000}," +
                        "{\"name\": \"Sản phẩm test 2\", \"startingPrice\": 12000000}" +
                    "]";
                    response = new Response("SUCCESS", "Lấy dữ liệu thành công", dummyJsonArray);
                    break;

                default:
                    response = new Response("ERROR", "Không tìm thấy Action", null);
            }
            

                String jsonResponse = gson.toJson(response);
                out.println(jsonResponse);
            }
        } catch (Exception e) {
            System.out.println("Client đã ngắt kết nối.");
        }
    }
}