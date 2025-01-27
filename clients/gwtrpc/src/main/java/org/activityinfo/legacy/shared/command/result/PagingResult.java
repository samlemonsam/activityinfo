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
package org.activityinfo.legacy.shared.command.result;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

import java.util.List;

/**
 * Abstract base class for <code>CommandResult</code>s that are compatible with
 * the GXT loading framework.
 *
 * @param <D> The type of model contained in the list
 * @see com.extjs.gxt.ui.client.data.PagingLoadResult
 */
public abstract class PagingResult<D extends ModelData> extends ListResult<D> implements CommandResult,
        PagingLoadResult<D> {

    private int offset;
    private int totalLength;

    protected PagingResult() {

    }

    public PagingResult(List<D> data) {
        super(data);
        this.offset = 0;
        this.totalLength = data.size();
    }

    public PagingResult(List<D> data, int offset, int totalCount) {
        super(data);
        this.offset = offset;
        this.totalLength = totalCount;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getTotalLength() {
        return totalLength;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

}
