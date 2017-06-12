package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * Internal interface for the MySqlCatalog, which helps to delegate FormStorage implementation
 * based on the domain of the form id. This is in place because different kinds of forms, such as 
 * "sites", "targets", "locations", are stored in very differently-shaped tables.
 */
public interface FormProvider {

    /**
     * True if this provider can handle the given {@code formId}
     */
    boolean accept(ResourceId formId);

    FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException;

    Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException;
}
