package org.example;

import org.jmrtd.*;
import org.jmrtd.io.*;
import org.jmrtd.lds.*;
import javax.smartcardio.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;

public class DecodeCCCD {

    // Hàm chuyển đổi byte[] thành chuỗi Hex
    public static String toHex(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            String hex = Integer.toHexString(0xff & byteArray[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    public static void main(String[] args) {
        String raspberryPiIP = "192.168.0.111";  // Địa chỉ IP của Raspberry Pi
        int port = 9999;  // Cổng giao tiếp

        try (Socket socket = new Socket(raspberryPiIP, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✅ Kết nối đến Raspberry Pi thành công!");

            // Đọc dữ liệu từ Raspberry Pi (dữ liệu thẻ)
            StringBuilder cccdData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                cccdData.append(line).append("\n");
            }

            // Kết nối với đầu đọc thẻ qua Java Smart Card API (PCSC)
            TerminalFactory terminalFactory = TerminalFactory.getDefault();
            CardTerminal cardTerminal = terminalFactory.terminals().list().get(0);
            System.out.println("✅ Đầu đọc thẻ đã được kết nối!");

            // Kiểm tra nếu có thẻ trong đầu đọc
            if (!cardTerminal.isCardPresent()) {
                System.out.println("❌ Không tìm thấy thẻ.");
                return;
            }

            // Kết nối với thẻ
            Card card = cardTerminal.connect("*");
            System.out.println("✅ Thẻ đã được kết nối!");

            // Khởi tạo CardService để tương tác với thẻ
            Iso7816CardService cardService = new Iso7816CardService(card);
            MRTDReader reader = new MRTDReader(cardService);

            // Đọc thông tin từ thẻ (ví dụ: DG1 - thông tin cá nhân)
            TR103173Document document = (TR103173Document) reader.readDocument();
            MRZ mrz = document.getMRZ();

            // In các thông tin đã đọc từ thẻ
            System.out.println("Full Name: " + mrz.getFullName());
            System.out.println("Document Number: " + mrz.getDocumentNumber());
            System.out.println("Nationality: " + mrz.getNationality());

            // Tiến hành gửi thông tin hoặc tiếp tục xử lý theo yêu cầu của bạn
            // Ví dụ: gửi lại thông tin về cho server hoặc thực hiện hành động tiếp theo.

            // Đóng kết nối với thẻ
            card.disconnect(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
