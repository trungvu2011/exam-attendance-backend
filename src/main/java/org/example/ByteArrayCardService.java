package org.example;

import javax.smartcardio.*;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.HashSet;

import net.sf.scuba.smartcards.*;
import net.sf.scuba.smartcards.CommandAPDU;
import net.sf.scuba.smartcards.ResponseAPDU;


public class ByteArrayCardService extends CardService {

    private static final Logger LOGGER = Logger.getLogger("net.sf.scuba");

    private ByteArrayInputStream byteArrayInputStream;
    private byte[] atr;
    private Collection<APDUListener> apduListeners;

    // Constructor
    public ByteArrayCardService(ByteArrayInputStream byteArrayInputStream) {
        this.byteArrayInputStream = byteArrayInputStream;
        this.atr = new byte[]{0x3B, (byte) 0x8F, (byte) 0x80, 0x01, 0x01}; // ATR mặc định
        this.apduListeners = new HashSet<>();
        this.state = SESSION_STOPPED_STATE;
    }

    @Override
    public void open() throws CardServiceException {
        // Mở kết nối - trong trường hợp này, chỉ cần kiểm tra byte array.
        if (byteArrayInputStream == null) {
            throw new CardServiceException("Không có dữ liệu thẻ để xử lý.");
        }
        this.state = SESSION_STARTED_STATE;
        LOGGER.log(Level.INFO, "Kết nối đến ByteArrayCardService đã mở.");
    }

    @Override
    public boolean isOpen() {
        return this.state == SESSION_STARTED_STATE;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU command) throws CardServiceException {
        // Giả lập gửi lệnh CommandAPDU và nhận ResponseAPDU từ byte array
        try {
            byte[] commandBytes = command.getBytes();
            LOGGER.log(Level.INFO, "Gửi CommandAPDU: " + bytesToHex(commandBytes));

            // Giả lập việc xử lý và trả về một ResponseAPDU từ byte array
            byte[] responseBytes = processCommand(commandBytes);
            return new ResponseAPDU(responseBytes);
        } catch (Exception e) {
            throw new CardServiceException("Lỗi khi truyền APDU", e);
        }
    }

    @Override
    public byte[] getATR() throws CardServiceException {
        return atr;  // Trả về ATR giả lập
    }

    @Override
    public void close() {
        // Đóng kết nối
        this.state = SESSION_STOPPED_STATE;
        LOGGER.log(Level.INFO, "Kết nối ByteArrayCardService đã đóng.");
    }

    // Giả lập xử lý command và trả về response
    private byte[] processCommand(byte[] commandBytes) {
        // Giả lập xử lý dữ liệu từ commandBytes và trả về response
        byte[] response = new byte[commandBytes.length];
        for (int i = 0; i < commandBytes.length; i++) {
            response[i] = (byte) (commandBytes[i] ^ 0xFF);  // XOR đơn giản chỉ là ví dụ
        }
        return response;
    }

    // Chuyển byte array thành chuỗi hex
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // Thêm APDU listener
    public void addAPDUListener(APDUListener listener) {
        apduListeners.add(listener);
    }

    // Xóa APDU listener
    public void removeAPDUListener(APDUListener listener) {
        apduListeners.remove(listener);
    }

    // Thông báo APDU listeners về sự kiện
    protected void notifyExchangedAPDU(APDUEvent event) {
        for (APDUListener listener : apduListeners) {
            listener.exchangedAPDU(event);
        }
    }
}
