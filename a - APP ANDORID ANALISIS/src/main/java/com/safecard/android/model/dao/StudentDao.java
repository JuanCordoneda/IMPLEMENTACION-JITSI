package com.safecard.android.model.dao;

import android.content.Context;
import android.util.Log;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.model.AccessModel;
import com.safecard.android.model.dataobjects.GateData;
import com.safecard.android.model.dataobjects.PropertyData;
import com.safecard.android.model.dataobjects.SectorData;
import com.safecard.android.model.dataobjects.StudentData;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentDao {

    private List<StudentData> students;
    private String TAG = "PropertyDao";

    public StudentDao(Context context){
        students = new ArrayList<>();
        loadFromPersisted(context);
    }

    public List<StudentData> getStudents(){
        return students;
    }

    public int studentsSize() {
        return students.size();
    }

    public boolean existStudent(int studentId) {
        return getStudent(studentId) != null;
    }

    public StudentData getStudent(int studentId) {
        StudentData data = null;
        for(int i = 0; i < students.size(); i++){
            StudentData aux = students.get(i);
            if(aux.getStudentId() == studentId){
                data = aux;
                break;
            }
        }
        return data;
    }

    public void loadFromPersisted(Context context){
        loadStudentsFromPersisted(context);
    }

    private void loadStudentsFromPersisted(Context context){
        try {
            List<StudentData>  students = new ArrayList<>();
            JSONObject initData = Utils.getDefaultJSONObject("init_data", context);
            Log.d(TAG, "loadPropertiesFromPersisted initData:" + initData.toString());
            if(initData.has("students")) {
                JSONArray propertiesJSONArray = new JSONArray(initData.getString("students"));
                for (int i = 0; i < propertiesJSONArray.length(); i++) {
                    JSONObject json = propertiesJSONArray.getJSONObject(i);
                    students.add(getStudentDataFromJson(json));
                }
            }
            if(students.size() > 0){
                this.students = students;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private StudentData getStudentDataFromJson(JSONObject json) {
        StudentData studentData = new StudentData();
        try {
            studentData.setStudentId(json.getInt("student_id"));
            studentData.setFullName(json.getString("full_name"));
            studentData.setCondoName(json.getString("condo_name"));
            studentData.setStdschoolId(json.getInt("stdschool_id"));
            studentData.setSchoolId(json.getInt("school_id"));
            studentData.setStudentMobile(json.getString("student_mobile"));
            studentData.setStatus(json.getInt("status"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return studentData;
    }

    @Override
    public String toString() {
        return "PropertyDao{" +
                "properties=" + students.toString() +
                '}';
    }
}