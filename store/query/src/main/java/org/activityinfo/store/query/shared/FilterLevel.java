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
package org.activityinfo.store.query.shared;

/**
 * Defines the level of filters that are applied to a query.
 */
public enum FilterLevel {

    /**
     * No filter applied to the raw results from FormStorage
     */
    NONE,

    /**
     * Sub-records of deleted parent records are hidden
     */
    BASE,

    /**
     * Form- and record-level permissions are applied in addition
     * to the "BASE" filters.
     */
    PERMISSIONS
}
