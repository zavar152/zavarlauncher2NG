package com.zavar.common.minecraft;

import org.update4j.*;
import org.update4j.service.DefaultUpdateHandler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class UpdateTester {

    private static final Path userHomeFolder = Path.of(System.getProperty("user.home"));
    private static final Path launcherFolder = userHomeFolder.resolve("zavarlauncher2");
    private static final Path minecraftFolder = launcherFolder.resolve("minecraft");
    private static final Path tempFolder = launcherFolder.resolve("temp");

    public static void main(String[] args) throws IOException {
        Configuration configurationCreate = Configuration.builder().dynamicProperty("server.ip", "").baseUri("http://${server.ip}/3/minecraft/GlebAndTest")
                .basePath(minecraftFolder).files(FileMetadata.streamDirectory(userHomeFolder.resolve(".glebTest")).peek(r -> r.ignoreBootConflict(true))).build();
        try(Writer out = Files.newBufferedWriter(launcherFolder.resolve("glebAndTest.xml"))) {
            configurationCreate.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Configuration configurationLoad = null;
        try(Reader in = Files.newBufferedReader(launcherFolder.resolve("glebAndTest.xml"))) {
            configurationLoad = Configuration.read(in, Map.of("server.ip", "85.15.70.65"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(configurationLoad);

        UpdateResult result = configurationLoad.update(UpdateOptions.archive(tempFolder.resolve("glebAndTestUpdate.zip")).updateHandler(new CustomUpdateHandler()));//new UpdateHandler("109.167.166.234", 21, "minecraft", "qwerty")));
        if(!Files.exists(tempFolder.resolve("glebAndTestUpdate.zip"))) // optionally check if anything was downloaded, or nothing changed
            return;

        if(result.getException() == null) // success
            Archive.read(tempFolder.resolve("glebAndTestUpdate.zip")).install();
        //System.out.println(configuration.toString());
    }

    private static class CustomUpdateHandler extends DefaultUpdateHandler {

    }

}
