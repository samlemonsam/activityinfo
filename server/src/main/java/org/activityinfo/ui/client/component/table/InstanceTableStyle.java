package org.activityinfo.ui.client.component.table;
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

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.client.GWT;

/**
 * @author yuriyz on 3/21/14.
 */
@Source("InstanceTable.less")
public interface InstanceTableStyle extends Stylesheet {

    InstanceTableStyle INSTANCE = GWT.create(InstanceTableStyle.class);

    @ClassName("instance-table-toobar")
    String toolbar();

    @ClassName("column-half-width")
    String columnHalfWidth();

    @ClassName("table-header")
    String header();

    @ClassName("header-hover")
    String headerHover();

    @ClassName("row-present")
    String rowPresent();
}

