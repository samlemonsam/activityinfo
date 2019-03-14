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
package org.activityinfo.ui.client.page.common.grid;

import com.extjs.gxt.ui.client.Style;

public abstract class AbstractPagingGridPageState extends AbstractGridPageState {
    private int pageNum = -1;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    protected void appendGridStateToken(StringBuilder sb) {
        if (getSortInfo() != null && getSortInfo().getSortDir() != Style.SortDir.NONE &&
            getSortInfo().getSortField().length() != 0) {

            sb.append("/sort");
            if (getSortInfo().getSortDir() == Style.SortDir.DESC) {
                sb.append("-desc");
            }
            sb.append(":").append(getSortInfo().getSortField());
            if (pageNum > 0) {
                sb.append("/p").append(pageNum);
            }
        }
    }

}
