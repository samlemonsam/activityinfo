package org.activityinfo.store.server;

import com.google.common.base.Strings;
import org.activityinfo.model.form.CatalogEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * Serves the catalog of forms
 */
public class CatalogResource {

    private ApiBackend backend;

    public CatalogResource(ApiBackend backend) {
        this.backend = backend;
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public List<CatalogEntry> get(@QueryParam("parent") String parentId) {

        List<CatalogEntry> entries;
        if(Strings.isNullOrEmpty(parentId)) {
            return backend.getCatalog().getRootEntries();
        } else {
            return backend.getCatalog().getChildren(parentId, backend.getAuthenticatedUserId());
        }
    }

}
