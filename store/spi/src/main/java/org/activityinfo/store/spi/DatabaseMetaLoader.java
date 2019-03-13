package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loader interface for DatabaseMeta. DatabaseMeta are keyed by database {@link ResourceId}.
 */
public interface DatabaseMetaLoader {

    /**
     * Load requested DatabaseMeta from store.
     *
     * @param toLoad ResourceId of DatabaseMeta to load
     * @return Optional DatabaseMeta
     */
    @NotNull Optional<DatabaseMeta> load(@NotNull ResourceId toLoad);

    /**
     * Load all requested DatabaseMeta from store.
     *
     * @param toLoad Set of ResourceId of DatabaseMeta to load
     * @return Map of ResourceId of DatabaseMeta -> DatabaseMeta (if present)
     */
    @NotNull Map<ResourceId,DatabaseMeta> loadAll(@NotNull Set<ResourceId> toLoad);

}
