package android.net;

import java.net.URL;

public class Uri {
    //主机名
    private String host;
    //协议
    private String scheme;
    //端口
    private int port;
    //路径
    private String path;
    //查询字符串
    private String query;
    //片段标识符
    private String fragment;
    private String encodedPath;

    public static Uri parse(String url) {
        try {
            Uri uri = new Uri();
            URL u = new URL(url);
            uri.host = u.getHost();
            uri.scheme = u.getProtocol();
            uri.port = u.getPort();
            uri.path = u.getPath();
            uri.query = u.getQuery();
            uri.fragment = u.getRef();
            uri.encodedPath = u.getPath();
            return uri;
        } catch (Exception e) {
            return null;
        }
    }

    //返回协议，例如http，https
    public String getScheme() { return scheme; }
    //返回主机名
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getPath() { return path; }
    public String getQuery() { return query; }
    public String getFragment() { return fragment; }
    public String getEncodedPath() { return encodedPath; }
    public String getSchemeSpecificPart() { return ""; }
    public String getLastPathSegment() {
        if (path == null) return null;
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    public static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    public static String encode(String s, String allow) {
        return encode(s);
    }

    public static String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    public static class Builder {
        private String scheme;
        private String authority;
        private String encodedPath;
        private String path;
        private String query;
        private String fragment;

        public Builder scheme(String scheme) { this.scheme = scheme; return this; }
        public Builder authority(String authority) { this.authority = authority; return this; }
        public Builder encodedPath(String path) { this.encodedPath = path; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder appendQueryParameter(String key, String val) { return this; }
        public Builder fragment(String fragment) { this.fragment = fragment; return this; }
        public Uri build() {
            Uri uri = new Uri();
            uri.scheme = this.scheme;
            uri.host = this.authority;
            uri.path = this.path != null ? this.path : this.encodedPath;
            uri.encodedPath = this.encodedPath != null ? this.encodedPath : this.path;
            return uri;
        }
    }
}
