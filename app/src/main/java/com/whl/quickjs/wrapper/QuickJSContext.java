package com.whl.quickjs.wrapper;

public class QuickJSContext {
    public JSObject getGlobalObject() { return new JSObject(); }
    public JSArray createNewJSArray() { return new JSArray(); }
    public Object evaluate(String script) { return null; }
    public Object evaluate(String script, String fileName) { return null; }
    public void destroy() {}
}
