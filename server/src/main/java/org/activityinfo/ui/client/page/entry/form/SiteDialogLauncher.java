package org.activityinfo.ui.client.page.entry.form;

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

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.form.FormDialog;
import org.activityinfo.ui.client.component.form.FormDialogCallback;
import org.activityinfo.ui.client.page.entry.location.LocationDialog;

public class SiteDialogLauncher {

    private final Dispatcher dispatcher;
    private final ResourceLocator resourceLocator;
    private final EventBus eventBus;

    public SiteDialogLauncher(Dispatcher dispatcher, EventBus eventBus) {
        super();
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resourceLocator = new ResourceLocatorAdaptor(dispatcher);
    }

    public void addSite(final Filter filter, final SiteDialogCallback callback) {
        if (filter.isDimensionRestrictedToSingleCategory(DimensionType.Activity)) {
            final int activityId = filter.getRestrictedCategory(DimensionType.Activity);

            dispatcher.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    showError(caught);
                }

                @Override
                public void onSuccess(SchemaDTO schema) {
                    ActivityDTO activity = schema.getActivityById(activityId);
                    Log.trace("adding site for activity " + activity + ", locationType = " + activity.getLocationType());

                    if(activity.getDatabase().getPartners().isEmpty()) {
                        // Since we are creating a partner by default for every database,
                        // this shouldn't happen beyond the development environment
                        MessageBox.alert(I18N.CONSTANTS.error(), I18N.CONSTANTS.noPartners(), null);
                        return;
                    }

                    if (!activity.getClassicView()) {// modern view
                        final ResourceId instanceId = CuidAdapter.newLegacyFormInstanceId(activity.getFormClassId());
                        FormInstance newInstance = new FormInstance(instanceId, activity.getFormClassId());
                        showModernFormDialog(activity.getName(), newInstance, callback, true);
                        return;
                    }
                    dispatcher.execute(new GetActivityForm(activityId)).then(new AsyncCallback<ActivityFormDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            showError(caught);
                        }

                        @Override
                        public void onSuccess(ActivityFormDTO activity) {

                            if (activity.getLocationType().isAdminLevel()) {
                                addNewSiteWithBoundLocation(activity, callback);

                            } else if (activity.getLocationType().isNationwide()) {
                                addNewSiteWithNoLocation(activity, callback);

                            } else {
                                chooseLocationThenAddSite(activity, callback);
                            }
                        }
                    });

                }
            });
        }
    }

    private void showError(Throwable caught) {
        MessageBox.alert(I18N.CONSTANTS.serverError(), I18N.CONSTANTS.errorUnexpectedOccured(), null);
        Log.error("Error launching site dialog", caught);
    }

    public void showModernFormDialog(String formName, FormInstance instance, final SiteDialogCallback callback, boolean isNew) {
        showModernFormDialog(formName, instance, callback, isNew, resourceLocator);
    }

    public static void showModernFormDialog(String formName, FormInstance instance, final SiteDialogCallback callback, boolean isNew, ResourceLocator resourceLocator) {
        String h2Title = isNew ? I18N.CONSTANTS.newSubmission() : I18N.CONSTANTS.editSubmission();
        FormDialog dialog = new FormDialog(resourceLocator);
        dialog.setDialogTitle(formName, h2Title);
        dialog.show(instance, new FormDialogCallback() {
            @Override
            public void onPersisted(FormInstance instance) {
                callback.onSaved();
            }
        });
    }


    public void editSite(final SiteDTO site, final SiteDialogCallback callback) {
        dispatcher.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                showError(caught);
            }

            @Override
            public void onSuccess(SchemaDTO schema) {
                final ActivityDTO activity = schema.getActivityById(site.getActivityId());

                if (!activity.getClassicView()) {// modern view
                    resourceLocator.getFormInstance(site.getInstanceId()).then(new SuccessCallback<FormInstance>() {
                        @Override
                        public void onSuccess(FormInstance result) {
                            showModernFormDialog(activity.getName(), result, callback, false);
                        }
                    });

                    return;
                }

                // check whether the site has been locked
                // (this only applies to Once-reported activities because
                //  otherwise the date criteria applies to the monthly report)
                if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
                    LockedPeriodSet locks = new LockedPeriodSet(schema);
                    if (locks.isLocked(site)) {
                        MessageBox.alert(I18N.CONSTANTS.lockedSiteTitle(), I18N.CONSTANTS.siteIsLocked(), null);
                        return;
                    }
                }

                dispatcher.execute(new GetActivityForm(activity.getId())).then(new AsyncCallback<ActivityFormDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        showError(caught);
                    }

                    @Override
                    public void onSuccess(ActivityFormDTO result) {
                        SiteDialog dialog = new SiteDialog(dispatcher, result, eventBus);
                        dialog.showExisting(site, callback);
                    }
                });
            }
        });
    }

    private void chooseLocationThenAddSite(final ActivityFormDTO activity, final SiteDialogCallback callback) {
        LocationDialog dialog = new LocationDialog(dispatcher,
                activity.getLocationType());

        dialog.show(new LocationDialog.Callback() {

            @Override
            public void onSelected(LocationDTO location, boolean isNew) {
                SiteDTO newSite = new SiteDTO();
                newSite.setActivityId(activity.getId());
                newSite.setLocation(location);

                SiteDialog dialog = new SiteDialog(dispatcher, activity, eventBus);
                dialog.showNew(newSite, location, isNew, callback);
            }
        });
    }

    private void addNewSiteWithBoundLocation(ActivityFormDTO activity, SiteDialogCallback callback) {
        SiteDTO newSite = new SiteDTO();
        newSite.setActivityId(activity.getId());

        LocationDTO location = new LocationDTO();
        location.setId(new KeyGenerator().generateInt());
        location.setLocationTypeId(activity.getLocationTypeId());

        SiteDialog dialog = new SiteDialog(dispatcher, activity, eventBus);
        dialog.showNew(newSite, location, true, callback);
    }

    private void addNewSiteWithNoLocation(ActivityFormDTO activity, SiteDialogCallback callback) {
        SiteDTO newSite = new SiteDTO();
        newSite.setActivityId(activity.getId());

        LocationDTO location = new LocationDTO();
        location.setId(activity.getLocationTypeId());
        location.setLocationTypeId(activity.getLocationTypeId());

        SiteDialog dialog = new SiteDialog(dispatcher, activity, eventBus);
        dialog.showNew(newSite, location, true, callback);
    }
}
