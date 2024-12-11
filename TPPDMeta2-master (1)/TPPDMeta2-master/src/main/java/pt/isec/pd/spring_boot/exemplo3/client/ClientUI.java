package pt.isec.pd.spring_boot.exemplo3.client;



import pt.isec.pd.spring_boot.exemplo3.utils.Utils;

import java.net.InetAddress;
import java.util.Scanner;

import static pt.isec.pd.spring_boot.exemplo3.utils.Utils.promptUserInput;


public class ClientUI {
    private Scanner scanner;
    private ClientLogic logic;
    private String name;
    private String event;

    public ClientUI(InetAddress serverAddress, int serverPort){
        this.scanner = new Scanner(System.in);
        this.logic = new ClientLogic(serverAddress, serverPort);
    }

    protected void runUI(){
        boolean loggedIn = false;

        while (!loggedIn) {
            System.out.println("1. Criar conta");
            System.out.println("2. Login");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    String name = promptUserInput("Name: ");
                    int studentNumber = Integer.parseInt(promptUserInput("Student Number: "));
                    String email = promptUserInput("Email: ");
                    String password = promptUserInput("Password: ");
                    loggedIn = logic.register(name,studentNumber,email,password);
                    break;
                case 2:
                    loggedIn = logic.login();
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                    break;
            }
        }
        if(!logic.getIsAdmin()) {
            while (loggedIn) {
                System.out.println("Bem-vindo " + logic.clientInfo.getName());
                System.out.println("1. Editar dados de registo");
                System.out.println("2. Submeter código");
                System.out.println("3. Consultar presenças");
                System.out.println("4. Obter ficheiro CSV");
                System.out.println("5. Logout");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        logic.editClientInfo();
                        break;
                    case 2:
                        logic.submitCode();
                        break;
                    case 3:
                        logic.clientCheckAttendance();
                        break;
                    case 4:
                        logic.getCSVFile();
                        break;
                    case 5:
                        if (logic.logout()) {
                            loggedIn = false;
                        }
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                        break;
                }
            }
        }else{
            while(loggedIn){
                System.out.println("1. Criar um evento");
                System.out.println("2. Editar dados de um evento");
                System.out.println("3. Eliminar um evento");
                System.out.println("4. Consultar eventos");
                System.out.println("5. Gerar código para registo de eventos");
                System.out.println("6. Consultar presenças num evento");
                System.out.println("7. Obter ficheiro CSV das presenças num evento");
                System.out.println("8. Consulta dos eventos, por utilizador");
                System.out.println("9. Obter ficheiro CSV dos eventos, por utilizador");
                System.out.println("10. Eliminar presenças");
                System.out.println("11. Inserir presenças");
                System.out.println("12. Atualizar informação visualizada");
                System.out.println("13. Logout");

                int choice = scanner.nextInt();

                switch (choice){
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

                        logic.createEvent(name, local, data, startingHour, finishingHour);
                        break;
                    case 2:
                        String eventName = Utils.promptUserInput("Evento a editar: ");
                        System.out.println("1. Editar Local");
                        System.out.println("2. Editar data");
                        System.out.println("3. Editar hora de início");
                        System.out.println("4. Editar hora de término");
                        System.out.println(logic.editEvent(eventName));
                        break;
                    case 3:
                        name = Utils.promptUserInput("Nome do evento: ");
                        System.out.println(logic.deleteEvent(name));
                        break;
                    case 4:
                        System.out.println(logic.checkEvents());
                        break;
                    case 5:
                        name = Utils.promptUserInput("Evento: ");
                        int time = Utils.promptUserInputInt("Tempo (Min): ");
                        logic.createCodigoRegisto(name, time);
                        break;
                    case 6:
                        name = Utils.promptUserInput("Nome do Evento: ");
                        logic.adminCheckAttendance(name);
                        break;
                    case 7:
                        logic.getCSVAttendance();
                        break;
                    case 8:
                        String email = Utils.promptUserInput("Email utilizador: ");
                        logic.checkAttendanceByUser(email);
                        break;
                    case 9:
                        logic.getCSVEvents();
                        break;
                    case 10:
                        event = Utils.promptUserInput("Evento: ");
                        name = Utils.promptUserInput("Utilizador: ");
                        System.out.println(logic.deleteAttendance(event, name));
                        break;
                    case 11:
                        event = Utils.promptUserInput("Evento: ");
                        name = Utils.promptUserInput("Utilizador: ");
                        System.out.println(logic.insertAttendance(event, name));
                        break;
                    case 12:

                        break;
                    case 13:
                        if (logic.logoutAdm()) {
                            loggedIn = false;
                        }
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 13.");
                        break;
                }
            }
        }
    }


}
