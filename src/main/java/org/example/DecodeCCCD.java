package org.example;

import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.smartcardio.*;

import org.jmrtd.BACKey;
import org.jmrtd.PassportService;
import org.jmrtd.lds.icao.*;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import net.sf.scuba.smartcards.*;
import java.util.List;

public class DecodeCCCD {
    public static void main(String[] args) {
        try {
            // 🔹 Kết nối với PN532 qua giao diện PC/SC
            CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
            Card card = terminal.connect("T=1");
            CardService service = new PCSCCardService(card.getBasicChannel());

            PassportService passportService = new PassportService(service, 256, 256, true, false);
            passportService.open();

            String documentNumber = "123456789"; // Thay bằng số CCCD
            String dateOfBirth = "900101"; // YYMMDD
            String dateOfExpiry = "300101"; // YYMMDD
            BACKey bacKey = new BACKey(documentNumber, dateOfBirth, dateOfExpiry);
            passportService.doBAC(bacKey);

            // 🔹 Đọc và lưu dữ liệu DG1 (Thông tin cá nhân)
            DG1File dg1 = new DG1File(passportService.getInputStream(PassportService.EF_DG1));
            String personalInfo = dg1.getMRZInfo().toString();
            Files.writeString(Paths.get("cccd_info.txt"), personalInfo);
            System.out.println("✅ Thông tin CCCD đã được lưu: cccd_info.txt");

            // 🔹 Đọc và lưu dữ liệu DG2 (Ảnh khuôn mặt)
            DG2File dg2 = new DG2File(passportService.getInputStream(PassportService.EF_DG2));
            List<FaceImageInfo> faceImages = dg2.getFaceInfos().get(0).getFaceImageInfos();
            if (!faceImages.isEmpty()) {
                byte[] imageData = faceImages.get(0).getImageInputStream().readAllBytes();
                ImageIO.write(ImageIO.read(new ByteArrayInputStream(imageData)), "png", new File("cccd_photo.png"));
                System.out.println("✅ Ảnh CCCD đã được lưu: cccd_photo.png");
            }

            // 🔹 Đóng kết nối
            card.disconnect(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}