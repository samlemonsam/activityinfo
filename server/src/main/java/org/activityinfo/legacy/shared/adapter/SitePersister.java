package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBinding;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBindingFactory;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists a FormInstance as a Site
 */
public class SitePersister {

    private final Dispatcher dispatcher;

    public SitePersister(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Promise<Void> persist(final FormInstance siteInstance) {

        int activityId = CuidAdapter.getLegacyIdFromCuid(siteInstance.getClassId());
        return dispatcher.execute(new GetActivityForm(activityId))
                         .then(new SiteBindingFactory())
                         .join(new Function<SiteBinding, Promise<Void>>() {
                             @Nullable @Override
                             public Promise<Void> apply(@Nullable SiteBinding binding) {
                                 return persist(binding, siteInstance).thenDiscardResult();
                             }
                         });
    }

    private Promise<? extends CommandResult> persist(SiteBinding siteBinding, FormInstance instance) {

        Map<String, Object> siteProperties = siteBinding.toChangePropertyMap(instance);
        siteProperties.put("activityId", siteBinding.getActivity().getId());
        if (siteProperties.get("reportingPeriodId") == null) {  // indicators are not saved if report id is not set
            siteProperties.put("reportingPeriodId", new KeyGenerator().generateInt());
        }

        if (siteProperties.get("partnerId") == null) {
            siteProperties.put("partnerId", siteBinding.getDefaultPartnerId());
        }

        if (siteBinding.getLocationType().isNationwide()) {
            siteProperties.put("locationId", siteBinding.getLocationType().getId());
        }

        
        final CreateSite createSite = new CreateSite(siteProperties);

        if (siteBinding.getLocationType().isAdminLevel()) {
            // we need to create the dummy location as well
            Promise<Command> createLocation = Promise.resolved(siteBinding.getAdminEntityId(instance))
                                                     .join(new FetchEntityFunction())
                                                     .then(new CreateDummyLocation(createSite.getLocationId(),
                                                             siteBinding.getLocationType()));

            return createLocation.join(new Function<Command, Promise<BatchResult>>() {
                @Nullable @Override
                public Promise<BatchResult> apply(@Nullable Command createLocation) {
                    return dispatcher.execute(new BatchCommand(createLocation, createSite));
                }
            });

        } else {
            return dispatcher.execute(createSite);
        }
    }

    private class FetchEntityFunction implements Function<Integer, Promise<List<AdminEntityDTO>>> {

        @Nullable @Override
        public Promise<List<AdminEntityDTO>> apply(@Nullable Integer input) {
            GetAdminEntities query = new GetAdminEntities().setEntityId(input);

            Promise<AdminEntityDTO> entity = dispatcher.execute(query)
                                                       .then(new SingleListResultAdapter<AdminEntityDTO>());

            Promise<List<AdminEntityDTO>> parents = entity.join(new FetchParentsFunction());

            return Promise.prepend(entity, parents);
        }
    }

    private class FetchParentsFunction implements Function<AdminEntityDTO, Promise<List<AdminEntityDTO>>> {

        @Override
        public Promise<List<AdminEntityDTO>> apply(AdminEntityDTO input) {
            if (input.getParentId() == null) {
                return Promise.resolved(Collections.<AdminEntityDTO>emptyList());
            } else {
                return Promise.resolved(input.getParentId()).join(new FetchEntityFunction());
            }
        }
    }

    private class CreateDummyLocation implements Function<List<AdminEntityDTO>, Command> {

        private final LocationTypeDTO locationType;
        private int locationId;

        private CreateDummyLocation(int locationId, LocationTypeDTO locationType) {
            this.locationType = locationType;
            this.locationId = locationId;
        }

        @Override
        public CreateLocation apply(List<AdminEntityDTO> entities) {

            AdminEntityDTO entity = entities.get(0);
            Preconditions.checkState(entity.getLevelId() == locationType.getBoundAdminLevelId());

            Map<String, Object> properties = new HashMap<>();
            properties.put("id", locationId);
            properties.put("locationTypeId", locationType.getId());
            properties.put("name", entity.getName());

            for (AdminEntityDTO parent : entities) {
                properties.put(AdminLevelDTO.getPropertyName(parent.getLevelId()), parent.getId());
            }

            return new CreateLocation(properties);
        }
    }
}
