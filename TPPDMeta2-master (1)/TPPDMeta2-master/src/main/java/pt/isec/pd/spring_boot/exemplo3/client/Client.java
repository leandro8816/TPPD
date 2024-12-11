package pt.isec.pd.spring_boot.exemplo3.client;



import pt.isec.pd.spring_boot.exemplo3.server.ClientServerRequests;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class Client implements Runnable, ClientServerRequests {
    InetAddress serverAddress;
    int serverPort;

    public Client(InetAddress address, int port){
        this.serverAddress = address;
        this.serverPort = port;
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Sintaxe: java Client serverAddress serverPort");
            return;
        }

        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);

        if (!testServerConnection(address, port)) {
            System.out.println("Servidor ainda não está disponível.");
            return;
        }

        Runnable run = new Client(address,port);
        Thread ui = new Thread(run);
        ui.start();
    }

    private static boolean testServerConnection(InetAddress address, int port) {
        try (Socket socket = new Socket(address, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    @Override
    public void run() {
        ClientUI clientUI = new ClientUI(serverAddress, serverPort);
        clientUI.runUI();
    }
}