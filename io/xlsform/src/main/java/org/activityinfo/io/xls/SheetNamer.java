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
package org.activityinfo.io.xls;

import org.apache.poi.ss.util.WorkbookUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides unique names for an Excel Worksheet
 */
public class SheetNamer {

    private static final int MAX_WORKSHEET_LENGTH = 31;

    private Map<String, Integer> existingNames = new HashMap<>();

    /**
     * Creates a unique name for a worksheet and remove
     * @param desiredName
     * @return
     */
    public String name(String desiredName) {

        String sheetName = desiredName;

        // to avoid conflict with our own disambiguation scheme, remove any trailing "(n)" 
        // from sheet names
        sheetName = sheetName.replaceFirst("\\((\\d+)\\)$", "$1");

        // shorten and translate the name to meet excel requirements
        String safeName = WorkbookUtil.createSafeSheetName(sheetName);

        // assure that the sheet name is unique
        if (!existingNames.containsKey(safeName)) {
            existingNames.put(safeName, 1);
            return safeName;
        } else {
            int index = existingNames.get(safeName) + 1;
            existingNames.put(safeName, index);

            String disambiguatedNamed = safeName + " (" + index + ")";
            if (disambiguatedNamed.length() > MAX_WORKSHEET_LENGTH) {
                int toTrim = disambiguatedNamed.length() - MAX_WORKSHEET_LENGTH;
                String trimmed = safeName.substring(0, safeName.length() - toTrim);
                return name(trimmed);
            }
            return disambiguatedNamed;
        }
    }
}
