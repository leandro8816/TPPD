package pt.isec.pd.spring_boot.exemplo3.client;



import pt.isec.pd.spring_boot.exemplo3.server.ClientServerRequests;
import pt.isec.pd.spring_boot.exemplo3.utils.CodigoRegisto;
import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.Utils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import static pt.isec.pd.spring_boot.exemplo3.utils.Utils.promptUserInput;


public class ClientLogic implements ClientServerRequests {
    public static final int TIMEOUT = 1;
    protected ClientInfo clientInfo;
    protected AdministratorInfo adminInfo;
    private Scanner scanner;
    private InetAddress serverAddress;
    private boolean isAdministrator;
    int serverPort;

    public ClientLogic(InetAddress serverAddress, int serverPort){
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        scanner = new Scanner(System.in);
    }

    //------------------------------------------------
    //Comandos Cliente
    //------------------------------------------------

    protected boolean register(String name, int studentNumber, String email, String password){
        clientInfo = new ClientInfo(name, studentNumber, email, password, REGISTER);
        ClientInfo response = sendMessageToServerClient(clientInfo);
        return response.getSuccess();
    }

    protected boolean login(){
        String email = promptUserInput("Email: ");
        String password = promptUserInput("Password: ");

        clientInfo = new ClientInfo(email, password, LOGIN);
        ClientInfo response = sendMessageToServerClient(clientInfo);
        if(response.isAdm()) {
            setAdministrator();
            adminInfo = new AdministratorInfo(email,password,LOGIN);
            return true;
        }
        if(response.getSuccess()){
            fillClientInformation(response);
            return true;
        }
        return false;
    }

    protected boolean editClientInfo(){
        while(true){
            System.out.println("Editar informações de registo: ");
            System.out.println("1. Nome");
            System.out.println("2. Numero de estudante");
            System.out.println("3. Email");
            System.out.println("4. Password");
            System.out.println("5. Sair");

            int choice = scanner.nextInt();

            switch(choice){
                case 1:
                    String newName = Utils.promptUserInput("Novo nome: ");
                    clientInfo.setName(newName);
                    break;
                case 2:
                    int newStudentNumber = Utils.promptUserInputInt("Novo numero de estudante: ");
                    clientInfo.setStudentNumber(newStudentNumber);
                    break;
                case 3:
                    String newEmail = Utils.promptUserInput("Novo email: ");
                    clientInfo.setNewEmail(newEmail);
                    break;
                case 4:
                    String newPassword = Utils.promptUserInput("Nova Password: ");
                    clientInfo.setPassword(newPassword);
                    break;
                case 5:
                    return false;
            }
            clientInfo.setRequest(EDIT);
            sendMessageToServerClient(clientInfo);
        }
    }

    protected void submitCode() {
        int code;
        do {
            System.out.print("Código: ");
            code = scanner.nextInt();
        }while (String.valueOf(code).length() != 4);

        clientInfo.setCode(code);
        clientInfo.setRequest(CHECK_CODE);
        sendMessageToServerClient(clientInfo);
    }

    protected void clientCheckAttendance() {
        clientInfo.setRequest(CHECK_ATTENDANCE_CLI);
        sendMessageToServerClient(clientInfo);
    }

