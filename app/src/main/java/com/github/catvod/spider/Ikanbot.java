package com.github.catvod.spider;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ikanbot extends Spider {

    private static final String siteUrl = "https://www.ikanbot.com";
    private static final String cateUrl = siteUrl + "/hot";
    private static final String detailUrl = siteUrl + "/play/";
    private static final String searchUrl = siteUrl + "/search?q=";

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }

    private List<Vod> parseVods(Document doc) {
        List<Vod> list = new ArrayList<>();
        for (Element element : doc.select("a.item")) {
            String pic = element.select("img").attr("data-src") + "@Referer=https://api.douban.com/@User-Agent=" + Util.CHROME;
            // String url = element.attr("href");
            String name = element.select("img").attr("alt");
            // String id = url.split("/")[2];
            String id = element.select("img").attr("id");
            list.add(new Vod(id, name, pic));
        }
        return list;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Class> classes = new ArrayList<>();
        String[] typeIdList = {"/index-movie-热门", "/index-tv-热门", "/index-tv-综艺", "/index-tv-纪录片", "/index-tv-日本动画"};
        String[] typeNameList = {"电影", "剧集", "综艺", "纪录片", "动漫"};
        for (int i = 0; i < typeNameList.length; i++) {
            classes.add(new Class(typeIdList[i], typeNameList[i]));
        }
        String filters = "{\"/index-movie-热门\":[{\"key\":\"key\",\"name\":\"分类\",\"value\":[{\"n\":\"全部\",\"v\":\"/index-movie-热门\"},{\"v\":\"/index-movie-最新\",\"n\":\"最新\"},{\"v\":\"/index-movie-经典\",\"n\":\"经典\"},{\"v\":\"/index-movie-豆瓣高分\",\"n\":\"豆瓣高分\"},{\"v\":\"/index-movie-冷门佳片\",\"n\":\"冷门佳片\"},{\"v\":\"/index-movie-华语\",\"n\":\"华语\"},{\"v\":\"/index-movie-欧美\",\"n\":\"欧美\"},{\"v\":\"/index-movie-韩国\",\"n\":\"韩国\"},{\"v\":\"/index-movie-日本\",\"n\":\"日本\"},{\"v\":\"/index-movie-动作\",\"n\":\"动作\"},{\"v\":\"/index-movie-喜剧\",\"n\":\"喜剧\"},{\"v\":\"/index-movie-爱情\",\"n\":\"爱情\"},{\"v\":\"/index-movie-科幻\",\"n\":\"科幻\"},{\"v\":\"/index-movie-悬疑\",\"n\":\"悬疑\"},{\"v\":\"/index-movie-恐怖\",\"n\":\"恐怖\"},{\"v\":\"/index-movie-动画\",\"n\":\"动画\"},{\"v\":\"/index-movie-豆瓣top250\",\"n\":\"豆瓣top250\"}]}],\"/index-tv-热门\":[{\"key\":\"key\",\"name\":\"分类\",\"value\":[{\"v\":\"/index-tv-热门\",\"n\":\"全部\"},{\"v\":\"/index-tv-美剧\",\"n\":\"美剧\"},{\"v\":\"/index-tv-英剧\",\"n\":\"英剧\"},{\"v\":\"/index-tv-韩剧\",\"n\":\"韩剧\"},{\"v\":\"/index-tv-日剧\",\"n\":\"日剧\"},{\"v\":\"/index-tv-国产剧\",\"n\":\"国产剧\"},{\"v\":\"/index-tv-港剧\",\"n\":\"港剧\"}]}]}";
        Document doc = Jsoup.parse(OkHttp.string(siteUrl + "/billboard.html", getHeaders()));
        List<Vod> list = new ArrayList<>();
        for (Element element : doc.select("div.item-root")) {
            String pic = element.select("img").attr("data-src") + "@Referer=https://api.douban.com/@User-Agent=" + Util.CHROME;
            // String url = element.select("a").attr("href");
            String url = element.select("img").attr("id");
            String name = element.select("img").attr("alt");
            // try {
            //     String id = url.split("/")[2];
            //     list.add(new Vod(id, name, pic));
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }
            list.add(new Vod(url, name, pic, "vod_remarks", "vod_actor"));
        }
        return Result.string(classes, list, filter ? JsonParser.parseString(filters) : null);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String target = "";
        if (extend != null && !extend.isEmpty()) {
            target = cateUrl + extend.get("key");
        } else {
            target = cateUrl + tid;
        }
        System.out.print(target);
        if (!"1".equals(pg)) {
            target = target + "-p-" + pg;
        }
        Document doc = Jsoup.parse(OkHttp.string(target.concat(".html"), getHeaders()));
        List<Vod> list = parseVods(doc);
        Integer total = (Integer.parseInt(pg) + 1) * 24;
        return Result.get().vod(list).page(Integer.parseInt(pg), Integer.parseInt(pg) + 1, 24, total).string();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(detailUrl.concat(ids.get(0)), getHeaders()));
        String name = doc.select("h1").text();
        String pic = doc.select("meta[property=og:image]").attr("content") + "@Referer=https://api.douban.com/@User-Agent=" + Util.CHROME;
        Elements desc = doc.select("div.detail > h3");
        String year = "";
        String area = "";
        String actor = "";
        String director = "";
        for (int i = 0; i < desc.size(); i++) {
            String text = desc.get(i).text().trim();
            if (i == 0 && !text.matches("^\\d{4}$")) continue;

            // 判断是否是年份
            if (text.matches("^\\d{4}$")) {
                year = text;
                continue;
            }
            // 判断是否是演员（包含 / 导演/演员格式）
            if (text.contains("/")) {
                String[] textArr = text.split("/", 2); // 只分割一次，避免名字含斜杠拆分错乱
                director = textArr[0].trim();
                actor = textArr.length > 1 ? textArr[1].trim() : "";
                continue;
            }
            area = text;
        }

        String current_id = doc.select("input#current_id").attr("value");
        String e_token = doc.select("input#e_token").attr("value");
        String mtype = doc.select("input#mtype").attr("value");
        String tks = get_tks(current_id, e_token);
        String url = siteUrl + "/api/getResN?videoId=" + ids.get(0) + "&mtype=" + mtype + " &token=" + tks;
        String data = OkHttp.string(url, getHeaders());
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        JsonArray array = jsonObject.getAsJsonObject("data").getAsJsonArray("list");
        String PlayFrom = "";
        String PlayUrl = "";
        for (JsonElement element : array) {

            // 使用正则表达式匹配 "flag" 和 "url"
            Pattern pattern = Pattern.compile("\\\"flag\\\":\\\"(.*?)\\\",\\\"url\\\":\\\"(.*?)\\\"");
            Matcher matcher = pattern.matcher(String.valueOf(element.getAsJsonObject().get("resData")).replace("\\", ""));
            String flag = "";
            String liUrl = "";
            // 提取匹配到的内容
            if (matcher.find()) {
                flag = matcher.group(1);
                liUrl = matcher.group(2);
            }
            if (!"".equals(PlayFrom)) {
                PlayFrom = PlayFrom + "$$$" + flag;
            } else {
                PlayFrom = PlayFrom + flag;
            }
            if (!"".equals(PlayUrl)) {
                PlayUrl = PlayUrl + "$$$" + liUrl.replace("$" + flag, "");
            } else {
                PlayUrl = PlayUrl + liUrl.replace("$" + flag, "");
            }

//            PlayUrl += String.valueOf(element.getAsJsonObject().get("resData")).replace("\\","").replace("\\\"","\"");
        }
        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(pic);
        vod.setVodYear(year);
        vod.setVodDirector(director);
        vod.setVodActor(actor);
        vod.setVodArea(area);
        vod.setVodName(name);
        vod.setVodPlayFrom(PlayFrom);
        vod.setVodPlayUrl(PlayUrl.replace("##", "#").replace("#$$$", "$$$"));
        vod.setTypeName("type_name");
        vod.setVodRemarks("vod_remarks");
        vod.setVodContent("vod_content");
        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(searchUrl.concat(URLEncoder.encode(key, "UTF-8")), getHeaders()));
        List<Vod> list = new ArrayList<>();
        for (Element element : doc.select("a.cover-link")) {
            String pic = element.select("img").attr("data-src") + "@Referer=https://api.douban.com/@User-Agent=" + Util.CHROME;
            String url = element.attr("href");
            String name = element.select("img").attr("alt");
            String id = url.split("/")[2];

            list.add(new Vod(id, name, pic));
        }
        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().url(id).header(getHeaders()).string();
    }


