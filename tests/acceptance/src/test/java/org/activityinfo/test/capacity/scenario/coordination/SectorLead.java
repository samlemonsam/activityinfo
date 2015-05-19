package org.activityinfo.test.capacity.scenario.coordination;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.action.CompositeAction;
import org.activityinfo.test.capacity.action.SyncOfflineWithApi;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.DatabaseBuilder;
import org.activityinfo.test.capacity.model.UserRole;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.logging.Logger;

public class SectorLead implements UserRole {

    private static final Logger LOGGER = Logger.getLogger(SectorLead.class.getName());
    public static final int RETRY_COUNT = 3;

    private final Sector sector;

    private final DatabaseBuilder builder;
    private final List<List<String>> formsToCreate;

    public SectorLead(Sector sector) {
        this.sector = sector;
        builder = new DatabaseBuilder();
        formsToCreate = Lists.partition(sector.getActivityForms(), 3);
    }

    @Override
    public String getNickName() {
        return sector + " Sector Lead";
    }

    @Override
    public Optional<UserAction> getTask(int dayNumber) {
        
        switch(dayNumber) {
            case 0:
                // On the first day, setup the databases and invite users
                return Optional.<UserAction>of(new CompositeAction(
                        new CreateDatabase(),
                        new CreateForms(formsToCreate.get(0)),
                        SyncOfflineWithApi.INSTANCE,
                        new InviteUsers(),
                        SyncOfflineWithApi.INSTANCE));
            case 1:
                // Continue to create forms while reporting users are 
                // synchronizing and adding data to see the effects of the
                // these two activities occurring together
                return Optional.<UserAction>of(new CompositeAction(
                        new CreateForms(formsToCreate.get(1)),
                        new ExportDatabase(),
                        SyncOfflineWithApi.INSTANCE));
            case 2:
                return Optional.<UserAction>of(new CompositeAction(
                        new CreateForms(formsToCreate.get(2)),
                        new ExportDatabase(),
                        SyncOfflineWithApi.INSTANCE));

            default:
                return Optional.<UserAction>of(new CompositeAction(
                        SyncOfflineWithApi.INSTANCE,
                        new ExportDatabase()));

        }
    }

    private class CreateDatabase implements UserAction {

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {
            builder.createDatabase(sector.getDatabaseName());
            builder.flush(driver.setRetryCount(RETRY_COUNT));
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
                builder.flush(driver.setRetryCount(RETRY_COUNT));
            }

            builder.flush(driver.setRetryCount(RETRY_COUNT));

            LOGGER.info(getNickName() + " invited users.");
        }

        @Override
        public String toString() {
            return "InviteUsers";
        }
    }

    private class CreateForms implements UserAction {
        private List<String> forms;

        public CreateForms(List<String> forms) {
            this.forms = forms;
        }

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {
            for(String form : forms) {
                builder.createForm(form);
                builder.createEnumeratedField(3);
                builder.createEnumeratedField(15);
                builder.createEnumeratedField(10);

                for (int indicatorIndex = 0; indicatorIndex < sector.getIndicatorCount(); ++indicatorIndex) {
                    builder.createQuantityField(sector.getIndicatorName(form, indicatorIndex));
                }
                builder.flush(driver.setRetryCount(RETRY_COUNT));
            }
        }

        @Override
        public String toString() {
            return "CreateForms";
        }
    }
    
    private class ExportDatabase implements UserAction {

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {
            File file = driver.exportDatabase(sector.getDatabaseName());
            try {
                HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
                
                LOGGER.info(getNickName() + " exported results to Excel [" + workbook.getNumberOfSheets() + " sheets]");
                
            } finally {
                boolean deleted = file.delete();
                if(!deleted) {
                    LOGGER.fine("Failed to delete temporary file " + file);
                }
            }

        }
    }

}
