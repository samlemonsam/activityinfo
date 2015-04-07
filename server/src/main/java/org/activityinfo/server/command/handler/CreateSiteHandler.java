package org.activityinfo.server.command.handler;

import org.activityinfo.legacy.shared.command.CreateSite;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.AttributeDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;


public class CreateSiteHandler implements CommandHandler<CreateSite> {
    
    private final EntityManager entityManager;
    private final PermissionOracle permissionOracle;

    @Inject
    public CreateSiteHandler(EntityManager entityManager, PermissionOracle permissionOracle) {
        this.entityManager = entityManager;
        this.permissionOracle = permissionOracle;
    }

    @Override
    public CommandResult execute(CreateSite cmd, User user) throws CommandException {
        
        
        PropertyMap propertyMap = new PropertyMap(cmd.getProperties());

        Site site = entityManager.find(Site.class, cmd.getSiteId());
        if(site == null) {
            Activity activity = activityReference(cmd);
            Partner partner = partnerReference(cmd);

            permissionOracle.assertEditSiteAllowed(user, activity, partner);

            site = new Site();
            site.setId(cmd.getSiteId());    
            site.setActivity(activity);
            site.setPartner(partner);
            site.setVersion(activity.incrementSiteVersion());
            applyProperties(site, propertyMap);
            validateProperties(site);

            entityManager.persist(site);
            updateAttributeValues(site, propertyMap);

            
            if(site.getActivity().getReportingFrequency() == ActivityDTO.REPORT_ONCE &&
                propertyMap.containsKey("reportingPeriodId")) {
                
                newReportingPeriod(site, propertyMap);
            }


        } else {
            permissionOracle.assertEditAllowed(site, user);
            
            if(propertyMap.containsKey("partnerId")) {
                Partner newPartner = partnerReference(cmd);
                permissionOracle.assertEditSiteAllowed(user, site.getActivity(), newPartner);
                site.setPartner(newPartner);
            }

            site.setVersion(site.getActivity().incrementSiteVersion());
            applyProperties(site, propertyMap);
            updateAttributeValues(site, propertyMap);

            if(site.getActivity().getReportingFrequency() == ActivityDTO.REPORT_ONCE) {
                updateUniqueReportingPeriod(site, propertyMap);
            }
            
            validateProperties(site);
        }

        return new CreateResult(cmd.getSiteId());
    }

    private void updateUniqueReportingPeriod(Site site, PropertyMap propertyMap) {
        ReportingPeriod period;
        if(!site.getReportingPeriods().isEmpty()) {
            period = site.getReportingPeriods().iterator().next();
        } else {
            period = new ReportingPeriod(site);
        }
        period.setDate1(site.getDate1());
        period.setDate2(site.getDate2());        
    
        updateIndicatorValues(period, propertyMap);
    }

    private Activity activityReference(CreateSite cmd) {
        Activity activity = entityManager.find(Activity.class, cmd.getActivityId());
        if(activity == null) {
            throw new CommandException("No activity with id " + cmd.getActivityId());
        }
        return activity;
    }

    private void newReportingPeriod(Site site, PropertyMap propertyMap) {
        ReportingPeriod period = new ReportingPeriod(site);
        period.setDate1(propertyMap.getDate("date1"));
        period.setDate2(propertyMap.getDate("date2"));
        period.setId(propertyMap.getRequiredInt("reportingPeriodId"));
        entityManager.persist(period);

        updateIndicatorValues(period, propertyMap);

    }

    private void validateProperties(Site site) {
        if(site.getLocation() == null) {
            throw new CommandException("LocationId is required.");
        }
    }

    private void applyProperties(Site site, PropertyMap propertyMap) {
        if(propertyMap.containsKey("locationId")) {
            site.setLocation(locationReference(propertyMap));
        } 
        if(propertyMap.containsKey("date1")) {
            site.setDate1(propertyMap.getDate("date1"));
        }
        if(propertyMap.containsKey("date2")) {
            site.setDate2(propertyMap.getDate("date2"));
        }
        if(propertyMap.containsKey("comments")) {
            site.setComments(propertyMap.getString("comments"));
        }
        if(propertyMap.containsKey("projectId")) {
            if (propertyMap.get("projectId") == null) {
                site.setProject(null);
            } else {
                Project project = entityManager.find(Project.class, propertyMap.getRequiredInt("projectId"));
                site.setProject(project);
//                if (project.getUserDatabase().getId() != site.getActivity().getDatabase().getId()) {
//                    throw new CommandException(String.format("Project %d does not belong to database %d",
//                            project.getId(), site.getActivity().getDatabase().getId()));
//                }
            }
        }
    }

    private Location locationReference(PropertyMap cmd) {
        Location location = entityManager.find(Location.class, cmd.getRequiredInt("locationId"));
        if (location == null) {
            throw new CommandException("No location with id " + cmd.getRequiredInt("locationId"));
        }
        return location;
    }

    private Partner partnerReference(CreateSite cmd) {
        Partner partner = entityManager.find(Partner.class, cmd.getPartnerId());
        if(partner == null) {
            throw new CommandException("No partner with id " + cmd.getPartnerId());
        }
        return partner;
    }
    
    private void updateIndicatorValues(ReportingPeriod period, PropertyMap propertyMap) {
        
        Map<Integer, IndicatorValue> existingValues = new HashMap<>();
        for (IndicatorValue value : period.getIndicatorValues()) {
            existingValues.put(value.getIndicator().getId(), value);
        }
        
        for (String property : propertyMap.getNames()) {
            if(property.startsWith(IndicatorDTO.PROPERTY_PREFIX)) {

                int indicatorId = IndicatorDTO.indicatorIdForPropertyName(property);
                Object value = propertyMap.get(property);

                IndicatorValue valueEntity = existingValues.get(indicatorId);

                if (valueEntity == null) {
                    if (value != null) {
                        Indicator indicator = entityManager.getReference(Indicator.class, indicatorId);
                        valueEntity = new IndicatorValue(period, indicator);
                        valueEntity.setValue(value);
                        entityManager.persist(valueEntity);
                    }
                } else {
                    valueEntity.setValue(value);
                }
            }            
        }
    }
    
    private void updateAttributeValues(Site site, PropertyMap propertyMap) {


        Map<Integer, AttributeValue> existingValues = new HashMap<>();
        for (AttributeValue value : site.getAttributeValues()) {
            existingValues.put(value.getAttribute().getId(), value);
        }

        for (String property : propertyMap.getNames()) {
            if (property.startsWith(AttributeDTO.PROPERTY_PREFIX)) {

                int id = AttributeDTO.idForPropertyName(property);
                Boolean value = propertyMap.get(property);
                
                AttributeValue valueEntity = existingValues.get(id);
                if(valueEntity == null) {
                    if(value == Boolean.TRUE) {
                        valueEntity = new AttributeValue(site, entityManager.getReference(Attribute.class, id), true);
                        entityManager.persist(valueEntity);
                    } 
                } else {
                    valueEntity.setValue(value == Boolean.TRUE);
                }
            }
        }
    }

}
