package pt.isec.pd.spring_boot.exemplo3.server;


import pt.isec.pd.spring_boot.exemplo3.serverbackup.GetRemoteDBInterface;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteFileServiceImpl extends UnicastRemoteObject implements GetRemoteDBServiceInterface {
    private static final int MAX_CHUNK_SIZE = 10000; // bytes
    private File localDirectory;

    public RemoteFileServiceImpl(File localDirectory) throws RemoteException {
        super();
        this.localDirectory = localDirectory;
    }

    protected FileInputStream getRequestedFileInputStream(String fileName) throws IOException {
        String requestedCanonicalFilePath;

        fileName = fileName.trim();

        requestedCanonicalFilePath = new File(localDirectory + File.separator + fileName).getCanonicalPath();

        if (!requestedCanonicalFilePath.startsWith(localDirectory.getCanonicalPath() + File.separator)) {
            System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
            System.out.println("A directoria de base nao corresponde a " + localDirectory.getCanonicalPath() + "!");
            throw new AccessDeniedException(fileName);
        }

        return new FileInputStream(requestedCanonicalFilePath);
    }



    public void getFile(String fileName, GetRemoteDBInterface cliRemoto) throws IOException {
        byte[] fileChunk = new byte[MAX_CHUNK_SIZE];
        int nbytes;

        fileName = fileName.trim();
        System.out.println("Recebido pedido para: " + fileName);

        try (FileInputStream requestedFileInputStream = getRequestedFileInputStream(fileName)) {
            while ((nbytes = requestedFileInputStream.read(fileChunk)) != -1) {
                cliRemoto.writeFileChunk(fileChunk, nbytes);
            }

            System.out.println("Ficheiro " + new File(localDirectory + File.separator + fileName).getCanonicalPath() +
                    " transferido para o cliente com sucesso.");

            System.out.println();

        } catch (FileNotFoundException e) {
            System.out.println("Ocorreu a excecao {" + e + "} ao tentar abrir o ficheiro!");
            throw new FileNotFoundException(fileName);
        } catch (IOException e) {
            System.out.println("Ocorreu a excecao de E/S: \n\t" + e);
            throw new IOException(fileName, e.getCause());
        }
    }
}
