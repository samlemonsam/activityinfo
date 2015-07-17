package org.activityinfo.ui.client.page.config.design;

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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.junit.GWTMockUtilities;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.client.state.StateManagerStub;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.ui.client.MockEventBus;
import org.activityinfo.ui.client.dispatch.DispatcherStub;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.common.grid.ConfirmCallback;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.page.entry.place.DataEntryPlace;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;

public class DesignTest {

    @Test
    public void testSave() {

        // Dummy Data
        SchemaDTO schema = DTOs.pear();

        // Collaborator
        MockEventBus eventBus = new MockEventBus();

        // Collaborator
        DispatcherStub service = new DispatcherStub();
        service.setResult(GetActivityForm.class, new ActivityFormDTO(schema.getDatabaseById(1).getActivities().get(0)));
        service.setResult(GetSchema.class, schema);
        service.setResult(UpdateEntity.class, new VoidResult());

        // Collaborator
        DesignPresenter.View view = createNiceMock(DesignPresenter.View.class);
        replay(view);

        // Localisation resources
        UiConstants constants = createNiceMock(UiConstants.class);
        replay(constants);

        DesignPresenter designer = new DesignPresenter(eventBus, service,
                new StateManagerStub(),
                view, constants);
        designer.go(schema.getDatabaseById(1));

        // Verify that following a change to the record, a save call
        // triggers an update command

        ActivityDTO activity = (ActivityDTO) ((TreeStore) designer.getStore()).getRootItems().get(0);
        Record record = designer.getStore().getRecord(activity);

        record.set("name", "New Name");

        designer.onUIAction(UIActions.SAVE);

        UpdateEntity cmd = service.getLastExecuted(UpdateEntity.class);

        Assert.assertTrue(cmd.getChanges().containsKey("name"));
        Assert.assertEquals("New Name", cmd.getChanges().get("name"));

    }

    @Test
    public void testSaveOnNavigateAway() {

        // Dummy Data
        SchemaDTO schema = DTOs.pear();

        // Collaborator
        MockEventBus eventBus = new MockEventBus();

        // Collaborator
        DispatcherStub service = new DispatcherStub();
        service.setResult(GetActivityForm.class, new ActivityFormDTO(schema.getDatabaseById(1).getActivities().get(0)));
        service.setResult(GetSchema.class, schema);
        service.setResult(UpdateEntity.class, new VoidResult());

        // Collaborator
        DesignPresenter.View view = createNiceMock(DesignPresenter.View.class);
        replay(view);

        // Collaborator
        UiConstants constants = createNiceMock(UiConstants.class);
        replay(constants);

        DesignPresenter designer = new DesignPresenter(eventBus, service,
                new StateManagerStub(),
                view, constants);
        designer.go(schema.getDatabaseById(1));

        // Verify that following a change to the record, a save call
        // triggers an update command

        ActivityDTO activity = (ActivityDTO) ((TreeStore) designer.getStore()).getRootItems().get(0);
        Record record = designer.getStore().getRecord(activity);

        record.set("name", "New Name");

        designer.requestToNavigateAway(new DataEntryPlace(),
                new NavigationCallback() {
                    @Override
                    public void onDecided(boolean allowed) {

                    }
                });

        UpdateEntity cmd = service.getLastExecuted(UpdateEntity.class);

        Assert.assertTrue(cmd.getChanges().containsKey("name"));
        Assert.assertEquals("New Name", cmd.getChanges().get("name"));

    }

    @Test
    public void testDelete() {

        // Dummy Data
        SchemaDTO schema = DTOs.pear();

        // Collaborator
        DispatcherStub service = new DispatcherStub();
        service.setResult(GetActivityForm.class, new ActivityFormDTO(schema.getDatabaseById(1).getActivities().get(0)));
        service.setResult(GetSchema.class, schema);
        service.setResult(Delete.class, new VoidResult());

        // Collaborator
        DesignPresenter.View view = createNiceMock(DesignPresenter.View.class);

        expect(view.getSelection()).andReturn(schema.getActivityById(91));
        view.confirmDeleteSelected(isA(ConfirmCallback.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                ((ConfirmCallback) getCurrentArguments()[0]).confirmed();
                return null;
            }
        });
        replay(view);

        // Collaborator
        UiConstants constants = createNiceMock(UiConstants.class);
        replay(constants);

        DesignPresenter designer = new DesignPresenter(new MockEventBus(),
                service, new StateManagerStub(),
                view, constants);
        designer.go(schema.getDatabaseById(1));

        // Verify that the proper delete command executes

        designer.onUIAction(UIActions.DELETE);

        Delete cmd = service.getLastExecuted(Delete.class);

        Assert.assertEquals("Activity", cmd.getEntityName());
        Assert.assertEquals(91, cmd.getId());

    }

    @Test
    public void testDeleteEnabled() {

        // Dummy Data
        SchemaDTO schema = DTOs.pear();

        // Collaborator
        DispatcherStub service = new DispatcherStub();
        service.setResult(GetActivityForm.class, new ActivityFormDTO(schema.getDatabaseById(1).getActivities().get(0)));
        service.setResult(GetSchema.class, schema);
        service.setResult(Delete.class, new VoidResult());

        // Collaborator
        DesignPresenter.View view = createNiceMock(DesignPresenter.View.class);
        view.setActionEnabled(UIActions.DELETE, false);
        replay(view);

        // Collaborator
        UiConstants constants = createNiceMock(UiConstants.class);
        replay(constants);

        DesignPresenter designer = new DesignPresenter(new MockEventBus(),
                service, new StateManagerStub(),
                view, constants);
        designer.go(schema.getDatabaseById(1));

        // Verify that the delete command is initially disabled
        verify(view);

        // Verify that the delete command is enabled when an activity is
        // selected
        resetToDefault(view);
        view.setActionEnabled(UIActions.DELETE, true);
        view.setActionEnabled(UIActions.OPEN_TABLE, true);
        view.setActionEnabled(UIActions.EDIT, true);
        replay(view);

        designer.onSelectionChanged(schema.getActivityById(91));

        verify(view);

        // Verify that the delete command is disabled when a folder is selected
        reset(view);
        view.setActionEnabled(UIActions.OPEN_TABLE, false);
        view.setActionEnabled(UIActions.DELETE, false);
        view.setActionEnabled(UIActions.EDIT, false);

        replay(view);

        designer.onSelectionChanged(new IndicatorFolder(null));

        verify(view);

    }

}
