package pt.isec.pd.spring_boot.exemplo3.serverbackup;


import pt.isec.pd.spring_boot.exemplo3.server.GetRemoteDBServiceInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiServerBackup implements Runnable {

    private String localFilePath;
    private String dbBackupName;
    private String objectUrl = "rmi://" + "127.0.0.1" + "/servidor-tppd";;
    private GetRemoteDBServiceInterface remoteDBService;
    private GetRemoteDBService myRemoteService;
    String DBOriginalName = "database";
    String newBdPath;

    public RmiServerBackup(String dbBackupName,String newBdPath) {
        this.dbBackupName = dbBackupName;
        this.newBdPath = newBdPath;
        init();
    }

    private void init(){
        //GetRemoteDBService myRemoteService = null;
        //GetRemoteDBServiceInterface remoteDBService;


        //String objectUrl = "rmi://" + "127.0.0.1" + "/servidor-tppd";
        File localDirectory = new File(newBdPath);

        // Verificar se a diretoria está vazia
        String[] files = localDirectory.list();

        if (!(files != null && files.length == 0)) {
            System.out.println("A diretoria não está vazia.");
            System.exit(1);
        }

        if (!localDirectory.exists()) {
            System.out.println("A diretoria " + localDirectory + " nao existe!");
            System.exit(1);

        }
        if (!localDirectory.isDirectory()) {
            System.out.println("O caminho " + localDirectory + " nao se refere a uma diretoria!");
            System.exit(1);

        }
        if (!localDirectory.canWrite()) {
            System.out.println("Sem permissoes de escrita na diretoria " + localDirectory);
            System.exit(1);
        }

        try {
            localFilePath = new File(localDirectory.getPath() + File.separator + dbBackupName).getCanonicalPath();
        } catch (IOException ex) {
            System.out.println("Erro E/S - " + ex);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        getDataBase();
    }

    public void getDatabase(){
        getDataBase();
    }

    private void getDataBase() {
        try (FileOutputStream localFileOutputStream = new FileOutputStream(localFilePath)) { //Cria o ficheiro local

            System.out.println("Ficheiro " + localFilePath + " criado.");

            /*
             * Obtem a referencia remota para o servico com nome "servidor-ficheiros-pd".
             */
            remoteDBService = (GetRemoteDBServiceInterface) Naming.lookup(objectUrl);

            /*
             * Lanca o servico local para acesso remoto por parte do servidor.
             */
            myRemoteService = new GetRemoteDBService();

            /*
             * Passa ao servico RMI LOCAL uma referencia para o objecto localFileOutputStream.
             */
            myRemoteService.setFout(localFileOutputStream);

            /*
             * Obtem o ficheiro pretendido, invocando o metodo getFile no servico remoto.
             */
            remoteDBService.getFile(DBOriginalName, myRemoteService);


        } catch (RemoteException e) {
            System.out.println("Erro remoto - " + e);
        } catch (NotBoundException e) {
            System.out.println("Servico remoto desconhecido - " + e);
        } catch (IOException e) {
            System.out.println("Erro E/S - " + e);
        } catch (Exception e) {
            System.out.println("Erro - " + e);
        } finally {
            if (myRemoteService != null) {

                /*
                 * Retira do servico local a referencia para o objecto localFileOutputStream.
                 */
                myRemoteService.setFout(null);

                /*
                 * Termina o servi�o local.
                 */
                try {
                    UnicastRemoteObject.unexportObject(myRemoteService, true);
                } catch (NoSuchObjectException e) {
                }
            }
        }
    }
}