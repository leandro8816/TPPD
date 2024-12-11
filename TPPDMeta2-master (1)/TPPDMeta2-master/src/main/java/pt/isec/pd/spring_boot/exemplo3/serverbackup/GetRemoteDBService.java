package pt.isec.pd.spring_boot.exemplo3.serverbackup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GetRemoteDBService extends UnicastRemoteObject implements GetRemoteDBInterface{
    FileOutputStream fout = null;

    public GetRemoteDBService() throws RemoteException {
        //super() é sempre chamado
    }

    public synchronized void setFout(FileOutputStream fout) {
        this.fout = fout;
    }

    @Override
    public synchronized void writeFileChunk(byte [] fileChunk, int nbytes) throws RemoteException, IOException {
        if(fout == null){
            System.out.println("O ficheiro de destino ainda nao foi definido!");
            throw new IOException("O ficheiro de destino ainda nao foi definido!");
        }

        try{
            fout.write(fileChunk, 0, nbytes);
        } catch (IOException e) {
            System.out.println("Erro ao escrever no ficheiro de destino: "+e);
            throw new IOException("Erro ao escrever no ficheiro de destino: "+e.getCause());
        }
    }
}
