package org.activityinfo.core.shared.importing.strategy;
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

import org.activityinfo.core.shared.importing.source.SourceRow;

/**
 * @author yuriyz on 03/31/2016.
 */
public class EmptyColumn implements ColumnAccessor {

    private String header;

    public EmptyColumn(String header) {
        this.header = header;
    }

    @Override
    public String getHeading() {
        return header;
    }

    @Override
    public String getValue(SourceRow row) {
        return "";
    }

    @Override
    public boolean isMissing(SourceRow row) {
        return true;
    }
}
