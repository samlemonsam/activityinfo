/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.AddPartner;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.DuplicateCreateResult;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Alex Bertram
 * @see org.activityinfo.legacy.shared.command.AddPartner
 */
public class AddPartnerHandler implements CommandHandler<AddPartner> {

    private final EntityManager em;

    @Inject
    public AddPartnerHandler(EntityManager em) {
        this.em = em;
    }

    @Override @SuppressWarnings("unchecked")
    public CommandResult execute(AddPartner cmd, User user) {

        Database db = em.find(Database.class, cmd.getDatabaseId());
        PermissionOracle.using(em).assertManagePartnerAllowed(db, user);

        // first check to see if an organization by this name is already
        // a partner
        Set<Partner> dbPartners = db.getPartners();
        for (Partner partner : dbPartners) {
            if (partner.getName().equals(cmd.getPartner().getName())) {
                return new DuplicateCreateResult();
            }
        }

        // now try to match this partner by name
        List<Partner> allPartners = em.createQuery("select p from Partner p where p.name = ?1")
                                      .setParameter(1, cmd.getPartner().getName())
                                      .getResultList();


        Partner partner;
        if (!allPartners.isEmpty()) {
            partner = allPartners.get(0);

        } else {
            partner = new Partner();
            partner.setName(cmd.getPartner().getName());
            partner.setFullName(cmd.getPartner().getFullName());
            em.persist(partner);
        }


        db.setLastSchemaUpdate(new Date());
        em.persist(db);

        db.getPartners().add(partner);

        return new CreateResult(partner.getId());
    }
}
