package org.activityinfo.api.client;

import com.google.gson.JsonParser;
import com.google.gwt.http.client.*;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityInfoClientAsyncImpl implements ActivityInfoClientAsync {
  public static final Logger LOGGER = Logger.getLogger(ActivityInfoClientAsync.class.getName());

  public static final JsonParser JSON_PARSER = new JsonParser();

  public final String baseUrl;

  public ActivityInfoClientAsyncImpl() {
    baseUrl = "/resources";
  }

  public ActivityInfoClientAsyncImpl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Get a List of Forms
   *
   * This endpoint provides access to a hierarchy of Forms that are visible to the authenticated user.
   * @param parent Fetches children of this parent.
   */
  public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/catalog");
    if(parent != null) {
      urlBuilder.append("?parent=").append(UriUtils.encode(parent));
    }
    final String url = urlBuilder.toString();
    final Promise<List<CatalogEntry>> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(CatalogEntry.fromJsonArray(new JsonParser().parse(response.getText()).getAsJsonArray()));
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Get a Single Form Record
   *
   * @param formId Id of the Form
   * @param recordId Id of the record
   */
  public Promise<FormRecord> getRecord(String formId, String recordId) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/record");
    urlBuilder.append("/").append(recordId);
    final String url = urlBuilder.toString();
    final Promise<FormRecord> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(FormRecord.fromJson(response.getText()));
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Update a Form Record
   *
   * @param formId Id of the Form
   * @param recordId Id of the record
   * @param update The record to update
   */
  public Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder update) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/record");
    urlBuilder.append("/").append(recordId);
    final String url = urlBuilder.toString();
    final Promise<Void> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.PUT, url);
    requestBuilder.setHeader("Content-Type", "application/json");
    requestBuilder.setRequestData(update.toJsonString());
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(null);
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Get a Record's History
   *
   * @param formId Id of the Form
   * @param recordId Id of the record
   */
  public Promise<List<FormHistoryEntry>> getRecordHistory(String formId, String recordId) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/record");
    urlBuilder.append("/").append(recordId);
    urlBuilder.append("/history");
    final String url = urlBuilder.toString();
    final Promise<List<FormHistoryEntry>> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(FormHistoryEntry.fromJsonArray(new JsonParser().parse(response.getText()).getAsJsonArray()));
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Get All Records
   *
   * Gets all records belonging to a Form.
   * @param formId Id of the Form containing the Record
   * @param parentId Limits the records to those which are sub-records of this parent record id.
   */
  public Promise<FormRecordSet> getRecords(String formId, String parentId) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/records");
    if(parentId != null) {
      urlBuilder.append("?parentId=").append(UriUtils.encode(parentId));
    }
    final String url = urlBuilder.toString();
    final Promise<FormRecordSet> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(FormRecordSet.fromJson(new JsonParser().parse(response.getText())));
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Create a New Record
   *
   * @param formId Id of the Form
   * @param newRecord The record to create
   */
  public Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/records");
    final String url = urlBuilder.toString();
    final Promise<Void> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
    requestBuilder.setHeader("Content-Type", "application/json");
    requestBuilder.setRequestData(newRecord.toJsonString());
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(null);
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

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
  public Promise<FormClass> getFormSchema(String formId) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/schema");
    final String url = urlBuilder.toString();
    final Promise<FormClass> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(FormClass.fromJson(response.getText()));
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Updates a Form's Schema
   *
   * Updates the form
   *
   * @param formId Id of the Form
   * @param updatedSchema Updates the schema describing this form's fields
   */
  public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/form");
    urlBuilder.append("/").append(formId);
    urlBuilder.append("/schema");
    final String url = urlBuilder.toString();
    final Promise<Void> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.PUT, url);
    requestBuilder.setHeader("Content-Type", "application/json");
    requestBuilder.setRequestData(updatedSchema.toJsonString());
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(null);
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }

  /**
   * Query Table Columns
   *
   * @param query The shape of the table to retrieve
   */
  public Promise<ColumnSet> queryTableColumns(QueryModel query) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/query");
    urlBuilder.append("/columns");
    final String url = urlBuilder.toString();
    final Promise<ColumnSet> result = new Promise<>();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
    requestBuilder.setHeader("Content-Type", "application/json");
    requestBuilder.setRequestData(query.toJsonString());
    requestBuilder.setCallback(new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          result.resolve(ColumnSetParser.fromJson(response.getText()));
          return;
        }
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
        result.reject(new ApiException(response.getStatusCode()));
      }

      @Override
      public void onError(Request request, Throwable error) {
        LOGGER.log(Level.SEVERE, "Request to " + url + " failed: " + error.getMessage(), error);
        result.reject(error);
      }
    });
    try {
      requestBuilder.send();
    } catch(RequestException e) {
      result.reject(e);
    }
    return result;
  }
}
