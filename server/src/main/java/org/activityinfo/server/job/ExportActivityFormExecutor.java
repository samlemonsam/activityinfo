package org.activityinfo.server.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.FilterUrlSerializer;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.analysis.table.ExportFormat;
import org.activityinfo.model.formTree.ColumnNode;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.job.ExportActivityFormJob;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.command.handler.AttributeFilterMap;
import org.activityinfo.server.command.handler.QueryFilterBuilder;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.spi.UserDatabaseProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ExportActivityFormExecutor implements JobExecutor<ExportActivityFormJob, ExportResult> {

    private static final Logger LOGGER = Logger.getLogger(ExportActivityFormExecutor.class.getName());

    private FormSource formSource;
    private StorageProvider storageProvider;
    private UserDatabaseProvider userDatabaseProvider;
    private Provider<DispatcherSync> dispatcher;
    private Provider<AuthenticatedUser> authUser;

    @Inject
    public ExportActivityFormExecutor(FormSource formSource,
                                      StorageProvider storageProvider,
                                      UserDatabaseProvider userDatabaseProvider,
                                      Provider<DispatcherSync> dispatcher,
                                      Provider<AuthenticatedUser> authUser) {
        this.formSource = formSource;
        this.storageProvider = storageProvider;
        this.userDatabaseProvider = userDatabaseProvider;
        this.dispatcher = dispatcher;
        this.authUser = authUser;
    }

    @Override
    public ExportResult execute(ExportActivityFormJob descriptor) throws IOException {
        ExportFormExecutor formExporter = new ExportFormExecutor(formSource, storageProvider, userDatabaseProvider, authUser.get());

        Filter filter = FilterUrlSerializer.fromUrlFragment(descriptor.getFilter());
        ResourceId activityFormId = fetchFormId(filter);
        FormTree formTree = formSource.getFormTree(activityFormId).waitFor();
        List<ImmutableTableColumn> columns = computeColumns(formTree);
        FormulaNode queryFilter = computeQueryFilter(filter, formTree);
        TableModel tableModel = computeTableModel(activityFormId, columns, queryFilter);

        ExportFormJob job = new ExportFormJob(tableModel, ExportFormat.CSV);
        return formExporter.execute(job);
    }

    private List<ImmutableTableColumn> computeColumns(FormTree formTree) {
        return formTree.getColumnNodes().stream()
                .map(ExportActivityFormExecutor::convertToTableColumn)
                .collect(Collectors.toList());
    }

    private TableModel computeTableModel(ResourceId activityFormId, List<ImmutableTableColumn> columns, FormulaNode queryFilter) {
        ImmutableTableModel.Builder tableModelBuilder = ImmutableTableModel.builder();
        tableModelBuilder.formId(activityFormId);
        tableModelBuilder.columns(columns);
        if (queryFilter != null) {
            tableModelBuilder.filter(queryFilter.asExpression());
        }
        return tableModelBuilder.build();
    }

    private static ImmutableTableColumn convertToTableColumn(ColumnNode column) {
        return ImmutableTableColumn.builder()
                .label(column.getHeader())
                .formula(column.getExpr().asExpression())
                .build();
    }

    private FormulaNode computeQueryFilter(Filter filter, FormTree formTree) {
        filter.clearRestrictions(DimensionType.Activity);
        AttributeFilterMap attributeFilterMap = new AttributeFilterMap(filter, Collections.singleton(formTree));
        QueryFilterBuilder queryFilterBuilder = new QueryFilterBuilder(filter, attributeFilterMap);
        return queryFilterBuilder.composeTargetFilter();
    }

    private ResourceId fetchFormId(Filter filter) {
        int activityId = filter.getRestrictedCategory(DimensionType.Activity);
        ActivityFormDTO activityForm = dispatcher.get().execute(new GetActivityForm(activityId));

        return activityForm.getReportingFrequency() == ActivityFormDTO.REPORT_MONTHLY
                ? CuidAdapter.reportingPeriodFormClass(activityId)
                : CuidAdapter.activityFormClass(activityId);
    }

}
