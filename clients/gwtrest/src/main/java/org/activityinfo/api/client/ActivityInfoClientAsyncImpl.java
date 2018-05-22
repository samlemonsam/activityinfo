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
package org.activityinfo.api.client;

import com.google.common.base.Function;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonMappingException;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobRequest;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private String formUrl(String formId) {
        return baseUrl + "/form" + "/" + formId;
    }

    private String formUrl(ResourceId formId) {
        return formUrl(formId.asString());
    }

    private String schemaUrl(String formId) {
        return formUrl(formId) + "/schema";
    }

    private String recordUrl(String formId, String recordId) {
        return formUrl(formId) + "/record/" + recordId;
    }

    @Override
    public Promise<UserDatabaseMeta> getDatabase(ResourceId databaseId) {
        return get(baseUrl + "/database/" + CuidAdapter.getLegacyIdFromCuid(databaseId), UserDatabaseMeta::fromJson);
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
        return get(urlBuilder.toString(), CatalogEntry::fromJsonArray);
    }

    /**
     * Get a Single Form Record
     *
     * @param formId Id of the Form
     * @param recordId Id of the record
     */
    public Promise<Maybe<FormRecord>> getRecord(final String formId, final String recordId) {
        return getRaw(recordUrl(formId, recordId), response -> {
            switch (response.getStatusCode()) {
                case 200:
                    return Maybe.of(FormRecord.fromJson(JSON_PARSER.parse(response.getText())));
                case 403:
                    return Maybe.forbidden();
                case 404:
                    return Maybe.notFound();
                default:
                    throw new ApiException(response.getStatusCode());
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
        return post(RequestBuilder.PUT, recordUrl(formId, recordId), update.toJsonString());
    }

    /**
     * Get a Record's History
     *
     * @param formId Id of the Form
     * @param recordId Id of the record
     */
    public Promise<RecordHistory> getRecordHistory(String formId, String recordId) {
        return get(recordUrl(formId, recordId) + "/history", jsonElement -> {
            try {
                return Json.fromJson(RecordHistory.class, jsonElement);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
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
        StringBuilder urlBuilder = new StringBuilder(formUrl(formId));
        urlBuilder.append("/records");
        if(parentId != null) {
            urlBuilder.append("?parentId=").append(UriUtils.encode(parentId));
        }
        return getRecords(ResourceId.valueOf(formId), urlBuilder.toString());
    }

    @Override
    public Promise<FormSyncSet> getRecordVersionRange(String formId, long localVersion, final long toVersion, Optional<String> cursor) {
        String url = formUrl(formId) +
                "/records/versionRange" +
                "?localVersion=" + localVersion +
                "&version=" + toVersion;

        if(cursor.isPresent()) {
            url = url + "&cursor=" + cursor.get();
        }

        return get(url, value -> {
            try {
                return Json.fromJson(FormSyncSet.class, value);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private Promise<FormRecordSet> getRecords(final ResourceId formId, final String url) {
        return getRaw(url, response -> {
            switch (response.getStatusCode()) {
                case Response.SC_OK:
                    return FormRecordSet.fromJson(Json.parse(response.getText()));

                case Response.SC_NOT_FOUND:
                case Response.SC_FORBIDDEN:
                    return new FormRecordSet(formId.asString());

                default:
                    throw new ApiException(response.getStatusCode());
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
        return post(RequestBuilder.POST, baseUrl + "/form" + "/" + formId + "/records", newRecord.toJsonString());
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
        return get(schemaUrl(formId), FormClass::fromJson);
    }

    @Override
    public Promise<FormMetadata> getFormMetadata(String formId) {
        return get(formUrl(formId), FormMetadata::fromJson);
    }

    @Override
    public Promise<FormTree> getFormTree(final ResourceId formId) {
        return get(formUrl(formId) + "/tree", jsonElement -> {
            JsonValue root = jsonElement;
            JsonValue forms = root.get("forms");
            final Map<ResourceId, FormClass> formMap = new HashMap<>();
            for (Map.Entry<String, JsonValue> entry : forms.entrySet()) {
                FormClass formClass = FormClass.fromJson(entry.getValue());
                formMap.put(formClass.getId(), formClass);
            }
            FormTreeBuilder builder = new FormTreeBuilder(new FormClassProvider() {
                @Override
                public FormClass getFormClass(ResourceId formId1) {
                    FormClass formClass = formMap.get(formId1);
                    assert formClass != null;
                    return formClass;
                }
            });
            return builder.queryTree(formId);
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
        return post(RequestBuilder.PUT, schemaUrl(formId), updatedSchema.toJsonString());
    }


    /**
     * Query Table Columns
     *
     * @param query The shape of the table to retrieve
     */
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        return post(RequestBuilder.POST, baseUrl + "/query/columns", query.toJsonString(), ColumnSetParser::fromJson);
    }

    @Override
    public Promise<Void> updateRecords(RecordTransaction transaction) {
        return post(RequestBuilder.POST, baseUrl + "/update", Json.stringify(transaction));
    }

    @Override
    public Promise<Maybe<Analysis>> getAnalysis(String id) {
        return getRaw(baseUrl + "/analysis/" + id, response -> {
            switch (response.getStatusCode()) {
                case Response.SC_OK:
                    try {
                        return Maybe.of(Json.fromJson(Analysis.class, Json.parse(response.getText())));
                    } catch (JsonMappingException e) {
                        throw new RuntimeException(e);
                    }
                case Response.SC_FORBIDDEN:
                    return Maybe.forbidden();
                case Response.SC_NOT_FOUND:
                    return Maybe.notFound();
                default:
                    throw new ApiException(response.getStatusCode());
            }
        });
    }

    @Override
    public Promise<Void> updateAnalysis(AnalysisUpdate analysis) {
        return post(RequestBuilder.POST, baseUrl + "/analysis", Json.stringify(analysis));
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Promise<JobStatus<T, R>> startJob(T job) {

        JobRequest request = new JobRequest(job, LocaleInfo.getCurrentLocale().getLocaleName());

        return post(RequestBuilder.POST, baseUrl + "/jobs", request.toJsonObject().toJson(),
                s -> JobStatus.fromJson(JSON_PARSER.parse(s)));
    }

    @Override
    public Promise<JobStatus<?, ?>> getJobStatus(String jobId) {
        return get(baseUrl + "/jobs/" + jobId, JobStatus::fromJson);
    }

    private <R> Promise<R> getRaw(final String url, final Function<Response, R> parser) {
        final Promise<R> result = new Promise<>();
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        requestBuilder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    result.resolve(parser.apply(response));
                } catch (Exception e) {
                    result.reject(e);
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

    private <R> Promise<R> get(final String url, final Function<JsonValue, R> parser) {
        return getRaw(url, response -> {
            if(response.getStatusCode() == 200) {
                return parser.apply(JSON_PARSER.parse(response.getText()));
            } else {
                throw new ApiException(response.getStatusCode());
            }
        });
    }


    private Promise<Void> post(RequestBuilder.Method method, final String url, String jsonRequest) {
        return post(method, url, jsonRequest, s -> null);
    }

    private <R> Promise<R> post(RequestBuilder.Method method, final String url, String jsonRequest, final Function<String, R> parser) {
        final Promise<R> result = new Promise<>();
        RequestBuilder requestBuilder = new RequestBuilder(method, url);
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
