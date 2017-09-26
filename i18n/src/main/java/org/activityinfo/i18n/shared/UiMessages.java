package org.activityinfo.i18n.shared;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;
import java.util.Date;

/**
 * Messages for the application.
 */
public interface UiMessages extends com.google.gwt.i18n.client.Messages {

    @DefaultMessage("Are you sure you want to delete the database <b>{0}</b>? <br><br>You will lose all activities and indicator results.")
    SafeHtml confirmDeleteDb(String arg0);

    @DefaultMessage("Are you sure you want to delete the form <b>{0}</b>? <br><br>You will lose all information associated with it.")
    SafeHtml confirmDeleteForm(String arg0);

    @DefaultMessage("The coordinate falls outside of the bounds of {0}")
    String coordOutsideBounds(String arg0);

    @DefaultMessage("Last Sync''d: {0}")
    String lastSynced(String arg0);

    @DefaultMessage("There is already data entered for partner {0}. Delete this partner''s data first.")
    String partnerHasDataWarning(String arg0);

    @DefaultMessage("Projects for database {0}")
    String projectsForDatabase(String arg0);

    @DefaultMessage("Importing... {0}/{1}, retries: {2}")
    String importingData(int completed, int total, int retries);

    @DefaultMessage("Imported {0} rows from {1}. Retry import of {2} failed  rows?")
    String imported(int completed, int total, int failed);

    @DefaultMessage("{0,number,#}Q{1}")
    @Key("quarterName")
    String quarter(int year, int quarter);

    @DefaultMessage("{0,number,#}W{1}")
    String week(int year, int week);

    @DefaultMessage("{0,date,MMM}")
    @Key("monthName")
    String month(Date month);

    @DefaultMessage("Filter by ''{0}''")
    String filterBy(String arg0);

    @DefaultMessage("Add new entry for form ''{0}''")
    String addNewSiteForActivity(String activityName);

    @DefaultMessage("{0,number} matching sites")
    String matchingLocations(int count);

    @DefaultMessage("Use site ''{0}''")
    @Key("useLocationWithName")
    String useLocation(String name);

    @DefaultMessage("Targets for database {0}")
    String targetsForDatabase(String arg0);

    @DefaultMessage("Report ''{0}'' added to dashboard.")
    String addedToDashboard(String reportName);

    @DefaultMessage("Report ''{0}'' removed from dashboard.")
    String removedFromDashboard(String reportName);

    @DefaultMessage("The report ''{0}'' has been saved.")
    String reportSaved(String name);

    @DefaultMessage("Are you sure you want to delete the report \"{0}\"")
    String confirmDeleteReport(String reportTitle);

    @DefaultMessage("You are not the owner of this report.<br/>Do you want to save a new copy?")
    String confirmSaveCopy();

    @DefaultMessage("The form \"{0}\" has not been marked as public by the database owner and so cannot be embedded in a public web page. Please contact the database owner and request that the activity be published.")
    String activityNotPublic(String name);

    @DefaultMessage("In order to embed this sheet in a public web page, the form \"{0}\" must be made public. Do you want to make this form public now?")
    String promptPublishActivity(String name);

    @DefaultMessage("{0} minutes ago")
    String minutesAgo(int minutes);

    @DefaultMessage("{0} hours ago")
    String hoursAgo(int hours);

    @DefaultMessage("{0} days ago")
    String daysAgo(int hours);

    @DefaultMessage("{0,date,dd-MM-yyyy - HH:mm} {1} ({2}) added the entry.")
    String siteHistoryCreated(Date date, String userName, String userEmail);

    @DefaultMessage("{0,date,dd-MM-yyyy - HH:mm} {1} ({2}) added the entry in the {3} sub form.")
    String siteHistorySubFormCreated(Date date, String userName, String userEmail, String subFormName);

    @DefaultMessage("Added on {0,date,dd-MM-yyyy}.")
    String siteHistoryDateCreated(Date date);

    @DefaultMessage("{0,date,dd-MM-yyyy - HH:mm} {1} ({2}) updated the entry:")
    String siteHistoryUpdated(Date date, String userName, String userEmail);

    @DefaultMessage("{0,date,dd-MM-yyyy - HH:mm} {1} ({2}) updated an entry in the {3} sub form.")
    String siteHistorySubFormUpdated(Date date, String userName, String userEmail, String subFormName);

    @DefaultMessage("No history is available for this form entry.")
    String siteHistoryNotAvailable();

    @DefaultMessage("History on form entries is only available from {0,date,dd MMMM yyyy} onward.")
    String siteHistoryAvailableFrom(Date date);

    @DefaultMessage("was: {0}")
    String siteHistoryOldValue(Object oldValue);

    @DefaultMessage("was: blank")
    String siteHistoryOldValueBlank();

    @DefaultMessage("{0}, {1,date,MMMM yyyy}")
    String siteHistoryIndicatorName(String name, Date date);

    @DefaultMessage("Added attribute {0}")
    String siteHistoryAttrAdd(String attrName);

    @DefaultMessage("Removed attribute {0}")
    String siteHistoryAttrRemove(String attrName);

    @DefaultMessage("ActivityInfo digest for {0,date,dd-MM-yyyy}")
    String digestSubject(Date now);

    @DefaultMessage("Hi {0},")
    String digestGreeting(String userName);

    @DefaultMessage("Best regards,<br>The ActivityInfo Team")
    String digestSignature();

