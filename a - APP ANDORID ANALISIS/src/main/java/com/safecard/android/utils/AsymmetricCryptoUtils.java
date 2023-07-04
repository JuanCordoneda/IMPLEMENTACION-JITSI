package com.safecard.android.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.safecard.android.Config;

import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;


import static com.safecard.android.utils.Utils.bytesToHex;

public class AsymmetricCryptoUtils extends ContextWrapper {
    private final String TAG = "AsymmetricCryptoUtils";
    private static byte[] publicPhoneSign;
    private static byte[] privatePhoneSign;
    private static byte[] publicPhoneEncrypt;
    private static byte[] privatePhoneEncrypt;

    private byte[] tentativePublicKeySign;
    private byte[] tentativePrivateKeySign;
    private byte[] tentativePublicKeyEncrypt;
    private byte[] tentativePrivateKeyEncrypt;


    private static byte[] publicApiEnc;
    private static byte[] publicApiSign;

    public AsymmetricCryptoUtils(Context base) {
        super(base);
        Sodium sodium = NaCl.sodium();
        loadKeys(false);
    }

    public void loadKeys(boolean forceLoad) {
        if(forceLoad || publicPhoneSign == null || privatePhoneSign == null || publicPhoneEncrypt == null || privatePhoneEncrypt == null) {
            String keys = Utils.getDefaultString("sodiumPhoneKeys", getApplicationContext());
            if (!keys.equals("")) {
                String[] keyArray = keys.split(",");
                publicPhoneEncrypt = Utils.hexToBytes(keyArray[0]);
                privatePhoneEncrypt = Utils.hexToBytes(keyArray[1]);
                publicPhoneSign = Utils.hexToBytes(keyArray[2]);
                privatePhoneSign = Utils.hexToBytes(keyArray[3]);
            }
        }
        if(forceLoad || publicApiEnc == null) {
            String key = Utils.getDefaultString("publicApiEnc", getApplicationContext());
            if (!key.equals("")) {
                publicApiEnc = Utils.hexToBytes(key);
            }
        }
        if(forceLoad || publicApiSign == null) {
            String key = Utils.getDefaultString("publicApiSign", getApplicationContext());
            if (!key.equals("")) {
                publicApiSign = Utils.hexToBytes(key);
            }
        }
    }

    public String getTentativePublicKeySign() {
        return Utils.bytesToHex(tentativePublicKeySign);
    }
    public String getTentativePublicKeyEncrypt() {
        return Utils.bytesToHex(tentativePublicKeyEncrypt);
    }

    public void create() {
        int pkLen = Sodium.crypto_box_publickeybytes();
        int skLen = Sodium.crypto_box_secretkeybytes();
        tentativePublicKeyEncrypt = new byte[pkLen];
        tentativePrivateKeyEncrypt = new byte[skLen];
        Sodium.crypto_box_keypair(tentativePublicKeyEncrypt, tentativePrivateKeyEncrypt);

        pkLen = Sodium.crypto_sign_publickeybytes();
        skLen = Sodium.crypto_sign_secretkeybytes();
        tentativePublicKeySign = new byte[pkLen];
        tentativePrivateKeySign = new byte[skLen];
        Sodium.crypto_sign_keypair(tentativePublicKeySign, tentativePrivateKeySign);
    }

    public void persist() {
        Utils.setDefaultString("sodiumPhoneKeys",
                Utils.bytesToHex(tentativePublicKeyEncrypt) + "," +
                        Utils.bytesToHex(tentativePrivateKeyEncrypt)+ "," +
                        Utils.bytesToHex(tentativePublicKeySign)+ "," +
                        Utils.bytesToHex(tentativePrivateKeySign), getApplicationContext());
        loadKeys(true);
    }

    public boolean exist() {
        return publicPhoneSign != null &&
                privatePhoneSign != null &&
                publicPhoneEncrypt != null &&
                privatePhoneEncrypt != null;
    }

    public boolean existApiKeys() {
        return publicApiSign != null && publicApiEnc != null;
    }

    public byte[] encryptWithPK(byte[] plainData, byte[] recipientPublicKey) {
        byte[] cipherData = new byte[Sodium.crypto_box_sealbytes() + plainData.length];
        Sodium.crypto_box_seal(cipherData, plainData, plainData.length, recipientPublicKey);
        //Log.d(TAG, "encryptWithPK cipherData:" + bytesToHex(cipherData));
        return cipherData;
    }

    public byte[] getNonce() {
        int nonceLen = Sodium.crypto_box_noncebytes();
        byte[] nonce = new byte[nonceLen];
        Sodium.randombytes_buf(nonce, nonceLen);
        return nonce;
    }

    public byte[] encryptWithPKSK(byte[] plainData, byte[] recipientPublicKey, byte[] nonce) {
        byte[] cipherData = new byte[Sodium.crypto_box_macbytes() + plainData.length];
        Sodium.crypto_box_easy(cipherData, plainData, plainData.length, nonce, recipientPublicKey, privatePhoneEncrypt);
        //Log.d(TAG, "encryptWithPK cipherData:" + bytesToHex(cipherData));
        return cipherData;
    }

    public byte[] sign(byte[] plainData) {
        //Log.d(TAG, "sign privatePhoneSign:" + bytesToHex(privatePhoneSign));
        //Log.d(TAG, "sign publicPhoneSign:" + bytesToHex(publicPhoneSign));
        int sLen = Sodium.crypto_sign_bytes();
        byte[] signature = new byte[sLen + plainData.length];
        final int[] signatureLen = new int[1];
        Sodium.crypto_sign_detached(signature, signatureLen, plainData, plainData.length, privatePhoneSign);
        return signature;
    }

    public void persistApi(String pApiEnc, String pApiSign) {
        Utils.setDefaultString("publicApiEnc", pApiEnc, getApplicationContext());
        Utils.setDefaultString("publicApiSign", pApiSign, getApplicationContext());

        publicApiEnc = Utils.hexToBytes(pApiEnc);
        publicApiSign = Utils.hexToBytes(pApiSign);

    }

    public boolean isApiSignOk(byte[] message, byte[] signature) {
        return Sodium.crypto_sign_verify_detached(
                signature, message, message.length, publicApiSign) == 0;
    }
}