package pt.isec.pd.spring_boot.exemplo3.serverbackup;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetRemoteDBInterface extends Remote {
    void writeFileChunk(byte [] fileChunk, int nbytes) throws RemoteException, IOException;
}