package org.activityinfo.store.spi;

import org.activityinfo.model.form.CatalogEntry;

import java.util.List;

/**
 * Interface to a provider of a hierarchy of folders and forms.
 */
public interface FormCatalog {

    List<CatalogEntry> getRootEntries();

    List<CatalogEntry> getChildren(String parentId, int userId);

}
