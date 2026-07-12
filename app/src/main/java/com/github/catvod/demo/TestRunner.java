package com.github.catvod.demo;

import android.content.Context;  // Android上下文，爬虫初始化可能需要

// 导入catvod框架的爬虫相关类
import com.github.catvod.crawler.Spider;
import com.github.catvod.spider.Init;  // 爬虫初始化类，不直接加载爬虫

// JSON处理相关类
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
// import java.util.Map;  // 导入Java标准库的相关类
import java.util.Scanner;   // 导入Scanner类用于交互式输入
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Flexible test runner for any CatVod spider.
 * 适用于任何 CatVod 爬虫的灵活测试运行器。
 *
 * Usage:
 * 用法：
 *   ./gradlew run --args="SpiderName"
 *   ./gradlew run --args="SpiderName extend"
 *   ./gradlew run --args="AppYsV2 http://example.com/api.php/app/"
 *
 * Interactive mode (no args): will prompt for input.
 * 交互模式（无参数）：将提示输入。
 */
public class TestRunner {

    // ====== Configuration 配置 ======
    private static String spiderClass = "Douban";
    // private static String extend = "https://api123.adys.app/xgapp.php/v3/";
    private static String extend = "";
    private static boolean filterSwitch = true;
    private static int testTypeIndex = 0;
    // 筛选类型的索引（如：0=类型，1=地区，2=语言等，根据实际网站定义）
    private static int filter_type_index = 0;
    // 测试的筛选选项索引（如：地区筛选中的"中国大陆"对应索引1）
    private static int filter_num_index = 1;
    private static int testCategoryPage = 1;
    private static int testVodIndex = 0;
    private static int testVodFromIndex = 0;
    private static boolean searchSwitch = true;
    private static String searchKeyword = "斗";

