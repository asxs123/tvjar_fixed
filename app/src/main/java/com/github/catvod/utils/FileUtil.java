package com.github.catvod.utils;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

    public static void openFile(File file) {
        System.out.println("[FileUtil] openFile: " + file.getAbsolutePath());
    }

    public static void unzip(File target, File path) {
        try (ZipFile zip = new ZipFile(target.getAbsolutePath())) {
            Enumeration<?> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File out = new File(path, entry.getName());
                if (entry.isDirectory()) out.mkdirs();
                else Path.copy(zip.getInputStream(entry), out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
