package chdc.frontend.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * Defines string that will need to be translated.
 */
public interface ChdcLabels extends Constants {

    final ChdcLabels LABELS = GWT.create(ChdcLabels.class);

    @DefaultStringValue("Add Single Incident")
    String addSingleIncident();

    @DefaultStringValue("Bulk Edit")
    String bulkEdit();

    @DefaultStringValue("Show All Incidents")
    String showAllIncidents();

    @DefaultStringValue("Search in Incidents")
    String searchInIncidents();

    @DefaultStringValue("Search")
    String search();

    @DefaultStringValue("New Row")
    String newRow();
}
