package org.activityinfo.server.endpoint.rest;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.inject.Provider;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.service.store.FormCatalog;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * Serves the catalog of formsorm
 */
public class CatalogResource {

    private Provider<FormCatalog> catalog;

    public CatalogResource(Provider<FormCatalog> catalog) {
        this.catalog = catalog;
    }

    @GET
    public Response get(@QueryParam("parent") String parentId) {

        List<CatalogEntry> entries;
        if(Strings.isNullOrEmpty(parentId)) {
            entries = catalog.get().getRootEntries();
        } else {
            entries = catalog.get().getChildren(parentId);
        }
        return Response.ok()
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(toJson(entries)).build();
    }

    private String toJson(List<CatalogEntry> entries) {
        JsonArray array = new JsonArray();
        for (CatalogEntry entry : entries) {
            array.add(entry.toJsonElement());
        }
        return array.toString();
    }

}
