package android.os;

import java.io.File;

public class Environment {
    public static final String DIRECTORY_DOWNLOADS = "Download";

    public static File getExternalStorageDirectory() {
        return new File(System.getProperty("user.home"));
    }

    public static File getExternalStoragePublicDirectory(String type) {
        File dir = new File(System.getProperty("user.home"), type);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
}
