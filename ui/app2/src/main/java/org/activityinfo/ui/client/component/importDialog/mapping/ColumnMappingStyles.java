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
package org.activityinfo.ui.client.component.importDialog.mapping;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.client.GWT;

/**
 * Placeholder until we have a better way to integrate LESS and gwt styles
 */
@Source("ColumnMapping.less")
public interface ColumnMappingStyles extends Stylesheet {

    public static final ColumnMappingStyles INSTANCE = GWT.create(ColumnMappingStyles.class);

    @ClassName("cm-datagrid")
    String grid();

    @ClassName("source-column")
    String sourceColumnHeader();

    @ClassName("mapping")
    String mappingHeader();

    @ClassName("state-ignored")
    String stateIgnored();

    @ClassName("state-bound")
    String stateBound();

    @ClassName("state-unset")
    String stateUnset();

    @ClassName("selected")
    String selected();

    @ClassName("cm-field-selector")
    String fieldSelector();

    @ClassName("incomplete")
    String incomplete();

    @ClassName("type-matched")
    String typeMatched();

    @ClassName("type-not-matched")
    String typeNotMatched();

}