    @DefaultMessage("Here are the updates to ActivityInfo in the last {0} hours, for your information.")
    String geoDigestIntro(int hours);

    @DefaultMessage("<a href=\"mailto:{0}\">{1}</a> edited the {2} at {3}")
    String geoDigestSiteMsg(String userEmail, String userName, String activityName, String locationName);

    @DefaultMessage("<span title=\"{0,date,dd-MM-yyyy}\">today</span>.")
    String geoDigestSiteMsgDateToday(Date date);

    @DefaultMessage("<span title=\"{0,date,dd-MM-yyyy}\">yesterday</span>.")
    String geoDigestSiteMsgDateYesterday(Date date);

    @DefaultMessage("on <span>{0,date,dd-MM-yyyy}</span>.")
    String geoDigestSiteMsgDateOther(Date date);

    @DefaultMessage("Unmapped Sites")
    String geoDigestUnmappedSites();

    @DefaultMessage("Here is the summary of the updates by user for the ActivityInfo databases you administer over the last {0} days.")
    String activityDigestIntro(int days);

    @DefaultMessage("The following ActivityInfo databases have not been updated in the last {0} days:")
    String activityDigestInactiveDatabases(int days);

    @DefaultMessage("{0} update(s) on {1,date,dd-MM-yyyy}")
    String activityDigestGraphTooltip(int updates, Date date);

    @DefaultMessage("Are you sure want to delete?")
    String confirmDeleteSite();

    @DefaultMessage("Select a site above.")
    String SelectSiteAbove();

    @DefaultMessage("Showing {0} of {1} columns.")
    String showColumns(int numberOfColumnsShown, int numberOfColumnsTotal);

    @DefaultMessage("Are you sure you want to delete {0} row(s) from {1}?")
    String removeTableRowsConfirmation(int numberOfRows, String formClassLabel);

    @DefaultMessage("{0} updated!")
    String newVersion(String appTitle);

    @DefaultMessage("Do you want to retry deleting {0} row(s) from {1}?")
    @Key("retryDeletionAtRow")
    String retryDeletion(int size, String formClassLabel);

    @DefaultMessage("Deleting {0} row(s) from {1}...")
    String deletingRows(int size, String formClassLabel);

    @DefaultMessage("<b>Showing {0} of {1} columns.</b> You can choose visible columns with ''{2}'' button")
    SafeHtml notAllColumnsAreShown(int visibleColumns, int allColumns, String chooseColumnButtonName);

    @DefaultMessage("Field is mandatory but not mapped: {0}")
    String fieldIsMandatory(String fieldLabel);

    @DefaultMessage("Please map all mandatory columns, missed mapping for {0} required fields: {1}")
    String missedMapping(int missedColumnCount, String columnLabels);

    @DefaultMessage("{0} rows are invalid and won''t be imported. Continue?")
    String continueImportWithInvalidRows(int invalidRowsCount);

    @DefaultMessage("{0}% Complete")
    String percentComplete(int percent);

    @DefaultMessage("Oh no! Your import is missing required column(s): {0}")
    String missingColumns(String missingColumns);

    @DefaultMessage("{0} code does not exist.")
    String doesNotExist(String placeholder);

    @DefaultMessage("Please provide valid comma separated text. Column count does not match in row number {0}.")
    @Key("provideCsvAtRow")
    String pleaseProvideCommaSeparatedText(int rowNumber);

    @DefaultMessage("Exceeds maximum length of {0} characters.")
    String exceedsMaximumLength(int maxLength);

    @DefaultMessage("Invalid value. Please enter date in following format: {0}")
    String dateFieldInvalidValue(String format);

    @DefaultMessage("Please enter a number. For example: {0} or {1} or {2}")
    String quantityFieldInvalidValue(int integer, String doubleWithoutPoint, String doubleWithPoint);

    @DefaultMessage("Please enter a value that matches the pattern ''{0}''")
    String invalidTextInput(String mask);

    @Messages.DefaultMessage("{0} Users")
    String databaseUserGroup(String databaseName);

    @Messages.DefaultMessage("Matched to ''{0}'' ({1}%)")
    String importValidationCellTooltip(String matchedValue, int confidencePercent);

    @Messages.DefaultMessage("{0,date,medium} to {1,date,medium}")
    String dateRange(Date formDate, Date toDate);

    @Messages.DefaultMessage("Logged in as {0}")
    String loggedInAs(String email);

    @DefaultMessage("Switch to {0} now")
    String switchToLanguageNow(String localeName);

    @DefaultMessage("Option {0,number}")
    String defaultEnumItem(int number);

    @DefaultMessage("Unknown value: {0}. Each selection of ''Multiple selection'' control must have separate column with allowed values: TRUE, FALSE or no value (which is considered as FALSE)")
    String unknownMultiEnumValue(String value);

    @DefaultMessage("Required field ''{0}'' is missing in row {1}")
    String requiredFieldMissing(String fieldName, int rowIndex);

    @DefaultMessage("You didn''t provide a column named ''{0}'', so we''ll default to ''{1}''.")
    String missingWithDefault(String columnName, String defaultValue);

    @DefaultMessage("For fields of type ''{0}'', a column named ''{1}'', containing the id of the form to reference, is required.")
    String referenceFieldRequiresRange(String fieldTypeName, String columnName);

    @DefaultMessage("Count of {0}")
    String countMeasure(String formLabel);
}
