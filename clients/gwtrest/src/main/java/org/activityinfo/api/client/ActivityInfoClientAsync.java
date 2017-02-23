package org.activityinfo.api.client;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.promise.Promise;

import java.util.List;

public interface ActivityInfoClientAsync {
  /**
   * Get a List of Forms
   *
   * This endpoint provides access to a hierarchy of Forms that are visible to the authenticated user.
   * @param parent Fetches children of this parent.
   */
  Promise<List<CatalogEntry>> getFormCatalog(String parent);

  /**
   * Get a Single Form Record
   *
   * @param formId Id of the Form
   * @param recordId Id of the record
   */
  Promise<FormRecord> getRecord(String formId, String recordId);

  /**
   * Update a Form Record
   *
   * @param formId Id of the Form
   * @param recordId Id of the record
   * @param update The record to update
   */
  Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder update);

  /**
   * Get a Record's History
   *
   * @param formId Id of the Form
   * @param recordId Id of the record
   */
  Promise<List<FormHistoryEntry>> getRecordHistory(String formId, String recordId);

  /**
   * Get All Records
   *
   * Gets all records belonging to a Form.
   * @param formId Id of the Form containing the Record
   * @param parentId Limits the records to those which are sub-records of this parent record id.
   */
  Promise<FormRecordSet> getRecords(String formId, String parentId);

  /**
   * Create a New Record
   *
   * @param formId Id of the Form
   * @param newRecord The record to create
   */
  Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord);

  /**
   * Get a Form's Schema
   *
   * Retrieves the schema that describes this form's fields.
   * A form in ActivityInfo is a set of records that have a common set of fields, or schema. 
   * A form can be a user-defined form created by a user, or it can be an application-defined like the set of
   *  countries, which has the id `_countries`.
   *
   * @param formId Id of the form
   */
  Promise<FormClass> getFormSchema(String formId);

  /**
   * Updates a Form's Schema
   *
   * Updates the form
   *
   * @param formId Id of the Form
   * @param updatedSchema Updates the schema describing this form's fields
   */
  Promise<Void> updateFormSchema(String formId, FormClass updatedSchema);

  /**
   * Query Table Columns
   *
   * @param query The shape of the table to retrieve
   */
  Promise<ColumnSet> queryTableColumns(QueryModel query);
}
