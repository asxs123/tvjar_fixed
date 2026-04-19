package com.github.catvod.spider;

import com.github.catvod.crawler.SpiderDebug;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Proxy {

    private static int port = 9978;

    public static Object[] proxy(Map<String, String> params) {
        if ("ck".equals(params.get("do")))
            return new Object[]{200, "text/plain; charset=utf-8",
                    new ByteArrayInputStream("ok".getBytes(StandardCharsets.UTF_8))};
        return null;
    }

    public static void init() {
        SpiderDebug.log("Desktop mode - proxy not available");
    }

    public static int getPort() {
        return port;
    }

    public static String getUrl(String siteKey, String param) {
        return "proxy://do=csp&siteKey=" + siteKey + param;
    }

    public static String getUrl() {
        return getUrl(true);
    }

    public static String getUrl(boolean local) {
        return "http://127.0.0.1:" + port + "/proxy";
    }
}
