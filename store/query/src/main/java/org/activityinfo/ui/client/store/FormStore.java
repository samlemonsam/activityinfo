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
package org.activityinfo.ui.client.store;

import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.query.shared.FormOfflineStatus;
import org.activityinfo.store.query.shared.FormSource;

import java.util.List;

public interface FormStore extends FormSource {

    Observable<UserDatabaseMeta> getDatabase(ResourceId databaseId);

    Observable<FormMetadata> getFormMetadata(ResourceId formId);

    Observable<Maybe<RecordTree>> getRecordTree(RecordRef rootRecordId);

    Promise<Void> deleteForm(ResourceId formId);

    Observable<List<CatalogEntry>> getCatalogRoots();

    Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId);

    Observable<List<FormRecord>> getSubRecords(ResourceId formId, RecordRef parent);

    void setFormOffline(ResourceId formId, boolean offline);

    Observable<FormOfflineStatus> getOfflineStatus(ResourceId formId);

    Observable<RecordHistory> getFormRecordHistory(RecordRef ref);

    /**
     * Applies an update transactionally to the Form store.
     */
    Promise<Void> updateRecords(RecordTransaction tx);

    Promise<Void> updateAnalysis(AnalysisUpdate update);

    <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>>  startJob(T job);
}
