package pt.isec.pd.spring_boot.exemplo3.server;



import pt.isec.pd.spring_boot.exemplo3.client.AdministratorInfo;
import pt.isec.pd.spring_boot.exemplo3.client.ClientInfo;
import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.UserInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;


public class AttendTCPClientsThread extends Thread implements ClientServerRequests{
    private Socket clientSocket;
    private List<ClientInfo> loggedUsers;
    private AdministratorInfo loggedAdmin;
    ManageDB db;

    public AttendTCPClientsThread(Socket clientSocket, List<ClientInfo> loggedUsers, List<Event> events , ManageDB manageDb){
        this.clientSocket = clientSocket;
        this.loggedUsers = loggedUsers;
        this.db = manageDb;
    }

    @Override
    public void run(){
        boolean success = false;
        try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            Object obj = in.readObject();

            if (obj instanceof ClientInfo clientInfo) {
                System.out.println("[AttendTCPClientsThread] Request: " + clientInfo.getRequest());

                switch (clientInfo.getRequest()) {
                    case REGISTER:
                        if (registerClient(clientInfo)) {
                            printLoggedUsers();
                            success = true;
                        } else {
                            success = false;
                            clientInfo.setServerResponse("Erro: Email já existe.");
                        }
                        clientInfo.setSuccess(success);
                        break;
                    case LOGIN:
                        if (checkCredentials(clientInfo) && !isUserLoggedIn(clientInfo)) {
                            if(clientInfo.getEmail().equals("admin@isec.pt"))
                                clientInfo.setAdm();
                            addLoggedUserToList(clientInfo);
                            db.fillClient(clientInfo);
                            printLoggedUsers();
                            success = true;
                            db.incrementarVersao("pd");
                        } else {
                            success = false;
                            clientInfo.setServerResponse("Erro: Credenciais inválidas, ou o utilizador já se encontra registado. Por favor, verifique seu nome de usuário e senha e tente novamente.");
                        }
                        clientInfo.setSuccess(success);
                        break;
                    case EDIT:
                        if(db.editUtilizador(clientInfo.getEmail(), clientInfo.getName(), clientInfo.getStudentNumber(), clientInfo.getPassword())){
                            success = true;
                        }
                        else{
                            success = false;
                            clientInfo.setServerResponse("Erro: Falha na edição das informações");
                        }
                        db.getAllUsers();
                        clientInfo.setSuccess(success);
                        break;
                    case CHECK_CODE:
                        System.out.println("O cliente " + clientInfo.getName() + " introduziu o numero [" + clientInfo.getCode() + "]");

                        if (db.codeExistsInTime(clientInfo.getCode())) {
                            db.insertAssiste(clientInfo.getEmail(),db.getEventNameByCode(clientInfo.getCode()));
                            clientInfo.setIsInEvent();
                            success = true;
                        } else {
                            clientInfo.setServerResponse("Erro: Não foi possivel assinalar a presença.");
                            success = false;
                        }
                        clientInfo.setSuccess(success);
                        break;
                    case CHECK_ATTENDANCE_CLI:
                        clientInfo.setServerResponse(db.get3cli(clientInfo.getEmail()));
                        break;
                    case LOGOUT:
                        if (isUserLoggedIn(clientInfo)) {
                            removeLoggedUserFromList(clientInfo);
                            printLoggedUsers();
                            success = true;
                        } else {
                            success = false;
                            clientInfo.setServerResponse("Erro: Não foi possivel realizar a operação Logout.");
                        }
                        clientInfo.setSuccess(success);
                        break;
                }
                out.writeObject(clientInfo);
            } else if (obj instanceof AdministratorInfo adminInfo) {
                switch (adminInfo.getRequest()) {
                    case LOGIN:
                        if (checkCredentials(adminInfo) && !isUserLoggedIn(adminInfo)) {
                            System.out.println("[AttendTCPClientsThread] Admin iniciou sessão");
                            addAdmin(adminInfo);
                            printLoggedUsers();
                            success = true;
                        } else {
                            success = false;
                            adminInfo.setServerResponse("Erro: Credenciais inválidas, ou o utilizador já se encontra registado. Por favor, verifique seu nome de usuário e senha e tente novamente.");
                        }
                        adminInfo.setSuccess(success);
                        break;
                    case CREATE_EVENT:
                        if(db.insertEvent(adminInfo.getEvent().getName(),adminInfo.getEvent().getPlace(),adminInfo.getEvent().getDate(),adminInfo.getEvent().getStartingHour(),adminInfo.getEvent().getFinishingHour())){
                            success = true;
                        }
                        else{
                            success = false;
                            adminInfo.setServerResponse("Erro: Falha na criação de um evento.");
                        }
                        db.displayAllEvents();
                        adminInfo.setSuccess(success);
                        break;
                    case EDIT_EVENT_DATA:
                        adminInfo.setServerResponse(db.editEvent(adminInfo.getEventName(),adminInfo.getEvent().getPlace(),adminInfo.getEvent().getDate(),adminInfo.getEvent().getStartingHour(),adminInfo.getEvent().getFinishingHour()));
                        db.displayAllEvents();
                        break;
                    case DELETE_EVENT:
                        adminInfo.setServerResponse(db.deleteEvent(adminInfo.getEventName()));
                        db.displayAllEvents();
                        adminInfo.setSuccess(success);
                        break;
                    case CHECK_EVENTS:
                        String events = db.displayAllEvents();
                        if(events != null){
                            adminInfo.setServerResponse(events);
                            adminInfo.setSuccess(true);
                        }
                        else{
                            adminInfo.setSuccess(false);
                        }
                        break;
                    case GENERATE_CODE:
                        adminInfo.setServerResponse(db.insertCodigoRegisto(adminInfo.getCodigoRegisto()));
                        db.displayAllCodigosRegisto();
                        break;
                    case CHECK_ATTENDANCE:
                        adminInfo.setServerResponse(db.obterInfoUtilizadoresPorEvento(adminInfo.getEventName()));
                        break;
                    case OBTAIN_CSV_F1:
                        adminInfo.setEvent(db.getEventByName(adminInfo.getEventName()));
                        adminInfo.setServerResponse(db.obterInfoUtilizadoresPorEvento(adminInfo.getEventName()));
                        break;
                    case CHECK_ATTENDANCE_BY_USER:
                        adminInfo.setEvents(db.getConsultasByUtilizador(adminInfo.getEmUser()));
                        break;
                    case OBTAIN_CSV_F2:
                        adminInfo.setIdUser(db.getStudentNumberByEmail(adminInfo.getEmUser()));
                        adminInfo.setNomelUser(db.getNameByEmail(adminInfo.getEmUser()));
                        adminInfo.setEvents(db.getConsultasByUtilizador(adminInfo.getEmUser()));
                        break;
                    case DELETE_ATTENDANCE:
                        adminInfo.setServerResponse(db.deleteAssiste(adminInfo.getEmUser(),adminInfo.getEventName()));
                        break;
                    case INSERT_ATTENDANCE:
                        adminInfo.setServerResponse(db.insertAssiste(adminInfo.getEmUser(),adminInfo.getEventName()));
                        break;
                    case UPDATE:

                        break;
                    case LOGOUT:
                        if (isAdmLoggedIn()) {
                            removeAdmFromList();
                            printLoggedUsers();
                            success = true;
                        } else {
                            success = false;
                            adminInfo.setServerResponse("Erro: Logout");
                        }
                        adminInfo.setSuccess(success);
                        break;
                }
                out.writeObject(adminInfo);
            }
            out.flush();
        }catch (SocketException e){
            System.out.println("Cliente ligado");
        } catch (IOException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
        }
    }

    private boolean checkCredentials(UserInfo userInfo){
        if(db.checkPass(userInfo.getEmail(),userInfo.getPassword()) == MSG.RIGHT_PASSWORD){
            return true;
        }
        return false;
    }

    private boolean isUserLoggedIn(UserInfo userInfo){
        for (ClientInfo info : loggedUsers) {
            if(info.getEmail().equals(userInfo.getEmail())){
                return true;
            }
        }
        return false;
    }

    private boolean isAdmLoggedIn(){
        for (ClientInfo info : loggedUsers) {
            if(info.getEmail().equals("admin@isec.pt")){
                return true;
            }
        }
        return false;
    }
    private void addLoggedUserToList(ClientInfo clientInfo){
        synchronized (this.loggedUsers){
            this.loggedUsers.add(clientInfo);
        }
    }

    private void addAdmin(AdministratorInfo adminInfo){
        this.loggedAdmin = adminInfo;
    }

    private void removeLoggedUserFromList(ClientInfo clientToRemove){
        synchronized (this.loggedUsers){
            for (ClientInfo clientInfo : loggedUsers) {
                if(clientInfo.getEmail().equals(clientToRemove.getEmail())){
                    loggedUsers.remove(clientInfo);
                    break;
                }
            }
        }
    }
    private void removeAdmFromList(){
        synchronized (this.loggedUsers){
            for (ClientInfo clientInfo : loggedUsers) {
                if(clientInfo.getEmail().equals("admin@isec.pt")){
                    loggedUsers.remove(clientInfo);
                    break;
                }
            }
        }
    }

    private boolean registerClient(ClientInfo clientInfo){
        boolean success = db.insertUtl(clientInfo.getName(),clientInfo.getStudentNumber(),clientInfo.getEmail(),clientInfo.getPassword());
        db.getAllUsers();

        addLoggedUserToList(clientInfo);

        return success;
    }

    private void printLoggedUsers(){
        for (ClientInfo loggedUser : loggedUsers) {
            System.out.println("Utilizador online: " + loggedUser.getEmail());
        }
    }
}
