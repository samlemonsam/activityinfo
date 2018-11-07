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
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.Published;
import org.activityinfo.model.database.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.Permission;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.permission.PermissionQuery;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.server.endpoint.rest.DatabaseProviderImpl;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class to adapt legacy permission requests (e.g. isDesignAllowed) for {@link PermissionOracle}
 */
public class LegacyPermissionAdapter {

    private final DatabaseProvider provider;

    private static final Logger LOGGER = Logger.getLogger(LegacyPermissionAdapter.class.getName());

    @Inject
    public LegacyPermissionAdapter(Provider<EntityManager> em) {
        this.provider = new DatabaseProviderImpl(em);
    }

    public LegacyPermissionAdapter(EntityManager em) {
        this(Providers.of(em));
    }

    /**
     * Returns true if the given user is allowed to edit the values of the
     * given site.
     */
    public boolean isEditAllowed(Site site, User user) {
        return isEditSiteAllowed(user, site.getActivity(), site.getPartner());
    }

    public void assertEditAllowed(Site site, User user) {
        if(!isEditAllowed(site, user)) {
            LOGGER.severe(String.format("User %d does not have permission to edit" +
                    " site %d", user.getId(), site.getId()));
            throw new IllegalAccessCommandException();
        }
    }

    public boolean isEditSiteAllowed(User user, Activity activity, Partner partner) {
        Database database = activity.getDatabase();
        ResourceId databaseId = CuidAdapter.databaseId(database.getId());
        ResourceId activityId = activity.getFormId();
        UserDatabaseMeta db = provider.getDatabaseMetadata(databaseId, user.getId());
        PermissionQuery query = new PermissionQuery(user.getId(), database.getId(), Operation.EDIT_RECORD, activityId);
        Permission edit = PermissionOracle.query(query, db);

        if (edit.isPermitted() && !edit.isFiltered()) {
            return true;
        } else if (edit.isPermitted()){
            return PermissionOracle.filterContainsPartner(edit.getFilter(),
                    CuidAdapter.partnerFormId(database.getId()),
                    CuidAdapter.partnerRecordId(partner.getId()));
        } else {
            return false;
        }
    }

    public static int partnerFromFilter(String filter) {
        FormulaNode filterFormula = FormulaParser.parse(filter);
        FunctionCallNode equalFunctionCall = (FunctionCallNode) filterFormula;
        SymbolNode partnerFieldNode = (SymbolNode) equalFunctionCall.getArgument(1);
        return CuidAdapter.getLegacyIdFromCuid(partnerFieldNode.asResourceId());
    }

}
