package pt.isec.pd.spring_boot.exemplo3.client;



import pt.isec.pd.spring_boot.exemplo3.utils.CodigoRegisto;
import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.UserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class AdministratorInfo implements Serializable, UserInfo {
    private String email, password;
    private String request;
    private String serverResponse;
    private Event event;
    private CodigoRegisto codigoRegisto;
    private List<Event> events;
    private String eventName; //para verificar se o evento existe
    private String emUser; //para verificar se o evento existe
    private String nomeUser;
    private int idUser;

    private boolean success = false;

    public AdministratorInfo(String email, String password, String request){
        this.email = email;
        this.password = password;
        this.request = request;
        events = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRequest() {
        return this.request;
    }
    public void setRequest(String request) {
        this.request = request;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public CodigoRegisto getCodigoRegisto() {
        return codigoRegisto;
    }

    public void setCodigoRegisto(CodigoRegisto codigoRegisto) {
        this.codigoRegisto = codigoRegisto;
    }

    public String getServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEmUser() {
        return emUser;
    }

    public void setEmUser(String emUser) {
        this.emUser = emUser;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getNomeUser() {
        return nomeUser;
    }

    public void setNomelUser(String nomeUser) {
        this.nomeUser = nomeUser;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
}
