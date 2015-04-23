package org.activityinfo.server.endpoint.odk;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.command.ResourceLocatorSyncImpl;
import org.activityinfo.io.xform.manifest.MediaFile;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.server.endpoint.odk.build.XFormBuilder;
import org.activityinfo.io.xform.manifest.XFormManifest;
import org.activityinfo.service.store.ResourceNotFound;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.logging.Logger;


@Path("/forms")
public class FormResource {

    private static final Logger LOGGER = Logger.getLogger(FormResource.class.getName());

    /**
     * This is the filename expected by ODK
     */
    private static final String ITEMSETS_CSV = "itemsets.csv";

    private ResourceLocatorSyncImpl locator;
    private OdkFormFieldBuilderFactory factory;
    private Provider<AuthenticatedUser> authProvider;
    private AuthenticationTokenService authenticationTokenService;
    private ItemSetBuilder itemSetBuilder;

    @Inject
    public FormResource(ResourceLocatorSyncImpl locator, OdkAuthProvider authProvider, OdkFormFieldBuilderFactory factory,
                        AuthenticationTokenService authenticationTokenService, ItemSetBuilder itemSetBuilder) {
        this.locator = locator;
        this.authProvider = authProvider;
        this.factory = factory;
        this.authenticationTokenService = authenticationTokenService;
        this.itemSetBuilder = itemSetBuilder;
    }

    @VisibleForTesting
    FormResource(ResourceLocatorSyncImpl locator, Provider<AuthenticatedUser> authProvider, OdkFormFieldBuilderFactory factory,
                 AuthenticationTokenService authenticationTokenService) {
        this.locator = locator;
        this.authProvider = authProvider;
        this.factory = factory;
        this.authenticationTokenService = authenticationTokenService;
    }

    private FormClass fetchFormClass(int id) {
        FormClass formClass;
        try {
            formClass = locator.getFormClass(CuidAdapter.activityFormClass(id));
        } catch (ResourceNotFound resourceNotFound) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return formClass;
    }

    @GET
    @Path("{id}/xform")
    @Produces(MediaType.TEXT_XML)
    public Response form(@PathParam("id") int id) {

        AuthenticatedUser user = authProvider.get();

        LOGGER.finer("ODK activity form " + id + " requested by " +
                     user.getEmail() + " (" + user.getId() + ")");

        FormClass formClass = fetchFormClass(id);

        String authenticationToken = authenticationTokenService
                .createAuthenticationToken(user.getId(), formClass.getId());

        XForm xForm = new XFormBuilder(factory)
                .setUserId(authenticationToken)
                .build(formClass);

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
                .path(FormResource.class)
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
