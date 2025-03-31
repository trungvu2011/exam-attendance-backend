package org.example;

import java.io.*;
import java.net.*;

public class RemoteNFCClient {
    public static void main(String[] args) {
        String raspberryPiIP = "192.168.0.111"; // Đổi thành IP của Raspberry Pi
        int port = 9999;

        try (Socket socket = new Socket(raspberryPiIP, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✅ Kết nối đến Raspberry Pi thành công!");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // In kết quả thẻ CCCD
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
        }
    }
}
