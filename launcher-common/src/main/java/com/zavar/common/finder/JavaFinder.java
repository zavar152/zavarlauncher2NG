package com.zavar.common.finder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public final class JavaFinder {
    private static final Gson GSON;
    private static final Set<Java> javas = new HashSet<>();

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Java.class, new JavaGsonAdapter());
        GSON = builder.create();
    }

    public record Java(String name, int version, Path home) implements Serializable {
    }

    /**
     * Only Windows support, TODO - Linux and MacOS support
     * @return set of java installed in system
     */
    public static Set<Java> find() {
        if(SystemUtils.IS_OS_WINDOWS) {
            HashMap<String, String> temp = new HashMap<>();
            String[] jdkKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Development Kit");
            for (String key : jdkKeys) {
                String home = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Development Kit\\" + key, "JavaHome");
                temp.put(home, "jdk" + key);
            }
            jdkKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\JDK");
            for (String key : jdkKeys) {
                String home = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\JDK\\" + key, "JavaHome");
                temp.put(home, "jdk" + key);
            }

            jdkKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment");
            for (String key : jdkKeys) {
                String home = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\" + key, "JavaHome");
                temp.put(home, "jre" + key);
            }
            for (String home : temp.keySet()) {
                javas.add(new Java(temp.get(home), getVersion(temp.get(home)), Path.of(home)));
            }
        }
        return javas;
    }

    public static void javasToJson(List<Java> javas, Path path) throws IOException {
        FileWriter fileWriter = new FileWriter(path.toFile());
        GSON.toJson(javas, fileWriter);
        fileWriter.close();
    }

    public static List<Java> jsonToJavas(Path path) throws IOException {
        try {
            return GSON.fromJson(new FileReader(path.toFile()), new TypeToken<List<Java>>(){}.getType());
        } catch (FileNotFoundException e) {
            List<Java> temp = Collections.emptyList();
            javasToJson(temp, path);
            return temp;
        }
    }

    private static int getVersion(String version) {
        version = version.replaceAll("jdk", "").replaceAll("jre", "");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    private static class JavaGsonAdapter extends TypeAdapter<Java> {

        @Override
        public void write(JsonWriter jsonWriter, Java java) throws IOException {
            if(java == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.beginObject();
            jsonWriter.name("name").value(java.name());
            jsonWriter.name("version").value(java.version());
            jsonWriter.name("home").value(java.home().toString());
            jsonWriter.endObject();
            //jsonWriter.flush();
        }

        @Override
        public Java read(JsonReader jsonReader) throws IOException {
            if(jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            jsonReader.beginObject();
            String name = null;
            int version = 0;
            String path = null;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "name" -> name = jsonReader.nextString();
                    case "version" -> version = jsonReader.nextInt();
                    case "home" -> path = jsonReader.nextString();
                }
            }
            jsonReader.endObject();
            assert path != null;
            return new Java(name, version, Path.of(path));
        }
    }
}
