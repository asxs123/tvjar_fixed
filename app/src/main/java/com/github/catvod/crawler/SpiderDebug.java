package com.github.catvod.crawler;

public class SpiderDebug {
    public static void log(Throwable th) {
        System.err.println("[SpiderError] " + th.getMessage());
        th.printStackTrace(System.err);
    }

    public static void log(String msg) {
        System.out.println("[SpiderLog] " + msg);
    }
}
