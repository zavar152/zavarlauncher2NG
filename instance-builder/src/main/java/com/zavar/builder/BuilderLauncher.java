package com.zavar.builder;

import com.vdurmont.semver4j.Semver;
import com.zavar.common.minecraft.MinecraftInstance;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BuilderLauncher {

    private static final Path userHomeFolder = Path.of(System.getProperty("user.home"));
    private static final Path launcherFolder = userHomeFolder.resolve("zavarlauncher2");
    private static final Path builderFolder = launcherFolder.resolve("builder");
    private static final Path instancesFolder = builderFolder.resolve("instances");
    private static final Path manifestFolder = instancesFolder.resolve(".manifest");

    public static void main(String[] args) throws IOException, URISyntaxException {
        if(!Files.exists(builderFolder))
            FileUtils.forceMkdir(builderFolder.toFile());
        if(!Files.exists(instancesFolder))
            FileUtils.forceMkdir(instancesFolder.toFile());
        if(!Files.exists(manifestFolder))
            FileUtils.forceMkdir(manifestFolder.toFile());

        MinecraftInstance.MinecraftInstanceConfiguration minecraftInstanceConfiguration = MinecraftInstance.prepareNewConfiguration("GlebAndTest", new Semver("1.12.2"), 8, launcherFolder.resolve("minecraft"), userHomeFolder.resolve(".glebTest"), new URI("http://${server.ip}/3/minecraft/GlebAndTest"));
        MinecraftInstance.
    }
}
