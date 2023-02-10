package com.zavar.bootstrapper.launcher;

import com.zavar.bootstrapper.java.JreManager;
import com.zavar.common.finder.JavaFinder;
import javafx.concurrent.Task;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;

public class LauncherStartTask extends Task<Void> {

    private final Path launcherFolder;
    private final Set<JavaFinder.Java> javas;
    private final JreManager jreManager;
    private final Set<Integer> installedJavas;
    private final Path jreFolder;

    public LauncherStartTask(Path launcherFolder, Set<JavaFinder.Java> javas, JreManager jreManager, Set<Integer> installedJavas, Path jreFolder) {
        this.launcherFolder = launcherFolder;
        this.javas = javas;
        this.jreManager = jreManager;
        this.installedJavas = installedJavas;
        this.jreFolder = jreFolder;
    }

    @Override
    protected Void call() throws Exception {
        File launcherFile = FileUtils.getFile(launcherFolder + "/launcher.jar");
        ZipFile launcherJarFile = new ZipFile(launcherFile);
        if (launcherFile.exists()) {
            if(!launcherJarFile.isValidZipFile())
                throw new IllegalAccessException("Launcher is corrupted");
            URL versionPath = new URL("jar:file:" + launcherFolder + "/launcher.jar!/version.properties");
            Properties properties = new Properties();
            JarURLConnection jarConn = (JarURLConnection) versionPath.openConnection();
            jarConn.setUseCaches(false);
            JarFile jarFile = jarConn.getJarFile();
            properties.load(jarFile.getInputStream(jarConn.getJarEntry()));
            jarConn.getInputStream().close();

            int launcherJavaVersion = Integer.parseInt(properties.getProperty("launcherJavaVersion"));
            if (jreManager != null && jreManager.isJreExists(launcherJavaVersion)) {
                ProcessBuilder processBuilder = new ProcessBuilder(jreManager.getJreFolderPath().resolve(String.valueOf(launcherJavaVersion)).resolve("bin").resolve("java.exe").toString(), "-jar", launcherFolder.resolve("launcher.jar").toString());
                processBuilder.start();
            } else if (javas.stream().anyMatch(java -> java.version() >= launcherJavaVersion)) {
                ProcessBuilder processBuilder = new ProcessBuilder(javas.stream().filter(java -> java.version() >= launcherJavaVersion).findFirst().get().home().resolve("bin").resolve("java.exe").toString(), "-jar", launcherFolder.resolve("launcher.jar").toString());
                processBuilder.start();
            } else if (installedJavas.stream().anyMatch(integer -> integer == launcherJavaVersion)) {
                ProcessBuilder processBuilder = new ProcessBuilder(jreFolder.resolve(Paths.get(String.valueOf(installedJavas.stream().filter(integer -> integer == launcherJavaVersion).findFirst().get()))).resolve("bin").resolve("java.exe").toString(), "-jar", launcherFolder.resolve("launcher.jar").toString());
                processBuilder.start();
            } else {
                throw new InstantiationException("Java " + launcherJavaVersion + " couldn't be found");
            }

        } else {
            throw new FileNotFoundException("Launcher not found");
        }
        return null;
    }
}