    public static void main(String[] args) {
        System.out.println("+==========================================+");
        System.out.println("|        CatVod Spider Test Runner         |");
        System.out.println("|       Compatible with CatVodSpider       |");
        System.out.println("+==========================================+");
        System.out.println();

        // Initialize environment
        Context context = new Context();
        Init.init(context);

        // If interactive mode
        if (args.length == 0) {
            interactiveMode(context);
            return;
        } else {
            // Parse arguments
            if (args.length >= 1) {
                spiderClass = args[0];
            }
            if (args.length >= 2) {
                extend = args[1];
            }

            // Load spider
            Spider spider = loadSpider(spiderClass);
            if (spider == null) {
                System.out.println("❌ 加载爬虫失败： " + spiderClass);
                printAvailableSpiders();
                return;
            }

            System.out.println("✅ 加载爬虫成功： " + spiderClass);
            if (extend != null && !extend.isEmpty()) {
                System.out.println("📦 扩展： " + extend);
            }
            System.out.println();
            // Auto test mode
            try {
                runFullTest(spider, context);
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void runFullTest(Spider spider, Context context) throws Exception {
        // Init spider
        System.out.println("================== 初始化 ===================");
        spider.init(context, extend);
        System.out.println("✅ 爬虫初始化成功\n");

        // homeContent
        System.out.println("========= 测试首页内容(homeContent) =========");
        String homeResult = spider.homeContent(filterSwitch);
        // printJson("homeContent", homeResult);

        // Parse categories from home
        List<String> typeIds = new ArrayList<>();
        String firstTypeId = "";
        HashMap<String, String> extendMap = new HashMap<>();
        if (homeResult == null || homeResult.isEmpty()) {
            System.out.println("    homeContent返回为空，测试终止");
        } else {
            try {
                JSONObject home = new JSONObject(new String(homeResult));
                JSONArray classes = home.getJSONArray("class");
                try {
                    for (int i = 0; i < classes.length(); i++) {
                        String tid = classes.getJSONObject(i).getString("type_id");
                        String tname = classes.getJSONObject(i).getString("type_name");
                        typeIds.add(tid);
                        System.out.println("  📂 " + tname + " [" + tid + "]");
                        if (filterSwitch) {
                            try {
                                JSONArray filters = home.getJSONObject("filters")
                                        .getJSONArray(tid);
                                
                                for (int j = 0; j < filters.length(); j++) {
                                    // 输出筛选条件名称（如"地区"、"年份"）
                                    System.out.print("   " + filters.getJSONObject(j).getString("name") + " [");
                                    // 获取筛选选项列表（如地区筛选下的"中国大陆"、"美国"）
                                    JSONArray value = filters.getJSONObject(j).getJSONArray("value");
                                    String key = filters.getJSONObject(j).getString("key");  // 筛选参数名（如"area"）
                                    
                                    // 存储该筛选条件下的所有选项
                                    // ArrayList<HashMap<String, String>> leixing = new ArrayList<>();
                                    for (int k = 0; k < value.length(); k++) {
                                        // HashMap<String, String> hashMap = new HashMap<>();
                                        // 输出筛选选项名称（如"中国大陆"）
                                        System.out.print(value.getJSONObject(k).getString("n") + "  ");
                                        // 存储选项参数（key=筛选参数名，value=选项值）
                                        // hashMap.put(key, value.getJSONObject(k).getString("v"));
                                        // 如果是目标测试的筛选条件，记录选中的选项
                                        if (testTypeIndex == i && filter_type_index == j && filter_num_index == k) {
                                            extendMap.put(key, value.getJSONObject(k).getString("v"));
                                        } 
                                        // leixing.add(hashMap);
                                    }
                                    System.out.println("]\r\n");
                                }
                            } catch (Exception e) {
                                System.out.println("  ⚠️ 未读取到[" + tid + "]的筛选信息\r\n");
                            }
                        } else {
                            if (i == classes.length() - 1) {
                                System.out.println("\n筛选开关已关闭!!!");
                            }
                        }
                    }
                    if (!typeIds.isEmpty()) {
                        firstTypeId = typeIds.get(Math.min(testTypeIndex, typeIds.size() - 1));
                    }
                } catch (Exception e) {
                    System.out.println("  ⚠️ 未读取到type_name或者type_id，测试终止!!!");
                }
            } catch (Exception e) {
                System.out.println("  ⚠️ 未读取到分类列表（class），测试终止!!!");
            }
        }
        System.out.println();

        // categoryContent
        if (!firstTypeId.isEmpty()) {
            System.out.println("======= 测试分类内容(categoryContent) =======");
            System.out.println("  测试的分类为: " + firstTypeId + ", 筛选: " + extendMap + ", 页码: " + testCategoryPage);
            String catResult = spider.categoryContent(firstTypeId,
                    String.valueOf(testCategoryPage), true, extendMap);
            // printJson("categoryContent", catResult);

            List<String> vodIds = new ArrayList<>();
            if (catResult == null || catResult.isEmpty()) {
                System.out.println("    categoryContent返回为空，测试终止");
            } else {
                try {
                    JSONObject cat = new JSONObject(new String(catResult));
                    try {
                        int page = cat.getInt("page");
                        System.out.print("  当前页 ");
                        System.out.print("page:" + page);
                    } catch (Exception e) {
                        // System.out.print("  当前页 ");
                        System.out.println("没有解析到page，请检查！！！");
                    }
                    try {
                        int pagecount = cat.getInt("pagecount");
                        System.out.print(" 总共");
                        System.out.print("pagecount:" + pagecount + "页");
                    } catch (Exception e) {
                        // System.out.print(" 总共几页 ");
                        System.out.println("没有解析到pagecount，请检查！！！");
                    }
                    try {
                        int limit = cat.getInt("limit");
                        System.out.print(" 每页");
                        System.out.print("limit:" + limit + "条数据");
                    } catch (Exception e) {
                        // System.out.print(" 每页几条数据 ");
                        System.out.println("没有解析到limit，请检查！！！");
                    }
                    try {
                        int total = cat.getInt("total");
                        System.out.print("  总共");
                        System.out.println("total:" + total + "条数据\r\n");
                    } catch (Exception e) {
                        // System.out.print("  总共多少条数据 ");
                        System.out.println("没有解析到total，请检查！！！ \r\n");
                    }

                    JSONArray list = cat.getJSONArray("list");
                    for (int i = 0; i < Math.min(list.length(), 10); i++) {
                        JSONObject vod = list.getJSONObject(i);
                        String name = vod.optString("vod_name", "?");
                        String id = vod.optString("vod_id", "?");
                        String pic = vod.optString("vod_pic", "?");
                        String remarks = vod.optString("vod_remarks", "?");
                        vodIds.add(id);
                        System.out.println("  🎬 " + name + " [" + id + "]");
                        System.out.println("  🖼️  [" + pic + "] remarks [" + remarks + "]\r\n");
                    }
                } catch (Exception e) {
                    System.out.println("  ⚠️ 未解析到视频列表（list），请检查！！！");
                }
            }
            System.out.println();

            // detailContent
            if (!vodIds.isEmpty()) {
                String testVodId = vodIds.get(Math.min(testVodIndex, vodIds.size() - 1));
                System.out.println("======== 测试视频详情(detailContent) ========");
                System.out.println("  测试的链接为: " + testVodId);
                List<String> ids = new ArrayList<>();
                ids.add(testVodId);
                String detailResult = spider.detailContent(ids);
                // printJson("detailContent", detailResult);

                // Parse play info
                String playFlag = "";
                String playId = "";
                if (detailResult == null || detailResult.isEmpty()) {
                    System.out.println("    detailContent返回为空，测试终止");
                } else {
                    try {
                        JSONObject detail = new JSONObject(new String(detailResult));
                        JSONArray list = detail.getJSONArray("list");
                        if (list.length() > 0) {
                            JSONObject vod = list.getJSONObject(0);
                            System.out.print("  视频ID(vod_id): ");
                            System.out.println(vod.optString("vod_id", "未定义"));
                            System.out.print("  视频名称(vod_name): ");
                            System.out.println(vod.optString("vod_name", "未定义"));
                            System.out.print("  视频封面(vod_pic): ");
                            System.out.println(vod.optString("vod_pic", "未定义"));
                            System.out.print("  类型(type_name): ");
                            System.out.println(vod.optString("type_name", "未定义"));
                            System.out.print("  年份(vod_year): ");
                            System.out.println(vod.optString("vod_year", "未定义"));
                            System.out.print("  地区(vod_area): ");
                            System.out.println(vod.optString("vod_area", "未定义"));
                            System.out.print("  提示信息(vod_remarks): ");
                            System.out.println(vod.optString("vod_remarks", "未定义"));
                            System.out.print("  主演(vod_actor): ");
                            System.out.println(vod.optString("vod_actor", "未定义"));
                            System.out.print("  导演(vod_director): ");
                            System.out.println(vod.optString("vod_director", "未定义"));
                            System.out.print("  简介(vod_content): ");
                            System.out.println(vod.optString("vod_content", "未定义"));
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
                                        System.out.println("    ▶ " + (parts.length > 0 ? parts[0] : ep) + (parts.length > 0 ? " [" + parts[1] + "]" : ""));
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
                        System.out.println("  ⚠️ 未解析到详情列表（list）,请检查!!!");
                    }
                }
                System.out.println();

                // playerContent
                if (!playFlag.isEmpty() && !playId.isEmpty()) {
                    System.out.println("======== 测试播放内容(playerContent) ========");
                    System.out.println("  测试源Flag: " + playFlag + ", 测试ID: " + playId);
                    // List<String> vipFlags = new ArrayList<>();
                    String playerResult = spider.playerContent(playFlag, playId, null);
                    // printJson("playerContent", playerResult);
                    if (playerResult == null || playerResult.isEmpty()) {
                        System.out.println("    (playerContent返回为空，测试终止)");
                    } else {
                        JSONObject player = new JSONObject(new String(playerResult));
                        System.out.print("  请求头信息: ");
                        System.out.println(player.optString("header", "未定义请求头"));
                        try {
                            System.out.print("  播放方式(parse): ");
                            if (player.getInt("parse") == 0 || player.getInt("jx") == 0) {
                                System.out.println("直接播放");
                            } else if (player.getInt("parse") == 1 || player.getInt("jx") == 1) {
                                System.out.println("嗅探播放");
                            } else {
                                System.out.println("播放方式出错");
                            }
                        } catch (Exception e) {
                            System.out.println("  播放方式(parse): 未定义播放方式");
                        }
                        System.out.print("  解析器链接(playurl): ");
                        System.out.println(player.optString("playurl", "未获取到解析器链接"));
                        System.out.print("  播放链接(url): ");
                        System.out.println(player.optString("url", ""));
                    }
                    System.out.println();
                }
            }
        }

        // searchContent
        if (searchSwitch) {
            System.out.println("======== 测试搜索功能(searchContent) ========");
            System.out.println("  测试的搜索关键字Keyword: " + searchKeyword);
            String searchResult = spider.searchContent(searchKeyword, true);
            // printJson("searchContent", searchResult);
            if (searchResult == null || searchResult.isEmpty()) {
                System.out.println("    searchContent返回为空，测试终止");
            } else {
                JSONObject result = new JSONObject(new String(searchResult));
                try {
                    JSONArray list = result.getJSONArray("list");
                    for (int i = 0; i < Math.min(list.length(), 10); i++) {
                        try {
                            // 获取视频ID
                            String vod_id = list.getJSONObject(i).getString("vod_id");
                            try {
                                // 获取视频名称并输出
                                String vod_name = list.getJSONObject(i).getString("vod_name");
                                System.out.println("  🎬 " + vod_name + " [" + vod_id + "]");
                                System.out.println("  🖼️  [" + list.getJSONObject(i).get("vod_pic") + "] remarks ["
                                    + list.getJSONObject(i).get("vod_remarks") + "]\r\n");
                            } catch (Exception e) {
                                System.out.println("没有解析到vod_name（视频名称）");
                            }
                        } catch (Exception e) {
                            System.out.println("没有解析到vod_id（视频ID）");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("没有解析到list（搜索结果列表）");
                }
            }
            System.out.println();
        }

        System.out.println("✅ 测试完成!");
    }

    private static void interactiveMode(Context context) {
        // Scanner scanner = new Scanner(System.in);
        try (Scanner scanner = new Scanner(System.in)) {
            // 先让用户输入爬虫类名和扩展参数
            System.out.println("===== 爬虫交互启动配置 =====");
            System.out.println("请输入爬虫完整类名（spiderClass）：");
            // System.out.flush(); // 强制刷新输出，防止打印卡顿错乱
            spiderClass = scanner.nextLine().trim();

            // 拦截空爬虫名
            if (spiderClass.isEmpty()) {
                System.out.println("❌ 爬虫类名不能为空！");
                printAvailableSpiders();
                return;
            }

            System.out.println("请输入扩展参数 extend（无则直接回车）：");
            // System.out.print("> ");
            extend = scanner.nextLine().trim();

            // 加载指定爬虫
            Spider spider = loadSpider(spiderClass);
            if (spider == null) {
                System.out.println("❌ 加载爬虫失败： " + spiderClass);
                printAvailableSpiders();
                return;
            }

            System.out.println("✅ 加载爬虫成功： " + spiderClass);
            if (extend != null && !extend.isEmpty()) {
                System.out.println("📦 扩展： " + extend);
            }

            spider.init(context, extend);
            System.out.println("✅ Spider initialized. Available commands:");
            System.out.println("  home [filter]             - 测试首页，filter 可选 true/false");
            System.out.println("  cat <tid> [page]          - 测试分类列表，tid=分类ID, page=页码");
            System.out.println("  detail <id>               - 测试视频详情，id=视频ID");
            System.out.println("  player <flag> <id>        - 测试播放地址，flag=播放源名, id=播放ID");
            System.out.println("  search <keyword>          - 测试搜索，keyword=搜索关键字");
            System.out.println("  spider <name> [extend]    - 切换到另一个爬虫, name=爬虫名, extend=扩展参数");
            System.out.println("  extend <value>            - 修改 extend 配置并重新初始化");
            System.out.println("  exit                      - 退出");
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
                            // if (parts[2] != null && !parts[2].isEmpty()) {
                            //     printJson("searchContent", spider.searchContent(parts[1], true, parts[2]));
                            //     break;
                            // }
                            printJson("searchContent", spider.searchContent(keyword, true));
                            break;
                        case "spider":
                            if (parts.length < 2) { System.out.println("Usage: spider <ClassName>"); break; }
                            if (parts[2] != null && !parts[2].isEmpty()) {
                                extend = parts[2];
                            }
                            Spider newSpider = loadSpider(parts[1]);
                            if (newSpider != null) {
                                spider = newSpider;
                                spiderClass = parts[1];
                                spider.init(context, extend);
                                System.out.print("✅ Switched to: " + spiderClass);
                                if (extend != null && !extend.isEmpty()) {
                                    System.out.println("  📦 扩展： " + extend);
                                }
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
        // System.out.println("\nAvailable spiders:");

        List<String> spiderNames = findSpiderClasses("com.github.catvod.spider");

        if (spiderNames.isEmpty()) {
            System.out.println("  ⚠️ 未扫描到任何爬虫类");
            return;
        }

        System.out.println("\n可用的爬虫类:");
        for (String name : spiderNames) {
            Spider sp = loadSpider(name);
            if (sp != null) {
                System.out.println("  ✅ " + name);
            }
        }
    }

    /**
     * 扫描指定包下所有类名
     */
    private static List<String> findSpiderClasses(String packageName) {
        List<String> classNames = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');

        try {
            // 获取当前线程的类加载器能加载的所有资源
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // 普通目录中的 class 文件
                    scanDirectory(new File(resource.toURI()), classNames);
                } else if ("jar".equals(protocol)) {
                    // JAR 包中的 class 文件
                    scanJarPackage(resource, packagePath, classNames);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 按名称排序，方便查看
        Collections.sort(classNames);
        return classNames;
    }

    /**
     * 扫描目录下的 .class 文件
     */
    private static void scanDirectory(File directory, List<String> result) {
        if (!directory.exists() || !directory.isDirectory()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(".class")) {
                // 去掉 .class 后缀，只取类名
                String className = file.getName().replace(".class", "");
                // 排除内部类（如 SomeSpider$1.class）
                if (!className.contains("$")) {
                    result.add(className);
                }
            }
        }
    }

    /**
     * 扫描 JAR 包中指定包路径下的类
     */
    private static void scanJarPackage(URL jarUrl, String packagePath, List<String> result) {
        try {
            JarURLConnection jarConn = (JarURLConnection) jarUrl.openConnection();
            try (JarFile jarFile = jarConn.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.startsWith(packagePath + "/")
                            && entryName.endsWith(".class")
                            && !entryName.contains("$")) {

                        // 取文件名部分
                        String className = entryName.substring(entryName.lastIndexOf('/') + 1);
                        className = className.replace(".class", "");
                        result.add(className);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void printJson(String label, String json) {
        System.out.println("  📋 " + label + ":");
        if (json == null || json.isEmpty()) {
            System.out.println("    (返回为空，测试终止)");
            return;
        }
        try {
            JSONObject obj = new JSONObject(new String(json));
            System.out.println("    " + obj.toString(2));
        } catch (Exception e) {
            try {
                JSONArray arr = new JSONArray(new String(json));
                System.out.println("    " + arr.toString(2));
            } catch (Exception e2) {
                System.out.println("    " + json.substring(0, Math.min(json.length(), 500)));
            }
        }
    }
}
