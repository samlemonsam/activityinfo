package org.activityinfo.ui.client.page.config;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.MockEventBus;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

;

public class DbListPresenterTest {
    private static final int OWNED_DB_ID = 1;
    private static final int DESIGNABLE_DB_ID = 2;
    private static final int VIEWABLE_DB_ID = 3;

    private Dispatcher dispatcher = createMock(Dispatcher.class);

    private MockEventBus eventBus = new MockEventBus();
    private DbListPresenter.View view = createMock(DbListPresenter.View.class);
    private DbListPresenter presenter;
    private SchemaDTO schema = new SchemaDTO();
    protected UserDatabaseDTO ownedDb;
    protected UserDatabaseDTO designableDb;
    protected UserDatabaseDTO viewableDb;

    @Before
    public void setUp() throws Exception {

        ownedDb = new UserDatabaseDTO(OWNED_DB_ID, "My Database");
        ownedDb.setAmOwner(true);
        ownedDb.setDesignAllowed(true);
        ownedDb.setManageUsersAllowed(true);
        schema.getDatabases().add(ownedDb);

        designableDb = new UserDatabaseDTO(DESIGNABLE_DB_ID, "My Database");
        designableDb.setAmOwner(false);
        designableDb.setDesignAllowed(true);
        designableDb.setManageUsersAllowed(true);
        schema.getDatabases().add(designableDb);

        viewableDb = new UserDatabaseDTO(VIEWABLE_DB_ID, "My database");
        viewableDb.setAmOwner(false);
        viewableDb.setDesignAllowed(false);
        viewableDb.setManageUsersAllowed(false);
        schema.getDatabases().add(viewableDb);
    }

    @Test
    public void loaderPopulatesStore() {

        ignoreView();

        expectDispatch(new GetSchema(), schema);
        replay(dispatcher);

        createPresenter();
        ListStore<UserDatabaseDTO> store = presenter.getStore();

        assertThat("store.getCount()", store.getCount(), is(equalTo(3)));

        verify(dispatcher);
    }

    @Test
    public void deleteAndEditAreDisabledIfNoDatabaseIsSelected() {
        expectSetActionEnabled();
        replay(view);

        createPresenter();
        presenter.onSelectionChanged(null);

        assertThat(disabledActions, hasItems(UIActions.EDIT, UIActions.DELETE));
    }

    @Test
    public void deleteAndEditAreEnabledIfDatabaseIsOwned() {
        expectSetActionEnabled();
        replay(view);

        createPresenter();
        presenter.onSelectionChanged(ownedDb);

        assertThat(disabledActions, not(hasItem(UIActions.EDIT)));
        assertThat(disabledActions, not(hasItem(UIActions.DELETE)));
    }

    @Test
    public void editIsEnabledIfUserHasDesignRights() {
        expectSetActionEnabled();
        replay(view);

        createPresenter();
        presenter.onSelectionChanged(designableDb);

        assertThat(disabledActions, hasItem(UIActions.DELETE));
        assertThat(disabledActions, not(hasItem(UIActions.EDIT)));
    }

    @Test
    public void editDeleteAreDisabledWithoutRights() {
        expectSetActionEnabled();
        replay(view);

        createPresenter();
        presenter.onSelectionChanged(viewableDb);

        assertThat(disabledActions, hasItems(UIActions.EDIT, UIActions.DELETE));
    }

    @Test
    public void commandShouldBePreparedProperly() {

        Capture<CreateEntity> cmd = new Capture<CreateEntity>();
        expectDispatch(new GetSchema(), schema);
        captureDispatch(cmd);
        replay(dispatcher);

        UserDatabaseDTO newDb = new UserDatabaseDTO();
        newDb.setCountry(new CountryDTO(31, "Haiti"));
        newDb.setName("My Db");

        createPresenter();
        presenter.save(newDb, niceFormDialogMock());

        assertTrue("command was dispatched", cmd.hasCaptured());
        assertThat((Integer) cmd.getValue().getProperties().get("countryId"),
                is(equalTo(31)));
    }

    private FormDialogTether niceFormDialogMock() {
        FormDialogTether mock = createNiceMock(FormDialogTether.class);
        replay(mock);
        return mock;
    }

    private void captureDispatch(Capture<CreateEntity> cmd) {
        dispatcher.execute(and(isA(CreateEntity.class), capture(cmd)),
                optionalMonitor(), isA(AsyncCallback.class));
    }

    private void createPresenter() {
        presenter = new DbListPresenter(eventBus, dispatcher, view);
    }

    private void ignoreView() {
        view = createNiceMock(DbListPresenter.View.class);
        replay(view);
    }

    private Set<String> disabledActions = new HashSet<String>();

    private void expectSetActionEnabled() {
        view.setActionEnabled(isA(String.class), anyBoolean());
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                String actionId = (String) getCurrentArguments()[0];
                Boolean enabled = (Boolean) getCurrentArguments()[1];
                if (enabled) {
                    disabledActions.remove(actionId);
                } else {
                    disabledActions.add(actionId);
                }
                return null;
            }
        }).anyTimes();
    }

    private <T extends CommandResult> void expectDispatch(Command<T> command, final T result) {
        this.dispatcher.execute(eq(command), isA(AsyncCallback.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                ((AsyncCallback) getCurrentArguments()[1]).onSuccess(schema);
                return null;
            }
        });
    }

    private AsyncMonitor optionalMonitor() {
        return or(isA(AsyncMonitor.class), EasyMock.<AsyncMonitor>isNull());
    }

}