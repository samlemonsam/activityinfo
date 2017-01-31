package org.activityinfo.ui.client.page.config.design.importer.wrapper;
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

import org.activityinfo.legacy.shared.model.EntityDTO;

/**
 * @author yuriyz on 9/22/14.
 */
public class DtoWrapper {

    private final WrapperKey key;
    private EntityDTO dto;

    public DtoWrapper(WrapperKey key) {
        this(key, null);
    }

    public DtoWrapper(WrapperKey key, EntityDTO dto) {
        this.key = key;
        this.dto = dto;
    }

    public WrapperKey getKey() {
        return key;
    }

    public EntityDTO getDto() {
        return dto;
    }

    public void setDto(EntityDTO dto) {
        this.dto = dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DtoWrapper that = (DtoWrapper) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
