package com.thegrizzlylabs.sardineandroid;

import java.util.Date;

/**
 * Stub for compilation on JVM. Real implementation is in sardine-android AAR.
 */
public class DavResource {

    private final String name;
    private final String path;
    private final boolean directory;
    private final long contentLength;
    private final Date modified;

    public DavResource(String path, String name, boolean directory, long contentLength, Date modified) {
        this.path = path;
        this.name = name;
        this.directory = directory;
        this.contentLength = contentLength;
        this.modified = modified;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public boolean isDirectory() { return directory; }
    public long getContentLength() { return contentLength; }
    public Date getModified() { return modified; }
    public String getContentType() { return ""; }
}
