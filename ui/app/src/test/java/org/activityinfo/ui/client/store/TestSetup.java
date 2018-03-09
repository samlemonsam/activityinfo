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
package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.indexedb.IDBFactoryStub;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.store.http.HttpStore;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.RecordSynchronizer;

/**
 * Wires together a FormStoreImpl along with stubbed or mocked out components
 * suitable for unit testing.
 */
public class TestSetup {

    private final AsyncClientStub client;
    private final HttpStore httpStore;
    private final OfflineStore offlineStore;
    private final FormStore formStore;
    private final StubScheduler scheduler;
    private final TestingStorageProvider catalog;
    private final RecordSynchronizer synchronizer;

    public TestSetup() {
        this(new IDBFactoryStub(), true);
    }

    public TestSetup(IDBFactoryStub database, boolean connected) {
        catalog = new TestingStorageProvider();
        client = new AsyncClientStub(catalog);
        client.setConnected(connected);

        scheduler = new StubScheduler();
        httpStore = new HttpStore(client, scheduler);
        offlineStore = new OfflineStore(httpStore, database);
        formStore = new FormStoreImpl(httpStore, offlineStore, scheduler);
        synchronizer = new RecordSynchronizer(httpStore, offlineStore);
    }

    public FormStore getFormStore() {
        return formStore;
    }

    public OfflineStore getOfflineStore() {
        return offlineStore;
    }

    public void deleteForm(ResourceId formId) {
        catalog.deleteForm(formId);
    }

    public void runScheduled() {
        while(scheduler.executeScheduledCommands()) {
            System.err.println("Still executing...");
        }
    }

    public void setConnected(boolean connected) {
        client.setConnected(connected);
    }

    public <T> Connection<T> connect(Observable<T> observable) {

        Connection<T> connection = new Connection<>(observable);

        runScheduled();

        return connection;
    }

    public Survey getSurveyForm() {
        return catalog.getSurvey();
    }

    public BioDataForm getBioDataForm() {
        return catalog.getBioDataForm();
    }

    public NfiForm getNfiForm() {
        return catalog.getNfiForm();
    }

    public VillageForm getVillageForm() {
        return catalog.getVillageForm();
    }

    public FormTree getFormTree(ResourceId formId) {
        FormTreeBuilder builder = new FormTreeBuilder(catalog);
        return builder.queryTree(formId);
    }

    public TestingStorageProvider getCatalog() {
        return catalog;
    }
}
