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
package org.activityinfo.test.driver;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import cucumber.api.PendingException;
import cucumber.runtime.java.guice.ScenarioScoped;
import io.appium.java_client.AppiumDriver;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.test.pageobject.odk.BlankForm;
import org.activityinfo.test.pageobject.odk.FormList;
import org.activityinfo.test.pageobject.odk.OdkApp;
import org.activityinfo.test.pageobject.odk.Question;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Drives interaction with the application through ODK Collect
 */
@ScenarioScoped
public class OdkApplicationDriver extends ApplicationDriver {

    private ApiApplicationDriver apiDriver;
    private final Provider<AppiumDriver> appiumDriver;
    private Server server;
    private UserAccount currentUser;
    private OdkApp odk;

    @Inject
    public OdkApplicationDriver(ApiApplicationDriver apiDriver, Provider<AppiumDriver> appiumDriver, Server server) {
        super(apiDriver.getAliasTable());
        this.apiDriver = apiDriver;
        this.appiumDriver = appiumDriver;
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
    
    public OdkApp pageObject() {
        if(odk == null) {
            odk = new OdkApp(appiumDriver.get());
            odk.openGeneralSettings()
                    .setAccountEmail(currentUser.getEmail())
                    .setPassword(currentUser.getPassword())
                    .setUrl(server.getRootUrl());
        }
        return odk;
    }

    @Override
    public ApplicationDriver setup() {
        return apiDriver;
    }


    
    @Override
    public DataEntryDriver startNewSubmission(String formName) {
        downloadForm(formName);

        Question question = pageObject()
                .openMainMenu()
                .fillBlankForms()
                .choose(getAliasTable().getAlias(formName));
    
        return question;
    }

    private void downloadForm(String formName) {
        // First try to download the form
        FormList formList = pageObject().openFormList();
        String qualifiedFormName = findForm(formList, formName);
        formList.select(qualifiedFormName);
        formList.getSelected();
    }

    @Override
    public ResourceId submitForm(String formName, List<FieldValue> values) throws Exception {
        // Ignored for now!!
        throw new PendingException();
    }

    public List<String> queryFormList() {
        Client client = Client.create();
        if(currentUser != null) {
            client.addFilter(new HTTPBasicAuthFilter(currentUser.getEmail(), currentUser.getPassword()));
        }
        Document formList = client.resource(server.getRootUrl()).path("formList").get(Document.class);

        NodeList formNodes = formList.getElementsByTagName("form");
        List<String> forms = new ArrayList<>();
        
        for(int i=0;i<formNodes.getLength();++i) {
            forms.add(parseFormName(formNodes.item(i)));
        }
        
        return forms;
    }
    
    public void assertFormIsNotPresent(String formName) {
        String alias = getAliasTable().getAlias(formName);
        for (String form : queryFormList()) {
            if(form.contains(alias)) {
                throw new AssertionError(format("'%s' is present in the list of blank forms.", formName));
            }
        }
    }

    private String parseFormName(Node item) {
        return item.getTextContent();
    }

    private String findForm(FormList formList, String formName) {
        for (BlankForm blankForm : formList.getForms()) {
            if(blankForm.getName().endsWith("/ " + getAliasTable().getAlias(formName))) {
                return blankForm.getName();
            }
        }
        throw new AssertionError(format("Form '%s' is not listed", formName));
        
    }

    public void quit() {
        if(odk != null) {
            odk.quit();
        }
    }
}
