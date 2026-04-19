package com.github.catvod.utils;

public class Shell {
    public static void exec(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            // silently ignore on desktop
        }
    }
}
