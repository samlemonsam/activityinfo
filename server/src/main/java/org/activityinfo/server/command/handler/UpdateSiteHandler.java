package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.CreateSite;
import org.activityinfo.legacy.shared.command.UpdateSite;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.database.hibernate.entity.User;


public class UpdateSiteHandler implements CommandHandler<UpdateSite> {
    
    private final CreateSiteHandler createSiteHandler;

    @Inject
    public UpdateSiteHandler(CreateSiteHandler createSiteHandler) {
        this.createSiteHandler = createSiteHandler;
    }


    @Override
    public CommandResult execute(UpdateSite cmd, User user) throws CommandException {
        CreateSite createSite = new CreateSite(cmd.getProperties().getTransientMap());
        createSite.getProperties().put("id", cmd.getSiteId());
        createSiteHandler.execute(createSite, user);
        
        return VoidResult.EMPTY;
    }
}
