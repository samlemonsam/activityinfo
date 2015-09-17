package org.activityinfo.i18n.shared;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

import java.util.Date;

/**
 * Messages for the application.
 */
public interface UiMessages extends com.google.gwt.i18n.client.Messages {

    @DefaultMessage("Are you sure you want to delete the database <b>{0}</b>? <br><br>You will loose all activities and indicator results.")
    String confirmDeleteDb(String arg0);

    @DefaultMessage("The coordinate falls outside of the bounds of {0}")
    String coordOutsideBounds(String arg0);

    @DefaultMessage("Last Sync''d: {0}")
    String lastSynced(String arg0);

    @DefaultMessage("There is already data entered for partner {0}. Delete this partner''s data first.")
    String partnerHasDataWarning(String arg0);

    @DefaultMessage("There is already data entered for the project {0}. Before deleting this project, you must delete the project''s data.")
    String projectHasDataWarning(String arg0);

    @DefaultMessage("Projects for database {0}")
    String projectsForDatabase(String arg0);

    @DefaultMessage("Importing... {0}/{1}, retries: {2}")
    String importingData(int completed, int total, int retries);

    @DefaultMessage("Imported {0} rows from {1}. Retry import of {2} failed  rows?")
    String imported(int completed, int total, int failed);

    /**
     * Translated "{0,number,integer}Q{1,number,integer}".
     *
     * @return translated "{0,number,integer}Q{1,number,integer}"
     */
    @DefaultMessage("{0,number,#}Q{1}")
    @Key("quarterName")
    String quarter(int year, int quarter);

    @DefaultMessage("{0,number,#}W{1}")
    String week(int year, int week);

    @DefaultMessage("{0,date,MMM}")
    @Key("monthName")
    String month(Date month);

    @DefaultMessage("{0} most recent added sites for search query")
    String recentlyAddedSites(String arg0);

    @DefaultMessage("{0} most recent edited sites for search query")
    String recentlyEditedSites(String arg0);

    @DefaultMessage("Filter by ''{0}''")
    String filterBy(String arg0);

    @DefaultMessage("Nothing entered to search on: please enter something you want to search for")
    String searchQueryEmpty();

    @DefaultMessage("Enter a search query with at least 3 characters")
    String searchQueryTooShort();

    @DefaultMessage("For query \"{0}\", found {1} databases, {2} activities and {3} indicators")
    String searchResultsFound(String arg0, String arg1, String arg2, String arg3);

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

    @DefaultMessage("{0}: New {1} at {2} by {3}")
    String newSiteSubject(String databaseName, String activityName, String locationName, String partnerName);

    @DefaultMessage("{0}: Updated {1} at {2}")
    String updatedSiteSubject(String databaseName, String activityName, String locationName);

    @DefaultMessage("{0}: Deleted {1} at {2}")
    String deletedSiteSubject(String databaseName, String activityName, String locationName);

    @DefaultMessage("Hi {0},")
    String sitechangeGreeting(String userName);

    @DefaultMessage("{0} ({1}) created a new {2} at {3} in the {4} database on {5,date,dd-MM-yyyy ''at'' HH:mm}. Here are the details:")
    String siteCreateIntro(String userName, String userEmail, String activityName, String locationName, String databaseName, Date date);

    @DefaultMessage("{0} ({1}) updated the {2} at {3} in the {4} database on {5,date,dd-MM-yyyy ''at'' HH:mm}. Here are the details:")
    String siteChangeIntro(String userName, String userEmail, String activityName, String locationName, String database, Date date);

    @DefaultMessage("{0} ({1}) deleted the {2} at {3} in the {4} database on {5,date,dd-MM-yyyy ''at'' HH:mm}.")
    String siteDeleteIntro(String userName, String userEmail, String activityName, String locationName, String database, Date date);

    @DefaultMessage("Best regards,<br>The ActivityInfo Team")
    String sitechangeSignature();

    @DefaultMessage("{0,date,dd-MM-yyyy - HH:mm} {1} ({2}) added the entry.")
    String siteHistoryCreated(Date date, String userName, String userEmail);

    @DefaultMessage("{0,date,dd-MM-yyyy - HH:mm} {1} ({2}) updated the entry:")
    String siteHistoryUpdated(Date date, String userName, String userEmail);

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

    @DefaultMessage("If you don''t wish to receive this email, please click <a href=\"{0}\" style=\"text-decoration: underline;\">Unsubscribe</a>.")
    String digestUnsubscribe(String unsubscribeLink);

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

    @DefaultMessage("Set all rows to ''{0}''")
    String updateAllRowsTo(String value);

    @DefaultMessage("Are you sure want to delete?")
    String confirmDeleteSite();

    @DefaultMessage("Select a site above.")
    String SelectSiteAbove();

    @DefaultMessage("Choose the destination field for the source column \"<i>{0}</i>\".")
    SafeHtml columnMatchPrompt(String columnName);

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

    @DefaultMessage("Please map all mandatory columns, missed mapping for {0}")
    String pleaseMapAllMandatoryColumns(String columnLabels);

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

    @Messages.DefaultMessage("Failed to find nationwide location type, db: {0}, country: {1}")
    String noNationWideLocationType(String dbName, String countryName);

    @Messages.DefaultMessage("{0} Users")
    String databaseUserGroup(String databaseName);

    @Messages.DefaultMessage("Confidence: {0}% #013;Matched to: {1} #013;Value will be imported with {0} confidence.")
    String importValidationCellTooltip(int confidencePercent, String matchedValue);

}
