package org.activityinfo.ui.client.page.config.mvp;

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

import org.activityinfo.legacy.shared.model.DTO;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.config.mvp.CanCreate.*;
import org.activityinfo.ui.client.page.config.mvp.CanDelete.ConfirmDeleteEvent;
import org.activityinfo.ui.client.page.config.mvp.CanDelete.ConfirmDeleteHandler;
import org.activityinfo.ui.client.page.config.mvp.CanDelete.RequestDeleteEvent;
import org.activityinfo.ui.client.page.config.mvp.CanDelete.RequestDeleteHandler;
import org.activityinfo.ui.client.page.config.mvp.CanFilter.FilterEvent;
import org.activityinfo.ui.client.page.config.mvp.CanFilter.FilterHandler;
import org.activityinfo.ui.client.page.config.mvp.CanRefresh.RefreshEvent;
import org.activityinfo.ui.client.page.config.mvp.CanRefresh.RefreshHandler;
import org.activityinfo.ui.client.page.config.mvp.CanUpdate.*;

import java.util.List;

@Deprecated
public class ListPresenterBase<M extends DTO, L extends List<M>, P extends DTO, V extends CrudView<M,
        P>> extends PresenterBase<V, M> implements UpdateHandler, CancelUpdateHandler, ConfirmDeleteHandler,
        CreateHandler, FilterHandler, RefreshHandler, RequestDeleteHandler, StartCreateHandler, CancelCreateHandler,
        RequestUpdateHandler {

    protected P parentModel;

    public ListPresenterBase(Dispatcher service, EventBus eventBus, V view) {
        super(service, eventBus, view);
    }

    @Override
    protected void addListeners() {
        // Create
        view.addStartCreateHandler(this);
        view.addCancelCreateHandler(this);
        view.addCreateHandler(this);

        // Update
        view.addRequestUpdateHandler(this);
        view.addCancelUpdateHandler(this);
        view.addUpdateHandler(this);

        // Delete
        view.addRequestDeleteHandler(this);
        view.addConfirmDeleteHandler(this);

        view.addFilterHandler(this);
        view.addRefreshHandler(this);
    }

    @Override
    public void onRequestDelete(RequestDeleteEvent deleteEvent) {
    }

    @Override
    public void onRefresh(RefreshEvent refreshEvent) {
    }

    @Override
    public void onFilter(FilterEvent filterEvent) {
    }

    @Override
    public void onCreate(CreateEvent createEvent) {
    }

    @Override
    public void onConfirmDelete(ConfirmDeleteEvent deleteEvent) {
    }

    @Override
    public void onCancelUpdate(CancelUpdateEvent updateEvent) {
    }

    @Override
    public void onUpdate(UpdateEvent updateEvent) {
    }

    @Override
    public void onRequestUpdate(RequestUpdateEvent requestUpdateEvent) {
    }

    @Override
    public void onCancelCreate(CancelCreateEvent createEvent) {
        view.cancelCreate();
    }

    @Override
    public void onStartCreate(StartCreateEvent createEvent) {
        view.startCreate();
    }
}
