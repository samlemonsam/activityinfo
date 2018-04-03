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
package org.activityinfo.ui.client.page.config;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.GalleryView;
import org.activityinfo.ui.client.page.config.design.DbEditor;

import javax.validation.constraints.NotNull;

public class DbConfigPresenter implements DbPage {

    private final GalleryView view;

    public static final PageId PAGE_ID = new PageId("db");

    @Inject
    public DbConfigPresenter(GalleryView view, Dispatcher dispatcher) {
        this.view = view;
    }

    @Override
    public void go(UserDatabaseDTO db) {
        view.setHeading(!Strings.isNullOrEmpty(db.getName()) ? db.getName() : "");
        view.setIntro(!Strings.isNullOrEmpty(db.getFullName()) ? db.getFullName() : "");

        maybeAddDesignPage(db);
        maybeAddPartnerPage(db);
        maybeAddUsersPage(db);
        maybeAddLocksPage(db);
        maybeAddProjectPage(db);
        maybeAddTargetPage(db);

        if (view.getStore().getCount() == 0) {
            view.setPermissionsInfo(I18N.CONSTANTS.noDbDesignPermissions());
        }
    }

    /**
     * Viewable if user has Design Permissions
     */
    private void maybeAddDesignPage(@NotNull UserDatabaseDTO db) {
        if (db.isDesignAllowed()) {
            view.add(I18N.CONSTANTS.design(),
                    I18N.CONSTANTS.designDescription(),
                    "db-design.png",
                    new DbPageState(DbEditor.PAGE_ID, db.getId()));
        }
    }

    /**
     * Viewable if user has Design permissions and ALL folder access
     */
    private void maybeAddPartnerPage(@NotNull UserDatabaseDTO db) {
        if (db.isDesignAllowed() && !db.hasFolderLimitation()) {
            view.add(I18N.CONSTANTS.partner(),
                    I18N.CONSTANTS.partnerEditorDescription(),
                    "db-partners.png",
                    new DbPageState(DbPartnerEditor.PAGE_ID, db.getId()));
        }
    }

    /**
     * Viewable if user has Manage Users permissions
     */
    private void maybeAddUsersPage(@NotNull UserDatabaseDTO db) {
        if (db.isManageUsersAllowed()) {
            view.add(I18N.CONSTANTS.users(),
                    I18N.CONSTANTS.userManagerDescription(),
                    "db-users.png",
                    new DbPageState(DbUserEditor.PAGE_ID, db.getId()));
        }
    }

    /**
     * Viewable if user has Design permissions
     */
    private void maybeAddLocksPage(@NotNull UserDatabaseDTO db) {
        if (db.isDesignAllowed()) {
            view.add(I18N.CONSTANTS.timeLocks(),
                    I18N.CONSTANTS.lockPeriodsDescription(),
                    "db-lockedperiods.png",
                    new DbPageState(LockedPeriodsPresenter.PAGE_ID, db.getId()));
        }
    }

    /**
     * Viewable if user has Design permissions and ALL folder access
     */
    private void maybeAddProjectPage(@NotNull UserDatabaseDTO db) {
        if (db.isDesignAllowed() && !db.hasFolderLimitation()) {
            view.add(I18N.CONSTANTS.project(),
                    I18N.CONSTANTS.projectManagerDescription(),
                    "db-projects.png",
                    new DbPageState(DbProjectEditor.PAGE_ID, db.getId()));
        }
    }

    /**
     * Viewable if user has Design permissions
     */
    private void maybeAddTargetPage(@NotNull UserDatabaseDTO db) {
        if (db.isDesignAllowed()) {
            view.add(I18N.CONSTANTS.target(),
                    I18N.CONSTANTS.targetDescription(),
                    "db-targets.png",
                    new DbPageState(DbTargetEditor.PAGE_ID, db.getId()));
        }
    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return view;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        callback.onDecided(true);
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return false;
    }

    @Override
    public void shutdown() {
        //
    }
}
