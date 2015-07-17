package org.activityinfo.model.type;
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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author yuriyz on 8/13/14.
 */
public class TypeRegistryTest {

    /**
     * Check whether serialization/deserialization works for all parametrized types. If there is error in deserialization
     * code it may lead to form designer/form dialog failure. (User will see "Oh no, some bug occured...")
     */
    @Test
    public void serializationDeserialization() {
        for (FieldTypeClass typeClass : TypeRegistry.get().getTypeClasses()) {
            if (typeClass instanceof ParametrizedFieldTypeClass) {
                ParametrizedFieldTypeClass parametrizedFieldTypeClass = (ParametrizedFieldTypeClass) typeClass;
                ParametrizedFieldType parametrizedFieldType = (ParametrizedFieldType) parametrizedFieldTypeClass.createType();
                parametrizedFieldTypeClass.deserializeType(parametrizedFieldType.getParameters());

                // it's important to have "classId" specified for all parametrized types. Otherwise SimpleFormPanel will fail to load FormClass
                String classId = parametrizedFieldType.getParameters().getString("classId");
                Assert.assertNotNull(classId);
                Assert.assertTrue(classId.startsWith("_"));
            }
        }
    }
}
