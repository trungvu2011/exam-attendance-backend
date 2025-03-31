package org.example;

import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.smartcardio.*;
import org.jmrtd.PassportService;
import org.jmrtd.BACKey;
import org.jmrtd.lds.icao.*;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import java.util.List;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CommandAPDU;
import net.sf.scuba.smartcards.ResponseAPDU;
import net.sf.scuba.smartcards.CardServiceException;

class PCSCCardService extends CardService {
    private CardChannel channel;

    public PCSCCardService(CardChannel channel) {
        this.channel = channel;
    }

    @Override
    public void open() throws CardServiceException {
        if (channel == null) {
            throw new CardServiceException("Card channel is not available.");
        }
        state = SESSION_STARTED_STATE;
    }

    @Override
    public void close() {
        try {
            if (channel != null) {
                channel.getCard().disconnect(false);
            }
            state = SESSION_STOPPED_STATE;
        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOpen() {
        return state == SESSION_STARTED_STATE;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU command) throws CardServiceException {
        try {
            javax.smartcardio.CommandAPDU javaxCommand = new javax.smartcardio.CommandAPDU(command.getBytes());
            javax.smartcardio.ResponseAPDU javaxResponse = channel.transmit(javaxCommand);
            return new ResponseAPDU(javaxResponse.getBytes());
        } catch (CardException e) {
            throw new CardServiceException("Error transmitting APDU", e);
        }
    }

    @Override
    public byte[] getATR() throws CardServiceException {
        try {
            return channel.getCard().getATR().getBytes();
        } catch (Exception e) { // Bắt mọi lỗi chung chung
            throw new CardServiceException("Error retrieving ATR", e);
        }
    }

}