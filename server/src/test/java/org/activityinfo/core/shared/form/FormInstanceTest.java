package org.activityinfo.core.shared.form;
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

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yuriyz on 7/30/14.
 */
public class FormInstanceTest {

    @Test
    public void test() {
        FormInstance instance = new FormInstance(ResourceId.valueOf("id"), ResourceId.valueOf("classId"));
        instance.set(ResourceId.valueOf("text"), "text");
        instance.set(ResourceId.valueOf("quantity"), 1);
        instance.set(ResourceId.valueOf("boolean"), true);

        Resource resource = instance.asResource();

        FormInstance fromResource = FormInstance.fromResource(resource);

        Assert.assertEquals(fromResource.getId(), instance.getId());
        Assert.assertEquals(fromResource.getClassId(), instance.getClassId());
        assertValue(fromResource, instance, ResourceId.valueOf("text"));
        assertValue(fromResource, instance, ResourceId.valueOf("quantity"));
        assertValue(fromResource, instance, ResourceId.valueOf("boolean"));
    }

    public void assertValue(FormInstance fromResource, FormInstance instance, ResourceId valueId) {
        Assert.assertEquals(fromResource.get(valueId), instance.get(valueId));
    }
}
