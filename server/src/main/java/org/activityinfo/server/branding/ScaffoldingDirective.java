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
package org.activityinfo.server.branding;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import freemarker.core.Environment;
import freemarker.template.*;
import org.activityinfo.server.database.hibernate.entity.Domain;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class ScaffoldingDirective implements TemplateDirectiveModel {

    private Provider<Domain> domainProvider;
    private Configuration templateConfiguration;
    private Template defaultScaffoldingTemplate;

    @Inject
    public ScaffoldingDirective(Provider<Domain> domainProvider,
                                Configuration templateConfiguration) throws IOException {
        super();
        this.domainProvider = domainProvider;
        this.templateConfiguration = templateConfiguration;
        this.defaultScaffoldingTemplate = templateConfiguration.getTemplate("/page/DefaultScaffolding.ftl");
    }

    @Override
    public void execute(Environment env,
                        Map params,
                        TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {

        // write out the inner body to a string
        StringWriter bodyWriter = new StringWriter();
        body.render(bodyWriter);

        env.setVariable("body", new SimpleScalar(bodyWriter.toString()));
        env.setVariable("title", (TemplateModel) params.get("title"));
        env.include(getTemplate());

    }

    private Template getTemplate() {
        String scaffoldingTemplateSource = domainProvider.get().getScaffolding();
        if (Strings.isNullOrEmpty(scaffoldingTemplateSource)) {
            return defaultScaffoldingTemplate;
        } else {
            try {
                return new Template(domainProvider.get().getHost(), scaffoldingTemplateSource, templateConfiguration);
            } catch (IOException e) {
                throw new RuntimeException("Exception creating custom scaffolding template", e);
            }
        }
    }

}
