package pt.isec.pd.spring_boot.exemplo3.server;


import pt.isec.pd.spring_boot.exemplo3.serverbackup.GetRemoteDBInterface;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetRemoteDBServiceInterface extends Remote {
    void getFile(String fileName, GetRemoteDBInterface cliRef) throws RemoteException, IOException;


}