    protected void getCSVFile() {
        String fileName = "attendance_"+clientInfo.getName() +".csv";
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(clientInfo.getName() + ";" + clientInfo.getStudentNumber()+ ";" + clientInfo.getEmail());
            writer.newLine();
            writer.newLine();

            List<Event> eventos = clientInfo.getEvents();
            for (Event event : eventos) {
                writer.write(event.getName() + ";" + event.getPlace() + ";" + event.getStartingHour() + ";" + event.getStartingHour());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientInfo.setRequest("OBTAIN_CSV");
    }

    protected boolean logout(){
        clientInfo.setRequest("LOGOUT");
        ClientInfo response = sendMessageToServerClient(clientInfo);
        return response.getSuccess();
    }


    private void setAdministrator() {
        isAdministrator = true;
    }


    //------------------------------------------------
    //Comandos administrador
    //------------------------------------------------

    protected String createEvent(String name, String local, String data, String startingHour, String finishingHour) {
        Event event = new Event(name,local,Utils.stringToDate(data),Utils.stringToTime(startingHour),Utils.stringToTime(finishingHour));
        adminInfo.setEvent(event);
        adminInfo.setRequest(CREATE_EVENT);

        AdministratorInfo response = sendMessageToServerAdmin(adminInfo);
        return response.getServerResponse();
    }

    protected String editEvent(String eventName) {

        String newPlace = null, dateString = null, startingHour = null, finishingHour = null;
        java.sql.Date date = null;
        Time shour = null;
        Time fhour = null;

        int choice = scanner.nextInt();
        switch (choice){
            case 1:
                newPlace = Utils.promptUserInput("Novo local: ");
                break;
            case 2:
                dateString = Utils.promptUserInput("Nova data: ");
                date = Utils.stringToDate(dateString);
                break;
            case 3:
                startingHour = Utils.promptUserInput("Nova hora de começo: ");
                shour = Utils.stringToTime(startingHour);
                break;
            case 4:
                finishingHour = Utils.promptUserInput("Nova hora de término: ");
                fhour = Utils.stringToTime(finishingHour);
                break;
            default:
                System.out.println("Escolha uma opção válida...");
                break;
        }

        Event event = new Event(newPlace, date, shour,fhour);

        adminInfo.setEvent(event);
        adminInfo.setEventName(eventName);

        adminInfo.setRequest(EDIT_EVENT_DATA);
        AdministratorInfo response = sendMessageToServerAdmin(adminInfo);
        return (response.getServerResponse());
    }

    protected String deleteEvent(String name) {
        adminInfo.setEventName(name);
        adminInfo.setRequest(DELETE_EVENT);

        AdministratorInfo response = sendMessageToServerAdmin(adminInfo);
        return (response.getServerResponse());
    }

    protected String checkEvents() {
        adminInfo.setRequest(CHECK_EVENTS);
        AdministratorInfo response = sendMessageToServerAdmin(adminInfo);
        return(response.getServerResponse());
    }

    protected String createCodigoRegisto(String name, int time) {
        CodigoRegisto codigoRegisto = new CodigoRegisto(Utils.calcularHoraFutura(time),name);
        adminInfo.setCodigoRegisto(codigoRegisto);
        adminInfo.setRequest(GENERATE_CODE);
        adminInfo = sendMessageToServerAdmin(adminInfo);
        return (adminInfo.getServerResponse());
    }

    protected String adminCheckAttendance(String name) {
        adminInfo.setEventName(name);
        adminInfo.setRequest(CHECK_ATTENDANCE);

        adminInfo = sendMessageToServerAdmin(adminInfo);
        return(adminInfo.getServerResponse());
    }

    protected void getCSVAttendance() {
        String name = Utils.promptUserInput("Nome do evento: ");

        String fileName = "attendance_event_"+name +".csv";

        System.out.println(name);
        adminInfo.setEventName(name);
        adminInfo.setRequest(OBTAIN_CSV_F1);

        adminInfo = sendMessageToServerAdmin(adminInfo);
        System.out.println(adminInfo.getServerResponse());

        String[] linhas = adminInfo.getServerResponse().split("\n");
        System.out.println(adminInfo.getEvent().toString());


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            Date eventData = adminInfo.getEvent().getDate();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(eventData);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            writer.write(adminInfo.getEvent().getName());
            writer.newLine();
            writer.write(adminInfo.getEvent().getPlace());
            writer.newLine();
            writer.write(day+";"+month+";"+year);
            //writer.write("Data;"+adminInfo.getEvent().getDate().toString().);
            writer.newLine();
            writer.write(adminInfo.getEvent().getStartingHour().getHours()+";"+adminInfo.getEvent().getStartingHour().getMinutes());
            writer.newLine();
            writer.write(+adminInfo.getEvent().getFinishingHour().getHours()+";"+adminInfo.getEvent().getFinishingHour().getMinutes());
            writer.newLine();
            writer.newLine();

            int count=0;
            for (String linha : linhas) {
                if (linha.startsWith("Nome: ")) {
                    String nome = linha.substring("Nome: ".length());
                    writer.write(nome+ ";");
                } else if (linha.startsWith("Número de Estudante: ")) {
                    String numeroEstudante = linha.substring("Número de Estudante: ".length());
                    writer.write(numeroEstudante+ ";");
                } else if (linha.startsWith("Email: ")) {
                    String email = linha.substring("Email: ".length());
                    writer.write(email);
                    count++;
                }
                if(count>0){
                    writer.newLine();
                    count=0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void checkAttendanceByUser(String email) {
        adminInfo.setEmUser(email);
        adminInfo.setRequest(CHECK_ATTENDANCE_BY_USER);

        adminInfo = sendMessageToServerAdmin(adminInfo);

        for(Event event: adminInfo.getEvents()){
            System.out.println(event.toString());
        }
    }

    protected void getCSVEvents() {
        String email = Utils.promptUserInput("Email do utilizador: ");
        String fileName = "attendance_user_" + email + ".csv";

        String nome;
        int id;
        //adminInfo.setEmUser(name);
        adminInfo.setEmUser(email);
        adminInfo.setRequest(OBTAIN_CSV_F2);

        adminInfo = sendMessageToServerAdmin(adminInfo);

        nome = adminInfo.getNomeUser();
        id = adminInfo.getIdUser();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(nome + ";" + id+ ";" + email);
            writer.newLine();
            writer.newLine();
            List<Event> eventos = adminInfo.getEvents();
            for (Event event : eventos) {
                writer.write(event.getName() + ";" + event.getPlace() + ";" + event.getStartingHour() + ";" + event.getStartingHour());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientInfo.setRequest("OBTAIN_CSV");
    }

    protected String deleteAttendance(String event, String name) {
       adminInfo.setEventName(event);
        adminInfo.setEmUser(name);
        adminInfo.setRequest(DELETE_ATTENDANCE);

        adminInfo = sendMessageToServerAdmin(adminInfo);
        return adminInfo.getServerResponse();
    }

    protected String insertAttendance(String event, String name) {
        adminInfo.setEventName(event);
        adminInfo.setEmUser(name);
        adminInfo.setRequest(INSERT_ATTENDANCE);

        adminInfo = sendMessageToServerAdmin(adminInfo);
        return adminInfo.getServerResponse();
    }

    protected boolean logoutAdm(){
        adminInfo.setRequest(LOGOUT);
        AdministratorInfo response = sendMessageToServerAdmin(adminInfo);
        return response.getSuccess();
    }

    protected boolean getIsAdmin() {
        return isAdministrator;
    }

    //------------------------------------------------
    //Funções adicionais
    //------------------------------------------------

    private ClientInfo sendMessageToServerClient(ClientInfo clientInfo){
        try(Socket socket = new Socket(serverAddress, serverPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            socket.setSoTimeout(TIMEOUT * 1000);

            out.writeObject(clientInfo);
            out.flush();

            ClientInfo response = (ClientInfo) in.readObject();

            if(!response.getSuccess())
                System.out.println(response.getServerResponse());

            return response;

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private AdministratorInfo sendMessageToServerAdmin(AdministratorInfo adminInfo){
        try(Socket socket = new Socket(serverAddress, serverPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            socket.setSoTimeout(1000);

            out.writeObject(adminInfo);
            out.flush();

            AdministratorInfo response = (AdministratorInfo) in.readObject();

            return response;

        } catch (SocketTimeoutException e){
            System.out.println("Timeout");

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void fillClientInformation(ClientInfo serverResponse){
        clientInfo.setName(serverResponse.getName());
        clientInfo.setStudentNumber(serverResponse.getStudentNumber());
    }
}
