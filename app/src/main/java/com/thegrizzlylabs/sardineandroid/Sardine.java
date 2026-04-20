package com.thegrizzlylabs.sardineandroid;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Stub for compilation on JVM. Real implementation is in sardine-android AAR.
 */
public interface Sardine {
    void setCredentials(String username, String password);
    List<DavResource> list(String url) throws java.io.IOException;
    InputStream get(String url, Map<String, String> headers) throws java.io.IOException;
}
