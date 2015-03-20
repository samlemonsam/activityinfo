package org.activityinfo.test.capacity.scenario.coordination;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.action.CompositeAction;
import org.activityinfo.test.capacity.action.SyncOfflineWithApi;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.DatabaseBuilder;
import org.activityinfo.test.capacity.model.UserRole;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.Arrays;
import java.util.logging.Logger;

public class SectorLead implements UserRole {

    private static final Logger LOGGER = Logger.getLogger(SectorLead.class.getName());
    public static final int RETRY_COUNT = 3;

    private final Sector sector;
    
    private final AliasTable aliasTable = new AliasTable();
    private final DatabaseBuilder builder;

    public SectorLead(Sector sector) {
        this.sector = sector;
        builder = new DatabaseBuilder();

    }

    @Override
    public String getNickName() {
        return sector + " Sector Lead";
    }

    @Override
    public Optional<UserAction> getTask(int dayNumber) {
        if(dayNumber == 0) {
            // On the first day, setup the databases and invite useres
            return Optional.<UserAction>of(new CompositeAction(
                    new CreateDatabase(), 
                    SyncOfflineWithApi.INSTANCE, 
                    new InviteUsers(), 
                    SyncOfflineWithApi.INSTANCE));
            
        } else if(dayNumber < 5) {
            // Afterwards, for the next week, and a new form each day
            return Optional.<UserAction>of(new CompositeAction(
                    new CreateForms(2),
                    SyncOfflineWithApi.INSTANCE));
        } else {
            return Optional.absent();
        }
    }

    private class CreateDatabase implements UserAction {

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {
            builder.createDatabase(sector.getDatabaseName());
            builder.createForm();
            builder.createEnumeratedField(3);
            builder.createEnumeratedField(15);
            builder.createEnumeratedField(10);

            for(int indicatorIndex = 0; indicatorIndex < sector.getIndicatorCount(); ++indicatorIndex) {
                builder.createQuantityField();
            }
            builder.flush(driver.withNamespace(aliasTable).setRetryCount(RETRY_COUNT));
        }

        @Override
        public String toString() {
            return "CreateDatabase";
        }
    }
    
    private class InviteUsers implements UserAction {

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {

            // add the coordinator
            builder.addPartner(CoordinationScenario.COORDINATING_AGENCY);
            for (Sector otherSector : sector.getScenario().getSectors()) {
                if(otherSector != sector) {
                    builder.grantPermission(otherSector.getSectorLead(), "View", "View All");
                }
            }

            for(PartnerOrganization partner : sector.getScenario().getPartners()) {
                builder.addPartner(partner.getName());

                for (UserRole user : partner.getUsers()) {
                    builder.grantPermission(user.getNickName(), "View", "View All", "Edit");
                }
            }

            builder.flush(driver.withNamespace(aliasTable).setRetryCount(RETRY_COUNT));

            LOGGER.info(getNickName() + " invited users.");
        }

        @Override
        public String toString() {
            return "InviteUsers";
        }
    }
    
    private class CreateForms implements UserAction {
        private int count;

        public CreateForms(int count) {
            this.count = count;
        }

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {
            for(int i=0;i<count;++i) {
                builder.createForm();
                builder.createEnumeratedField(3);
                builder.createEnumeratedField(15);
                builder.createEnumeratedField(10);

                for (int indicatorIndex = 0; indicatorIndex < sector.getIndicatorCount(); ++indicatorIndex) {
                    builder.createQuantityField();
                }
                builder.flush(driver.withNamespace(aliasTable).setRetryCount(RETRY_COUNT));
            }
        }

        @Override
        public String toString() {
            return "CreateForms";
        }
    }

}
