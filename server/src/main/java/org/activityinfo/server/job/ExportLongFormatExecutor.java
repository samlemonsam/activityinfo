package org.activityinfo.server.job;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.analysis.pivot.LongFormatTableBuilder;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.analysis.pivot.PivotModel;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.error.ApiError;
import org.activityinfo.model.error.ApiErrorCode;
import org.activityinfo.model.error.ApiErrorType;
import org.activityinfo.model.error.ApiException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportLongFormatJob;
import org.activityinfo.model.job.ExportPivotTableJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.spi.UserDatabaseProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExportLongFormatExecutor implements JobExecutor<ExportLongFormatJob, ExportResult> {

    private final AuthenticatedUser authenticatedUser;
    private final DispatcherSync dispatcher;
    private final FormSource formSource;
    private final UserDatabaseProvider userDatabaseProvider;
    private final ExportPivotTableExecutor pivotTableExporter;

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

    private static Predicate<FormTree> valid() {
        return formTree -> formTree.getRootState() == FormTree.State.VALID;
    }

    @Inject
    public ExportLongFormatExecutor(AuthenticatedUser authenticatedUser,
                                    DispatcherSync dispatcher,
                                    FormSource formSource,
                                    UserDatabaseProvider userDatabaseProvider,
                                    ExportPivotTableExecutor pivotTableExecutor) {
        this.authenticatedUser = authenticatedUser;
        this.dispatcher = dispatcher;
        this.formSource = formSource;
        this.userDatabaseProvider = userDatabaseProvider;
        this.pivotTableExporter = pivotTableExecutor;
    }

    @Override
    public ExportResult execute(ExportLongFormatJob descriptor) throws IOException {
        int databaseId = descriptor.getDatabaseId();
        UserDatabaseDTO database = dispatcher.execute(new GetSchema()).getDatabaseById(databaseId);
        Optional<UserDatabaseMeta> databaseMeta = userDatabaseProvider.getDatabaseMetadata(CuidAdapter.databaseId(databaseId), authenticatedUser.getUserId());

        if (database == null || !databaseMeta.isPresent()) {
            ApiError error = new ApiError(ApiErrorType.INVALID_REQUEST_ERROR, ApiErrorCode.DATABASE_NOT_FOUND);
            throw new ApiException(error.toJson().toJson());
        }

        for (Resource resource : databaseMeta.get().getResources()) {
            if (!PermissionOracle.canExportRecords(resource.getId(), databaseMeta.get())) {
                ApiError error = new ApiError(ApiErrorType.AUTHORIZATION_ERROR, ApiErrorCode.EXPORT_FORMS_FORBIDDEN);
                throw new ApiException(error.toJson().toJson());
            }
        }

        List<FormTree> formScope = getFormScope(database);
        PivotModel longFormatModel = LongFormatTableBuilder.build(formScope);
        Map<ResourceId,String> folderMapping = mapFormsToFolderLabels(databaseMeta.get(), formScope);
        ExportPivotTableJob exportJob = new ExportPivotTableJob(longFormatModel, true, folderMapping);
        return pivotTableExporter.execute(exportJob);
    }

    private Map<ResourceId,String> mapFormsToFolderLabels(UserDatabaseMeta databaseMeta, List<FormTree> formScope) {
        Map<ResourceId,String> formFolderLabelMap = formScope.stream()
                .map(FormTree::getRootFormClass)
                .filter(form -> !form.isSubForm())
                .map(FormClass::getId)
                .map(ExportLongFormatExecutor::monthlyToParentFormId)
                .map(databaseMeta::getResource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        Resource::getId,
                        resource -> getParentLabel(databaseMeta, resource)));
        Map<ResourceId,String> subFormFolderLabelMap = formScope.stream()
                .map(FormTree::getRootFormClass)
                .filter(FormClass::isSubForm)
                .filter(subForm -> subForm.getParentFormId().isPresent())
                .filter(subForm -> formFolderLabelMap.containsKey(subForm.getParentFormId().get()))
                .collect(Collectors.toMap(
                        FormClass::getId,
                        subForm -> formFolderLabelMap.get(subForm.getParentFormId().get())
                ));
        formFolderLabelMap.putAll(subFormFolderLabelMap);
        return formFolderLabelMap;
    }

    private static ResourceId monthlyToParentFormId(ResourceId resourceId) {
        if (resourceId.getDomain() != CuidAdapter.MONTHLY_REPORT_FORM_CLASS) {
            return resourceId;
        }
        int monthlyActivityId = CuidAdapter.getLegacyIdFromCuid(resourceId);
        return CuidAdapter.activityFormClass(monthlyActivityId);
    }

    private String getParentLabel(UserDatabaseMeta databaseMeta, Resource child) {
        Optional<Resource> parent = databaseMeta.getResource(child.getParentId());
        return parent.map(Resource::getLabel).orElse("");
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
                .filter(valid())
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
                .filter(valid())
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
                .filter(valid())
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
                .filter(valid())
                .collect(Collectors.toList());
    }

    private static ResourceId subFormId(FormField refField) {
        SubFormReferenceType type = (SubFormReferenceType) refField.getType();
        return type.getClassId();
    }
}
