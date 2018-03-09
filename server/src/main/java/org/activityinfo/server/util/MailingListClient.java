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
package org.activityinfo.server.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.database.hibernate.entity.User;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client that handles subscriptions to the ActivityInfo
 * mailing list
 */
@Singleton
public class MailingListClient {

    private static final Logger LOGGER = Logger.getLogger(MailingListClient.class.getName());

    private final String apiKey;
    private final String masterListId;

    private final String subbed = "subscribed";
    private final String unsubbed = "unsubscribed";

    private final String invitedGroup;
    private final String uninvitedGroup;
    private final String unknownGroup;
    private final String noAccountGroup;

    private final String newsletterGroup;
    private final String supportGroup;

    @Inject
    public MailingListClient(DeploymentConfiguration config) {
        this.apiKey = config.getProperty("mailchimp.api.key");
        this.masterListId = config.getProperty("mailchimp.list.id", "9289430112");

        this.invitedGroup = config.getProperty("mailchimp.group.invited", "a39940fdf7");
        this.uninvitedGroup = config.getProperty("mailchimp.group.uninvited", "25ecbf7449");
        this.unknownGroup = config.getProperty("mailchimp.group.unknown", "cd58ffd8d6");
        this.noAccountGroup = config.getProperty("mailchimp.group.noaccount", "394e27b603");

        this.newsletterGroup = config .getProperty("mailchimp.group.newsletter", "c3e12c0ba3");
        this.supportGroup = config.getProperty("mailchimp.group.support", "ec789f4553");
    }

    public void subscribe(User user, boolean invited, boolean newsletter) {

        AddListMemberMethod addToMasterList = new AddListMemberMethod();
        addToMasterList.emailAddress = user.getEmail();
        addToMasterList.status = subbed;
        addToMasterList.mergeVars.email = user.getEmail();
        addToMasterList.mergeVars.firstName = user.getName();
        setInterests(addToMasterList, invited, newsletter);

        try {
            post(addToMasterList, masterListId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to subscribe user", e);
        }
    }

    private void post(AddListMemberMethod method, String listId) throws Exception {
        URL url = new URL("https://us4.api.mailchimp.com/3.0/lists/" + listId + "/members");
        HttpURLConnection conn = openConnection(url);
        ObjectMapper mapper = new ObjectMapper();
        sendRequest(method, conn, mapper);
    }

    private HttpURLConnection openConnection(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        setAuthentication(conn);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5 * 60 * 1000);
        conn.setReadTimeout(5 * 60 * 1000);
        return conn;
    }

    private void setAuthentication(HttpURLConnection conn) throws UnsupportedEncodingException {
        byte[] authMessage = ("apikey:" + apiKey).getBytes("UTF-8");
        String authEncoded = DatatypeConverter.printBase64Binary(authMessage);
        conn.setRequestProperty("Authorization", "Basic " + authEncoded);
    }

    private void sendRequest(AddListMemberMethod method, HttpURLConnection conn, ObjectMapper mapper) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), Charsets.UTF_8);
        String json = mapper.writeValueAsString(method);
        LOGGER.fine("MailChimp: " + json);
        writer.write(json);
        writer.flush();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        writer.close();
        reader.close();
    }

    private void setInterests(AddListMemberMethod method, boolean invited, boolean newsletter) {
        method.interests.put(invitedGroup, invited);
        method.interests.put(uninvitedGroup, !invited);
        method.interests.put(unknownGroup, false);
        method.interests.put(noAccountGroup, false);

        method.interests.put(newsletterGroup, newsletter);
        method.interests.put(supportGroup, true);
    }

    // Holds a subscriber's merge_vars info (see http://apidocs.mailchimp.com/legacy/1.3/listsubscribe.func.php )
    public static class MergeVars {

        @JsonProperty("EMAIL")
        private String email;

        @JsonProperty("FNAME")
        private String firstName;

    }

    // Request body parameters specified by MailChimp 3.0 API
    // http://developer.mailchimp.com/documentation/mailchimp/reference/lists/members/
    public static class AddListMemberMethod {

        @JsonProperty("email_address")
        private String emailAddress;

        @JsonProperty("status")
        private String status;

        @JsonProperty("merge_fields")
        private MergeVars mergeVars = new MergeVars();

        // Specifies the "interest" groups user is associated with
        @JsonProperty("interests")
        private Map<String, Boolean> interests = Maps.newHashMap();

    }

}
