package com.zavar.zavarlauncher;


import org.update4j.*;
import org.update4j.service.DefaultUpdateHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateTest {
    public static void main(String[] args) throws IOException {
        Configuration configuration = Configuration.builder().dynamicProperty("server.ip", "").baseUri("http://${server.ip}/2/test")
                .basePath("${user.home}/update4j/").files(FileMetadata.streamDirectory("C:\\Users\\yarus\\AppData\\Roaming\\.zak").filter(reference -> !reference.getSource().toString().endsWith(".git")).peek(r -> r.ignoreBootConflict(true))).build();

        try (Writer out = Files.newBufferedWriter(Path.of("C:/config/config2.xml"))) {
            configuration.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println(configuration.toString());

        try (InputStream in = new URL("file:///C:/config/config2.xml").openStream()) {
            configuration = Configuration.read(new InputStreamReader(in), Map.of("server.ip", "109.167.166.234"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream in = new URL("file:///C:/config/config.xml").openStream()) {
            List<String> filesNew = configuration.generateXmlMapper().files.stream().map(fileMapper -> fileMapper.path).collect(Collectors.toList());
            List<String> filesOld = Configuration.read(new InputStreamReader(in), Map.of("server.ip", "109.167.166.234")).generateXmlMapper().files.stream().map(fileMapper -> fileMapper.path).collect(Collectors.toList());
            List<String> differences = filesOld.stream()
                    .filter(element -> !filesNew.contains(element))
                    .collect(Collectors.toList());
            System.out.println(differences);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Become
        Path zip = Paths.get("update.zip"); // you can work with string only too
        UpdateResult result = configuration.update(UpdateOptions.archive(zip).updateHandler(new DefaultUpdateHandler()));//new UpdateHandler("109.167.166.234", 21, "minecraft", "qwerty")));
        if(!Files.exists(zip)) // optionally check if anything was downloaded, or nothing changed
            return;

        if(result.getException() == null) // success
            Archive.read(zip).install();
        //System.out.println(configuration.toString());
    }
}
