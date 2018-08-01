package org.activityinfo.server.job;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.analysis.pivot.LongFormatTableBuilder;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.analysis.pivot.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportLongFormatJob;
import org.activityinfo.model.job.ExportPivotTableJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.spi.FormStorageProvider;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExportLongFormatExecutor implements JobExecutor<ExportLongFormatJob, ExportResult> {

    private final FormStorageProvider formStorageProvider;
    private final StorageProvider storageProvider;
    private final DispatcherSync dispatcher;
    private final FormSource formSource;

    private static Predicate<ActivityDTO> activity() {
        return activity -> activity.getClassicView() && (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE);
    }

    private static Predicate<ActivityDTO> monthlyActivity() {
        return activity -> activity.getClassicView() && (activity.getReportingFrequency() == ActivityFormDTO.REPORT_MONTHLY);
    }

    private static Predicate<ActivityDTO> form() {
        return activity -> !activity.getClassicView();
    }

    private static Predicate<FormField> subFormField() {
        return field -> field.getType() instanceof SubFormReferenceType;
    }

    @Inject
    public ExportLongFormatExecutor(FormStorageProvider formStorageProvider,
                                    StorageProvider storageProvider,
                                    DispatcherSync dispatcher,
                                    FormSource formSource) {
        this.formStorageProvider = formStorageProvider;
        this.storageProvider = storageProvider;
        this.dispatcher = dispatcher;
        this.formSource = formSource;
    }

    @Override
    public ExportResult execute(ExportLongFormatJob descriptor) throws IOException {
        int databaseId = descriptor.getDatabaseId();
        UserDatabaseDTO database = dispatcher.execute(new GetSchema()).getDatabaseById(databaseId);

        if (database == null) {
            throw new IllegalStateException("Database " + databaseId + " could not be found");
        }

        List<FormTree> formScope = getFormScope(database);
        PivotModel longFormatModel = LongFormatTableBuilder.build(formScope);
        ExportPivotTableExecutor pivotTableExport = new ExportPivotTableExecutor(storageProvider, formSource);
        ExportPivotTableJob exportJob = new ExportPivotTableJob(longFormatModel);
        return pivotTableExport.execute(exportJob);
    }

    private List<FormTree> getFormScope(UserDatabaseDTO database) {
        List<FormTree> scope = Lists.newArrayList();
        scope.addAll(getActivityForms(database));
        scope.addAll(getMonthlyActivityForms(database));
        scope.addAll(getForms(database));
        return scope;
    }

    private List<FormTree> getActivityForms(UserDatabaseDTO database) {
        return database.getActivities().stream()
                .filter(activity())
                .map(ActivityDTO::getFormId)
                .map(this::getFormTree)
                .collect(Collectors.toList());
    }

    private FormTree getFormTree(ResourceId rootForm) {
        return formSource.getFormTree(rootForm).waitFor();
    }

    private List<FormTree> getMonthlyActivityForms(UserDatabaseDTO database) {
        return database.getActivities().stream()
                .filter(monthlyActivity())
                .map(ActivityDTO::getId)
                .map(CuidAdapter::reportingPeriodFormClass)
                .map(this::getFormTree)
                .collect(Collectors.toList());
    }

    private List<FormTree> getForms(UserDatabaseDTO database) {
        List<FormTree> formScope = Lists.newArrayList();
        formScope.addAll(getParentForms(database));
        formScope.addAll(getSubForms(formScope));
        return formScope;
    }

    private List<FormTree> getParentForms(UserDatabaseDTO database) {
        return database.getActivities().stream()
                .filter(form())
                .map(ActivityDTO::getFormId)
                .map(this::getFormTree)
                .collect(Collectors.toList());
    }

    private List<FormTree> getSubForms(List<FormTree> parentForms) {
        return parentForms.stream()
                .map(FormTree::getRootFormClass)
                .map(FormClass::getFields)
                .flatMap(List::stream)
                .filter(subFormField())
                .map(ExportLongFormatExecutor::subFormId)
                .map(this::getFormTree)
                .collect(Collectors.toList());
    }

    private static ResourceId subFormId(FormField refField) {
        SubFormReferenceType type = (SubFormReferenceType) refField.getType();
        return type.getClassId();
    }
}
