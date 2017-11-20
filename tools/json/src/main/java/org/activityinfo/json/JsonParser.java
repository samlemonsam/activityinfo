package org.activityinfo.json;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.Reader;

/**
 * Facilitates migration away from Gson...
 */
public class JsonParser {


    public JsonValue parse(String json) {
        return Json.parse(json);
    }

    @GwtIncompatible
    public JsonValue parse(Reader json) throws IOException {
        return parse(CharStreams.toString(json));
    }
}
