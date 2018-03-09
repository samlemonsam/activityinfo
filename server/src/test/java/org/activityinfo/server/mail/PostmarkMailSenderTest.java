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
package org.activityinfo.server.mail;

import com.google.inject.util.Providers;
import freemarker.template.TemplateModelException;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.database.hibernate.entity.Domain;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.TemplateModule;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class PostmarkMailSenderTest {

    private PostmarkMailSender sender;

    @Before
    public void setUp() throws TemplateModelException {
        Properties properties = new Properties();
        properties.setProperty(PostmarkMailSender.POSTMARK_API_KEY, "POSTMARK_API_TEST");
        DeploymentConfiguration config = new DeploymentConfiguration(properties);

        TemplateModule templateModule = new TemplateModule();

        sender = new PostmarkMailSender(config, templateModule.provideConfiguration(Providers.of(Domain.DEFAULT)));
    }

    @Test
    public void textEmail() {
        User user = new User();
        user.setChangePasswordKey("xyz123");
        user.setName("Alex");
        user.setEmail("akbertram@gmail.com");

        ResetPasswordMessage model = new ResetPasswordMessage(user);
        sender.send(model);
    }


}
