package com.github.catvod.demo;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.catvod.spider.Init;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Flexible test runner for any CatVod spider.
 *
 * Usage:
 *   ./gradlew run --args="SpiderName"
 *   ./gradlew run --args="SpiderName extend"
 *   ./gradlew run --args="AppYsV2 http://example.com/api.php/app/"
 *
 * Interactive mode (no args): will prompt for input.
 */
public class TestRunner {

    // ====== Configuration ======
    private static String spiderClass = "AppYsV2";
    // private static String extend = "https://api123.adys.app/xgapp.php/v3/";
    private static String extend = "";
    private static boolean filterSwitch = true;
    private static int testTypeIndex = 0;
    private static int testCategoryPage = 1;
    private static int testVodIndex = 0;
    private static int testVodFromIndex = 0;
    private static boolean searchSwitch = false;
    private static String searchKeyword = "测试";

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       CatVod Spider Test Runner          ║");
        System.out.println("║       Compatible with CatVodSpider-larn  ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        // Parse arguments
        if (args.length >= 1) {
            spiderClass = args[0];
        }
        if (args.length >= 2) {
            extend = args[1];
        }

        // Initialize environment
        Context context = new Context();
        Init.init(context);

        // Load spider
        Spider spider = loadSpider(spiderClass);
        if (spider == null) {
            System.out.println("❌ Failed to load spider: " + spiderClass);
            printAvailableSpiders();
            return;
        }

        System.out.println("✅ Loaded spider: " + spiderClass);
        System.out.println("📦 Extend: " + extend);
        System.out.println();

        // If interactive mode
        if (args.length == 0) {
            interactiveMode(spider, context);
            return;
        }

        // Auto test mode
        try {
            runFullTest(spider, context);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static void runFullTest(Spider spider, Context context) throws Exception {
        // Init spider
        System.out.println("═════════ Initializing ═════════");
        spider.init(context, extend);
        System.out.println("✅ Spider initialized\n");

        // homeContent
        System.out.println("═════════ homeContent ═════════");
        String homeResult = spider.homeContent(filterSwitch);
        printJson("homeContent", homeResult);

        // Parse categories from home
        List<String> typeIds = new ArrayList<>();
        String firstTypeId = "";
        try {
            JSONObject home = new JSONObject(homeResult);
            JSONArray classes = home.getJSONArray("class");
            for (int i = 0; i < classes.length(); i++) {
                String tid = classes.getJSONObject(i).getString("type_id");
                String tname = classes.getJSONObject(i).getString("type_name");
                typeIds.add(tid);
                System.out.println("  📂 " + tname + " [" + tid + "]");
            }
            if (!typeIds.isEmpty()) {
                firstTypeId = typeIds.get(Math.min(testTypeIndex, typeIds.size() - 1));
            }
        } catch (Exception e) {
            System.out.println("  ⚠️ Could not parse categories");
        }
        System.out.println();

        // categoryContent
        if (!firstTypeId.isEmpty()) {
            System.out.println("═════════ categoryContent ═════════");
            System.out.println("  Category: " + firstTypeId + ", Page: " + testCategoryPage);
            HashMap<String, String> extendMap = new HashMap<>();
            String catResult = spider.categoryContent(firstTypeId,
                    String.valueOf(testCategoryPage), true, extendMap);
            printJson("categoryContent", catResult);

            List<String> vodIds = new ArrayList<>();
            try {
                JSONObject cat = new JSONObject(catResult);
                JSONArray list = cat.getJSONArray("list");
                for (int i = 0; i < Math.min(list.length(), 10); i++) {
                    JSONObject vod = list.getJSONObject(i);
                    String name = vod.optString("vod_name", "?");
                    String id = vod.optString("vod_id", "?");
                    vodIds.add(id);
                    System.out.println("  🎬 " + name + " [" + id + "]");
                }
            } catch (Exception e) {
                System.out.println("  ⚠️ Could not parse vod list");
            }
            System.out.println();

            // detailContent
            if (!vodIds.isEmpty()) {
                String testVodId = vodIds.get(Math.min(testVodIndex, vodIds.size() - 1));
                System.out.println("═════════ detailContent ═════════");
                System.out.println("  Vod ID: " + testVodId);
                List<String> ids = new ArrayList<>();
                ids.add(testVodId);
                String detailResult = spider.detailContent(ids);
                printJson("detailContent", detailResult);

                // Parse play info
                String playFlag = "";
                String playId = "";
                try {
                    JSONObject detail = new JSONObject(detailResult);
                    JSONArray list = detail.getJSONArray("list");
                    if (list.length() > 0) {
                        JSONObject vod = list.getJSONObject(0);
                        String playFrom = vod.optString("vod_play_from", "");
                        String playUrl = vod.optString("vod_play_url", "");
                        String[] froms = playFrom.split("\\$\\$\\$");
                        String[] urls = playUrl.split("\\$\\$\\$");
                        for (int i = 0; i < froms.length; i++) {
                            System.out.println("  📺 Source: " + froms[i]);
                            if (urls.length > i) {
                                String[] episodes = urls[i].split("#");
                                for (String ep : episodes) {
                                    String[] parts = ep.split("\\$");
                                    System.out.println("    ▶ " + (parts.length > 0 ? parts[0] : ep));
                                }
                            }
                        }
                        int fromIdx = Math.min(testVodFromIndex, froms.length - 1);
                        if (froms.length > 0) playFlag = froms[fromIdx];
                        if (urls.length > fromIdx) {
                            String[] episodes = urls[fromIdx].split("#");
                            if (episodes.length > 0) {
                                String[] parts = episodes[0].split("\\$");
                                playId = parts.length > 1 ? parts[1] : parts[0];
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  ⚠️ Could not parse play info");
                }
                System.out.println();

                // playerContent
                if (!playFlag.isEmpty() && !playId.isEmpty()) {
                    System.out.println("═════════ playerContent ═════════");
                    System.out.println("  Flag: " + playFlag + ", ID: " + playId);
                    List<String> vipFlags = new ArrayList<>();
                    String playerResult = spider.playerContent(playFlag, playId, vipFlags);
                    printJson("playerContent", playerResult);
                    System.out.println();
                }
            }
        }

        // searchContent
        System.out.println("═════════ searchContent ═════════");
        System.out.println("  Keyword: " + searchKeyword);
        String searchResult = spider.searchContent(searchKeyword, true);
        printJson("searchContent", searchResult);
        System.out.println();

        System.out.println("✅ Test completed!");
    }

    private static void interactiveMode(Spider spider, Context context) {
        Scanner scanner = new Scanner(System.in);
        try {
            spider.init(context, extend);
            System.out.println("✅ Spider initialized. Available commands:");
            System.out.println("  home [filter]       - Test homeContent (true/false)");
            System.out.println("  cat <tid> [page]    - Test categoryContent");
            System.out.println("  detail <id>         - Test detailContent");
            System.out.println("  player <flag> <id>  - Test playerContent");
            System.out.println("  search <keyword>    - Test searchContent");
            System.out.println("  spider <name>       - Switch spider");
            System.out.println("  extend <value>      - Change extend");
            System.out.println("  exit                - Quit");
            System.out.println();

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+", 3);
                String cmd = parts[0].toLowerCase();

                try {
                    switch (cmd) {
                        case "home":
                            boolean filter = parts.length > 1 ? Boolean.parseBoolean(parts[1]) : true;
                            printJson("homeContent", spider.homeContent(filter));
                            break;
                        case "cat":
                            if (parts.length < 2) { System.out.println("Usage: cat <tid> [page]"); break; }
                            String tid = parts[1];
                            String pg = parts.length > 2 ? parts[2] : "1";
                            printJson("categoryContent", spider.categoryContent(tid, pg, true, new HashMap<>()));
                            break;
                        case "detail":
                            if (parts.length < 2) { System.out.println("Usage: detail <id>"); break; }
                            List<String> ids = new ArrayList<>();
                            ids.add(parts[1]);
                            printJson("detailContent", spider.detailContent(ids));
                            break;
                        case "player":
                            if (parts.length < 3) { System.out.println("Usage: player <flag> <id>"); break; }
                            printJson("playerContent", spider.playerContent(parts[1], parts[2], new ArrayList<>()));
                            break;
                        case "search":
                            if (parts.length < 2) { System.out.println("Usage: search <keyword>"); break; }
                            String keyword = parts.length > 2 ? parts[1] + " " + parts[2] : parts[1];
                            printJson("searchContent", spider.searchContent(keyword, true));
                            break;
                        case "spider":
                            if (parts.length < 2) { System.out.println("Usage: spider <ClassName>"); break; }
                            Spider newSpider = loadSpider(parts[1]);
                            if (newSpider != null) {
                                spider = newSpider;
                                spiderClass = parts[1];
                                spider.init(context, extend);
                                System.out.println("✅ Switched to: " + spiderClass);
                            } else {
                                System.out.println("❌ Spider not found: " + parts[1]);
                            }
                            break;
                        case "extend":
                            if (parts.length < 2) { System.out.println("Usage: extend <value>"); break; }
                            extend = parts[1];
                            spider.init(context, extend);
                            System.out.println("✅ Extend updated");
                            break;
                        case "exit":
                        case "quit":
                            return;
                        default:
                            System.out.println("Unknown command: " + cmd);
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error: " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("❌ Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Spider loadSpider(String name) {
        // Try direct class name
        String[] packages = {
            "com.github.catvod.spider.",
            "com.github.catvod.spider."
        };
        for (String pkg : packages) {
            try {
                Class<?> cls = Class.forName(pkg + name);
                return (Spider) cls.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static void printAvailableSpiders() {
        System.out.println("\nAvailable spiders:");
        // Scan for spider classes in the classpath
        String[] knownSpiders = {
            "AList", "Anime1", "Bili", "Btt", "Ddrk", "Douban", "Ikanbot",
            "IQIYI", "Jable", "Jianpian", "Kanqiu", "Local", "Market",
            "MGTV", "MQiTV", "PTT", "Proxy", "Push", "Samba", "SixV",
            "SP360", "W55Movie", "Wangfei", "WebDAV", "XtreamCode", "YHDM", "Ysj",
            "AppYsV2", "Alist3", "CaiHong", "Kunyu77", "ShaoEr", "Wogg", "Xinsj", "IkanBot"
        };
        for (String s : knownSpiders) {
            Spider sp = loadSpider(s);
            if (sp != null) {
                System.out.println("  ✅ " + s);
            }
        }
    }

    private static void printJson(String label, String json) {
        System.out.println("  📋 " + label + ":");
        if (json == null || json.isEmpty()) {
            System.out.println("    (empty)");
            return;
        }
        try {
            JSONObject obj = new JSONObject(json);
            System.out.println("    " + obj.toString(2));
        } catch (Exception e) {
            try {
                JSONArray arr = new JSONArray(json);
                System.out.println("    " + arr.toString(2));
            } catch (Exception e2) {
                System.out.println("    " + json.substring(0, Math.min(json.length(), 500)));
            }
        }
    }
}
