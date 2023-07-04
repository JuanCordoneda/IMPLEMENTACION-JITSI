package com.safecard.android.utils;

import java.util.ArrayList;
import java.util.List;

public class ByteCache {

    List<Byte> backing = new ArrayList<Byte>();

    public ByteCache(){
    }

    public ByteCache(Byte[] bytes){
        add(bytes);
    }

    public void add(Byte[] bytes){
        for(Byte b : bytes){
            backing.add(b);
        }
    }

    public void add(byte[] bytes){
        for(byte b : bytes){
            backing.add(b);
        }
    }

    public void add(byte b){
        backing.add(b);
    }

    public void add(String data) {
        add(data.getBytes());
    }

    public int length(){
        return backing.size();
    }

    public byte[] getAll(){
        return get(0,backing.size());
    }

    public byte[] get(int offset, int length){
        if(offset < 0 || length < 1){
            return null;
        }

        Byte[] toRet = new Byte[length];

        for(int i = offset; i < offset + length; i++){
            if(i == backing.size()){
                break;
            }
            toRet[i - offset] = backing.get(i);
        }
        return toPrimitive(toRet);
    }

    private byte[] toPrimitive(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }

}