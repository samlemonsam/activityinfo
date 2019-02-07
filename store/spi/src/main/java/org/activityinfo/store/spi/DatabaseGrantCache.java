package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.database.DatabaseGrantKey;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cache interface for DatabaseGrants
 */
public interface DatabaseGrantCache {

    /**
     * Load requested DatabaseGrant from Cache.
     *
     * @param toLoad DatabaseGrantKey of DatabaseGrant to load
     * @return Optional DatabaseGrant
     */
    @NotNull Optional<DatabaseGrant> load(@NotNull DatabaseGrantKey toLoad);

    /**
     * Load all requested DatabaseGrants from Cache.
     *
     * @param toLoad Set of DatabaseGrantKey of DatabaseGrants to load
     * @return Map of DatabaseGrantKey -> DatabaseGrant (if present)
     */
    @NotNull Map<DatabaseGrantKey,DatabaseGrant> loadAll(@NotNull Set<DatabaseGrantKey> toLoad);

}
