package org.activityinfo.model.type.subform;
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

import org.activityinfo.model.resource.ResourceId;

/**
 * @author yuriyz on 02/06/2015.
 */
public class SubformConstants {

    /**
     * In subform case we need to keep type of FormClass. Since we want to keep FormClass to model SubForms
     * solution is to store type via FormField that keeps reference on FormClass that describes type (e.g. Period or Territory)
     */
    public static final ResourceId TYPE_FIELD_ID = ResourceId.valueOf("_subform_class_type");

    /**
     * Tabs count on subform.
     */
    public static final ResourceId TAB_COUNT_FIELD_ID = ResourceId.valueOf("_subform_tab_count");

    public static final int DEFAULT_TAB_COUNT = 4;

    private SubformConstants() {
    }
}
