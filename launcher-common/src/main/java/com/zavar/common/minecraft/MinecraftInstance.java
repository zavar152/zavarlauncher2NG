package com.zavar.common.minecraft;

import com.google.gson.Gson;
import com.vdurmont.semver4j.Semver;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftInstance {
    private final MinecraftInstanceConfiguration instanceConfiguration;
    private boolean installed;

    private final static Gson gson;

    static {
        gson = new Gson();
    }

    private MinecraftInstance(MinecraftInstanceConfiguration instanceConfiguration) {
        this.instanceConfiguration = instanceConfiguration;
        installed = false;
    }

    public static MinecraftInstanceConfiguration prepareNewConfiguration(String title, Semver minecraftVersion, int javaVersion, Path installationDirectory, Path originalDirectory, URI remoteLocation) {
        if(!remoteLocation.toString().contains("${server.ip}"))
            throw new IllegalArgumentException("Remote location should contain dynamic server ip property");
        if(!installationDirectory.toString().contains("${user.home}"))
            throw new IllegalArgumentException("Remote location should contain dynamic user home property");

        Configuration configuration = Configuration.builder().dynamicProperty("server.ip", "").baseUri(remoteLocation.resolve(title))
                .basePath(installationDirectory.resolve(title)).files(FileMetadata.streamDirectory(originalDirectory).peek(r -> r.ignoreBootConflict(true))).build();
        MinecraftInstanceInformation instanceInformation = new MinecraftInstanceInformation(title, minecraftVersion.getValue(), configuration.getTimestamp().toString(), title.toLowerCase().concat(".json"), String.valueOf(javaVersion));
        return new MinecraftInstanceConfiguration(configuration, instanceInformation);
    }

    public static void createIdentificationFiles(MinecraftInstanceConfiguration instanceConfiguration, Path infoFile, Path manifestFile) throws IOException {
        gson.toJson(instanceConfiguration.instanceInformation, Files.newBufferedWriter(infoFile.resolve(instanceConfiguration.getMainInformation().title.toLowerCase().concat(".json"))));
        instanceConfiguration.configuration.write(Files.newBufferedWriter(manifestFile.resolve(instanceConfiguration.getMainInformation().title.toLowerCase().concat(".xml"))));
    }

    public static MinecraftInstanceConfiguration loadConfigurationFromIdentificationFiles(String title, Path infoFile, Path manifestFile) throws IOException {
        MinecraftInstanceInformation instanceInformation = gson.fromJson(Files.newBufferedReader(infoFile.resolve(title.toLowerCase().concat(".json"))), MinecraftInstanceInformation.class);
        Configuration configuration = Configuration.read(Files.newBufferedReader(manifestFile.resolve(title.toLowerCase().concat(".xml"))));
        return new MinecraftInstanceConfiguration(configuration, instanceInformation);
    }

    public static MinecraftInstance createInstanceFromConfiguration(MinecraftInstanceConfiguration instanceConfiguration) {
        return new MinecraftInstance(instanceConfiguration);
    }

    public record MinecraftInstanceInformation(String title, String minecraftVersion, String instanceVersion, String xmlName, String javaVersion) {

    }

    public static class MinecraftInstanceConfiguration {
        private final Configuration configuration;
        private final MinecraftInstanceInformation instanceInformation;

        public MinecraftInstanceConfiguration(Configuration configuration, MinecraftInstanceInformation instanceInformation) {
            this.configuration = configuration;
            this.instanceInformation = instanceInformation;
        }

        public Configuration getFilesConfiguration() {
            return configuration;
        }

        public MinecraftInstanceInformation getMainInformation() {
            return instanceInformation;
        }
    }

}
