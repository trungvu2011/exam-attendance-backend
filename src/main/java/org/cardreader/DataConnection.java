package org.cardreader;

import lombok.RequiredArgsConstructor;
import net.sf.scuba.smartcards.CardServiceException;
import org.jmrtd.PACEKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SecurityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andrii Tkachenko
 */
@RequiredArgsConstructor
@Validated
@Service
public class DataConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConnection.class);

    private final PassportService ps;

    private static final String RASPBERRY_PI_IP = "192.168.0.111";  // Địa chỉ IP của Raspberry Pi
    private static final int PORT = 9999;  // Cổng giao tiếp

    // Mở kết nối và thực hiện xác thực PACE với thẻ căn cước
    public boolean initConnection(@NotNull String can) throws CardServiceException, IOException {
        // Tạo kết nối với Raspberry Pi qua Socket
        try (Socket socket = new Socket(RASPBERRY_PI_IP, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            LOGGER.info("✅ Kết nối đến Raspberry Pi thành công!");

            // Đọc dữ liệu từ Raspberry Pi (CCCD)
            StringBuilder cccdData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                cccdData.append(line).append("\n");
            }

            // Nếu không nhận được dữ liệu, trả về false
            if (cccdData.length() == 0) {
                LOGGER.error("Không nhận được dữ liệu từ thẻ!");
                return false;
            }

            // Hiển thị thông tin dữ liệu đã nhận từ Raspberry Pi
            LOGGER.info("Dữ liệu từ Raspberry Pi: " + cccdData.toString());

            // Khởi tạo đối tượng PassportService từ dữ liệu nhận được
            CardAccessFile cardAccessFile = new CardAccessFile(ps.getInputStream(PassportService.EF_CARD_ACCESS));
            Collection<SecurityInfo> securityInfos = cardAccessFile.getSecurityInfos();
            SecurityInfo securityInfo = securityInfos.iterator().next();
            LOGGER.info("ProtocolOIDString: " + securityInfo.getProtocolOIDString());
            LOGGER.info("ObjectIdentifier: " + securityInfo.getObjectIdentifier());

            // Tìm thông tin PACE trong SecurityInfo
            List<PACEInfo> paceInfos = getPACEInfos(securityInfos);
            LOGGER.debug("Found a card access file: paceInfos (" + (paceInfos == null ? 0 : paceInfos.size()) + ") = " + paceInfos);
            if (paceInfos != null && paceInfos.size() > 0) {
                PACEInfo paceInfo = paceInfos.get(0);

                // Tạo khóa PACE từ CAN
                PACEKeySpec paceKey = PACEKeySpec.createCANKey(can);
                ps.doPACE(paceKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()), paceInfo.getParameterId());

                ps.sendSelectApplet(true);
                LOGGER.info("✅ Xác thực PACE thành công!");
                return true;
            } else {
                LOGGER.error("Không tìm thấy thông tin PACE trong thẻ.");
                ps.close();
                return false;
            }
        }
    }

    // Lọc và trả về danh sách PACEInfo từ SecurityInfo
    private List<PACEInfo> getPACEInfos(Collection<SecurityInfo> securityInfos) {
        return securityInfos.stream()
                .filter(securityInfo -> securityInfo instanceof PACEInfo)
                .map(securityInfo -> (PACEInfo) securityInfo)
                .collect(Collectors.toList());
    }
}
