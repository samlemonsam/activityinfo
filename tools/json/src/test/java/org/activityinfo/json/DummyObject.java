package org.activityinfo.json;

import jsinterop.annotations.JsType;

/**
 * Because this object is annotated with @JsType, the GWT
 * compiler will preserve the name of the fields, so it can
 * be used directly for JSON serialization, both to a string
 * and to a plain old JS object that can be stored directly in
 * IndexedDB.
 */
@JsType
public class DummyObject {

    public boolean b;
    public int i;
    public double d;
    public String s;
    public String s0;
    public JsonValue object;

    private double[] da;

    private double privateField = 42;


}
