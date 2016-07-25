package org.activityinfo.server.command.handler;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.UpdateFormClass;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.MySqlCatalog;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateFormClassHandler implements CommandHandler<UpdateFormClass> {

    private static final Logger LOGGER = Logger.getLogger(UpdateFormClassHandler.class.getName());

    private Provider<MySqlCatalog> catalogProvider;
    private final PermissionOracle permissionOracle;

    @Inject
    public UpdateFormClassHandler(Provider<MySqlCatalog> catalogProvider, PermissionOracle permissionOracle) {
        this.catalogProvider = catalogProvider;
        this.permissionOracle = permissionOracle;
    }

    @Override
    public CommandResult execute(UpdateFormClass cmd, User user) throws CommandException {
        FormClass formClass = validateFormClass(cmd.getJson());
       
        catalogProvider.get().createOrUpdateFormSchema(formClass);

        return new VoidResult();
    }


    private FormClass validateFormClass(String json) {
        try {
            Resource resource = Resources.resourceFromJson(json);
            FormClass formClass = FormClass.fromResource(resource);

            for (FormField field : formClass.getFields()) {
                FieldType type = field.getType();
                if (type instanceof SubFormReferenceType) {
                    ResourceId subformClassId = ((SubFormReferenceType) type).getClassId();
                    validateSubformClassExist(subformClassId);
                }
            }

            return formClass;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Invalid FormClass json: " + e.getMessage(), e);
            throw new CommandException("Invalid FormClass json: " + e.getMessage());
        }
    }

    private void validateSubformClassExist(ResourceId classId) {
        HrdCatalog catalog = new HrdCatalog();
        Optional<FormAccessor> collection = catalog.getForm(classId);
        if (!collection.isPresent()) {
            LOGGER.log(Level.SEVERE, "Invalid SubFormClass reference. SubFormClass does not exist, id:" + classId.asString());
            throw new CommandException("Invalid SubFormClass reference. SubFormClass does not exist, id:" + classId.asString());
        }
    }

}
