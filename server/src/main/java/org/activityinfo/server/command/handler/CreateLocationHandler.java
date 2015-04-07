package org.activityinfo.server.command.handler;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.CreateLocation;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.database.hibernate.entity.AdminEntity;
import org.activityinfo.server.database.hibernate.entity.Location;
import org.activityinfo.server.database.hibernate.entity.LocationType;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

/**
 * Server-side create location handler.
 */
public class CreateLocationHandler implements CommandHandler<CreateLocation> {
    
    private final EntityManager entityManager;

    @Inject
    public CreateLocationHandler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public VoidResult execute(CreateLocation cmd, User user) throws CommandException {

        LocationType locationType = entityManager.find(LocationType.class, cmd.getLocationTypeId());
        if(locationType == null) {
            throw new CommandException("LocationType " + cmd.getLocationTypeId() + " does not exist");
        }

        PropertyMap propertyMap = new PropertyMap(cmd.getProperties());
        
        List<Location> existingLocation = entityManager
                .createQuery("SELECT L from Location L LEFT JOIN FETCH L.adminEntities WHERE L.id = :id", Location.class)
                .setParameter("id", cmd.getLocationId())
                .getResultList();
        
        if(existingLocation.isEmpty()) {
            Location location = new Location();
            location.setId(cmd.getLocationId());
            location.setLocationType(locationType);
            location.setVersion(locationType.incrementVersion());
            applyProperties(location, propertyMap);
            entityManager.persist(location);

        } else {
            Location location = existingLocation.get(0);
            location.setVersion(locationType.incrementVersion());
            if(location.getLocationType().getId() != locationType.getId()) {
                throw new CommandException("LocationType of a location cannot be changed");
            }
            applyProperties(location, propertyMap);
        }
        
        return VoidResult.EMPTY;
    }


    private void applyProperties(Location location, PropertyMap propertyMap) {
        location.setName(propertyMap.getString("name"));
        location.setAxe(propertyMap.getOptionalString("axe"));
        location.setAdminEntities(adminMembership(propertyMap));
        
        Double longitude = propertyMap.getOptionalDouble("longitude");
        Double latitude = propertyMap.getOptionalDouble("latitude");
        if(latitude != null && longitude != null) {
            location.setX(longitude);
            location.setY(latitude);
        } else {
            location.setX(null);
            location.setY(null);
        }

        location.setVersion(location.getLocationType().incrementVersion());
    }

    private Set<AdminEntity> adminMembership(PropertyMap propertyMap) {
        Set<AdminEntity> set = Sets.newHashSet();
        for (String property : propertyMap.getNames()) {
            if (property.startsWith(AdminLevelDTO.PROPERTY_PREFIX)) {
                set.add(entityManager.getReference(AdminEntity.class,
                        propertyMap.getRequiredInt(property)));
            }
        }
        return set;
    }

}
