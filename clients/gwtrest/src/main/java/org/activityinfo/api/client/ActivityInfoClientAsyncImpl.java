package org.activityinfo.api.client;

import com.google.common.base.Function;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobRequest;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityInfoClientAsyncImpl implements ActivityInfoClientAsync {
    private static final Logger LOGGER = Logger.getLogger(ActivityInfoClientAsync.class.getName());

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String baseUrl;

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
        return get(urlBuilder.toString(), new Function<JsonElement, List<CatalogEntry>>() {
            @Override
            public List<CatalogEntry> apply(JsonElement jsonElement) {
                return CatalogEntry.fromJsonArray(jsonElement.getAsJsonArray());
            }
        });
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

        return get(urlBuilder.toString(), new Function<JsonElement, FormRecord>() {
            @Override
            public FormRecord apply(JsonElement jsonElement) {
                return FormRecord.fromJson(jsonElement);
            }
        });
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

        return post(urlBuilder.toString(), update.toJsonString());
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

        return get(urlBuilder.toString(), new Function<JsonElement, List<FormHistoryEntry>>() {
            @Override
            public List<FormHistoryEntry> apply(JsonElement jsonElement) {
                return FormHistoryEntry.fromJsonArray(jsonElement.getAsJsonArray());
            }
        });
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
        return getRecords(urlBuilder.toString());
    }

    @Override
    public Promise<FormRecordSet> getRecordVersionRange(String formId, long localVersion, long toVersion) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("/form");
        urlBuilder.append("/").append(formId);
        urlBuilder.append("/records/versionRange");
        urlBuilder.append("?localVersion=" + localVersion);
        urlBuilder.append("&version=" + toVersion);

        return getRecords(urlBuilder.toString());
    }


    private Promise<FormRecordSet> getRecords(final String url) {
        return get(url, new Function<JsonElement, FormRecordSet>() {
            @Override
            public FormRecordSet apply(JsonElement jsonElement) {
                return FormRecordSet.fromJson(jsonElement);
            }
        });
    }

    /**
     * Create a New Record
     *
     * @param formId Id of the Form
     * @param newRecord The record to create
     */
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord) {
        return post(baseUrl + "/form" + "/" + formId + "/records", newRecord.toJsonString());
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
        return get(schemaUrl(formId), new Function<JsonElement, FormClass>() {
            @Override
            public FormClass apply(JsonElement jsonElement) {
                return FormClass.fromJson(jsonElement.getAsJsonObject());
            }
        });
    }

    @Override
    public Promise<FormMetadata> getFormMetadata(String formId) {
        return get(baseUrl + "/form/" + formId, new Function<JsonElement, FormMetadata>() {
            @Override
            public FormMetadata apply(JsonElement jsonElement) {
                return FormMetadata.fromJson(jsonElement.getAsJsonObject());
            }
        });
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
        return post(schemaUrl(formId), updatedSchema.toJsonString());
    }

    private String schemaUrl(String formId) {
        return baseUrl + "/form/" + formId + "/schema";
    }

    /**
     * Query Table Columns
     *
     * @param query The shape of the table to retrieve
     */
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        return post(baseUrl + "/query/columns", query.toJsonString(), new Function<String, ColumnSet>() {
            @Override
            public ColumnSet apply(String responseText) {
                return ColumnSetParser.fromJson(responseText);
            }
        });
    }

    @Override
    public Promise<Void> updateRecords(TransactionBuilder transaction) {
        return post(baseUrl + "/update", transaction.build().toString());
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Promise<JobStatus<T, R>> startJob(T job) {

        JobRequest request = new JobRequest(job, LocaleInfo.getCurrentLocale().getLocaleName());

        return post(baseUrl + "/jobs", request.toJsonObject().toString(), new Function<String, JobStatus<T, R>>() {
            @Override
            public JobStatus<T, R> apply(String s) {
                return JobStatus.fromJson(JSON_PARSER.parse(s).getAsJsonObject());
            }
        });
    }

    @Override
    public Promise<JobStatus<?, ?>> getJobStatus(String jobId) {
        return get(baseUrl + "/jobs/" + jobId, new Function<JsonElement, JobStatus<?, ?>>() {
            @Override
            public JobStatus<?, ?> apply(JsonElement jsonElement) {
                return JobStatus.fromJson(jsonElement.getAsJsonObject());
            }
        });
    }


    private <R> Promise<R> get(final String url, final Function<JsonElement, R> parser) {
        final Promise<R> result = new Promise<>();
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        requestBuilder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    try {
                        JsonElement jsonResult = JSON_PARSER.parse(response.getText());
                        result.resolve(parser.apply(jsonResult));
                    } catch (Exception e) {
                        result.reject(e);
                    }
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

    private Promise<Void> post(final String url, String jsonRequest) {
        return post(url, jsonRequest, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                return null;
            }
        });
    }

    private <R> Promise<R> post(final String url, String jsonRequest, final Function<String, R> parser) {
        final Promise<R> result = new Promise<>();
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
        requestBuilder.setHeader("Content-Type", "application/json");
        requestBuilder.setRequestData(jsonRequest);
        requestBuilder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    try {
                        result.resolve(parser.apply(response.getText()));
                    } catch (Exception e) {
                        result.reject(e);
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Request to " + url + " failed with status " + response.getStatusCode() + ": " + response.getStatusText());
                    result.reject(new ApiException(response.getStatusCode()));
                }
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
