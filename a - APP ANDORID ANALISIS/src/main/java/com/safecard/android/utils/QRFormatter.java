package com.safecard.android.utils;

import android.content.Context;
import com.safecard.android.Config;
import com.safecard.android.model.Models;
import com.safecard.android.model.PaymentMethodModel;
import com.safecard.android.model.dataobjects.PropertyData;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;

public final class QRFormatter {
    private static String TAG = "QRFormatter";
    static Hashtable<Integer, QRCache> OwnerCaches = new Hashtable<>();
    static Hashtable<Integer, QRCache> InvitationCaches = new Hashtable<>();
    static QRCache parkingCache;
    static int diff;

    public static byte[] getOwnerPropertyQRData(int propertyId, Context context) {

        AsymmetricCryptoUtils ac = new AsymmetricCryptoUtils(context);
        if(!ac.exist()
                || Utils.getUserId(context).equals("")
                || !Utils.existUserData(context)) {
            return "OWNER_QR_ERROR".getBytes();
        }

        QRCache cache = OwnerCaches.get(propertyId);
        if(cache == null) {
            PropertyData property = Models.getAccessModel(context).getPropertyDAO().getProperty(propertyId);
            ByteBuffer toCryptData = ByteBuffer.allocate(12);
            toCryptData.putInt(0);
            toCryptData.putInt(property.getCondoId());
            toCryptData.putInt(property.getHouseId());

            ByteBuffer header = ByteBuffer.allocate(9);
            header.put("QrX3".getBytes());
            header.put((byte) 0x00);
            header.putInt(Integer.parseInt(Utils.getUserId(context)));

            QRCache newCache =  new QRCache();
            newCache.setToCryptData(toCryptData.array());
            newCache.setHeader(header.array());
            newCache.setRecipientPublicKey(property.getCondoPublicKey());
            OwnerCaches.put(propertyId, newCache);
            cache = newCache;
        }

        diff = Utils.getDefaultInt("diff", context);
        long timestamp = System.currentTimeMillis() / 1000 + diff;
        if(timestamp - cache.getFullQRTimestamp() <  Config.time_refresh_qr / 1000 - 2){
            return cache.getFullQR();
        }

        ByteBuffer toCryptDataBuffer = ByteBuffer.allocate(cache.getToCryptData().length);
        toCryptDataBuffer.put(cache.getToCryptData());
        toCryptDataBuffer.putInt(0, (int) timestamp);
        byte[] toCryptData = toCryptDataBuffer.array();
        byte[] nonce = ac.getNonce();
        byte[] encrypted = ac.encryptWithPKSK(toCryptData,cache.getRecipientPublicKey(), nonce);

        ByteBuffer data = ByteBuffer.allocate(cache.getHeader().length +
                nonce.length +
                encrypted.length +
                2);

        //encrypted[7]^=(byte)0x04;
        data.put(cache.getHeader());
        data.put((byte) nonce.length);
        data.put(nonce);
        data.put((byte) encrypted.length);
        data.put(encrypted);

        //Log.d("AnalysisData", "recipientPublicKey:" + Utils.bytesToHex(cache.getRecipientPublicKey()));
        //Log.d("AnalysisData", "dataToEncrypt:" + Utils.bytesToHex(toCryptData));
        //Log.d("AnalysisData", "dataToEncrypt:" + Utils.bytesToHex(toCryptData));
        //Log.d("AnalysisData", "nonce.length:" +  nonce.length);
        //Log.d("AnalysisData", "encrypted.length:" + encrypted.length);
        //Log.d("AnalysisData", "encrypted:" + Utils.bytesToHex(encrypted));

        cache.setFullQRTimestamp(timestamp);
        cache.setFullQR(escapeData(data.array()));
        //Log.d("AnalysisData", "FullQRLen:" + cache.getFullQR().length);
        return cache.getFullQR();
    }

    public static byte[] getInvitationPropertyQRData(int invitationId, Context context) {

        AsymmetricCryptoUtils ac = new AsymmetricCryptoUtils(context);

        QRCache cache = InvitationCaches.get(invitationId);
        if(cache == null) {
            PropertyData property = Models.getAccessModel(context).getInvitationProperty(invitationId, context);
            if(!ac.exist()
                    || property.getHash() == null) {
                return "INVITATION_QR_ERROR".getBytes();
            }

            ByteBuffer header = ByteBuffer.allocate(10 + property.getNonce().length);
            header.put("QrX3".getBytes());
            header.put((byte) 0x01);
            header.putInt(property.getOrganizerId());
            header.put((byte) property.getNonce().length);
            header.put(property.getNonce());

            ByteBuffer toCryptData = ByteBuffer.allocate(5 + property.getHash().length);
            toCryptData.putInt(0);
            toCryptData.put((byte) property.getHash().length);
            toCryptData.put(property.getHash());

            QRCache newCache =  new QRCache();
            newCache.setHeader(header.array());
            newCache.setRecipientPublicKey(property.getCondoPublicKey());
            newCache.setToCryptData(toCryptData.array());
            OwnerCaches.put(invitationId, newCache);
            cache = newCache;
        }

        diff = Utils.getDefaultInt("diff", context);
        long timestamp = System.currentTimeMillis() / 1000 + diff;

        if(timestamp - cache.getFullQRTimestamp() < 4.9){
            return cache.getFullQR();
        }

        ByteBuffer toCryptDataBuffer = ByteBuffer.allocate(cache.getToCryptData().length);
        toCryptDataBuffer.put(cache.getToCryptData());
        toCryptDataBuffer.putInt(0, (int) timestamp);
        byte[] toCryptData = toCryptDataBuffer.array();
        byte[] encrypted = ac.encryptWithPK(toCryptData, cache.getRecipientPublicKey());

        //Log.d("AnalysisData", "dataToEncrypt:" + Utils.bytesToHex(toCryptData));
        //Log.d("AnalysisData", "encrypted.length:" + encrypted.length);
        //Log.d("AnalysisData", "encrypted:" + Utils.bytesToHex(encrypted));

        ByteBuffer data = ByteBuffer.allocate(cache.getHeader().length +
                encrypted.length +
                1);

        data.put(cache.getHeader());
        data.put((byte) encrypted.length);
        data.put(encrypted);
        cache.setFullQRTimestamp(timestamp);
        cache.setFullQR(escapeData(data.array()));
        //Log.d("AnalysisData", "FullQRLen:" + cache.getFullQR().length);
        return cache.getFullQR();
    }

