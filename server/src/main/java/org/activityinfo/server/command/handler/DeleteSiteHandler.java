package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.DeleteSite;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.database.hibernate.entity.Site;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.util.Date;


public class DeleteSiteHandler implements CommandHandler<DeleteSite> {
    
    private final EntityManager entityManager;
    private final PermissionOracle permissionOracle;

    @Inject
    public DeleteSiteHandler(EntityManager entityManager, PermissionOracle permissionOracle) {
        this.entityManager = entityManager;
        this.permissionOracle = permissionOracle;
    }

    @Override
    public VoidResult execute(DeleteSite cmd, User user) throws CommandException {

        Site site = entityManager.find(Site.class, cmd.getSiteId());
        
        permissionOracle.assertEditAllowed(site, user);
        
        site.setDateDeleted(new Date());
        site.setVersion(site.getActivity().incrementSiteVersion());
        
        return VoidResult.EMPTY;
    }
}
