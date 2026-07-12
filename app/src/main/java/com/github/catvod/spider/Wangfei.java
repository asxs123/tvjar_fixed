package com.github.catvod.spider;


import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Pattern;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


public class Wangfei extends Spider {


    private static final String siteUrl = "https://www.wfei.la";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }


    public String homeContent(boolean filter) {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        // 需要排除的关键词
        List<String> excludeKeywords = Arrays.asList("短剧");
        for (Element element : doc.select("li.swiper-slide > a:nth-child(1)")) {
            String name = element.attr("title");
            // 检查是否包含任何需要排除的关键词
            boolean shouldExclude = excludeKeywords.stream().anyMatch(name::contains);
            if (element.attr("href").startsWith("/vod-show") && !shouldExclude) {
                String id = element.attr("href").replaceAll("\\D+", "");
                // String name = element.attr("title");
                classes.add(new Class(id, name));
            }
        }
        for (Element element : doc.select("div.module-main > div.module-items > a")) {
            String img = element.select("div:nth-child(1) > div:nth-child(2) > img:nth-child(1)").attr("data-original");
            String name = element.attr("title");
            String remark = element.select("div:nth-child(1) > div:nth-child(1)").text();
            String id = element.attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(classes, list);

    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("/vod-show-id-%s-page-%s.html", tid, pg);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        Elements elements = doc.select("a.module-poster-item");
        int limit = elements.size();
        for (Element element : elements) {
            String img = element.select("div:nth-child(1) > div:nth-child(2) > img:nth-child(1)").attr("data-original");
            String name = element.attr("title");
            String remark = element.select("div:nth-child(1) > div:nth-child(1)").text();
            String id = element.attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        Element lastPageA = doc.selectFirst("a[title=尾页]");
        if (lastPageA == null) {
            lastPageA = doc.select("#page a:last-of-type").first();
        }
        String href = lastPageA.attr("href");
        String[] split1 = href.split("page-");
        int page = Integer.parseInt(pg);
        if (split1.length >= 2) {
            String suffix = split1[1];
            // 再按 .html 截取前面数字
            page = Integer.parseInt(suffix.split("\\.html")[0]);
        }
        // return Result.string(list);
        return Result.get().vod(list).page(Integer.parseInt(pg), page, limit, Integer.MAX_VALUE).string();
    }




    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(siteUrl.concat("/vod-detail-id-").concat(ids.get(0)), getHeaders()));
        String name = doc.select(".module-info-heading > h1:nth-child(1) > span:nth-child(1)").text();
        // String remarks = doc.select("div.module-info-item:nth-child(5) > div:nth-child(2)").text();
        String remarks = doc.select("div.module-info-item:nth-child(2)").text();
        String img = doc.select(".ls-is-cached").attr("data-original");
        String type = doc.select("div.module-info-tag-link:nth-child(3)").text();
        String actor = doc.select("div.module-info-item:nth-child(3) > div").text();
        String content = doc.select(".module-info-introduction-content > p").text();
        String director = doc.select("div.module-info-item:nth-child(4) > div").text();
        String year = doc.select("div.module-info-tag-link:nth-child(1)").text();
        String area = doc.select("div.module-info-tag-link:nth-child(2)").text();

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(img);
        vod.setVodName(name);
        vod.setVodActor(actor);
        vod.setVodRemarks(remarks);
        vod.setVodContent(content);
        vod.setVodDirector(director);
        vod.setTypeName(type);
        vod.setVodYear(year);
        vod.setVodArea(area);



        Map<String, String> sites = new LinkedHashMap<>();
        Elements sources = doc.select("div.module-tab-item");
        Elements sourceList = doc.select("div.module-play-list-content");
        for (int i = 0; i < sources.size(); i++) {
            Element source = sources.get(i);
            String sourceName = source.text();
            Elements playList = sourceList.get(i).select("a");
            List<String> vodItems = new ArrayList<>();
            for (int j = 0; j < playList.size(); j++) {
                Element e = playList.get(j);
                vodItems.add(e.text() + "$" + e.attr("href"));
            }
            if (vodItems.size() > 0) {
                sites.put(sourceName, TextUtils.join("#", vodItems));
            }
        }
        if (sites.size() > 0) {
            vod.setVodPlayFrom(TextUtils.join("$$$", sites.keySet()));
            vod.setVodPlayUrl(TextUtils.join("$$$", sites.values()));
        }


        return Result.string(vod);
    }



    public String searchContent(String key, boolean quick) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("/vod-search.html?wd=").concat(key);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.module-card-item")) {
            String img = element.select("a:nth-child(2) > div:nth-child(1) > div:nth-child(2) > img:nth-child(1)").attr("data-original");
            String name = element.select("div:nth-child(3) > div:nth-child(1) > a:nth-child(1) > strong:nth-child(1)").text();
            String remark = element.select("a:nth-child(2) > div:nth-child(1) > div:nth-child(1)").text();
            String id = element.select("a:nth-child(2)").attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String url = siteUrl + id;
        Document mainDoc = Jsoup.connect(url)
                                .userAgent(Util.CHROME)
                                .timeout(15000)
                                .ignoreContentType(true) // 防止部分接口非html返回报错
                                .get();
        Elements scripts = mainDoc.select("script");
        for (Element script : scripts) {
            String scriptText = script.html(); // 获取script内部文本
            if (scriptText.contains("var player_aaaa")) {
                String jsonstString = scriptText.replaceAll("var player_aaaa=", "").trim();
                // System.out.println(jsonstString);
                JSONObject jSONObject = new JSONObject(jsonstString);
                // System.out.println(jSONObject);
                String url1 = jSONObject.getString("url");
                int encrypt = jSONObject.getInt("encrypt");
                if (encrypt == 0) {
                    if (url1.contains("m3u8") || url1.contains("mp4") || url1.contains("flv")) {
                        url = url1;
                    } else {
                        Document iframeDoc = Jsoup.connect(url1)
                                                .userAgent(Util.CHROME)
                                                .timeout(15000)
                                                .ignoreContentType(true) // 防止部分接口非html返回报错
                                                .get();
                        String mainHtml = iframeDoc.html();
                        Matcher aliMatcher = Pattern.compile("(?:\\\")?source(?:\\\")?\\s*[:=]\\s*[\\\"'](https?://.*?\\.(m3u8|mp4|flv)(?:[^\\s\"']*|$))[\\\"']").matcher(mainHtml);
                        if (aliMatcher.find()) {
                            url = aliMatcher.group(1);
                        } else {
                            // 通用兜底匹配任意明文视频链接（兼容其他播放器）
                            System.out.println("兜底");
                            Matcher commonMatcher = Pattern.compile("https?://[^\"']+?\\.(m3u8|mp4|flv|ts)(?:[^\\s\"']*|$)").matcher(mainHtml);
                            if (commonMatcher.find()) {
                                url = commonMatcher.group();
                            } else {
                                System.out.println("兜底2");
                                Matcher commonMatcher2 = Pattern.compile("(?:https?://[^\"'\\s]*)?(/[^\"'\\s]*\\.(?:m3u8|mp4|flv|ts)(?:[^\\s\"']*|$))").matcher(mainHtml);
                                Matcher m = Pattern.compile("https?://[^/]+").matcher(url1);
                                String baseurl = m.find() ? m.group() : "";
                                if (commonMatcher2.find()) url = baseurl + commonMatcher2.group(1);
                            }
                        }
                    }
                }
                break;
            }
        }
        return Result.get().url(url).parse().header(getHeaders()).string();
    }
}