    private static byte[] escapables = new byte[]{
            (byte) 0x00, // '\0': no soportado por device
            (byte) 0x0A, // '\n': ?? revisar si es compatible localserver
            (byte) 0x0D, // '\r': no soportado por device
            (byte) 0x2C, // ',': ?? revisar si es compatible en protocolo
            (byte) 0x41, // 'A': reservados por dispositivos para mensajes AT
            (byte) 0x77  // 'w': caracter de escape
    };

    private static byte[] escapeData(byte[] data){
        ByteCache result = new ByteCache();
        for(int i=0;i<data.length;i++) {
            byte b = data[i];
            for(int j=0;j<escapables.length;j++) {
                byte escapable = escapables[j];
                if(b == escapable){
                    result.add((byte) 0x77);
                    b+=1;
                    break;
                }
            }
            result.add(b);
        }
        return result.getAll();
    }

    public static byte[] getParkingQRData(Context context) {

        AsymmetricCryptoUtils ac = new AsymmetricCryptoUtils(context);

        if(!ac.exist()
                || !Utils.isParkingPublicKeyEnc(context)
                || !PaymentMethodModel.arePaymentMethodEnrolled(context)) {
            return "PARKING_QR_ERROR".getBytes();
        }

        if(parkingCache == null) {

            ByteBuffer header = ByteBuffer.allocate(9);
            header.put("QrX3".getBytes());
            header.put((byte) 0x02);
            header.putInt(Integer.parseInt(Utils.getUserId(context)));

            QRCache newCache =  new QRCache();
            newCache.setHeader(header.array());
            parkingCache = newCache;
        }

        int paymentMethodId = PaymentMethodModel.getDefaultPaymentMethodId(context);

        diff = Utils.getDefaultInt("diff", context);
        long timestamp = System.currentTimeMillis() / 1000 + diff;
        if(parkingCache.getPaymentMethodId() == paymentMethodId &&
                timestamp - parkingCache.getFullQRTimestamp() <  Config.time_refresh_qr / 1000 - 2){
            return parkingCache.getFullQR();
        }

        ByteBuffer toCryptDataBuffer = ByteBuffer.allocate(8);
        toCryptDataBuffer.putInt((int) timestamp);
        toCryptDataBuffer.putInt(paymentMethodId);
        byte[] toCryptData = toCryptDataBuffer.array();
        byte[] nonce = ac.getNonce();
        byte[] encrypted = ac.encryptWithPKSK(toCryptData, Utils.getParkingPublicKeyEnc(context), nonce);

        ByteBuffer data = ByteBuffer.allocate(parkingCache.getHeader().length +
                nonce.length +
                encrypted.length +
                2);

        //encrypted[7]^=(byte)0x04;
        data.put(parkingCache.getHeader());
        data.put((byte) nonce.length);
        data.put(nonce);
        data.put((byte) encrypted.length);
        data.put(encrypted);

        //Log.d("AnalysisData", "recipientPublicKey:" + Utils.bytesToHex(parkingCache.getRecipientPublicKey()));
        //Log.d("AnalysisData", "dataToEncrypt:" + Utils.bytesToHex(toCryptData));
        //Log.d("AnalysisData", "dataToEncrypt:" + Utils.bytesToHex(toCryptData));
        //Log.d("AnalysisData", "nonce.length:" +  nonce.length);
        //Log.d("AnalysisData", "encrypted.length:" + encrypted.length);
        //Log.d("AnalysisData", "encrypted:" + Utils.bytesToHex(encrypted));

        parkingCache.setFullQRTimestamp(timestamp);
        parkingCache.setFullQR(escapeData(data.array()));
        parkingCache.setPaymentMethodId(paymentMethodId);
        //Log.d("AnalysisData", "FullQRLen:" + parkingCache.getFullQR().length);
        return parkingCache.getFullQR();
    }
}

final class QRCache {
    private byte[] header;
    private byte[] toCryptData;
    private byte[] recipientPublicKey;

    private int paymentMethodId;

    private byte[] fullQR;

    private long fullQRTimestamp = 0;

    byte[] getHeader() {
        return header;
    }

    void setHeader(byte[] header) {
        this.header = header;
    }

    byte[] getToCryptData() {
        return toCryptData;
    }

    void setToCryptData(byte[] toCryptData) {
        this.toCryptData = toCryptData;
    }

    public byte[] getFullQR() {
        return fullQR;
    }

    public void setFullQR(byte[] fullQR) {
        this.fullQR = fullQR;
    }

    public long getFullQRTimestamp() {
        return fullQRTimestamp;
    }

    public void setFullQRTimestamp(long fullQRTimestamp) {
        this.fullQRTimestamp = fullQRTimestamp;
    }

    public byte[] getRecipientPublicKey() {
        return recipientPublicKey;
    }

    public void setRecipientPublicKey(byte[] recipientPublicKey) {
        this.recipientPublicKey = recipientPublicKey;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}

