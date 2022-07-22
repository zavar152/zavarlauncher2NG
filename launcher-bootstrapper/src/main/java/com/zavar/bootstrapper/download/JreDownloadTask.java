package com.zavar.bootstrapper.download;

import com.zavar.bootstrapper.java.JreManager;
import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import com.zavar.bootstrapper.util.Util;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class JreDownloadTask extends Task<Void> {

    private final List<Integer> jreToInstall;
    private final Path tempFolder;
    private final Path jreFolder;
    private final JreManager jreManager;

    public JreDownloadTask(List<Integer> jreToInstall, Path tempFolder, Path jreFolder, JreManager jreManager, ProgressBar bar) {
        this.jreToInstall = jreToInstall;
        this.tempFolder = tempFolder;
        this.jreFolder = jreFolder;
        this.jreManager = jreManager;
    }

    @Override
    protected Void call() throws Exception {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
        for (Integer i : jreToInstall) {
            File archiveFile = new File(tempFolder + "/" + i + ".zip");

            if (archiveFile.exists() && FileUtils.sizeOf(archiveFile) != Util.contentLength(jreManager.getDownloadUrlForVersion(i))) {
                FileUtils.delete(archiveFile);
            }
            if (!archiveFile.exists()) {
                ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(jreManager.getDownloadUrlForVersion(i).openStream()), Util.contentLength(jreManager.getDownloadUrlForVersion(i)));
                rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
                    updateProgress((double) observableValue.getValue(), 1);
                });
                rbc.getProgressProperty().addListener((observableValue, number, t1) -> updateMessage(nf.format(observableValue.getValue())));
                updateTitle("Downloading java " + i);
                if (!tempFolder.toFile().exists())
                    FileUtils.forceMkdir(tempFolder.toFile());

                FileOutputStream fileOutputStream = new FileOutputStream(archiveFile);
                fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fileOutputStream.close();
            }
            if (isCancelled()) {
                break;
            }
            updateTitle("Unzipping java " + i);
            ZipFile toUnzipFile = new ZipFile(archiveFile);
            toUnzipFile.setRunInThread(true);
            ProgressMonitor progressMonitor = toUnzipFile.getProgressMonitor();
            toUnzipFile.extractAll(jreFolder + "/" + i);
            while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                updateMessage(nf.format(progressMonitor.getPercentDone() / 100.0));
                updateProgress(progressMonitor.getPercentDone() / 100.0, 1);
            }
            FileUtils.delete(archiveFile);
        }
        updateTitle("Java is ready");
        return null;
    }
}
