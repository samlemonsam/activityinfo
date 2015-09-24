package org.activityinfo.ui.client.component.importDialog;

import com.google.common.collect.Lists;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.importing.strategy.FieldImportStrategies;
import org.activityinfo.core.shared.importing.strategy.FieldImportStrategy;
import org.activityinfo.core.shared.importing.strategy.ImportTarget;
import org.activityinfo.core.shared.importing.validation.ValidatedRowTable;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Logger;


public class Importer {

    private static final Logger LOGGER = Logger.getLogger(Importer.class.getName());

    private ResourceLocator resourceLocator;

    static class TargetField {
        FormTree.Node node;
        FieldImportStrategy strategy;

        private TargetField(FormTree.Node node, FieldImportStrategy strategy) {
            this.node = node;
            this.strategy = strategy;
        }

        @Override
        public String toString() {
            return node.toString();
        }
    }

    private List<TargetField> fields = Lists.newArrayList();

    public Importer(ResourceLocator resourceLocator, FormTree formTree, FieldImportStrategies fieldImportStrategies) {
        this.resourceLocator = resourceLocator;
        for (FormTree.Node rootField : formTree.getRootFields()) {
            if (rootField.isCalculated()) {
                continue;
            }
            fields.add(new TargetField(rootField, fieldImportStrategies.forField(rootField)));
        }
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    public List<ImportTarget> getImportTargets() {
        List<ImportTarget> targets = Lists.newArrayList();
        for (TargetField binding : fields) {
            targets.addAll(binding.strategy.getImportSites(binding.node));
        }
        return targets;
    }

    public Promise<ValidatedRowTable> validateRows(final ImportModel model) {
        try {
            final ImportCommandExecutor modeller = new ImportCommandExecutor(model, fields, resourceLocator);
            return modeller.execute(new ValidateRowsImportCommand());
        } catch (Exception e) {
            LOGGER.severe("Failed to validate rows, exception: " + e.getMessage());
            return Promise.rejected(e);
        }
    }

    public Promise<List<ValidationResult>> validateClass(final ImportModel model) {
        try {
            final ImportCommandExecutor modeller = new ImportCommandExecutor(model, fields, resourceLocator);
            return modeller.execute(new ValidateClassImportCommand());
        } catch (Exception e) {
            LOGGER.severe("Failed to validate rows, exception: " + e.getMessage());
            return Promise.rejected(e);
        }
    }

    public Promise<Void> persist(final ImportModel model) {
        return persist(model, null);
    }

    public Promise<Void> persist(final ImportModel model, @Nullable PromisesExecutionMonitor monitor) {
        final ImportCommandExecutor modeller = new ImportCommandExecutor(model, fields, resourceLocator);
        return modeller.execute(new PersistImportCommand(monitor));
    }
}
