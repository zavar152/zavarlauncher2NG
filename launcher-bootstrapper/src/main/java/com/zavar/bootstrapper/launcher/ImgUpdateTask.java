package com.zavar.bootstrapper.launcher;

import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import com.zavar.bootstrapper.util.Util;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

public class ImgUpdateTask extends Task<Void> {

    private final Path launcherFolder;
    private final String url;
    private final Path imgFolder;

    public ImgUpdateTask(Path launcherFolder, String url, Path imgFolder) {
        this.launcherFolder = launcherFolder;
        this.url = url;
        this.imgFolder = imgFolder;
    }

    @Override
    protected Void call() throws Exception {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
        if (!imgFolder.toFile().exists())
            FileUtils.forceMkdir(imgFolder.toFile());

        Document doc = Jsoup.connect(url).get();
        for (Element file : doc.select("pre a").not("[href=../]")) {
            System.out.println(file.text());
            File imgFile = imgFolder.resolve(file.text()).toFile();
            if(!imgFile.exists()) {
                updateTitle("Update images (" + file.text() + ")");
                URL imgUrl = new URL(url + "/" + file.text());
                long remoteSize = Util.contentLength(imgUrl);
                ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(imgUrl.openStream()), remoteSize);
                rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
                    updateProgress((double) observableValue.getValue(), 1);
                    updateMessage(nf.format(observableValue.getValue()));
                });
                FileOutputStream fileOutputStream = new FileOutputStream(imgFile);
                fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fileOutputStream.close();
            }
        }
        return null;
    }
}
