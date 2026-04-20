package com.thegrizzlylabs.sardineandroid.impl;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stub for compilation on JVM. Real implementation is in sardine-android AAR.
 */
public class OkHttpSardine implements Sardine {

    public OkHttpSardine() {}

    @Override
    public void setCredentials(String username, String password) {}

    @Override
    public List<DavResource> list(String url) throws IOException {
        return new ArrayList<>();
    }

    @Override
    public InputStream get(String url, Map<String, String> headers) throws IOException {
        return new ByteArrayInputStream(new byte[0]);
    }
}
