package org.activityinfo.test.driver;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;
import java.util.List;

/**
 * Drives the Application through the XForm API 
 */
public class XFormApplicationDriver extends ApplicationDriver {
    private ApiApplicationDriver apiDriver;
    private Server server;
    private UserAccount currentUser;

    @Inject
    public XFormApplicationDriver(ApiApplicationDriver apiDriver, Server server) {
        super(apiDriver.getAliasTable());
        this.apiDriver = apiDriver;
        this.server = server;
    }

    @Override
    public void login() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(UserAccount account) {
        setup().login(account);
        currentUser = account;

    }

    @Override
    public ApplicationDriver setup() {
        return apiDriver;
    }

    @Override
    public void submitForm(String formName, List<FieldValue> values) throws Exception {
        Client client = Client.create();
        if(currentUser != null) {
            client.addFilter(new HTTPBasicAuthFilter(currentUser.getEmail(), currentUser.getPassword()));

        }
        WebResource root = client.resource(server.getRootUrl());
       // root.path("formList").get()
        throw new UnsupportedOperationException();
    }
}
