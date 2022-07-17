package com.zavar.common.finder;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class JavaFinder {
    private static final Set<Java> javas = new HashSet<>();

    public record Java(String name, int version, Path home) {
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
}
