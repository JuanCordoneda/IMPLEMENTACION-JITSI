package com.safecard.android.utils;

import android.content.Context;
import android.util.Log;

import com.safecard.android.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Invitation implements Serializable {

    private String name;

    private Calendar startDateTimeCalendar;
    private Calendar endDateTimeCalendar;

    private int propertyId;

    private String plateNumber;

    private boolean isCustomTime;
    private boolean isCustomDaysOfWeek;
    private boolean isPlateNumberUsed;

    private boolean[] daysOfWeekSelected;

    private List<Integer> IdsSelectedSectors;

    public Invitation() {
        seDefaultValues();
    }

    public void seDefaultValues() {
        name = "";

        startDateTimeCalendar = Calendar.getInstance();
        startDateTimeCalendar.add(Calendar.MINUTE, -5);
        startDateTimeCalendar.set(Calendar.SECOND, 0);
        startDateTimeCalendar.set(Calendar.MILLISECOND, 0);

        endDateTimeCalendar = Calendar.getInstance();
        endDateTimeCalendar.add(Calendar.HOUR, 1);
        endDateTimeCalendar.set(Calendar.SECOND, 0);
        endDateTimeCalendar.set(Calendar.MILLISECOND, 0);

        propertyId = -1;

        plateNumber = "";

        isCustomTime = false; // false es todo el dia
        isCustomDaysOfWeek = false; // false es repetir para todo dia del la semana
        isPlateNumberUsed = false;

        daysOfWeekSelected = new boolean[7];
        for(int i = 0; i < 7; i++){
            daysOfWeekSelected[i] = true;
        }

        IdsSelectedSectors = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //public List<Contact> getGuests() {
    //    return guests;
    //}

    //public void setGuests(List<Contact> guests) {
    //    this.guests = guests;
    //}

    public Calendar getStartDateTimeCalendar() {
        return startDateTimeCalendar;
    }

    public void setStartDateCalendar(int year, int monthOfYear, int dayOfMonth) {
        startDateTimeCalendar.set(Calendar.YEAR, year);
        startDateTimeCalendar.set(Calendar.MONTH, monthOfYear);
        startDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    public void setStartTimeCalendar(int hourOfDay, int minute) {
        startDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        startDateTimeCalendar.set(Calendar.MINUTE, minute);
    }

    public Calendar getEndDateTimeCalendar() {
        return endDateTimeCalendar;
    }

    public void setEndDateCalendar(int year, int monthOfYear, int dayOfMonth) {
        endDateTimeCalendar.set(Calendar.YEAR, year);
        endDateTimeCalendar.set(Calendar.MONTH, monthOfYear);
        endDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    public void setEndTimeCalendar(int hourOfDay, int minute) {
        endDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        endDateTimeCalendar.set(Calendar.MINUTE, minute);
    }

    public int getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public boolean isPlateNumberUsed() {
        return isPlateNumberUsed;
    }

    public void setPlateNumberUsed(boolean plateNumberUsed) {
        isPlateNumberUsed = plateNumberUsed;
    }

    public boolean isCustomTime() {
        return isCustomTime;
    }

    public void setCustomTime(boolean customTime) {
        this.isCustomTime = customTime;
    }

    public boolean isCustomDaysOfWeek() {
        return isCustomDaysOfWeek;
    }

    public void setCustomDaysOfWeek(boolean customDaysOfWeek) {
        this.isCustomDaysOfWeek = customDaysOfWeek;
    }

    public boolean[] getDaysOfWeekSelected() {
        return daysOfWeekSelected;
    }

    public void setDaysOfWeekSelected(boolean[] daysOfWeekSelected) {
        this.daysOfWeekSelected = daysOfWeekSelected;
    }

    public void toggleDayOfWeek(int index) {
        this.daysOfWeekSelected[index] = !daysOfWeekSelected[index];
    }

    public List<Integer> getIdsSelectedSectors() {
        return IdsSelectedSectors;
    }

    public void setIdsSelectedSectors(List<Integer> idsSelectedSectors) {
        IdsSelectedSectors = idsSelectedSectors;
    }


    public String getIdsSelectedSectorsAsString() {
        StringBuilder s = new StringBuilder();
        String separator = "";
        for (int i = 0; i < IdsSelectedSectors.size(); i++) {
            s.append(separator);
            s.append(IdsSelectedSectors.get(i));
            separator = ",";
        }
        return s.toString();
    }

    public boolean[] getDaysOfWeekInPeriod() {
        Calendar cStart = (Calendar) startDateTimeCalendar.clone();
        Calendar cEnd = (Calendar) endDateTimeCalendar.clone();
        // me aseguro que: time de cEnd > time de cStart
        cStart.set(Calendar.HOUR_OF_DAY, 0);
        cEnd.set(Calendar.HOUR_OF_DAY, 23);

        //Log.i("dias", "cStart" + cStart.getTime());
        //Log.i("dias", "cEnd" + cEnd.getTime());

        boolean[] daysOfWeekInPeriod = new boolean[7];
        for(int i = 0; i < daysOfWeekInPeriod.length; i++){
            daysOfWeekInPeriod[i] = false;
        }
        for (int i = 0; (cStart.before(cEnd) || cStart.equals(cEnd)) && i < 7; cStart.add(Calendar.DATE, 1), i++) {
            int dayOfWeek = cStart.get(Calendar.DAY_OF_WEEK) - 1;
            daysOfWeekInPeriod[dayOfWeek] = true;
        }
        return daysOfWeekInPeriod;
    }

    public List<Integer> getDaysOfWeekValids(){
        boolean[] daysOfWeekInPeriod = getDaysOfWeekInPeriod();
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < daysOfWeekInPeriod.length; i++) {
            if (daysOfWeekInPeriod[i] && daysOfWeekSelected[i]) {
                result.add(i);
            }
        }
        return result;
    }

    public String getDaysOfWeekValidsAsShortNamesString(Context mContext){
        String[] dayShortNames = new String[]{
                mContext.getString(R.string.sunday_first3_letters),
                mContext.getString(R.string.monday_first3_letters),
                mContext.getString(R.string.tuesday_first3_letters),
                mContext.getString(R.string.wednesday_first3_letters),
                mContext.getString(R.string.thursday_first3_letters),
                mContext.getString(R.string.friday_first3_letters),
                mContext.getString(R.string.saturday_first3_letters)};

        List<Integer> dayIndexes = getDaysOfWeekValids();
        StringBuilder result = new StringBuilder();

        String separator = "";
        for(int i: dayIndexes){
            result.append(separator);
            result.append(dayShortNames[i]);
            separator = ", ";
        }

        return result.toString();
    }

    public String getDaysOfWeekValidsAsString(){
        List<Integer> dayIndexes = getDaysOfWeekValids();
        StringBuilder result = new StringBuilder();

        String separator = "";
        for(int i: dayIndexes){
            result.append(separator);
            result.append(i);
            separator = ",";
        }

        return result.toString();
    }

    //cuenta los dias de calendario sin considerar las horas, ej de lunes a martes hay 2 dias
    public int daysInPeriodCount(){
        Calendar cStart = (Calendar) startDateTimeCalendar.clone();
        Calendar cEnd = (Calendar) endDateTimeCalendar.clone();
        // me aseguro que: time de cEnd > time de cStart
        cStart.set(Calendar.HOUR_OF_DAY, 0);
        cEnd.set(Calendar.HOUR_OF_DAY, 23);

        if(cStart.before(cEnd)){
            long duration = cEnd.getTimeInMillis() - cStart.getTimeInMillis();
            Long days = TimeUnit.DAYS.convert(duration, TimeUnit.MILLISECONDS);
            return days.intValue() + 1;
        }
        return -1;
    }
}