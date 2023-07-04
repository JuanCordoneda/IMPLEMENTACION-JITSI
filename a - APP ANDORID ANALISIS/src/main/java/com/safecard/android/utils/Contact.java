package com.safecard.android.utils;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Contact implements Serializable, Comparable<Contact> {

    //private variables
    int _id;
    String _name;
    String _mobile;
    String _email;
    int _selected;
    String photoUri;

    // constructor
    public Contact(int id, String name, String mobile, String email, String photoUri) {
        this._id = id;
        this._name = name;
        this._mobile = mobile;
        this._email = email;
        this._selected = 0;
        this.photoUri = photoUri;
    }

    public Contact(int id, String name, String mobile, String email, int selected) {
        this._id = id;
        this._name = name;
        this._mobile = mobile;
        this._email = email;
        this._selected = selected;
        this.photoUri = "";
    }

    // getting name
    public String getName() {
        return this._name;
    }

    // setting name
    public void setName(String name) {
        this._name = name;
    }

    // getting phone number
    public String getMobile() {
        return this._mobile;
    }

    // setting phone number
    public void setMobile(String mobile) {
        this._mobile = mobile;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    @Override
    public int compareTo(@NonNull Contact c) {
        return _mobile.compareTo(c.getMobile());
    }

    public void setSelected(int selected) {
        this._selected = selected;
    }

    public int getSelected() {
        return this._selected;
    }
}
