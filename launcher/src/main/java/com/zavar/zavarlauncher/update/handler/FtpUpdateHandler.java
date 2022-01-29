package com.zavar.zavarlauncher.update.handler;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.update4j.FileMetadata;
import org.update4j.service.DefaultUpdateHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FtpUpdateHandler extends DefaultUpdateHandler {

    private final String host;
    private final Integer port;
    private final String user;
    private final String password;
    private final FTPClient ftp;

    public FtpUpdateHandler(String host, Integer port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    }

    @Override
    public InputStream openDownloadStream(FileMetadata file) throws Throwable {
        open();
        return ftp.retrieveFileStream(URLDecoder.decode(file.getUri().toURL().toString(), StandardCharsets.UTF_8).replace("http://"+host, ""));
    }

    @Override
    public void succeeded() {
        super.succeeded();
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open() throws IOException {
        ftp.connect(host, port);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(user, password);
    }

    private void close() throws IOException {
        ftp.disconnect();
    }
}
