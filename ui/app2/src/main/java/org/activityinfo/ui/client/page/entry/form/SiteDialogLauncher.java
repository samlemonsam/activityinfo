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
package org.activityinfo.ui.client.page.entry.form;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.ui.client.App3;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.entry.location.LocationDialog;

public class SiteDialogLauncher {

    private final Dispatcher dispatcher;

    public SiteDialogLauncher(Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
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

                    if(!activity.getClassicView()) {
                        promptUseNewEntry(activity);
                        return;
                    }

                    Log.trace("adding site for activity " + activity + ", locationType = " + activity.getLocationType());

                    if(activity.getDatabase().getDatabasePartners().isEmpty()) {
                        // Since we are creating a partner by default for every database,
                        // this shouldn't happen beyond the development environment
                        MessageBox.alert(I18N.CONSTANTS.error(), I18N.CONSTANTS.noPartners(), null);
                        return;
                    }

                    LockedPeriodSet locks = new LockedPeriodSet(activity.getDatabase());

                    dispatcher.execute(new GetActivityForm(activityId)).then(new AsyncCallback<ActivityFormDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            showError(caught);
                        }

                        @Override
                        public void onSuccess(ActivityFormDTO activityForm) {

                            if (activityForm.getLocationType().isAdminLevel()) {
                                addNewSiteWithBoundLocation(locks, activityForm, callback);

                            } else if (activityForm.getLocationType().isNationwide()) {
                                addNewSiteWithNoLocation(locks, activityForm, callback);

                            } else {
                                chooseLocationThenAddSite(locks, activityForm, callback);
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


    public void editSite(final SiteDTO site, final SiteDialogCallback callback) {
        dispatcher.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                showError(caught);
            }

            @Override
            public void onSuccess(SchemaDTO schema) {
                final ActivityDTO activity = schema.getActivityById(site.getActivityId());

                if(!activity.getClassicView()) {
                    promptUseNewEntry(activity);
                    return;
                }


                // check whether the site has been locked
                // (this only applies to Once-reported activities because
                //  otherwise the date criteria applies to the monthly report)
                LockedPeriodSet locks = new LockedPeriodSet(schema);
                if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
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
                    public void onSuccess(ActivityFormDTO activity) {
                        SiteDialog dialog = new SiteDialog(dispatcher, locks, activity, false);
                        dialog.showExisting(site, callback);
                    }
                });
            }
        });
    }

    private void chooseLocationThenAddSite(LockedPeriodSet locks, final ActivityFormDTO activity, final SiteDialogCallback callback) {
        LocationDialog locationDialog = new LocationDialog(dispatcher,
                activity.getLocationType());

        locationDialog.show((location, isNew) -> {
            SiteDTO newSite = new SiteDTO();
            newSite.setActivityId(activity.getId());
            newSite.setLocation(location);

            SiteDialog siteDialog = new SiteDialog(dispatcher, locks, activity, true);
            siteDialog.showNew(newSite, location, isNew, callback);
        });
    }

    private void addNewSiteWithBoundLocation(LockedPeriodSet locks, ActivityFormDTO activity, SiteDialogCallback callback) {
        SiteDTO newSite = new SiteDTO();
        newSite.setActivityId(activity.getId());

        LocationDTO location = new LocationDTO();
        location.setId(new KeyGenerator().generateInt());
        location.setLocationTypeId(activity.getLocationTypeId());

        SiteDialog dialog = new SiteDialog(dispatcher, locks, activity, true);
        dialog.showNew(newSite, location, true, callback);
    }

    private void addNewSiteWithNoLocation(LockedPeriodSet locks, ActivityFormDTO activity, SiteDialogCallback callback) {
        SiteDTO newSite = new SiteDTO();
        newSite.setActivityId(activity.getId());

        LocationDTO location = new LocationDTO();
        location.setId(activity.getLocationTypeId());
        location.setLocationTypeId(activity.getLocationTypeId());

        SiteDialog dialog = new SiteDialog(dispatcher, locks, activity, true);
        dialog.showNew(newSite, location, true, callback);
    }



    private static void promptUseNewEntry(final ActivityDTO dto) {
        MessageBox box = new MessageBox();
        box.setTitle(dto.getName());
        box.setMessage(SafeHtmlUtils.fromString(I18N.CONSTANTS.pleaseUseNewDataEntry()));
        box.setButtons(MessageBox.OKCANCEL);
        box.addCallback(messageBoxEvent -> {
            if(messageBoxEvent.getButtonClicked().getItemId().equals(MessageBox.OK)) {
                App3.openNewTable(dto.getFormId());
            }
        });
        box.show();
    }
}
