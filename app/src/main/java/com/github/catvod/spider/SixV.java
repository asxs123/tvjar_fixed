package com.github.catvod.spider;

// import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
// import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;


/**
 * @author zhixc
 * 6V电影网（新版页面）
 */
public class SixV extends Spider {

    // 可用域名：
    //   https://www.6vdy.org
    //   https://www.66s6.cc
    //   https://www.xb6v.com
    private final String siteUrl = "https://www.6vdy.org";
    private String nextSearchUrlPrefix;
    // private String nextSearchUrlSuffix;

    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    private String req(String url, Map<String, String> header) {
        return OkHttp.string(url, header);
    }

    // private Response req(Request request) throws Exception {
    //     return okClient().newCall(request).execute();
    // }

    // private String req(Response response) throws Exception {
    //     if (!response.isSuccessful()) return "";
    //     String content = response.body().string();
    //     response.close();
    //     return content;
    // }

    // private OkHttpClient okClient() {
    //     return OkHttp.client();
    // }

    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        header.put("Referer", siteUrl + "/");
        return header;
    }

    private Map<String, String> getDetailHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        return header;
    }

    private Map<String, String> getSearchHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        return header;
    }

    private String find(Pattern pattern, String html) {
        Matcher m = pattern.matcher(html);
        // System.out.println(m.find() ? m.group(1).trim() : "");
        return m.find() ? m.group(1).trim() : "";
    }

    private JSONArray parseVodListFromDoc(String html) throws Exception {
        JSONArray videos = new JSONArray();
        Elements uls = Jsoup.parse(html).select("#post_container li.post");
        for (Element ul : uls) {
            Elements item = ul.select("[class=zoom]");
            String vodId = item.attr("href");
            String name = removeHtmlTag(item.attr("title"));
            String pic = item.select("img").attr("src");
            String remark = ul.select(".info_date").text();

            JSONObject vod = new JSONObject();
            vod.put("vod_id", vodId);
            vod.put("vod_name", name);
            vod.put("vod_pic", pic);
            vod.put("vod_remarks", remark);
            videos.put(vod);
        }
        return videos;
    }

    private String getActor(String html) {
        String actor = find(Pattern.compile("◎演　　员:?　(.*?)</p>"), html);
        if ("".equals(actor)) actor = find(Pattern.compile("◎主　　演:?　(.*?)</p>"), html);
        // System.out.print(actor);
        return clean(actor);
    }

    private String clean(String str) {
        return str.replaceAll("&amp;middot;", "・")
                .replaceAll("&middot;", "・")
                .replaceAll("&amp;", "")
                .replaceAll("&nbsp;", "")
                .replaceAll("<br>", "")
                .replaceAll("　　　　　　　", " / ")
                .replaceAll("　　　　　", " / ")
                .replaceAll("　　　　 　", " / ");
    }

    private String getDirector(String html) {
        return clean(find(Pattern.compile("◎导　　演:?　(.*?)<br>"), html));
    }

    private String getDescription(String html) {
        return clean(find(Pattern.compile("◎简　　介:?(.*?)<hr", Pattern.DOTALL), html)).replaceAll("\n", "").replaceAll("　", "").replaceAll("hellip;", "").replaceAll("ldquo;", "【").replaceAll("rdquo;", "】");
    }

    private String removeHtmlTag(String str) {
        return str.replaceAll("</?[^>]+>", "");
    }

    // private boolean isMovie(String vodId) {
    //     return !(vodId.startsWith("/donghuapian") || vodId.startsWith("/dianshiju") || vodId.startsWith("/ZongYi"));
    // }

    // private Map<String, String> parsePlayMapFromDoc(Elements sourceList1) {
    //     Map<String, String> playMap = new LinkedHashMap<>();
    //     Elements sourceList = sourceList1.select("#post_content");
    //     String vod_play_from = "磁力";
    //     int i = 0, j = 0;
    //     for (Element source : sourceList) {
    //         i++;
    //         Elements aList = source.select("table a");
    //         List<String> vodItems = new ArrayList<>();
    //         for (Element a : aList) {
    //             String episodeUrl = a.attr("href");
    //             String episodeName = a.text();
    //             if (!episodeUrl.startsWith("magnet")) continue;
    //             vodItems.add(episodeName + "$" + episodeUrl);
    //         }
    //         if (vodItems.size() > 0) playMap.put(vod_play_from + i, TextUtils.join("#", vodItems));
    //     }
    //     Elements sourceList2 = sourceList1.select(".widget.box.row");
    //     System.out.println(sourceList2);
    //     for (Element source : sourceList2) {
    //         Elements aList = source.select("a");
    //         System.out.println(aList);
    //         List<String> vodItems = new ArrayList<>();
    //         for (Element a : aList) {
    //             String episodeUrl = a.attr("href");
    //             System.out.println(episodeUrl);
    //             String episodeName = a.text();
    //             vodItems.add(episodeName + "$" + siteUrl + episodeUrl);
    //         }
    //         if (vodItems.size() > 0) {
    //             j++;
    //             playMap.put("在线" + j, TextUtils.join("#", vodItems));
    //         }
    //     }

    //     return playMap;
    // }

    private Map<String, String> parsePlayMapForMovieFromDoc(Elements sourceList1) {
        Map<String, String> playMap = new LinkedHashMap<>();
        Elements sourceList = sourceList1.select("#post_content");
        int i = 0, j = 0;
        for (Element source : sourceList) {
            i++;
            Elements aList = source.select("table a");
            List<String> vodItems = new ArrayList<>();
            for (Element a : aList) {
                // Element a = aList.get(i);
                String episodeUrl = a.attr("href");
                String episodeName = a.text();
                if (!episodeUrl.startsWith("magnet")) continue;
                vodItems.add(episodeName + "$" + episodeUrl);
            }
            if (vodItems.size() > 0) playMap.put("磁力" + i, TextUtils.join("#", vodItems));
        }
        Elements sourceList2 = sourceList1.select(".widget.box.row");
        for (Element source : sourceList2) {
            Elements aList = source.select("a");
            List<String> vodItems = new ArrayList<>();
            for (Element a : aList) {
                String episodeUrl = a.attr("href");
                System.out.println(episodeUrl);
                String episodeName = a.text();
                vodItems.add(episodeName + "$" + siteUrl + episodeUrl);
            }
            if (vodItems.size() > 0) {
                j++;
                playMap.put("在线" + j, TextUtils.join("#", vodItems));
            }
        }
        return playMap;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        JSONArray classes = new JSONArray();
        List<String> typeIds = Arrays.asList("xijupian", "dongzuopian", "aiqingpian", "kehuanpian", "kongbupian", "juqingpian", "zhanzhengpian", "jilupian", "donghuapian", "dianshiju/guoju", "dianshiju/duanju", "dianshiju/rihanju", "dianshiju/oumeiju", "ZongYi");
        List<String> typeNames = Arrays.asList("喜剧片", "动作片", "爱情片", "科幻片", "恐怖片", "剧情片", "战争片", "纪录片", "动画片", "国剧", "短剧", "日韩剧", "欧美剧", "综艺");
        for (int i = 0; i < typeIds.size(); i++) {
            JSONObject c = new JSONObject();
            c.put("type_id", typeIds.get(i));
            c.put("type_name", typeNames.get(i));
            classes.put(c);
        }
        JSONObject result = new JSONObject();
        result.put("class", classes);
        return result.toString();
    }

    @Override
    public String homeVideoContent() throws Exception {
        String html = req(siteUrl, getHeader());
        JSONArray videos = parseVodListFromDoc(html);
        JSONObject result = new JSONObject();
        result.put("list", videos);
        return result.toString();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String cateUrl = siteUrl + "/" + tid;
        if (!pg.equals("1")) cateUrl += "/index_" + pg + ".html";
        String html = req(cateUrl, getHeader());
        JSONArray videos = parseVodListFromDoc(html);
        int page = Integer.parseInt(pg), count = 999, limit = videos.length(), total = Integer.MAX_VALUE;
        JSONObject result = new JSONObject();
        result.put("page", page);
        result.put("pagecount", count);
        result.put("limit", limit);
        result.put("total", total);
        result.put("list", videos);
        return result.toString();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String vodId = ids.get(0);
        String detailUrl = siteUrl + vodId;
        String html = req(detailUrl, getDetailHeader());
        Document doc = Jsoup.parse(html);
        Elements source = doc.select(".context");
        // Map<String, String> playMap = isMovie(vodId) ? parsePlayMapForMovieFromDoc(source) : parsePlayMapFromDoc(source);
        Map<String, String> playMap = parsePlayMapForMovieFromDoc(source);

        String partHTML = doc.select(".context").html();
        // System.out.println(partHTML);
        String name = doc.select(".article_container > h1").text();
        String pic = doc.select("#post_content img").attr("src");
        String typeName = find(Pattern.compile("◎类　　别:?　(.*?)<br>"), partHTML);
        String year = find(Pattern.compile("◎年　　代:?　(.*?)<br>"), partHTML);
        String area = find(Pattern.compile("◎产　　地:?　(.*?)<br>"), partHTML);
        String remark = "上映日期：" + find(Pattern.compile("◎上映日期:?　(.*?)<br>"), partHTML);
        String actor = getActor(partHTML);
        String director = getDirector(partHTML);
        String description = removeHtmlTag(getDescription(partHTML));

        // 由于部分信息过长，故进行一些调整，将年份、地区等信息放到 类别、备注里面
        // typeName += " 地区:" + area;
        // area = "";
        // typeName += " 年份:" + year;
        // remark += " 年份:" + year;
        // year = "";

        JSONObject vod = new JSONObject();
        vod.put("vod_id", ids.get(0));
        vod.put("vod_name", name);
        vod.put("vod_pic", pic);
        vod.put("type_name", typeName);
        vod.put("vod_year", year);
        vod.put("vod_area", area);
        vod.put("vod_remarks", remark);
        vod.put("vod_actor", actor);
        vod.put("vod_director", director);
        vod.put("vod_content", description);
        if (playMap.size() > 0) {
            vod.put("vod_play_from", TextUtils.join("$$$", playMap.keySet()));
            vod.put("vod_play_url", TextUtils.join("$$$", playMap.values()));
        }
        JSONArray jsonArray = new JSONArray().put(vod);
        JSONObject result = new JSONObject().put("list", jsonArray);
        return result.toString();
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, quick, "1");
    }

    @Override
    // public String searchContent(String key, boolean quick, String pg) throws Exception {
    //     String searchUrl = siteUrl + "/e/search/11index.php";
    //     String html = "";
    //     if ("1".equals(pg)) {
    //         Map<String, String> headers = new HashMap<>();
    //         // 必带校验核心
    //         long ts = System.currentTimeMillis() / 1000;
    //         headers.put("Cookie", "tinmklastsearchtime=1783437419");
    //         headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0");
    //         headers.put("Referer", "https://www.6vdy.org/");
    //         headers.put("Origin", "https://www.6vdy.org");
    //         headers.put("Host", "www.6vdy.org");
    //         headers.put("Content-Type", "application/x-www-form-urlencoded");
    //         // Sec-Fetch整套安全指纹（建议保留，防拦截）
    //         headers.put("Sec-Fetch-Dest", "document");
    //         headers.put("Sec-Fetch-Mode", "navigate");
    //         headers.put("Sec-Fetch-Site", "same-origin");
    //         headers.put("Sec-Fetch-User", "?1");
    //         headers.put("Upgrade-Insecure-Requests", "1");
    //         // 可选（提升仿真，删了也能跑）
    //         headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
    //         headers.put("Accept-Language", "zh-CN,zh;q=0.9");
    //         headers.put("Accept-Encoding", "gzip, deflate, br");
    //         // String requestBody = "show=title&tempid=1&tbname=article&mid=1&dopost=search&submit=&keyboard=" + URLEncoder.encode(key, "GBK");
    //         Map<String,String> form = new HashMap<>();
    //         form.put("keyboard", "斗");
    //         form.put("submit","");
    //         form.put("dopost","search");
    //         form.put("mid","1");
    //         form.put("tbname","article");
    //         form.put("tempid","1");
    //         form.put("show","title");
    //         System.out.println("");
    //         System.out.println(form);
    //         html = searchPage(searchUrl, form, headers, siteUrl);
    //         System.out.println(html);
    //         // try {
    //         //     String redirectUrl = OkHttp.getLocation(searchUrl, headers);
    //         //     if (redirectUrl != null && redirectUrl.contains("?searchid=")) {
    //         //         String[] split = redirectUrl.split("\\?searchid=");
    //         //         nextSearchUrlPrefix = split[0].replace("index.php", "index.php?page=");
    //         //         nextSearchUrlSuffix = "&searchid=" + split[1];
    //         //     }
    //         // } catch (Exception e) {
    //         //     // 如果获取重定向URL失败，使用默认的URL格式
    //         //     nextSearchUrlPrefix = searchUrl + "?page=";
    //         //     nextSearchUrlSuffix = "&tempid=1&tbname=article&keyboard=" + URLEncoder.encode(key, "GBK") + "&show=title%2Csmalltext";
    //         // }
    //     } else {
    //         int page = Integer.parseInt(pg) - 1;
    //         searchUrl = nextSearchUrlPrefix + page + nextSearchUrlSuffix;
    //         html = req(searchUrl, getSearchHeader());
    //     }
    //     JSONArray videos = parseVodListFromDoc(html);
    //     JSONObject result = new JSONObject();
    //     result.put("list", videos);
    //     return result.toString();
    // }
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        String searchUrl = siteUrl + "/e/search/11index.php";
        String html = "";

        if ("1".equals(pg)) {
            // Step 1: POST form-data, disable redirect to capture Set-Cookie + Location
            OkHttpClient noRedirectClient = newClientBuilder().build();

            FormBody formBody = new FormBody.Builder()
                    .add("show", "title")
                    .add("tempid", "1")
                    .add("tbname", "article")
                    .add("mid", "1")
                    .add("dopost", "search")
                    .add("submit", "")
                    .add("keyboard", key)
                    .build();

            Request postReq = new Request.Builder()
                    .url(searchUrl)
                    .post(formBody)
                    .header("User-Agent", userAgent)
                    .header("Referer", siteUrl + "/")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            Response step1 = noRedirectClient.newCall(postReq).execute();

            String location = step1.header("location");
            if (location == null) location = step1.header("Location");

            String cookie = extractCookie(step1);

            // handle relative URL
            if (location != null && !location.startsWith("http")) {
                location = siteUrl + "/e/search/" + location;
            }

            step1.close();

            // Step 2: GET with cookie
            if (location != null) {
                Request getReq = new Request.Builder()
                        .url(location)
                        .header("User-Agent", userAgent)
                        .header("Cookie", cookie)
                        .build();
                Response step2 = noRedirectClient.newCall(getReq).execute();
                if (step2.isSuccessful() && step2.body() != null) {
                    html = step2.body().string();
                }
                step2.close();

                // save redirect info for next page
                nextSearchUrlPrefix = location;
            }
        } else {
            // pagination: append page param to saved URL
            String pageUrl = nextSearchUrlPrefix;
            if (pageUrl != null) {
                // String separator = pageUrl.contains("?") ? "&" : "?";
                // pageUrl = pageUrl + separator + "page=" + pg;
                int queryIndex = pageUrl.indexOf('?');
                String path;
                String oldQuery;
                if (queryIndex == -1) {
                    path = pageUrl;
                    oldQuery = null;
                } else {
                    path = pageUrl.substring(0, queryIndex);
                    oldQuery = pageUrl.substring(queryIndex + 1);
                }

                // 存储过滤后不包含page的旧参数
                StringBuilder newQuerySb = new StringBuilder();
                if (oldQuery != null && oldQuery.length() > 0) {
                    String[] params = oldQuery.split("&");
                    boolean first = true;
                    for (String param : params) {
                        // 跳过所有page开头的参数
                        if (param.startsWith("page=")) {
                            continue;
                        }
                        if (!first) {
                            newQuerySb.append("&");
                        }
                        newQuerySb.append(param);
                        first = false;
                    }
                }

                // 拼接新page参数
                String pageKV;
                int page = Integer.parseInt(pg) - 1;
                try {
                    pageKV = "page=" + URLEncoder.encode(String.valueOf(page), StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    pageKV = "page=" + page;
                }

                // 组装最终url
                String finalQuery;
                if (newQuerySb.length() == 0) {
                    finalQuery = pageKV;
                } else {
                    finalQuery = newQuerySb.append("&").append(pageKV).toString();
                }
                pageUrl = path + "?" + finalQuery;
            } else {
                pageUrl = searchUrl;
            }
            html = req(pageUrl, getSearchHeader());
        }

        JSONArray videos = parseVodListFromDoc(html);
        JSONObject result = new JSONObject();
        result.put("list", videos);
        return result.toString();
    }

    private String extractCookie(Response response) {
        StringBuilder sb = new StringBuilder();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            String name = headers.name(i);
            if ("set-cookie".equalsIgnoreCase(name)) {
                String val = headers.value(i);
                int semi = val.indexOf(';');
                if (sb.length() > 0) sb.append("; ");
                sb.append(val, 0, semi > 0 ? semi : val.length());
            }
        }
        return sb.toString();
    }

    private OkHttpClient.Builder newClientBuilder() {
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };
        SSLContext ssl;
        try {
            ssl = SSLContext.getInstance("TLS");
            ssl.init(null, trustAll, new SecureRandom());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new OkHttpClient.Builder()
                .sslSocketFactory(ssl.getSocketFactory(), (X509TrustManager) trustAll[0])
                .hostnameVerifier((hostname, session) -> true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false);
    }
    

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSONObject result = new JSONObject();
        result.put("parse", 0);
        // result.put("header", "");
        // result.put("playUrl", "");
        if (!id.startsWith("magnet")) {
            Document mainDoc = Jsoup.connect(id)
                                .userAgent(userAgent)
                                .timeout(15000)
                                .ignoreContentType(true) // 防止部分接口非html返回报错
                                .get();
            String mainHtml = "", iframe = "";
            if (!mainDoc.select("iframe[src]").isEmpty()) {
                System.out.println("iframeSrc");
                iframe = mainDoc.selectFirst("iframe").absUrl("src");
                // System.out.println(iframe);
                if (iframe != null) {
                    // String iframeSrc = iframe.absUrl("src"); // absUrl自动补全相对路径为完整http
                    // System.out.println(iframe);
                    Document iframeDoc = Jsoup.connect(iframe)
                                            .userAgent(userAgent)
                                            .timeout(15000)
                                            .ignoreContentType(true) // 防止部分接口非html返回报错
                                            .get();
                    mainHtml = iframeDoc.html();
                    // System.out.println(mainHtml);
                }
            } else {
                mainHtml = mainDoc.html();
            }
            Matcher aliMatcher = Pattern.compile("(?:\\\")?source(?:\\\")?\\s*[:=]\\s*[\\\"'](https?://.*?\\.(m3u8|mp4|flv)(?:[^\\s\"']*|$))[\\\"']").matcher(mainHtml);
            if (aliMatcher.find()) {
                id = aliMatcher.group(1);
            } else {
                // 通用兜底匹配任意明文视频链接（兼容其他播放器）
                System.out.println("兜底");
                Matcher commonMatcher = Pattern.compile("https?://[^\"']+?\\.(m3u8|mp4|flv|ts)(?:[^\\s\"']*|$)").matcher(mainHtml);
                if (commonMatcher.find()) {
                    id = commonMatcher.group();
                } else {
                    System.out.println("兜底2");
                    Matcher commonMatcher2 = Pattern.compile("(?:https?://[^\"'\\s]*)?(/[^\"'\\s]*\\.(?:m3u8|mp4|flv|ts)(?:[^\\s\"']*|$))").matcher(mainHtml);
                    Matcher m = Pattern.compile("https?://[^/]+").matcher(iframe);
                    String baseurl = m.find() ? m.group() : "";
                    if (commonMatcher2.find()) id = baseurl + commonMatcher2.group(1);
                }
            }
            
        }
        result.put("url", id);
        return result.toString();
    }
}