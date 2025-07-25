package com.tcs;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.util.List;

import static com.tcs.model.AppConfig.DATE_2_USE;

public class EAServer implements AutoCloseable{

    private static final String REMOTE_HOST = "wallix-europassistance.ecritel.net";

    private SSHClient sshClient;
    private SFTPClient sftpClient;

    public EAServer(final String machineId, final String username, final String password) throws IOException {
        this.connect(machineId, username, password);
    }

    public void connect(final String machineId, final String username, final String password) throws IOException {
        sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.connect(REMOTE_HOST);
        sshClient.useCompression();
        final String REMOTE_USERNAME = "europassistance@" + machineId + ":" + username;
        sshClient.authPassword(REMOTE_USERNAME, password);
        sftpClient = sshClient.newSFTPClient();
        System.out.println("-- Connect to server : " + machineId);
    }

//    public void downloadFile(String remoteFile, String localFile) throws IOException {
//        System.out.println("----- File to download : " + remoteFile + " to local : " + localFile);
//
//        try {
//            sftpClient.get(remoteFile, localFile);
//        } catch (Exception e) {
//            throw new IOException("File not found: " + remoteFile);
//        }
//
//    }

    public boolean downloadFile(String remoteDir,String localFile ) throws IOException {
        List<RemoteResourceInfo> AllFiles = sftpClient.ls(remoteDir);
        String dateToUse = DATE_2_USE;
        String datePattern = "." + dateToUse + ".";
        String remoteFile = "";
        Boolean flag=false;
        try {
            for (RemoteResourceInfo entry : AllFiles) {
                String filename = entry.getName();
                if (filename.contains(datePattern)) {
                     remoteFile = remoteDir + filename;
                    System.out.println("----- File to download : " + remoteFile + " to local : " + localFile+filename);
                    sftpClient.get(remoteFile, localFile+filename);
                    flag=true;
                }
            }
        }
        catch (Exception e) {
            throw new IOException("File not found: " + remoteFile);
        }

        return flag;
    }

    public void disconnect() throws IOException {
        sftpClient.close();
        sshClient.disconnect();
    }

    @Override
    public void close() throws Exception {
        this.disconnect();
    }

}

