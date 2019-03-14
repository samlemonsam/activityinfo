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
package org.activityinfo.ui.client.component.report.editor.map.symbols;

import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.HasAdminEntityValues;
import org.activityinfo.model.type.geo.Extents;

import java.util.Collection;

/**
 * Utility class to help calculate the lat/lng bounds of an activity site from
 * the admin entity membership
 *
 * @author Alex Bertram
 */
public final class AdminBoundsHelper {

    private AdminBoundsHelper() {
    }

    /**
     * Calculates the normative lat/lng bounds for a given site as function of
     * AdminEntity entity membership.
     * <p/>
     * For example, if a site is marked as being within Country A, the Province
     * B and Health Zone C, the method will return the intersection of the
     * bounds for A, B, and C, which are provided by
     * {@link org.activityinfo.legacy.shared.model.AdminEntityDTO#getBounds()} and
     * {@link org.activityinfo.legacy.shared.model.CountryDTO#getBounds()}
     *
     *
     * @param formClassBounds
     * @param entityAccessor an adapter class that provides AdminEntity membership for some
     *                       representation of a site.
     * @return the normative lat/lng bounds
     */
    public static Extents calculate(Extents formClassBounds, Collection<AdminLevelDTO> levels,
                                    HasAdminEntityValues entityAccessor) {
        Extents bounds = null;
        if (formClassBounds != null) {
            bounds = new Extents(formClassBounds);
        }
        if (bounds == null) {
            bounds = Extents.maxGeoBounds();
        }

        for (AdminLevelDTO level : levels) {
            AdminEntityDTO entity = entityAccessor.getAdminEntity(level.getId());
            if (entity != null && entity.hasBounds()) {
                bounds = bounds.intersect(entity.getBounds());
            }
        }

        return bounds;
    }

    /**
     * @param bounds
     * @param levels
     * @param getter
     * @return
     */
    public static String name(Extents bounds, Collection<AdminLevelDTO> levels, HasAdminEntityValues getter) {
        // find the entities that are the limiting bounds.
        // E.g., if the user selects North Kivu, distict de North Kivu, and
        // territoire
        // de Beni, the name we give to this bounds should just be 'Beni'.

        if (bounds == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (AdminLevelDTO level : levels) {
                AdminEntityDTO entity = getter.getAdminEntity(level.getId());
                if (entity != null && entity.hasBounds()) {
                    Extents b = entity.getBounds();

                    if (b != null && (!b.contains(bounds) || b.equals(bounds))) {
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        sb.append(entity.getName());

                    }
                }
            }
            return sb.toString();
        }
    }
}
