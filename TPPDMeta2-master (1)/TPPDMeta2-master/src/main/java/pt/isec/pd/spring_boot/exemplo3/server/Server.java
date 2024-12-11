package pt.isec.pd.spring_boot.exemplo3.server;


import pt.isec.pd.spring_boot.exemplo3.client.ClientInfo;
import pt.isec.pd.spring_boot.exemplo3.models.Event;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server implements Runnable {
    protected static final String SERVICE_NAME = "servidor-tppd";
    protected static String DATABASE_PATH = "src";

    protected MulticastSocket s;
    private ServerSocket socket;
    private List<ClientInfo> loggedUsers;
    private List<Event> events;
    private ManageDB manageDb;
    static HeartBeatSender heartBeatSender;

    public Server(int port) {
        try {
            heartBeatSender = new HeartBeatSender();
            //manageDb = new ManageDB(DATABASE_PATH, "database",heartBeatSender);
            socket = new ServerSocket(port);
            loggedUsers = Collections.synchronizedList(new ArrayList<>());
            //TODO IR BUSCAR OS EVENTOS Á BASE DE DADOS
            events = Collections.synchronizedList(new ArrayList<>());

            System.out.println("TCP Time Server iniciado no porto " + socket.getLocalPort() + " ...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Sintaxe: java Server serverPort");
            return;
        }
        int port = Integer.parseInt(args[0]);

        if (isPortInUse(port)) {
            System.out.println("Servidor já em execução. A encerrar...");
            System.exit(1);
        }

        Runnable run = new Server(port);
        Thread threadAcceptClients = new Thread(run);
        threadAcceptClients.start();

        Thread threadSendHeartBeat = new Thread(heartBeatSender);
        threadSendHeartBeat.start();

        Server serverInstance = (Server) run;
        heartBeatSender.sendHeartBeat(serverInstance.getDbVersion(),true);

        //RMI
        File localDirectory = new File(DATABASE_PATH);

        if(!localDirectory.exists()){
            System.out.println("A directoria " + localDirectory + " nao existe!");
            return;
        }

        if(!localDirectory.isDirectory()){
            System.out.println("O caminho " + localDirectory + " nao se refere a uma diretoria!");
            return;
        }

        if(!localDirectory.canRead()){
            System.out.println("Sem permissoes de leitura na diretoria " + localDirectory + "!");
            return;
        }

        /*
         * Lanca o rmiregistry localmente no porto TCP por omissao (1099).
         */
        try{

            try{

                System.out.println("Tentativa de lancamento do registry no porto " +
                        Registry.REGISTRY_PORT + "...");

                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

                System.out.println("Registry lancado!");

            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
                System.out.println("Já existe um servidor em execução... A encerrar este servidor...");
                System.exit(1);
            }

            /*
             * Cria o servico.
             */
            RemoteFileServiceImpl fileService = new RemoteFileServiceImpl(localDirectory);

            System.out.println("Servico GetRemoteFile criado e em execucao ("+fileService.getRef().remoteToString()+"...");

            /*
             * Regista o servico no rmiregistry local para que os clientes possam localiza'-lo, ou seja,
             * obter a sua referencia remota (endereco IP, porto de escuta, etc.).
             */

            Naming.bind("rmi://localhost/" + SERVICE_NAME, fileService);

            System.out.println("Servico " + SERVICE_NAME + " registado no registry...");

            /*
             * Para terminar um servico RMI do tipo UnicastRemoteObject:
             *
             *  UnicastRemoteObject.unexportObject(fileService, true).
             */

        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            System.exit(1);
        }
    }


    @Override
    public void run() {
        while(true){
            try {
                Socket toClientSocket = socket.accept();
                Thread attendClientThread = new AttendTCPClientsThread(toClientSocket, loggedUsers, events,manageDb);
                attendClientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public int getDbVersion(){
        return manageDb.obterVersaoPorNome("pd");
    }
}