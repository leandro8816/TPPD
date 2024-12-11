package pt.isec.pd.spring_boot.exemplo3.server;


import pt.isec.pd.spring_boot.exemplo3.utils.HeartBeatMSG;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

public class HeartBeatSender implements Runnable{
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;
    private static final String MULTICAST_IP_ADDRESS = "127.0.0.1";
    private static final int HEARTBEAT_TIME = 10;
    protected static final String REGISTRY_PORT = "1099";
    protected static final String SERVICE_NAME = "servidor-tppd";
    private InetAddress group;
    private int port;
    private NetworkInterface nif;
    private MulticastSocket socket;
    private int dbVersion;

    public HeartBeatSender() {
        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            port = MULTICAST_PORT;
            nif = NetworkInterface.getByInetAddress(InetAddress.getByName(MULTICAST_IP_ADDRESS));
            socket = new MulticastSocket(port);
            socket.joinGroup(new InetSocketAddress(group, port), nif);

        }  catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHeartBeat(int dbVersion,boolean update){
        try {
            ByteArrayOutputStream buff = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buff)) {
                out.writeObject(new HeartBeatMSG(REGISTRY_PORT, SERVICE_NAME, dbVersion,update));
            }

            DatagramPacket dgram = new DatagramPacket(buff.toByteArray(), buff.size(), group, port);
            socket.send(dgram);
            setDbVersion(dbVersion);
        } catch (IOException e) {
            throw new RuntimeException("Erro ", e);
        }
    }

    @Override
    public void run() {
        try {
            int count = 0;
            while (true) {
                while (count < HEARTBEAT_TIME) {
                    Thread.sleep( 1000);
                    count++;
                    //System.out.println("SEGUNDOS-> " + count);
                }
                count = 0;

                sendHeartBeat(getDbVersion(),false);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }
}