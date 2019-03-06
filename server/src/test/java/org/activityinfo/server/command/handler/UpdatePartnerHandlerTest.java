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

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.UpdatePartner;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/partners.db.xml")
public class UpdatePartnerHandlerTest extends CommandTestCase2 {

    public static final int NFI_DATABASE = 1;
    public static final int HEALTH_DATABASE = 2;

    @Test
    public void modifyShared() {

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO nfiDatabase = schema.getDatabaseById(NFI_DATABASE);
        UserDatabaseDTO healthDatabase = schema.getDatabaseById(HEALTH_DATABASE);

        // The "Default" partner starts out being shared between the two database.
        PartnerDTO nfiDefaultPartner = nfiDatabase.getDefaultPartner().get();
        PartnerDTO healthDefaultPartner = healthDatabase.getDefaultPartner().get();
        assertThat(nfiDefaultPartner.getId(), equalTo(healthDefaultPartner.getId()));

        // If I update the name of "Default" partner for my database, a copy should
        // be created.
        PartnerDTO updatedPartner = new PartnerDTO(nfiDefaultPartner.getId(), "Solidarites");
        updatedPartner.setFullName("Solidarites International");

        execute(new UpdatePartner(1, updatedPartner));

        // Now we should have two distinct partner objects
        schema = execute(new GetSchema());
        nfiDatabase = schema.getDatabaseById(NFI_DATABASE);
        healthDatabase = schema.getDatabaseById(HEALTH_DATABASE);

        assertThat(nfiDatabase.getDatabasePartners(), containsInAnyOrder(
                hasProperty("name", equalTo("Solidarites")),
                hasProperty("name", equalTo("NRC"))));

        assertThat(healthDatabase.getDatabasePartners(), contains(
                hasProperty("name", equalTo("Default"))));

        // Users in the NFI database should be reassigned to the new NRC partner object
        UserResult nfiUsers = execute(new GetUsers(NFI_DATABASE));
        Optional<UserPermissionDTO> bavon = nfiUsers.getData().stream().filter(u -> u.getEmail().equals("bavon@nrc.org")).findAny();
        Optional<UserPermissionDTO> lisa = nfiUsers.getData().stream().filter(u -> u.getEmail().equals("lisa@solidarites")).findAny();

        // Bavon should stay as NRC
        assertThat(bavon.get().getPartners().size(), equalTo(1));
        assertThat(bavon.get().getPartners().get(0), hasProperty("name", equalTo("NRC")));

        // Lisa should be moved to the new Solidarites partner
        assertThat(lisa.get().getPartners().size(), equalTo(1));
        assertThat(lisa.get().getPartners().get(0), hasProperty("name", equalTo("Solidarites")));

        // Users in the Health database should be unaffected

        UserResult healthUsers = execute(new GetUsers(HEALTH_DATABASE));
        Optional<UserPermissionDTO> bavonInHealth = healthUsers.getData().stream().filter(u -> u.getEmail().equals("bavon@nrc.org")).findAny();

        assertThat(bavonInHealth.get().getPartners().size(), equalTo(1));
        assertThat(bavonInHealth.get().getPartners().get(0), hasProperty("name", equalTo("Default")));

        // Sites in the NFI database should be update to point to the new partner
        SiteDTO nfiSite1 = execute(GetSites.byId(1)).getData().get(0);
        SiteDTO nfiSite3 = execute(GetSites.byId(3)).getData().get(0);

        assertThat(nfiSite1.getPartner(), hasProperty("name", equalTo("Solidarites")));
        assertThat(nfiSite3.getPartner(), hasProperty("name", equalTo("NRC")));

        // Sites in the Health database should be unaffected

        SiteDTO healthSite4 = execute(GetSites.byId(4)).getData().get(0);
        assertThat(healthSite4.getPartner(), hasProperty("name", equalTo("Default")));
    }

    @Test
    public void newWithSameName() {

        PartnerDTO newNRC = new PartnerDTO();
        newNRC.setName("NRC");
        newNRC.setFullName("National Red Cross");

        execute(new UpdatePartner(HEALTH_DATABASE, newNRC));

        SchemaDTO schema = execute(new GetSchema());
        PartnerDTO nrc1 = schema
                .getDatabaseById(NFI_DATABASE)
                .getDatabasePartners().stream()
                .filter(p -> p.getName().equals("NRC"))
                .findAny()
                .get();
        PartnerDTO nrc2 = schema
                .getDatabaseById(HEALTH_DATABASE)
                .getDatabasePartners().stream()
                .filter(p -> p.getName().equals("NRC"))
                .findAny()
                .get();

        assertThat(nrc1.getId(), not(equalTo(nrc2.getId())));
        assertThat(nrc1.getFullName(), nullValue());
        assertThat(nrc2.getFullName(), equalTo("National Red Cross"));
    }
}