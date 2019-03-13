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
package org.activityinfo.server.endpoint.odk;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.io.xform.manifest.MediaFile;
import org.activityinfo.io.xform.manifest.XFormManifest;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.server.endpoint.odk.build.XFormBuilder;
import org.activityinfo.store.query.UsageTracker;
import org.activityinfo.store.spi.UserDatabaseProvider;
import org.activityinfo.store.spi.FormNotFoundException;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.logging.Logger;


@Path("/forms")
public class XFormResources {

    private static final Logger LOGGER = Logger.getLogger(XFormResources.class.getName());

    private ResourceLocatorSyncImpl locator;
    private OdkFormFieldBuilderFactory factory;
    private Provider<AuthenticatedUser> authProvider;
    private AuthenticationTokenService authenticationTokenService;
    private ItemSetBuilder itemSetBuilder;
    private UserDatabaseProvider userDatabaseProvider;

    @Inject
    public XFormResources(ResourceLocatorSyncImpl locator,
                          OdkAuthProvider authProvider,
                          OdkFormFieldBuilderFactory factory,
                          AuthenticationTokenService authenticationTokenService,
                          ItemSetBuilder itemSetBuilder,
                          UserDatabaseProvider userDatabaseProvider) {
        this.locator = locator;
        this.authProvider = authProvider;
        this.factory = factory;
        this.authenticationTokenService = authenticationTokenService;
        this.itemSetBuilder = itemSetBuilder;
        this.userDatabaseProvider = userDatabaseProvider;
    }

    @VisibleForTesting
    XFormResources(ResourceLocatorSyncImpl locator,
                   Provider<AuthenticatedUser> authProvider,
                   OdkFormFieldBuilderFactory factory,
                   AuthenticationTokenService authenticationTokenService,
                   UserDatabaseProvider userDatabaseProvider) {
        this.locator = locator;
        this.authProvider = authProvider;
        this.factory = factory;
        this.authenticationTokenService = authenticationTokenService;
        this.userDatabaseProvider = userDatabaseProvider;
    }

    private FormClass fetchFormClass(int id) {
        FormClass formClass;
        try {
            formClass = locator.getFormClass(CuidAdapter.activityFormClass(id));
        } catch (FormNotFoundException formNotFoundException) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return formClass;
    }

    private FormPermissions fetchFormPermissions(int id) {
        FormPermissions formPermissions;
        try {
            formPermissions = userDatabaseProvider
                    .getDatabaseMetadataByResource(CuidAdapter.activityFormClass(id), authProvider.get().getUserId())
                    .map(db -> PermissionOracle.formPermissions(CuidAdapter.activityFormClass(id), db))
                    .orElseThrow(FormNotFoundException::new);
        } catch (FormNotFoundException formNotFoundException) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return formPermissions;
    }

    @GET
    @Path("{id}/xform")
    @Produces(MediaType.TEXT_XML)
    public Response form(@PathParam("id") int id) {

        AuthenticatedUser user = authProvider.get();

        LOGGER.finer("ODK activity form " + id + " requested by " +
                     user.getEmail() + " (" + user.getId() + ")");

        FormClass formClass = fetchFormClass(id);
        FormPermissions formPermissions = fetchFormPermissions(id);

        if (!formPermissions.isCreateAllowed()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        UsageTracker.track(user.getUserId(), "odk_download", formClass);

        String authenticationToken = authenticationTokenService
                .createAuthenticationToken(user.getId(), formClass.getId());

        XForm xForm = new XFormBuilder(factory)
                .setUserId(authenticationToken)
                .build(formClass, formPermissions);

        return Response.ok(xForm).build();
    }


    @GET
    @Path("{id}/manifest")
    @Produces(MediaType.TEXT_XML)
    public Response manifest(@Context UriInfo uri, @PathParam("id") int id) {

        AuthenticatedUser user = authProvider.get();

        LOGGER.finer("ODK manifest for " + id + " requested by " +
                user.getEmail() + " (" + user.getId() + ")");

        MediaFile itemSet = new MediaFile();
        itemSet.setFilename("itemsets.csv");
        itemSet.setHash("md5:00000000000000000000000000000000");
        itemSet.setDownloadUrl(uri.getBaseUriBuilder()
                .path(XFormResources.class)
                .path(Integer.toString(id))
                .path("itemsets.csv")
                .build());

        XFormManifest manifest = new XFormManifest();

        return OpenRosaResponse.build(manifest);
    }

    @GET
    @Path("{id}/itemsets.csv")
    @Produces(MediaType.TEXT_PLAIN)
    public Response itemSet(@PathParam("id") int id) throws IOException {

        authProvider.get();

        return Response.ok(itemSetBuilder.build(CuidAdapter.activityFormClass(id)))
                .type("text/plain; charset=utf-8")
                .build();
    }
}
