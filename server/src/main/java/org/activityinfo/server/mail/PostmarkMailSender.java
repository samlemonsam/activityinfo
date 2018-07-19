/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.mail;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import freemarker.template.Configuration;
import org.activityinfo.server.DeploymentConfiguration;
import org.apache.commons.codec.binary.Base64;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends emails through the Postmark API using the HTTP API.
 * <p/>
 * <p/>
 * PostMark is a service for transactional email that help ensure a high
 * delivery rate.
 * <p/>
 * <p/>
 * The API key can be set in the configuration file with the property
 * {@code postmark.key }
 * <p/>
 * <p/>
 * The docs are available from
 * http://developer.postmarkapp.com/developer-build.html
 */
public class PostmarkMailSender extends MailSender {
    
    public static final String POSTMARK_API_URL = "postmark.url";
    public static final String POSTMARK_API_KEY = "postmark.key";

    private final String apiKey;

    private static final Logger LOGGER = Logger.getLogger(PostmarkMailSender.class.getName());
    private final URL url;

    @Inject
    public PostmarkMailSender(DeploymentConfiguration deploymentConfig, Configuration templateCfg) {
        super(templateCfg);
        try {
            this.url = new URL(deploymentConfig.getProperty(POSTMARK_API_URL, "https://api.postmarkapp.com/email"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("%s contains a malformed URL: %s", POSTMARK_API_URL, 
                    e.getMessage()));
        }
        this.apiKey = deploymentConfig.getProperty(POSTMARK_API_KEY);
    }

    @Override
    public void send(Message message) throws MessagingException {
        try {
            JsonObject json = toJson(message);
            LOGGER.log(Level.INFO, "TextBody: " + json.get("TextBody"));
            postMessage(json);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to post message to postmark service", e);
        }
    }

    private void postMessage(JsonObject node) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("X-Postmark-Server-Token", apiKey);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5 * 60 * 1000);
        conn.setReadTimeout(5 * 60 * 1000);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), Charsets.UTF_8);
        writer.write(node.toString());
        writer.flush();

        if (conn.getResponseCode() != 200) {
            // Unsuccessful request
            LOGGER.severe("Postmark returned bad response code " + conn.getResponseCode());
            if (conn.getResponseCode() == 422) {
                // Postmark encodes certain API error codes in the response body of 422 responses
                // (see https://postmarkapp.com/developer/api/overview#error-codes) so we need to retrieve the
                // ErrorStream and log the error response
                logStream(conn.getErrorStream());
            }
            writer.close();
            return;
        }

        logStream(conn.getInputStream());
        writer.close();
        LOGGER.info("Posted message to " + url);
    }

    private void logStream(InputStream stream) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
    }

    private JsonObject toJson(Message message) throws MessagingException, IOException {
        JsonObject json = new JsonObject();
        json.addProperty("From", "notifications@activityinfo.org");
        json.addProperty("To", toString(message.getTo()));
        json.addProperty("Subject", message.getSubject());

        if (message.hasTextBody()) {
            json.addProperty("TextBody", message.getTextBody());
        }
        if (message.hasHtmlBody()) {
            json.addProperty("HtmlBody", message.getSafeHtmlBody());
        }

        JsonArray attachments = new JsonArray();
        for (MessageAttachment part : message.getAttachments()) {
            JsonObject attachment = new JsonObject();
            attachment.addProperty("Name", part.getFilename());
            attachment.addProperty("Content", toBase64(part));
            attachment.addProperty("ContentType", stripContentType(part.getContentType()));
            attachments.add(attachment);
        }
        if (attachments.size() > 0) {
            json.add("Attachments", attachments);
        }

        if (message.getReplyTo() != null) {
            json.addProperty("ReplyTo", toString(Arrays.asList(message.getReplyTo())));
        }
        return json;
    }

    private String stripContentType(String contentType) {
        int semicolon = contentType.indexOf(';');
        if (semicolon == -1) {
            return contentType.trim();
        }
        return contentType.substring(0, semicolon).trim();
    }

    private String toBase64(MessageAttachment part) throws IOException {
        return new String(Base64.encodeBase64(part.getContent()));
    }

    private String toString(Iterable<InternetAddress> addresses) {
        StringBuilder sb = new StringBuilder();
        if (addresses != null) {
            for (InternetAddress address : addresses) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(address.getAddress());
            }
        }
        return sb.toString();
    }
}
