package org.example;
import org.jmrtd.PACEKeySpec;
import org.jmrtd.PassportService;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.LDSFileUtil;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.*;

public class  Main {
    public static void main(String[] args) {
        // MRZ mẫu từ CCCD
        String rawMRZ =
                "I<VNM022203000771<<<<<<<<<<<<<<<\n" +
                        "0411206M3001019VNM<<<<<<<<<<<\n" +
                        "NGUYEN<<VAN<A<<<<<<<<<<<<<<<<<";

        decodeMRZ(rawMRZ);
    }

    public static void decodeMRZ(String rawMRZ) {
        String[] lines = rawMRZ.split("\n");

        if (lines.length < 3) {
            System.out.println("Lỗi: MRZ không hợp lệ!");
            return;
        }

        // 🔹 Dòng 1: Loại tài liệu, quốc gia phát hành, số CCCD
        String documentType = lines[0].substring(0, 2); // Loại tài liệu (I<)
        String issuingCountry = lines[0].substring(2, 5); // Quốc gia (VNM)
        String documentNumber = lines[0].substring(5, 14).replace("<", ""); // Số CCCD

        // 🔹 Dòng 2: Ngày sinh, giới tính, ngày hết hạn, quốc tịch
        String birthDate = lines[1].substring(0, 6); // 041120 (04/11/20)
        char gender = lines[1].charAt(7); // M (Nam) / F (Nữ)
        String expiryDate = lines[1].substring(8, 14); // 300101 (01/01/30)
        String nationality = lines[1].substring(15, 18); // VNM

        // 🔹 Dòng 3: Họ và tên
        String fullName = lines[2].replace("<", " ").trim();

        // 🔹 Kiểm tra Check Digit
        char checkDigitCCCD = checkDigit(documentNumber, false);
        char checkDigitBirth = checkDigit(birthDate, false);
        char checkDigitExpiry = checkDigit(expiryDate, false);

        // 🔹 In kết quả
        System.out.println("📄 Loại tài liệu: " + documentType);
        System.out.println("🌎 Quốc gia phát hành: " + issuingCountry);
        System.out.println("🆔 Số CCCD: " + documentNumber + " (Check: " + checkDigitCCCD + ")");
        System.out.println("📅 Ngày sinh: " + birthDate + " (Check: " + checkDigitBirth + ")");
        System.out.println("👤 Giới tính: " + (gender == 'M' ? "Nam" : "Nữ"));
        System.out.println("📅 Hạn CCCD: " + expiryDate + " (Check: " + checkDigitExpiry + ")");
        System.out.println("🌎 Quốc tịch: " + nationality);
        System.out.println("👨‍💼 Họ tên: " + fullName);
    }

    // 🔹 Hàm tính Check Digit (dựa trên code của thư viện jmrtd)
    private static char checkDigit(String str, boolean preferFillerOverZero) {
        try {
            byte[] chars = str.getBytes("UTF-8");
            int[] weights = {7, 3, 1};
            int result = 0;

            for (int i = 0; i < chars.length; ++i) {
                result = (result + weights[i % 3] * decodeMRZDigit(chars[i])) % 10;
            }

            return (char) ('0' + result);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Error in computing check digit", e);
        }
    }

    // 🔹 Hàm chuyển ký tự MRZ thành số (dựa trên jmrtd)
    private static int decodeMRZDigit(byte ch) {
        if (ch >= '0' && ch <= '9') return ch - '0';
        if (ch >= 'A' && ch <= 'Z') return ch - 'A' + 10;
        if (ch == '<') return 0;
        throw new NumberFormatException("Could not decode MRZ character: " + (char) ch);
    }

}