package pt.isec.pd.spring_boot.exemplo3.client;

import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.UserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClientInfo implements Serializable, UserInfo {
    private String name;
    private int studentNumber;
    private String email;
    private String newEmail;
    private String password;

    private String request;
    private String serverResponse;
    private List<Event> eventsList;
    private int code;
    private boolean isInEvent; //Está ou não presente num evento
    private boolean adm;//Verifica, no login, se o cliente é o adm
    private boolean success = false;

    public ClientInfo(String name, int studentNumber,String email, String password, String request){
        this.name = name;
        this.studentNumber = studentNumber;
        this.email = email;
        this.password = password;
        this.request = request;
        this.newEmail = null;
        this.isInEvent = false;
        this.eventsList = new ArrayList<>();
    }

    public ClientInfo(String email, String password, String request){
        this.email = email;
        this.password = password;
        this.request = request;
        this.newEmail = null;
        this.eventsList = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email){this.email = email;}

    public String getNewEmail() {
        return newEmail;
    }
    public void setNewEmail(String newEmail){ this.newEmail = newEmail; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){this.password = password;}

    public String getRequest() {
        return request;
    }
    public void setRequest(String request){this.request = request;}

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getName() {
        return name;
    }
    public void setName(String name){this.name = name;}

    public int getStudentNumber() {
        return studentNumber;
    }
    public void setStudentNumber(int studentNumber){this.studentNumber = studentNumber;}

    public String getServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }

    public void addEvent(Event event){
        eventsList.add(event);
    }

    public List<Event> getEvents() {
        return eventsList;
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setIsInEvent() {
        isInEvent = true;
    }

    public void setIsNotInEvent() {
        isInEvent = false;
    }

    public boolean isAdm() {
        return adm;
    }

    public void setAdm() {
        this.adm = true;
    }
    public void setEvents(List<Event> events) {
        this.eventsList = events;
    }

}