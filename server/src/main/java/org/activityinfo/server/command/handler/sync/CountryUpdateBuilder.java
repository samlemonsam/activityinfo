package org.activityinfo.server.command.handler.sync;


import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.database.hibernate.entity.LocationType;
import org.activityinfo.server.database.hibernate.entity.User;
import org.json.JSONException;

import javax.persistence.EntityManager;
import java.io.IOException;

public class CountryUpdateBuilder implements UpdateBuilder {

    public static final String REGION_PREFIX = "country/";
    private final EntityManager entityManager;

    @Inject
    public CountryUpdateBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws JSONException, IOException {

        int countryId = parseCountryId(request);
        
        JpaBatchBuilder batch = new JpaBatchBuilder(new SqliteBatchBuilder(), entityManager);
        
        String inCountry = "countryId = " + countryId;
        
        batch.insert(Country.class, inCountry);
        batch.insert(AdminLevel.class, inCountry);
        batch.delete(LocationType.class, inCountry);
        batch.insert(LocationType.class, inCountry);

        SyncRegionUpdate update = new SyncRegionUpdate();
        update.setComplete(true);
        update.setSql(batch.build());
        update.setVersion("1");
        return update;
    }

    private int parseCountryId(GetSyncRegionUpdates request) {
        if (!request.getRegionId().startsWith(REGION_PREFIX)) {
            throw new AssertionError("Expected region prefixed by '" + REGION_PREFIX +
                    "', got '" + request.getRegionId() + "'");
        }
        return Integer.parseInt(request.getRegionId().substring(REGION_PREFIX.length()));
    }
}
