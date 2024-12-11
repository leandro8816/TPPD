package pt.isec.pd.spring_boot.exemplo3.models;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public class Event implements Serializable {
    private String name;//desigancao;
    private String place;//local;
    private Date date;
    private Time startingHour,finishingHour;
    private int attendance;

    public Event(String name, String place, Date date, Time startingHour,Time finishingHour) {
        this.name = name;
        this.place = place;
        this.date = date;
        this.startingHour = startingHour;
        this.finishingHour = finishingHour;
        this.attendance = 0;
    }
    public Event(String place, Date date, Time startingHour,Time finishingHour) {
        this.place = place;
        this.date = date;
        this.startingHour = startingHour;
        this.finishingHour = finishingHour;
    }
    public Event(String name){
        this.name = name;
    }

    public Event(){}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }
    public void setPlace(String place) {
        this.place = place;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStartingHour() {
        return startingHour;
    }

    public void setStartingHour(Time startingHour) {
        this.startingHour = startingHour;
    }

    public Time getFinishingHour() {
        return finishingHour;
    }

    public void setFinishingHour(Time finishingHour) {
        this.finishingHour = finishingHour;
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", place='" + place + '\'' +
                ", date='" + date + '\'' +
                ", startingHour='" + startingHour + '\'' +
                ", finishingHour='" + finishingHour + '\'' +
                '}';
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }
}