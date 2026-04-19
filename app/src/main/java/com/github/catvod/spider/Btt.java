package com.github.catvod.spider;


import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Btt extends Spider {


    private static final String siteUrl = "https://www.bttwo.org/";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }


    public String homeContent(boolean filter) {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        Map<String, String> idName = new HashMap<>();
        idName.put("new-movie", "最新电影");
        idName.put("hot", "热门下载");
        idName.put("hot-month", "本月热门");
        idName.put("zgjun", "国产剧");
        idName.put("meiju", "美剧");
        idName.put("jpsrtv", "日韩剧");
        for (Map.Entry<String, String> entry : idName.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue();
            classes.add(new Class(id, name));
        }
        for (Element element : doc.select("div.bt_img.mi_ne_kd.newindex ul li")) {
            String img = element.select("a:nth-child(1) > img:nth-child(1)").attr("data-original");
            String name = element.select("h3:nth-child(3) > a:nth-child(1)").text();
            String remark = element.select("div:nth-child(2) > span:nth-child(1)").text();
            String id = element.select("a:nth-child(1)").attr("href").replaceAll("\\D+", "");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(classes, list);

    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("%s/page/%s", tid, pg);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select(".bt_img > ul:nth-child(1) > li")) {
            String img = element.select("a:nth-child(1) > img:nth-child(1)").attr("data-original");
            String name = element.select("h3:nth-child(3) > a:nth-child(1)").text();
            String remark = element.select("a:nth-child(1) > div:nth-child(2) > span:nth-child(1)").text();
            String id = element.select("h3:nth-child(3) > a:nth-child(1)").attr("href").replaceAll("\\D+", "");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }


    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(siteUrl.concat("movie/").concat(ids.get(0)).concat(".html"), getHeaders()));
        String name = doc.select(".moviedteail_tt > h1:nth-child(1)").text();
        String remarks = doc.select(".moviedteail_list > li:nth-child(3) > a:nth-child(1)").text();
        String img = doc.select(".dyimg > img:nth-child(1)").attr("src");
        String type = doc.select(".moviedteail_list > li:nth-child(1) a").text();
        String actor = doc.select(".moviedteail_list > li:nth-child(8) > a").text();
        String content = doc.select(".yp_context > p:nth-child(1)").text();
        String director = doc.select(".moviedteail_list > li:nth-child(6) a").text();
        String area = doc.select(".moviedteail_list > li:nth-child(2) a").text();

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(img);
        vod.setVodName(name);
        vod.setVodActor(actor);
        vod.setVodRemarks(remarks);
        vod.setVodYear(remarks);
        vod.setVodArea(area);
        vod.setVodContent(content);
        vod.setVodDirector(director);
        vod.setTypeName(type);


        Map<String, String> sites = new LinkedHashMap<>();
        Elements sources = doc.select(".mi_paly_box > div:nth-child(1) > div:nth-child(1) > span");
        Elements sourceList = doc.select(".paly_list_btn");
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
        // String target = siteUrl.concat("xsssearch?q=").concat(key);
        String target = siteUrl.concat("xssssearch?q=").concat(key);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select(".bt_img > ul:nth-child(1) > li")) {
            String img = element.select("a:nth-child(1) > img:nth-child(1)").attr("data-original");
            String name = element.select("h3:nth-child(2) > a:nth-child(1)").text();
            String remark = element.select("a:nth-child(1) > div:nth-child(2) > span:nth-child(1)").text();
            String id = element.select("a:nth-child(1)").attr("href").replaceAll("\\D+", "");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(id).parse().header(getHeaders()).string();
    }
}

