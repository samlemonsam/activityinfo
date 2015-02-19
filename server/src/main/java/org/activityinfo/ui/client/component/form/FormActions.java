package org.activityinfo.ui.client.component.form;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.promise.Promise;

import java.util.List;

/**
 * @author yuriyz on 02/18/2015.
 */
public class FormActions {

    private final ResourceLocator locator;
    private final SimpleFormPanel panel;

    public FormActions(ResourceLocator locator, SimpleFormPanel panel) {
        this.locator = locator;
        this.panel = panel;
    }

    public Promise<Void> save() {
        List<IsResource> toPersist = Lists.newArrayList();
        toPersist.addAll(panel.getModel().getSubformPresentTabs()); // tab instances
        toPersist.addAll(panel.getModel().getSubFormInstances().values()); // subform values (binded to tab instances)
        toPersist.add(panel.getModel().getWorkingRootInstance()); // root instance
        return locator.persist(toPersist);
    }


}
