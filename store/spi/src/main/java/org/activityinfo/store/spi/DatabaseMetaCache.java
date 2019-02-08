package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cache interface for DatabaseMeta
 */
public interface DatabaseMetaCache {

    /**
     * Load requested DatabaseMeta from Cache.
     *
     * @param toLoad ResourceId of DatabaseMeta to load
     * @return Optional DatabaseMeta
     */
    @NotNull Optional<DatabaseMeta> load(@NotNull ResourceId toLoad);

    /**
     * Load all requested DatabaseMeta from Cache.
     *
     * @param toLoad Set of ResourceId of DatabaseMeta to load
     * @return Map of ResourceId of DatabaseMeta -> DatabaseMeta (if present)
     */
    @NotNull Map<ResourceId,DatabaseMeta> loadAll(@NotNull Set<ResourceId> toLoad);

}
