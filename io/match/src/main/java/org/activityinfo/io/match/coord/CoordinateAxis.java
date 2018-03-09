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
package org.activityinfo.io.match.coord;

import org.activityinfo.i18n.shared.I18N;

public enum CoordinateAxis {
    LATITUDE {
        @Override
        public double getMaximumValue() {
            return 90;
        }

        @Override
        public String getLocalizedName() {
            return I18N.CONSTANTS.latitude();
        }

        @Override
        public String getDegreesOutOfBoundsMessage() {
            return I18N.CONSTANTS.latitudeOutOfBounds();
        }

        @Override
        public String getPositiveHemisphereCharacters() {
            return I18N.CONSTANTS.northHemiChars();
        }

        @Override
        public String getNegativeHemisphereCharacters() {
            return I18N.CONSTANTS.southHemiChars();
        }

    },
    LONGITUDE {


        @Override
        public double getMaximumValue() {
            return 180;
        }

        @Override
        public String getLocalizedName() {
            return I18N.CONSTANTS.longitude();
        }

        @Override
        public String getDegreesOutOfBoundsMessage() {
            return I18N.CONSTANTS.longitudeOutOfBounds();
        }

        @Override
        public String getPositiveHemisphereCharacters() {
            return I18N.CONSTANTS.eastHemiChars();
        }

        @Override
        public String getNegativeHemisphereCharacters() {
            return I18N.CONSTANTS.westHemiChars();
        }

    };
    
    public abstract double getMaximumValue();

    public abstract String getLocalizedName();

    public abstract String getDegreesOutOfBoundsMessage();

    public double getMinimumValue() {
        return -getMaximumValue();
    }

    /**
     * 
     * @return the range of characters used denote the positive hemisphere for this
     * axis in the current Locale. For example, 'N' is used to indicate the positive hemisphere for latitude.
     */
    public abstract String getPositiveHemisphereCharacters();

    /**
     * 
     * @return the range of characters used to denote the negative hemisphere for this
     * axis in the current Locale. For example, 'S', is used to indicate the negative hemisphere for latitude.
     */
    public abstract String getNegativeHemisphereCharacters();
}
