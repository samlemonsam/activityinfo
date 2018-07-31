package org.activityinfo.server.job;

import com.google.common.base.Optional;
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

        List<FormClass> formScope = getFormScope(database);
        PivotModel longFormatModel = LongFormatTableBuilder.build(formScope);
        ExportPivotTableExecutor pivotTableExport = new ExportPivotTableExecutor(storageProvider, formSource);
        ExportPivotTableJob exportJob = new ExportPivotTableJob(longFormatModel);
        return pivotTableExport.execute(exportJob);
    }

    private List<FormClass> getFormScope(UserDatabaseDTO database) {
        List<FormClass> scope = Lists.newArrayList();
        scope.addAll(getActivityForms(database));
        scope.addAll(getMonthlyActivityForms(database));
        scope.addAll(getForms(database));
        return scope;
    }

    private List<FormClass> getActivityForms(UserDatabaseDTO database) {
        return database.getActivities().stream()
                .filter(activity())
                .map(ActivityDTO::getFormId)
                .map(formStorageProvider::getForm)
                .filter(Optional::isPresent)
                .map(presentForm -> presentForm.get().getFormClass())
                .collect(Collectors.toList());
    }

    private List<FormClass> getMonthlyActivityForms(UserDatabaseDTO database) {
        return database.getActivities().stream()
                .filter(monthlyActivity())
                .map(ActivityDTO::getId)
                .map(CuidAdapter::reportingPeriodFormClass)
                .map(formStorageProvider::getForm)
                .filter(Optional::isPresent)
                .map(presentForm -> presentForm.get().getFormClass())
                .collect(Collectors.toList());
    }

    private List<FormClass> getForms(UserDatabaseDTO database) {
        List<FormClass> scope = Lists.newArrayList();
        scope.addAll(getParentForms(database));
        scope.addAll(getSubForms(scope));
        return scope;
    }

    private List<FormClass> getParentForms(UserDatabaseDTO database) {
        return database.getActivities().stream()
                .filter(form())
                .map(ActivityDTO::getFormId)
                .map(formStorageProvider::getForm)
                .filter(Optional::isPresent)
                .map(presentForm -> presentForm.get().getFormClass())
                .collect(Collectors.toList());
    }

    private List<FormClass> getSubForms(List<FormClass> parentForms) {
        return parentForms.stream()
                .flatMap(form -> form.getFields().stream())
                .filter(subFormField())
                .map(ExportLongFormatExecutor::subFormId)
                .map(formStorageProvider::getForm)
                .filter(Optional::isPresent)
                .map(presentForm -> presentForm.get().getFormClass())
                .collect(Collectors.toList());
    }

    private static ResourceId subFormId(FormField refField) {
        SubFormReferenceType type = (SubFormReferenceType) refField.getType();
        return type.getClassId();
    }
}
