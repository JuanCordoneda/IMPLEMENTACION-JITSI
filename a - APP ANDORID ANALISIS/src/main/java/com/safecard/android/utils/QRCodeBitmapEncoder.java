package com.safecard.android.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.safecard.android.Config;

import java.io.UnsupportedEncodingException;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;

public final class QRCodeBitmapEncoder {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    public static Bitmap encodeBytesAsBitmap(byte[] data) {
        try {
            String stringData = new String(data, "ISO-8859-1");
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            //hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
            return encodeAsBitmap(stringData, hints);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap encodeAsBitmap(String data, Map<EncodeHintType, Object> hints) {
        try {
            if (data == null || data.length() <= 0) {
                data = "A";
            }
            hints.put(EncodeHintType.MARGIN, 2);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result = null;

            result = writer.encode(data, BarcodeFormat.QR_CODE, 1, 1, hints);

            int width = result.getWidth();
            int height = result.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BitmapDrawable getDrawable(final byte[] data) {
        long now = System.currentTimeMillis();
        BitmapDrawable draw = draws.get(data);
        if(draw != null){
            Log.d("getDrawable", "cache");
            return draw;
        }
        draw = new BitmapDrawable(encodeBytesAsBitmap(data));
        draw.setDither(false);
        draw.setFilterBitmap(false);
        draw.setAntiAlias(false);
        draws.put(data,draw);
        Log.d("getDrawable", "nocache");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                draws.remove(data);
            }
        }, (int)(Config.time_refresh_qr * 1.5));

        return draw;
    }

    private static Hashtable<byte[], BitmapDrawable> draws = new Hashtable<>();

}