//    function get_tks() {
//                const _0xf07220 = _0xf746;
//        let _0x35162d = document['getElementById'] ('current_id').value 'current_id'
//                , _0xf25678 = document['getElementById'] ('e_token').value;
//        'e_token'
//        if (!_0x35162d || !_0xf25678)
//            return;
//        let _0x3882a3 = _0x35162d['length'], _0x52a097 = _0x35162d['substring']
//        (_0x3882a3 - 4, _0x3882a3)
//                  ,_0x2d9d1b = [];
//        for (let _0x570711 = 0x0; _0x570711 < _0x52a097['length']; _0x570711++) {
//            let _0x23e537 = parseInt(_0x52a097[_0x570711]), _0x48b93d = _0x23e537 % 0x3 + 0x1;
//            _0x2d9d1b[_0x570711] = _0xf25678['substring'] (_0x48b93d, _0x48b93d + 0x8),
//            _0xf25678 = _0xf25678['substring'] (_0x48b93d + 0x8, _0xf25678['length']);
//        }
//        v_tks = _0x2d9d1b['join'] ('');
//    }

    public String get_tks(String current_id, String e_token) {
//        String current_id = "798347";
//        String e_token = "mre0530ce88964487488y67d38c0a1uj7fd15cb8";
        System.out.printf("current_id " + current_id);
        System.out.printf("e_token " + e_token);
        if ("".equals(current_id) || "".equals(e_token)) {
            return "";
        }
        String[] list = new String[4];
        int idLength = current_id.length();
        String subString = current_id.substring(idLength - 4, idLength);
        for (int i = 0; i < subString.length(); i++) {
            int num = Character.getNumericValue(subString.charAt(i));
            int begin = num % 3 + 1;
            list[i] = e_token.substring(begin, begin + 8);
            e_token = e_token.substring(begin + 8);
        }

        StringBuilder v_tks = new StringBuilder();
        for (String string : list) {
            v_tks.append(string);
        }
        return v_tks.toString();
    }

}