package org.activityinfo.server.command.handler.json;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import org.activityinfo.legacy.shared.exception.UnexpectedCommandException;
import org.activityinfo.server.database.hibernate.entity.HasJson;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by yuriy on 3/1/2015.
 */
public class JsonHelper {


    private static final Logger LOGGER = Logger.getLogger(JsonHelper.class.getName());

    private static final int MIN_GZIP_BYTES = 1024 * 5;

    private JsonHelper() {
    }

    public static void updateWithJson(HasJson hasJson, String json) {

        if(json.length() > MIN_GZIP_BYTES) {
            hasJson.setGzJson(compressJson(json));
            hasJson.setJson(null);
        } else {
            hasJson.setJson(json);
            hasJson.setGzJson(null);
        }

    }

    public static byte[] compressJson(String json) {
        try {
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            GZIPOutputStream gzOut = new GZIPOutputStream(byteArrayOut);
            OutputStreamWriter writer = new OutputStreamWriter(gzOut, Charsets.UTF_8);
            writer.write(json);
            writer.close();
            byte[] bytes = byteArrayOut.toByteArray();
            LOGGER.log(Level.INFO, "GZipped json size = " + bytes.length);
            return bytes;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readJson(HasJson hasJson) {
        if (hasJson.getGzJson() != null) {
            try (Reader reader = new InputStreamReader(
                    new GZIPInputStream(
                            new ByteArrayInputStream(hasJson.getGzJson())), Charsets.UTF_8)) {

                return CharStreams.toString(reader);

            } catch (IOException e) {
                throw new UnexpectedCommandException(e);
            }

        } else if (hasJson.getJson() != null) {
            return hasJson.getJson();

        } else {
            return null;
        }
    }

    public static List<String> readJsons(List<? extends HasJson> hasJsons) {
        List<String> jsons = Lists.newArrayList();
        for (HasJson json : hasJsons) {
            jsons.add(readJson(json));
        }
        return jsons;
    }
}
