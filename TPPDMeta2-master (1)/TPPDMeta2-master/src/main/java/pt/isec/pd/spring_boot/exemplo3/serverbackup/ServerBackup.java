package pt.isec.pd.spring_boot.exemplo3.serverbackup;


import pt.isec.pd.spring_boot.exemplo3.utils.HeartBeatMSG;

import java.io.*;
import java.net.*;

public class ServerBackup extends Thread {
    protected boolean running;
    public static int MAX_SIZE = 1000;
    private static final int MULTICAST_PORT = 4444;
    protected static final String MULTICAST_ADDRESS = "230.44.44.44";
    protected static final String MULTICAST_IP_ADDRESS = "127.0.0.1";
    protected String REGISTRY_PORT = null;
    protected String SERVICE_NAME = null;

    protected MulticastSocket s;
    private int timeAlive;
    private RmiServerBackup rmi;
    private static ManageDBBackup db;
    public ServerBackup(MulticastSocket s,RmiServerBackup rmi,ManageDBBackup db) {
        running = true;
        ServerBackup.db = db;
        this.rmi = rmi;
        timeAlive = 0;
        this.s = s;

    }
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Sintaxe: java ServerBackup directory databaseBackupName ");
            return;
        }

        //Multicast
        InetAddress group;
        MulticastSocket socket = null;
        NetworkInterface nif;
        ServerBackup t = null;

        //RMI
        String dbBackupName = args[1];
        String newBdPath = args[0];

        RmiServerBackup rmi = new RmiServerBackup(dbBackupName,newBdPath);
        db = new ManageDBBackup(newBdPath, dbBackupName);
        rmi.getDatabase();

        //Multicast
        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            int port = MULTICAST_PORT;
            nif = NetworkInterface.getByInetAddress(InetAddress.getByName(MULTICAST_IP_ADDRESS)); //e.g., 127.0.0.1, 192.168.10.1, ...

            socket = new MulticastSocket(port);

            socket.joinGroup(new InetSocketAddress(group, port), nif);

            t = new ServerBackup(socket,rmi,db);
            t.start();

            while (true) {
                try {
                    while (t.getTimeAlive() < 30) {
                        Thread.sleep(1000);
                        t.incTimeAlive();
                        //System.out.println("SEGUNDOS-> " + t.getTimeAlive());
                    }
                    System.out.println("Não recebi um heartBeat nos últimos 30 segundos...A encerrar...");
                    break;
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (t != null) {
                t.terminate();
            }

            if (socket != null) {
                socket.close();
            }
        }
    }

    @Override
    public void run() {
        Object obj;
        DatagramPacket pkt;
        HeartBeatMSG heartBeatMsg;

        if (s == null || !running) {
            return;
        }

        try {

            while (running) {

                pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                s.receive(pkt);

                try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(pkt.getData(), 0, pkt.getLength()))) {

                    obj = in.readObject();

                    if (obj instanceof HeartBeatMSG) {

                        heartBeatMsg = (HeartBeatMSG) obj;

                        System.out.println();
                        System.out.print("(" + pkt.getAddress().getHostAddress() + ":" + pkt.getPort() + ") ");
                        REGISTRY_PORT = heartBeatMsg.getPort();
                        SERVICE_NAME = heartBeatMsg.getNameRmi();
                        System.out.println("RECEBI: REGISTRY PORT: [" + REGISTRY_PORT + "], SERVICE NAME: [" + SERVICE_NAME + "]: " + heartBeatMsg.getDbVersion() + " UPDATE: "+heartBeatMsg.isUpdate()+"| (" + heartBeatMsg.getClass() + ")");
                        resetTimeAlive();
                        db.connect();
                        if(heartBeatMsg.isUpdate()){
                            rmi.getDatabase();
                            if(heartBeatMsg.getDbVersion() == db.obterVersaoPorNome("pd")){
                                db.closeConnection();
                            }else {
                                System.out.println("VERSÃO DIFERENTE. A encerrar...");
                                System.exit(1);
                            }

                        }
                    } else if (obj instanceof String) {

                        System.out.println((String) obj + " (" + obj.getClass() + ")");
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println();
                    System.out.println("Mensagem recebida de tipo inesperado! " + e);
                } catch (IOException e) {
                    System.out.println();
                    System.out.println("Impossibilidade de aceder ao conteudo da mensagem recebida! " + e);
                } catch (Exception e) {
                    System.out.println();
                    System.out.println("Excepcao: " + e);
                }

            }

        } catch (IOException e) {
            if (running) {
                System.out.println(e);
            }
            if (!s.isClosed()) {
                s.close();
            }
        }
    }
    private void incTimeAlive() {
        timeAlive++;
    }

    public void terminate() {
        running = false;
    }

    public void resetTimeAlive(){
        timeAlive = 0;
    }
    public int getTimeAlive() {
        return timeAlive;
    }
}