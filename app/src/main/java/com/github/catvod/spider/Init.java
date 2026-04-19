package com.github.catvod.spider;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Desktop-compatible Init class.
 * Replaces Android's Handler/Looper dependency with direct execution.
 */
public class Init {

    private static Application app;
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    public static Application context() {
        return app;
    }

    public static void init(Context context) {
        app = (Application) context.getApplicationContext();
        Proxy.init();
    }

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void post(Runnable runnable) {
        runnable.run();
    }

    public static void post(Runnable runnable, int delay) {
        runnable.run();
    }
}
