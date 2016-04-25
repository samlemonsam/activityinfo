package org.activityinfo.server.endpoint.export;

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
     * Creates a unique name for
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
