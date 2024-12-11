package pt.isec.pd.spring_boot.exemplo3.consumer;


import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.LoginResult;
import pt.isec.pd.spring_boot.exemplo3.utils.Utils;

import java.io.IOException;
import java.util.Scanner;

import static pt.isec.pd.spring_boot.exemplo3.utils.Utils.promptUserInput;

public class RestClientUI {


    public static void main(String args[]) throws IOException {

        String token = null;
        Scanner scanner;
        scanner = new Scanner(System.in);
        boolean isAdmin = false;


        String loginUri = "http://localhost:8080/login";
        String isAdminUri = "http://localhost:8080/list/isAdmin";
        String submitCodeUri = "http://localhost:8080/list/submitCode";
        String registerUri = "http://localhost:8080/register";
        String addEventUri = "http://localhost:8080/list/addEvent";
        String deleteEventUri = "http://localhost:8080/list/deleteEvent";
        String getAllEventsUri = "http://localhost:8080/list/getAllEvents";
        String getAttendanceInEventUri = "http://localhost:8080/list/getAttendanceEvent";
        String getAttendanceClientUri = "http://localhost:8080/list/getAttendanceClient";
        String generateCodeUri = "http://localhost:8080/list/generateCode";


        boolean loggedIn = false;
        String email=null;
        while (!loggedIn) {
            System.out.println("1. Criar conta");
            System.out.println("2. Login");
            String password;

            int choice = scanner.nextInt();
            LoginResult loginResult = new LoginResult(false,null);

            switch (choice) {
                case 1:
                    String name = promptUserInput("Name: ");
                    int studentNumber = Integer.parseInt(promptUserInput("Student Number: "));
                    email = promptUserInput("Email: ");
                    password = promptUserInput("Password: ");
                    RestClient.register(registerUri,name,studentNumber,email,password);
                    break;
                case 2:
                    email = promptUserInput("Email: ");
                    password = promptUserInput("Password: ");
                    loginResult = RestClient.login(loginUri,email,password);
                    token = loginResult.getToken();
                    loggedIn = loginResult.isSuccess();
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                    break;
            }
        }

        if (RestClient.isAdmin(isAdminUri,token).equals("true"))
            isAdmin = true;

        if(!isAdmin){
            while (loggedIn) {
                System.out.println("Bem-vindo " + email);
                System.out.println("1. Submeter código");
                System.out.println("2. Consultar presenças");
                System.out.println("3. Logout");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        String code = promptUserInput("Code: ");
                        if(RestClient.submitCode(submitCodeUri,code,token)){
                            System.out.println("Codigo inserido com sucesso!\n");
                        }else{
                            System.out.println("Codigo não inserido!\n");
                        }
                        break;
                    case 2:
                        System.out.println(RestClient.checkAttendanceClient(getAttendanceClientUri,token));
                        break;
                    case 3:
                            loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                        break;
                }
            }
        }else{
            while (loggedIn) {
                System.out.println("1. Criar um evento");
                System.out.println("2. Eliminar um evento");
                System.out.println("3. Consultar eventos");
                System.out.println("4. Gerar código para registo de eventos");
                System.out.println("5. Consultar presenças num evento");
                System.out.println("6. Logout");


                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        String name = Utils.promptUserInput("Nome: ");
                        String local = Utils.promptUserInput("Local: ");
                        String data = Utils.promptUserInput("Data (yyyy-MM-dd): ");

                        if(!Utils.isValidDateFormat(data)){
                            System.out.println("Formato invalido");
                            break;
                        }

                        String startingHour = Utils.promptUserInput("Hora de Inicio (HH:mm:ss): ");

                        if(!Utils.isValidTimeFormat(startingHour)){
                            System.out.println("Formato invalido");
                            break;
                        }
                        String finishingHour = Utils.promptUserInput("Hora de Fim (HH:mm:ss): ");
                        if(!Utils.isValidTimeFormat(finishingHour)){
                            System.out.println("Formato invalido");
                            break;
                        }
                        Event event = new Event(
                                name,
                                local,
                                Utils.stringToDate(data),
                                Utils.stringToTime( startingHour),
                                Utils.stringToTime( finishingHour)
                        );
                        RestClient.createEvent(addEventUri,token,event);

                        break;
                    case 2:
                        String nome = Utils.promptUserInput("Nome do evento: ");
                        RestClient.deleteEvent(deleteEventUri,token,nome);
                        break;
                    case 3:
                        System.out.println(RestClient.checkEvents(getAllEventsUri,token));
                        RestClient.checkEvents(getAllEventsUri,token);
                        break;
                    case 4:
                        String evento = Utils.promptUserInput("Nome do evento: ");
                        int duracao = Integer.parseInt(promptUserInput("Duração: "));
                        RestClient.generateCode(generateCodeUri,token,evento,duracao);
                        break;
                    case 5:
                        String nomeEvento = Utils.promptUserInput("Nome do evento: ");
                        //System.out.println(RestClient.checkAttendance(getAttendanceInEventUri,token,nomeEvento));
                        RestClient.checkAttendance(getAttendanceInEventUri,token,nomeEvento);
                        break;
                    case 6:
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                        break;
                }
            }
        }
    }
}
