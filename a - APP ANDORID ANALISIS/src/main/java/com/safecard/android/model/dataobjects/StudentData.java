package com.safecard.android.model.dataobjects;

import org.json.JSONException;
import org.json.JSONObject;

public class StudentData implements AccessData{

    private int studentId;
    private String fullName;
    private String condoName;
    private int stdSchoolId;
    private int schoolId;
    private String studentMobile;
    private int status;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCondoName() {
        return condoName;
    }

    public void setCondoName(String condoName) {
        this.condoName = condoName;
    }

    public int getStdschoolId() {
        return stdSchoolId;
    }

    public void setStdschoolId(int stdSchoolId) {
        this.stdSchoolId = stdSchoolId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public String getStudentMobile() {
        return studentMobile;
    }

    public void setStudentMobile(String studentMobile) {
        this.studentMobile = studentMobile;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
